# Backend Code Update for Android Payment Support

## Problem

The iOS app uses Stripe SDK to create `PaymentMethod` client-side, then sends `paymentMethodId` to backend.  
The Android app doesn't have Stripe SDK, so it needs to send card details and have backend create `PaymentMethod` server-side.

## Current Backend Issue

Your `StripeService.confirmPayment()` only retrieves the PaymentIntent status - it doesn't actually **confirm** the payment with a PaymentMethod.  
Your `OrderService.confirmPayment()` doesn't accept or use `paymentMethodId`.

## Required Backend Updates

### 1. Update `ConfirmPaymentDto` to Accept Card Details

**File**: `src/order/dto/confirm-payment.dto.ts`

```typescript
import { IsNotEmpty, IsString, IsNumber, IsOptional, ValidateIf } from 'class-validator';

export class ConfirmPaymentDto {
  @IsString()
  @IsNotEmpty()
  paymentIntentId: string;

  // PaymentMethod ID (from Stripe SDK - iOS/Web)
  @IsString()
  @IsNotEmpty()
  @ValidateIf((o) => !o.cardNumber)  // Required only if cardNumber is not provided
  paymentMethodId?: string;

  // Card details (for Android without Stripe SDK)
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

### 2. Update `StripeService` to Support PaymentMethod Confirmation

**File**: `src/order/StripeService.ts`

```typescript
import { Injectable, InternalServerErrorException } from '@nestjs/common';
import Stripe from 'stripe';
import dotenv from 'dotenv';

dotenv.config();

@Injectable()
export class StripeService {
  private stripe: Stripe;

  constructor() {
    const secretKey = process.env.STRIPE_SECRET_KEY;

    if (!secretKey) {
      throw new InternalServerErrorException(
        'STRIPE_SECRET_KEY is not defined in .env',
      );
    }

    this.stripe = new Stripe(secretKey, {
      apiVersion: '2022-11-15' as any,
    });
  }

  /**
   * Create a PaymentIntent
   */
  async createPayment(
    amount: number,
    currency: string = 'usd',
  ): Promise<{ clientSecret: string; paymentIntentId: string }> {
    try {
      const paymentIntent = await this.stripe.paymentIntents.create({
        amount,
        currency,
        payment_method_types: ['card'],
      });

      return { 
        clientSecret: paymentIntent.client_secret!,
        paymentIntentId: paymentIntent.id,
      };
    } catch (error) {
      console.error('Stripe PaymentIntent creation failed:', error);
      throw new InternalServerErrorException(
        'Failed to create payment intent',
      );
    }
  }

  /**
   * Create PaymentMethod from card details (for Android)
   */
  async createPaymentMethodFromCard(
    cardNumber: string,
    expMonth: number,
    expYear: number,
    cvv: string,
    cardholderName?: string,
  ): Promise<string> {
    try {
      const paymentMethod = await this.stripe.paymentMethods.create({
        type: 'card',
        card: {
          number: cardNumber,
          exp_month: expMonth,
          exp_year: expYear,
          cvc: cvv,
        },
        billing_details: cardholderName
          ? {
              name: cardholderName,
            }
          : undefined,
      });

      return paymentMethod.id; // Returns pm_xxx
    } catch (error) {
      console.error('Stripe PaymentMethod creation failed:', error);
      throw new InternalServerErrorException(
        `Failed to create payment method: ${error.message}`,
      );
    }
  }

  /**
   * Confirm a payment intent with a PaymentMethod
   * This is the ACTUAL payment confirmation
   */
  async confirmPaymentWithMethod(
    paymentIntentId: string,
    paymentMethodId: string,
  ): Promise<{ status: string }> {
    try {
      // Confirm the payment intent with the payment method
      const paymentIntent = await this.stripe.paymentIntents.confirm(
        paymentIntentId,
        {
          payment_method: paymentMethodId,
        },
      );

      return { status: paymentIntent.status };
    } catch (error) {
      console.error('Stripe payment confirmation failed:', error);
      throw new InternalServerErrorException(
        `Payment failed: ${error.message}`,
      );
    }
  }

  /**
   * Retrieve PaymentIntent status (legacy - for checking only)
   */
  async confirmPayment(paymentIntentId: string): Promise<{ status: string }> {
    try {
      const paymentIntent = await this.stripe.paymentIntents.retrieve(paymentIntentId);
      return { status: paymentIntent.status };
    } catch (error) {
      console.error('Stripe PaymentIntent retrieval failed:', error);
      throw new InternalServerErrorException('Failed to confirm payment');
    }
  }
}
```

### 3. Update `OrderService.confirmPayment()` to Handle Both Cases

**File**: `src/order/order.service.ts`

Update the `confirmPayment` method:

```typescript
// -----------------------------
// CONFIRM CARD PAYMENT
// -----------------------------
async confirmPayment(
  paymentIntentId: string,
  confirmPaymentDto: ConfirmPaymentDto, // Accept full DTO
): Promise<{ success: boolean; order?: Order }> {
  try {
    // 1. Get payment from DB
    const payment = await this.paymentService.getPaymentByIntentId(paymentIntentId);
    if (!payment) {
      throw new NotFoundException('Payment not found');
    }

    let paymentMethodId: string;

    // 2. Determine payment method ID source
    if (confirmPaymentDto.paymentMethodId) {
      // Case 1: PaymentMethod ID provided (iOS/Web with Stripe SDK)
      paymentMethodId = confirmPaymentDto.paymentMethodId;
    } else if (confirmPaymentDto.cardNumber) {
      // Case 2: Card details provided (Android without Stripe SDK)
      // Create PaymentMethod from card details server-side
      paymentMethodId = await this.stripeService.createPaymentMethodFromCard(
        confirmPaymentDto.cardNumber,
        confirmPaymentDto.expMonth!,
        confirmPaymentDto.expYear!,
        confirmPaymentDto.cvv!,
        confirmPaymentDto.cardholderName,
      );
    } else {
      throw new BadRequestException(
        'Either paymentMethodId or card details must be provided',
      );
    }

    // 3. Confirm payment with Stripe using PaymentMethod
    const stripeResult = await this.stripeService.confirmPaymentWithMethod(
      paymentIntentId,
      paymentMethodId,
    );

    // 4. Check if payment succeeded
    if (stripeResult.status !== 'succeeded') {
      await this.paymentService.updatePaymentStatus(
        paymentIntentId,
        stripeResult.status,
      );
      throw new BadRequestException(
        `Payment status: ${stripeResult.status}`,
      );
    }

    // 5. Update payment status in DB
    await this.paymentService.updatePaymentStatus(paymentIntentId, 'succeeded');

    // 6. Return order if exists
    if (payment.orderId) {
      const order = await this.orderModel.findById(payment.orderId);
      return {
        success: true,
        order: order || undefined,
      };
    }

    return { success: true };
  } catch (error) {
    console.error('Error confirming payment:', error);
    throw error;
  }
}
```

### 4. Update `OrderController` to Pass Full DTO

**File**: `src/order/order.controller.ts`

```typescript
// -----------------------------
// CONFIRM CARD PAYMENT
// POST /orders/payment/confirm
// Body: { paymentIntentId, paymentMethodId? OR cardNumber, expMonth, expYear, cvv }
// -----------------------------
@Post('payment/confirm')
async confirmPayment(@Body() dto: ConfirmPaymentDto) {
  return this.orderService.confirmPayment(dto.paymentIntentId, dto);
}
```

## How It Works Now

### iOS Flow (with Stripe SDK):
```
1. User enters card details
2. iOS Stripe SDK creates PaymentMethod → pm_xxx
3. iOS sends: { paymentIntentId: "pi_xxx", paymentMethodId: "pm_xxx" }
4. Backend confirms payment with Stripe using paymentMethodId
5. ✅ Payment succeeded
```

### Android Flow (without Stripe SDK):
```
1. User enters card details
2. Android sends: { paymentIntentId: "pi_xxx", cardNumber: "4242...", expMonth: 12, expYear: 2025, cvv: "123" }
3. Backend creates PaymentMethod from card details → pm_xxx
4. Backend confirms payment with Stripe using paymentMethodId
5. ✅ Payment succeeded
```

## Testing

### Test with iOS-style request (PaymentMethod ID):
```bash
POST /orders/payment/confirm
{
  "paymentIntentId": "pi_xxx",
  "paymentMethodId": "pm_xxx"
}
```

### Test with Android-style request (Card details):
```bash
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

## Security Notes

1. **Card details are sent to your backend** - ensure HTTPS is enforced
2. **Don't log card details** - remove from logs
3. **Don't store card details** - only use them to create PaymentMethod, then discard
4. **Validate card numbers** - use Luhn algorithm before sending to Stripe

## Summary

The key changes:
1. ✅ DTO accepts both `paymentMethodId` (iOS) and card details (Android)
2. ✅ `StripeService` has method to create PaymentMethod from card details
3. ✅ `StripeService` has method to actually confirm payment (not just retrieve)
4. ✅ `OrderService` handles both cases and creates PaymentMethod if needed
5. ✅ Backward compatible - iOS flow still works

After these changes, both iOS and Android will work! 🎉



