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
        viewModelScope.launch {
            val cart = repository.addItemToCart(request, userId)
            _uiState.value = when {
                cart == null -> CartUiState.Error("Failed to add item")
                cart.items.isEmpty() -> CartUiState.Empty
                else -> CartUiState.Success(cart)
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
        deliveryAddress: String? = null,
        notes: String? = null,
        scheduledTime: String? = null,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Validate cart is loaded and has items
            if (currentState !is CartUiState.Success) {
                onError("Cart is empty or not loaded")
                return@launch
            }

            // Validate delivery address for delivery orders
            if (orderType == OrderType.DELIVERY && deliveryAddress.isNullOrBlank()) {
                onError("Delivery address is required for delivery orders")
                return@launch
            }

            val cart = currentState.cart

            // Convert cart items to order items
            val orderItems = cart.items.map { cartItem ->
                OrderItemRequest(
                    menuItemId = cartItem.menuItemId,
                    name = cartItem.name,
                    quantity = cartItem.quantity,
                    chosenIngredients = cartItem.chosenIngredients.map {
                        ChosenIngredientRequest(
                            name = it.name,
                            isDefault = it.isDefault
                        )
                    },
                    chosenOptions = cartItem.chosenOptions.map {
                        ChosenOptionRequest(
                            name = it.name,
                            price = it.price
                        )
                    },
                    calculatedPrice = cartItem.calculatedPrice
                )
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
                notes = notes
            )

            // Create order via OrderRepository
            val createdOrder = orderRepository.createOrder(orderRequest)

            if (createdOrder != null) {
                // Clear cart after successful order
                clearCart()
                onSuccess(createdOrder)
            } else {
                onError("Failed to create order. Please try again.")
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
