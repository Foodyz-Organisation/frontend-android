# Backend Update Required for Android Payment Flow

## Problem

The Android app doesn't have Stripe SDK integrated, so it cannot create a `PaymentMethod` client-side. The backend currently requires a `paymentMethodId` in the payment confirmation request, but rejects card details (`cardNumber`, `expMonth`, `expYear`, `cvv`, `cardholderName`).

## Current Backend Validation (WRONG for Android)

```typescript
// Current DTO validation rejects card details
class ConfirmPaymentDto {
  @IsString()
  @IsNotEmpty()
  paymentIntentId: string;

  @IsString()
  @IsNotEmpty()
  paymentMethodId: string;  // ❌ Requires PaymentMethod ID from Stripe SDK

  // ❌ These fields are REJECTED by current validation:
  // cardNumber, expMonth, expYear, cvv, cardholderName
}
```

## Required Backend Changes

### Option 1: Accept Card Details and Create PaymentMethod Server-Side (RECOMMENDED)

Update the DTO to accept card details:

```typescript
// src/order/dto/confirm-payment.dto.ts
import { IsString, IsNotEmpty, IsNumber, IsOptional, ValidateIf } from 'class-validator';

export class ConfirmPaymentDto {
  @IsString()
  @IsNotEmpty()
  paymentIntentId: string;

  // Make paymentMethodId optional if card details are provided
  @IsString()
  @IsNotEmpty()
  @ValidateIf((o) => !o.cardNumber)  // Required only if cardNumber is not provided
  paymentMethodId?: string;

  // Add card details (required if paymentMethodId is not provided)
  @IsString()
  @IsNotEmpty()
  @ValidateIf((o) => !o.paymentMethodId)  // Required only if paymentMethodId is not provided
  cardNumber?: string;

  @IsNumber()
  @ValidateIf((o) => !o.paymentMethodId)
  expMonth?: number;

  @IsNumber()
  @ValidateIf((o) => !o.paymentMethodId)
  expYear?: number;

  @IsString()
  @IsNotEmpty()
  @ValidateIf((o) => !o.paymentMethodId)
  cvv?: string;

  @IsString()
  @IsOptional()
  cardholderName?: string;
}
```

### Update the Service to Handle Both Cases

```typescript
// src/order/order.service.ts
import Stripe from 'stripe';

@Injectable()
export class OrderService {
  private stripe: Stripe;

  constructor(
    @InjectModel(Order.name) private orderModel: Model<OrderDocument>,
    @InjectModel(Payment.name) private paymentModel: Model<PaymentDocument>,
  ) {
    this.stripe = new Stripe(process.env.STRIPE_SECRET_KEY, {
      apiVersion: '2024-12-18.acacia',
    });
  }

  async confirmPayment(
    paymentIntentId: string,
    confirmPaymentDto: ConfirmPaymentDto,
  ): Promise<Order> {
    try {
      // Find the payment by paymentIntentId
      const payment = await this.paymentModel.findOne({
        paymentIntentId: paymentIntentId,
      });

      if (!payment) {
        throw new NotFoundException('Payment not found');
      }

      let paymentMethodId: string;

      // Case 1: PaymentMethod ID provided (from Stripe SDK on frontend)
      if (confirmPaymentDto.paymentMethodId) {
        paymentMethodId = confirmPaymentDto.paymentMethodId;
      }
      // Case 2: Card details provided (Android app without Stripe SDK)
      else if (confirmPaymentDto.cardNumber) {
        // Create PaymentMethod from card details server-side
        const paymentMethod = await this.stripe.paymentMethods.create({
          type: 'card',
          card: {
            number: confirmPaymentDto.cardNumber,
            exp_month: confirmPaymentDto.expMonth!,
            exp_year: confirmPaymentDto.expYear!,
            cvc: confirmPaymentDto.cvv,
          },
          billing_details: confirmPaymentDto.cardholderName
            ? {
                name: confirmPaymentDto.cardholderName,
              }
            : undefined,
        });

        paymentMethodId = paymentMethod.id;
      } else {
        throw new BadRequestException(
          'Either paymentMethodId or card details must be provided',
        );
      }

      // Confirm the payment with Stripe
      const paymentIntent = await this.stripe.paymentIntents.confirm(
        paymentIntentId,
        {
          payment_method: paymentMethodId,
        },
      );

      // Check if payment succeeded
      if (paymentIntent.status !== 'succeeded') {
        throw new InternalServerErrorException(
          `Payment failed: ${paymentIntent.status}`,
        );
      }

      // Update payment status
      payment.status = 'succeeded';
      await payment.save();

      // Find and return the order
      const order = await this.orderModel.findById(payment.orderId);
      if (!order) {
        throw new NotFoundException('Order not found');
      }

      return order;
    } catch (error) {
      if (error instanceof Stripe.errors.StripeError) {
        throw new InternalServerErrorException(
          `Payment failed: ${error.message}`,
        );
      }
      throw error;
    }
  }
}
```

### Update the Controller

```typescript
// src/order/order.controller.ts
@Post('payment/confirm')
async confirmPayment(
  @Body() confirmPaymentDto: ConfirmPaymentDto,
): Promise<{ success: boolean; order: Order }> {
  const order = await this.orderService.confirmPayment(
    confirmPaymentDto.paymentIntentId,
    confirmPaymentDto,
  );

  return {
    success: true,
    order,
  };
}
```

## Alternative Option 2: Separate Endpoint for Card Details

If you prefer to keep the existing endpoint unchanged, create a new endpoint:

```typescript
// src/order/dto/confirm-payment-with-card.dto.ts
export class ConfirmPaymentWithCardDto {
  @IsString()
  @IsNotEmpty()
  paymentIntentId: string;

  @IsString()
  @IsNotEmpty()
  cardNumber: string;

  @IsNumber()
  expMonth: number;

  @IsNumber()
  expYear: number;

  @IsString()
  @IsNotEmpty()
  cvv: string;

  @IsString()
  @IsOptional()
  cardholderName?: string;
}
```

```typescript
// src/order/order.controller.ts
@Post('payment/confirm-with-card')
async confirmPaymentWithCard(
  @Body() dto: ConfirmPaymentWithCardDto,
): Promise<{ success: boolean; order: Order }> {
  // Create PaymentMethod from card details
  const paymentMethod = await this.stripe.paymentMethods.create({
    type: 'card',
    card: {
      number: dto.cardNumber,
      exp_month: dto.expMonth,
      exp_year: dto.expYear,
      cvc: dto.cvv,
    },
  });

  // Then confirm payment
  const order = await this.orderService.confirmPayment(
    dto.paymentIntentId,
    { paymentIntentId: dto.paymentIntentId, paymentMethodId: paymentMethod.id },
  );

  return { success: true, order };
}
```

## Testing

After updating the backend:

1. **Test with PaymentMethod ID** (existing flow with Stripe SDK):
   ```json
   POST /orders/payment/confirm
   {
     "paymentIntentId": "pi_xxx",
     "paymentMethodId": "pm_xxx"
   }
   ```

2. **Test with Card Details** (Android app):
   ```json
   POST /orders/payment/confirm
   {
     "paymentIntentId": "pi_xxx",
     "cardNumber": "4242424242424242",
     "expMonth": 12,
     "expYear": 2025,
     "cvv": "123",
     "cardholderName": "John Doe"
   }
   ```

## Security Considerations

1. **PCI Compliance**: Card details are sent to your backend. Ensure:
   - HTTPS is enforced
   - Card details are not logged
   - Card details are not stored in database
   - Use Stripe's secure tokenization

2. **Validation**: Validate card numbers using Luhn algorithm before sending to Stripe

3. **Error Handling**: Don't expose sensitive Stripe errors to frontend

## Recommended Approach

**Option 1 is recommended** because:
- Single endpoint handles both cases
- Backward compatible (existing clients with Stripe SDK still work)
- Supports Android app without Stripe SDK
- Cleaner API design

