package com.example.damprojectfinal.user.feautre_order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.dto.order.CreateOrderRequest
import com.example.damprojectfinal.core.dto.order.OrderResponse
import com.example.damprojectfinal.core.dto.order.UpdateOrderStatusRequest
import com.example.damprojectfinal.core.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    // -----------------------------
    // STATES
    // -----------------------------

    private val _orders = MutableStateFlow<List<OrderResponse>?>(null)
    val orders: StateFlow<List<OrderResponse>?> = _orders

    private val _singleOrder = MutableStateFlow<OrderResponse?>(null)
    val singleOrder: StateFlow<OrderResponse?> = _singleOrder

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)

    val error: StateFlow<String?> = _error


    // ---------------------------------------------------------
    // CREATE ORDER
    // ---------------------------------------------------------
    fun createOrder(request: CreateOrderRequest) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.createOrder(request)

            if (result != null) {
                _singleOrder.value = result
            } else {
                _error.value = "Failed to create order"
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // LOAD ORDERS FOR USER
    // ---------------------------------------------------------
    fun loadOrdersByUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getOrdersByUser(userId)

            if (result != null) {
                _orders.value = result
            } else {
                _error.value = "Failed to load orders"
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // LOAD ORDERS FOR PROFESSIONAL
    // ---------------------------------------------------------
    fun loadOrdersByProfessional(professionalId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getOrdersByProfessional(professionalId)

            if (result != null) {
                _orders.value = result
            } else {
                _error.value = "Failed to load professional orders"
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // UPDATE ORDER STATUS
    // ---------------------------------------------------------
    fun updateOrderStatus(orderId: String, request: UpdateOrderStatusRequest) {
        viewModelScope.launch {
            // Don't set global loading to true to avoid full screen flicker
            // just perform the update quietly or let specific UI handle local loading
            _error.value = null

            val result = repository.updateOrderStatus(orderId, request)

            if (result != null) {
                _singleOrder.value = result
                
                // Update the order in the list locally to avoid full reload
                val currentOrders = _orders.value.orEmpty().toMutableList()
                val index = currentOrders.indexOfFirst { it._id == orderId }
                if (index != -1) {
                    currentOrders[index] = result
                    _orders.value = currentOrders
                }
            } else {
                _error.value = "Failed to update order status"
            }
        }
    }


    // ---------------------------------------------------------
    // LOAD PENDING ORDERS FOR PROFESSIONAL
    // ---------------------------------------------------------
    fun loadPendingOrders(professionalId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getPendingOrders(professionalId)

            if (result != null) {
                _orders.value = result
            } else {
                _error.value = "Failed to load pending orders"
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // LOAD SINGLE ORDER BY ID
    // ---------------------------------------------------------
    fun loadOrderById(orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getOrderById(orderId)

            if (result != null) {
                _singleOrder.value = result
            } else {
                _error.value = "Failed to load order"
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // DELETE SINGLE ORDER
    // ---------------------------------------------------------
    fun deleteOrder(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val success = repository.deleteOrder(orderId)

            if (success) {
                // Remove from local state
                _orders.value = _orders.value?.filter { it._id != orderId }
                _singleOrder.value = null
                onSuccess()
            } else {
                val errorMsg = "Failed to delete order"
                _error.value = errorMsg
                onError(errorMsg)
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // DELETE ALL ORDERS FOR USER
    // ---------------------------------------------------------
    fun deleteAllOrdersByUser(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val success = repository.deleteAllOrdersByUser(userId)

            if (success) {
                // Clear local state
                _orders.value = emptyList()
                _singleOrder.value = null
                onSuccess()
            } else {
                val errorMsg = "Failed to delete all orders"
                _error.value = errorMsg
                onError(errorMsg)
            }

            _loading.value = false
        }
    }


    // ---------------------------------------------------------
    // DELETE ALL ORDERS FOR PROFESSIONAL
    // ---------------------------------------------------------
    fun deleteAllOrdersByProfessional(professionalId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val success = repository.deleteAllOrdersByProfessional(professionalId)

            if (success) {
                // Clear local state
                _orders.value = emptyList()
                _singleOrder.value = null
                onSuccess()
            } else {
                val errorMsg = "Failed to delete all orders"
                _error.value = errorMsg
                onError(errorMsg)
            }

            _loading.value = false
        }
    }


    // ===================================================================
    // FACTORY
    // ===================================================================

    class Factory(private val repository: OrderRepository) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
                return OrderViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
