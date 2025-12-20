package com.example.damprojectfinal.user.feature_cart_item.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.cart.AddToCartRequest
import com.example.damprojectfinal.core.dto.cart.CartResponse
import com.example.damprojectfinal.core.dto.order.*
import com.example.damprojectfinal.core.repository.CartRepository
import com.example.damprojectfinal.core.repository.OrderRepository
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// -----------------------------
// UI State
// -----------------------------
sealed class CartUiState {
    object Loading : CartUiState()
    data class Success(val cart: CartResponse) : CartUiState()
    data class Error(val message: String) : CartUiState()
    object Empty : CartUiState()
}

// -----------------------------
// ViewModel
// -----------------------------
class CartViewModel(
    private val repository: CartRepository,
    private val orderRepository: OrderRepository,
    private val menuItemRepository: MenuItemRepository,
    private val tokenManager: TokenManager,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // -----------------------------
    // Load Cart
    // -----------------------------
    fun loadCart() {
        _uiState.value = CartUiState.Loading
        viewModelScope.launch {
            val cart = repository.getUserCart(userId)

            if (cart == null) {
                _uiState.value = CartUiState.Error("Failed to load cart")
                return@launch
            }

            if (cart.items.isEmpty()) {
                _uiState.value = CartUiState.Empty
                return@launch
            }

            // 🌟 FETCH IMAGES MANUALLY
            val token = tokenManager.getAccessTokenBlocking() ?: ""
            
            // Fetch images in parallel
            val updatedItems = cart.items.map { item ->
                async {
                    if (item.image.isNullOrEmpty()) {
                        val result = menuItemRepository.getMenuItemDetails(item.menuItemId, token)
                        val fetchedImage = result.getOrNull()?.image
                        if (!fetchedImage.isNullOrEmpty()) {
                            item.copy(image = fetchedImage)
                        } else {
                            item
                        }
                    } else {
                        item
                    }
                }
            }.awaitAll()

            // Create a new cart object with updated items (assuming only items list needs update)
            val updatedCart = cart.copy(items = updatedItems)
            
            _uiState.value = CartUiState.Success(updatedCart)
        }
    }

    // -----------------------------
    // Add Item to Cart
    // -----------------------------
    fun addItem(request: AddToCartRequest) {
        android.util.Log.d("CartViewModel", "🛒 ========== addItem() CALLED ==========")
        android.util.Log.d("CartViewModel", "Request: menuItemId=${request.menuItemId}, name=${request.name}, quantity=${request.quantity}")
        android.util.Log.d("CartViewModel", "UserId: $userId")
        android.util.Log.d("CartViewModel", "Ingredients: ${request.chosenIngredients.size}")
        android.util.Log.d("CartViewModel", "Options: ${request.chosenOptions.size}")
        android.util.Log.d("CartViewModel", "Price: ${request.calculatedPrice}")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("CartViewModel", "📡 Calling repository.addItemToCart()...")
                val cart = repository.addItemToCart(request, userId)
                
                if (cart == null) {
                    android.util.Log.e("CartViewModel", "❌ Repository returned null cart")
                    _uiState.value = CartUiState.Error("Failed to add item")
                } else {
                    android.util.Log.d("CartViewModel", "✅ Repository returned cart with ${cart.items.size} items")
                    cart.items.forEachIndexed { index, item ->
                        android.util.Log.d("CartViewModel", "  Item $index: ${item.name} (qty=${item.quantity})")
                    }
                    _uiState.value = when {
                        cart.items.isEmpty() -> {
                            android.util.Log.d("CartViewModel", "⚠️ Cart is empty after add")
                            CartUiState.Empty
                        }
                        else -> {
                            android.util.Log.d("CartViewModel", "✅ Setting UI state to Success")
                            CartUiState.Success(cart)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "❌ Exception in addItem(): ${e.message}", e)
                _uiState.value = CartUiState.Error("Error: ${e.message}")
            }
        }
    }

    // -----------------------------
    // Update Quantity
    // -----------------------------
    fun updateQuantity(index: Int, newQuantity: Int) {
        viewModelScope.launch {
            val cart = repository.updateItemQuantity(index, newQuantity, userId)
            _uiState.value = when {
                cart == null -> CartUiState.Error("Failed to update quantity")
                cart.items.isEmpty() -> CartUiState.Empty
                else -> CartUiState.Success(cart)
            }
        }
    }

    // -----------------------------
    // Remove Item
    // -----------------------------
    fun removeItem(index: Int) {
        viewModelScope.launch {
            val cart = repository.removeItem(index, userId)
            _uiState.value = when {
                cart == null -> CartUiState.Error("Failed to remove item")
                cart.items.isEmpty() -> CartUiState.Empty
                else -> CartUiState.Success(cart)
            }
        }
    }

    // -----------------------------
    // Clear Cart
    // -----------------------------
    fun clearCart() {
        viewModelScope.launch {
            val cart = repository.clearCart(userId)
            _uiState.value = if (cart == null || cart.items.isEmpty()) {
                CartUiState.Empty
            } else {
                CartUiState.Success(cart)
            }
        }
    }

    // -----------------------------
    // Checkout - Convert Cart to Order
    // -----------------------------
    fun checkout(
        professionalId: String,
        orderType: OrderType,
        paymentMethod: String, // "CASH" or "CARD"
        deliveryAddress: String? = null,
        notes: String? = null,
        scheduledTime: String? = null,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        checkoutWithPayment(
            professionalId = professionalId,
            orderType = orderType,
            paymentMethod = paymentMethod,
            deliveryAddress = deliveryAddress,
            notes = notes,
            scheduledTime = scheduledTime,
            onSuccess = { orderResponse, _ -> onSuccess(orderResponse) },
            onError = onError
        )
    }
    
    // -----------------------------
    // Checkout with Payment Info (for CARD payments)
    // -----------------------------
    fun checkoutWithPayment(
        professionalId: String,
        orderType: OrderType,
        paymentMethod: String, // "CASH" or "CARD"
        deliveryAddress: String? = null,
        notes: String? = null,
        scheduledTime: String? = null,
        onSuccess: (OrderResponse, String?) -> Unit, // OrderResponse and paymentIntentId (null for CASH)
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // First check current cart state
            val currentState = _uiState.value
            var cart: com.example.damprojectfinal.core.dto.cart.CartResponse? = null

            // If we have a valid cart in state, use it
            if (currentState is CartUiState.Success && currentState.cart.items.isNotEmpty()) {
                android.util.Log.d("CartViewModel", "✅ Using cart from UI state: ${currentState.cart.items.size} items")
                cart = currentState.cart
            } else {
                // Otherwise, reload from database
                android.util.Log.d("CartViewModel", "🔄 Reloading cart from database...")
                cart = repository.getUserCart(userId)
                
                if (cart == null || cart.items.isEmpty()) {
                    android.util.Log.e("CartViewModel", "❌ Cart is empty in database and UI state")
                    onError("Cart is empty. Please add items to cart first.")
                    return@launch
                }

                android.util.Log.d("CartViewModel", "✅ Cart loaded from database: ${cart.items.size} items")
            }

            // Validate delivery address for delivery orders
            if (orderType == OrderType.DELIVERY && deliveryAddress.isNullOrBlank()) {
                onError("Delivery address is required for delivery orders")
                return@launch
            }

            // Convert cart items to order items
            android.util.Log.d("CartViewModel", "📦 Converting ${cart.items.size} cart items to order items")
            val orderItems = cart.items.map { cartItem ->
                android.util.Log.d("CartViewModel", "🛒 Converting cart item: ${cartItem.name}")
                android.util.Log.d("CartViewModel", "  Ingredients count: ${cartItem.chosenIngredients.size}")
                
                cartItem.chosenIngredients.forEach { ingredient ->
                    android.util.Log.d("CartViewModel", "    - ${ingredient.name}: intensityValue=${ingredient.intensityValue}, type=${ingredient.intensityType}, color=${ingredient.intensityColor}")
                }
                
                val orderItem = OrderItemRequest(
                    menuItemId = cartItem.menuItemId,
                    name = cartItem.name,
                    quantity = cartItem.quantity,
                    chosenIngredients = cartItem.chosenIngredients.map {
                        val request = ChosenIngredientRequest(
                            name = it.name,
                            isDefault = it.isDefault,
                            intensityType = it.intensityType,
                            intensityColor = it.intensityColor,
                            intensityValue = it.intensityValue
                        )
                        android.util.Log.d("CartViewModel", "      → Order ingredient: ${request.name}, intensityValue=${request.intensityValue}")
                        request
                    },
                    chosenOptions = cartItem.chosenOptions.map {
                        ChosenOptionRequest(
                            name = it.name,
                            price = it.price
                        )
                    },
                    calculatedPrice = cartItem.calculatedPrice
                )
                
                android.util.Log.d("CartViewModel", "  ✅ Created OrderItemRequest with ${orderItem.chosenIngredients?.size ?: 0} ingredients")
                orderItem
            }

            // Calculate total price from cart items
            val totalPrice = cart.items.sumOf { it.calculatedPrice * it.quantity }

            // Create order request
            val orderRequest = CreateOrderRequest(
                userId = userId,
                professionalId = professionalId,
                orderType = orderType,
                scheduledTime = scheduledTime,
                items = orderItems,
                totalPrice = totalPrice,
                deliveryAddress = deliveryAddress,
                notes = notes,
                paymentMethod = paymentMethod
            )

            // Create order via OrderRepository
            // NOTE: Don't clear cart here - backend validates cart exists
            // For CARD payments, clear cart only after payment confirmation
            // For CASH payments, clear cart after order creation
            android.util.Log.d("CartViewModel", "💳 Processing payment method: $paymentMethod")
            
            if (paymentMethod == "CARD") {
                android.util.Log.d("CartViewModel", "💳 CARD payment - creating order with payment info")
                // For CARD payments, get payment info
                val paymentResponse = orderRepository.createOrderWithPayment(orderRequest)
                if (paymentResponse != null) {
                    android.util.Log.d("CartViewModel", "✅ CARD order created: ${paymentResponse.order._id}")
                    android.util.Log.d("CartViewModel", "💳 PaymentIntentId: ${paymentResponse.paymentIntentId}")
                    android.util.Log.d("CartViewModel", "💳 Order PaymentId: ${paymentResponse.order.paymentId}")
                    android.util.Log.d("CartViewModel", "🚫 CARD payment - NOT clearing cart yet (waiting for payment confirmation)")
                    android.util.Log.d("CartViewModel", "📤 Calling onSuccess callback with order and paymentIntentId")
                    // Don't clear cart yet - wait for payment confirmation
                    onSuccess(paymentResponse.order, paymentResponse.paymentIntentId)
                    android.util.Log.d("CartViewModel", "✅ onSuccess callback completed")
                } else {
                    android.util.Log.e("CartViewModel", "❌ Failed to create CARD order - paymentResponse is null")
                    onError("Failed to create order. Please try again.")
                }
            } else {
                android.util.Log.d("CartViewModel", "💵 CASH payment - creating order")
                // For CASH payments, just create order
                val createdOrder = orderRepository.createOrder(orderRequest)
                if (createdOrder != null) {
                    android.util.Log.d("CartViewModel", "✅ CASH order created: ${createdOrder._id}")
                    android.util.Log.d("CartViewModel", "💵 CASH payment - clearing cart immediately")
                    android.util.Log.d("CartViewModel", "🚫 CASH payment - NOT calling payment confirmation (not needed)")
                    // Clear cart after successful order creation for CASH
                    clearCart()
                    // Pass null for paymentIntentId to indicate this is CASH
                    onSuccess(createdOrder, null)
                } else {
                    android.util.Log.e("CartViewModel", "❌ Failed to create CASH order")
                    onError("Failed to create order. Please try again.")
                }
            }
        }
    }
    
    // -----------------------------
    // Confirm Card Payment (with PaymentMethod ID - legacy)
    // -----------------------------
    fun confirmPayment(
        paymentIntentId: String,
        paymentMethodId: String,
        onSuccess: (com.example.damprojectfinal.core.dto.order.OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            android.util.Log.d("CartViewModel", "💳 Confirming payment (legacy method with PaymentMethod ID)...")
            android.util.Log.d("CartViewModel", "  PaymentIntentId: $paymentIntentId")
            android.util.Log.d("CartViewModel", "  PaymentMethodId: $paymentMethodId")
            
            val confirmResponse = orderRepository.confirmPayment(paymentIntentId, paymentMethodId)
            
            if (confirmResponse != null && confirmResponse.success) {
                android.util.Log.d("CartViewModel", "✅ Payment confirmed successfully")
                android.util.Log.d("CartViewModel", "  Order ID: ${confirmResponse.order._id}")
                // Clear cart only after payment is confirmed
                clearCart()
                onSuccess(confirmResponse.order)
            } else {
                android.util.Log.e("CartViewModel", "❌ Payment confirmation failed")
                onError("Payment confirmation failed. Please try again.")
            }
        }
    }
    
    // -----------------------------
    // Confirm Card Payment with Card Details
    // Backend will create PaymentMethod from card details
    // -----------------------------
    fun confirmPaymentWithCardDetails(
        paymentIntentId: String,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvv: String,
        cardholderName: String,
        onSuccess: (com.example.damprojectfinal.core.dto.order.OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            android.util.Log.d("CartViewModel", "💳 Confirming payment with card details...")
            android.util.Log.d("CartViewModel", "  PaymentIntentId: $paymentIntentId")
            android.util.Log.d("CartViewModel", "  Card Number: ${cardNumber.take(4)}****${cardNumber.takeLast(4)}")
            android.util.Log.d("CartViewModel", "  Expiry: $expMonth/$expYear")
            android.util.Log.d("CartViewModel", "  CVV: ${cvv.length} digits")
            android.util.Log.d("CartViewModel", "  Cardholder: $cardholderName")
            android.util.Log.d("CartViewModel", "  ✅ Backend will create PaymentMethod from these details")
            
            val confirmResponse = orderRepository.confirmPaymentWithCardDetails(
                paymentIntentId = paymentIntentId,
                cardNumber = cardNumber,
                expMonth = expMonth,
                expYear = expYear,
                cvv = cvv,
                cardholderName = cardholderName
            )
            
            if (confirmResponse != null && confirmResponse.success) {
                android.util.Log.d("CartViewModel", "✅ Payment confirmed successfully")
                android.util.Log.d("CartViewModel", "  Order ID: ${confirmResponse.order._id}")
                // Clear cart only after payment is confirmed
                clearCart()
                onSuccess(confirmResponse.order)
            } else {
                android.util.Log.e("CartViewModel", "❌ Payment confirmation failed")
                onError("Payment confirmation failed. Please try again.")
            }
        }
    }
}

// -----------------------------
// ViewModel Factory
// -----------------------------
class CartViewModelFactory(
    private val repository: CartRepository,
    private val orderRepository: OrderRepository,
    private val menuItemRepository: MenuItemRepository,
    private val tokenManager: TokenManager,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository, orderRepository, menuItemRepository, tokenManager, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
