package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.dto.order.OrderType
import com.example.damprojectfinal.ui.theme.*


import com.example.damprojectfinal.core.dto.cart.CartItemResponse
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModel
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartUiState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.damprojectfinal.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.damprojectfinal.user.feautre_order.viewmodel.LocationTrackingViewModel
import com.example.damprojectfinal.core.api.TokenManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

private const val BASE_URL = "http://10.0.2.2:3000/"



// Payment flow state
sealed class PaymentFlowState {
    object OrderDetails : PaymentFlowState()
    object PaymentMethodSelection : PaymentFlowState()
    data class CardPayment(val paymentIntentId: String) : PaymentFlowState()
    object LocationSharing : PaymentFlowState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    cartViewModel: CartViewModel,
    professionalId: String,
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    // State for command type selection
    var selectedCommand by remember { mutableStateOf<OrderType?>(null) }
    val context = LocalContext.current

    // Payment flow state
    var paymentFlowState by remember { mutableStateOf<PaymentFlowState>(PaymentFlowState.OrderDetails) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var paymentIntentId by remember { mutableStateOf<String?>(null) }
    var createdOrder by remember {
        mutableStateOf<com.example.damprojectfinal.core.dto.order.OrderResponse?>(
            null
        )
    }
    
    // Store paymentId separately to ensure it's available for confirmation
    var orderPaymentId by remember { mutableStateOf<String?>(null) }
    
    // Store Stripe paymentIntentId separately (this is what backend expects for confirmation)
    var stripePaymentIntentId by remember { mutableStateOf<String?>(null) }

    // Location sharing state
    var isLocationSharingEnabled by remember { mutableStateOf(false) }
    var createdOrderId by remember { mutableStateOf<String?>(null) }

    // Location tracking ViewModel
    val locationViewModel: LocationTrackingViewModel = viewModel()
    val locationState by locationViewModel.state.collectAsState()

    // Token manager for user ID
    val tokenManager = remember { TokenManager(context) }
    val userId = remember { tokenManager.getUserId() ?: "" }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, prepare for sharing (will start after order creation)
            isLocationSharingEnabled = true
        } else {
            Toast.makeText(
                context,
                "Location permission is required for tracking",
                Toast.LENGTH_SHORT
            ).show()
            isLocationSharingEnabled = false
        }
    }

    // Load cart state from ViewModel
    val cartState by cartViewModel.uiState.collectAsState()
    
    // Load cart only when screen first appears (not when navigating between payment states)
    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }
    
    // Reload cart only when returning to order details from other screens
    LaunchedEffect(paymentFlowState) {
        if (paymentFlowState == PaymentFlowState.OrderDetails) {
            // Only reload if we're coming back to order details
            cartViewModel.loadCart()
        }
    }

    // Get cart items from state
    val orderItems = when (cartState) {
        is CartUiState.Success -> (cartState as CartUiState.Success).cart.items
        else -> emptyList()
    }

    // Calculate Order Total - use 0 if empty to avoid errors
    val orderTotal = if (orderItems.isNotEmpty()) {
        orderItems.sumOf { (it.quantity * it.calculatedPrice) }.toFloat()
    } else {
        0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Votre Commande",
                        color = AppDarkText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppDarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (paymentFlowState == PaymentFlowState.OrderDetails) {
                // Extract cart state to avoid smart cast issues
                val currentCartState = cartState
                val hasItems = when (currentCartState) {
                    is CartUiState.Success -> currentCartState.cart.items.isNotEmpty()
                    else -> false
                }
                
                ConfirmationBottomBar(
                    onCancel = onBackClick,
                    onConfirm = {
                        selectedCommand?.let { orderType ->
                            // Validate cart has items before proceeding
                            when (currentCartState) {
                                is CartUiState.Success -> {
                                    if (currentCartState.cart.items.isEmpty()) {
                                        Toast.makeText(context, "Cart is empty. Please add items first.", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Move to payment method selection only if cart has items
                                        paymentFlowState = PaymentFlowState.PaymentMethodSelection
                                    }
                                }
                                is CartUiState.Empty -> {
                                    Toast.makeText(context, "Cart is empty. Please add items first.", Toast.LENGTH_LONG).show()
                                }
                                is CartUiState.Error -> {
                                    Toast.makeText(context, "Error loading cart. Please try again.", Toast.LENGTH_LONG).show()
                                    cartViewModel.loadCart() // Retry loading
                                }
                                is CartUiState.Loading -> {
                                    // Wait for cart to load
                                }
                            }
                        }
                    },
                    isConfirmEnabled = selectedCommand != null && hasItems
                )
            }
        },
        containerColor = AppBackgroundLight
    ) { paddingValues ->
        // Handle different payment flow states
        when (paymentFlowState) {
            is PaymentFlowState.PaymentMethodSelection -> {
                // Use current cart state to check items
                val currentCartStateForPayment = cartState
                val itemsForPayment = when (currentCartStateForPayment) {
                    is CartUiState.Success -> currentCartStateForPayment.cart.items
                    else -> emptyList()
                }
                
                // Validate cart has items before showing payment method selection
                if (itemsForPayment.isEmpty()) {
                    // Cart is empty, go back to order details
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Cart is empty. Please add items to cart first.", Toast.LENGTH_LONG).show()
                        paymentFlowState = PaymentFlowState.OrderDetails
                    }
                    // Show empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Cart is empty",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppDarkText
                            )
                            Text(
                                "Please add items to cart first",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Button(
                                onClick = {
                                    paymentFlowState = PaymentFlowState.OrderDetails
                                    onBackClick()
                                }
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                } else {
                    // Calculate total from current items
                    val paymentTotal = if (itemsForPayment.isNotEmpty()) {
                        itemsForPayment.sumOf { (it.quantity * it.calculatedPrice) }.toFloat()
                    } else {
                        orderTotal
                    }
                    
                    PaymentMethodSelectionScreen(
                        totalPrice = paymentTotal,
                        onBackClick = {
                            paymentFlowState = PaymentFlowState.OrderDetails
                        },
                        onPaymentMethodSelected = { method ->
                            selectedPaymentMethod = method
                            android.util.Log.d("OrderConfirmation", "💳 Payment method selected: ${method.name}")
                            
                            selectedCommand?.let { orderType ->
                                // Validate cart again before creating order using current state
                                val currentState = cartState
                                val currentItems = when (currentState) {
                                    is CartUiState.Success -> currentState.cart.items
                                    else -> emptyList()
                                }
                                
                                if (currentItems.isEmpty()) {
                                    Toast.makeText(context, "Cart is empty. Please add items to cart first.", Toast.LENGTH_LONG).show()
                                    paymentFlowState = PaymentFlowState.OrderDetails
                                    return@let
                                }
                                
                                android.util.Log.d("OrderConfirmation", "📦 Creating order with ${currentItems.size} items, payment: ${method.name}")
                                
                                // Create order with selected payment method
                                cartViewModel.checkoutWithPayment(
                                    professionalId = professionalId,
                                    orderType = orderType,
                                    paymentMethod = method.name,
                                    onSuccess = { orderResponse, paymentIntentIdFromResponse ->
                                        android.util.Log.d("OrderConfirmation", "🎉 ========== ORDER CREATION SUCCESS CALLBACK ==========")
                                        android.util.Log.d("OrderConfirmation", "✅ Order created: ${orderResponse._id}")
                                        android.util.Log.d("OrderConfirmation", "💳 PaymentIntentId from response: $paymentIntentIdFromResponse")
                                        android.util.Log.d("OrderConfirmation", "📋 Full order response details:")
                                        android.util.Log.d("OrderConfirmation", "  - Order ID: ${orderResponse._id}")
                                        android.util.Log.d("OrderConfirmation", "  - Payment Method: ${orderResponse.paymentMethod}")
                                        android.util.Log.d("OrderConfirmation", "  - Payment ID: ${orderResponse.paymentId}")
                                        android.util.Log.d("OrderConfirmation", "  - Items count: ${orderResponse.items.size}")
                                        android.util.Log.d("OrderConfirmation", "  - Total Price: ${orderResponse.totalPrice}")
                                        
                                        // Store order and payment info
                                        createdOrder = orderResponse
                                        createdOrderId = orderResponse._id
                                        
                                        // CRITICAL: Store paymentId separately for payment confirmation
                                        // NOTE: There are TWO different IDs:
                                        // 1. paymentId (MongoDB) - links order to payment record
                                        // 2. paymentIntentId (Stripe) - used for Stripe payment confirmation
                                        orderPaymentId = orderResponse.paymentId
                                        android.util.Log.d("OrderConfirmation", "💳 PaymentId (MongoDB) from order: ${orderResponse.paymentId}")
                                        android.util.Log.d("OrderConfirmation", "💳 PaymentIntentId (Stripe) from response: $paymentIntentIdFromResponse")
                                        android.util.Log.d("OrderConfirmation", "💾 Stored paymentId (MongoDB): $orderPaymentId")
                                        
                                        // Store Stripe paymentIntentId separately (this is what backend expects)
                                        stripePaymentIntentId = paymentIntentIdFromResponse
                                        android.util.Log.d("OrderConfirmation", "🔍 PAYMENT IDS BREAKDOWN:")
                                        android.util.Log.d("OrderConfirmation", "  - MongoDB paymentId: ${orderResponse.paymentId} (for linking order to payment)")
                                        android.util.Log.d("OrderConfirmation", "  - Stripe paymentIntentId: $stripePaymentIntentId (for Stripe payment confirmation)")
                                        android.util.Log.d("OrderConfirmation", "  ⚠️ Backend confirmPayment needs Stripe paymentIntentId, NOT MongoDB paymentId")
                                        android.util.Log.d("OrderConfirmation", "💾 Stored Stripe paymentIntentId: $stripePaymentIntentId")
                                        
                                        // Verify storage immediately
                                        android.util.Log.d("OrderConfirmation", "🔍 Verification after storage:")
                                        android.util.Log.d("OrderConfirmation", "  - createdOrder?._id: ${createdOrder?._id}")
                                        android.util.Log.d("OrderConfirmation", "  - orderPaymentId: $orderPaymentId")
                                        
                                        if (orderPaymentId != null) {
                                            android.util.Log.d("OrderConfirmation", "✅ PaymentId successfully stored: $orderPaymentId")
                                        } else {
                                            android.util.Log.e("OrderConfirmation", "❌ CRITICAL ERROR: PaymentId is NULL in order response!")
                                            android.util.Log.e("OrderConfirmation", "  The backend did not return paymentId in the order.")
                                            android.util.Log.e("OrderConfirmation", "  This will cause payment confirmation to fail.")
                                        }
                                        android.util.Log.d("OrderConfirmation", "================================================")

                                        if (method == PaymentMethod.CARD) {
                                            // For CARD payments, always navigate to card payment screen
                                            // Priority: paymentIntentId from response > paymentId from order > placeholder
                                            val intentId = paymentIntentIdFromResponse 
                                                ?: orderResponse.paymentId 
                                                ?: "pi_${System.currentTimeMillis()}_placeholder"
                                            android.util.Log.d("OrderConfirmation", "💳 CARD payment selected")
                                            android.util.Log.d("OrderConfirmation", "📋 Order ID: ${orderResponse._id}")
                                            android.util.Log.d("OrderConfirmation", "💳 PaymentIntentId from response: $paymentIntentIdFromResponse")
                                            android.util.Log.d("OrderConfirmation", "💳 PaymentId from order: ${orderResponse.paymentId}")
                                            android.util.Log.d("OrderConfirmation", "🔄 Final paymentIntentId to use: $intentId")
                                            
                                            // Verify paymentId is stored
                                            if (orderPaymentId != null) {
                                                android.util.Log.d("OrderConfirmation", "✅ PaymentId stored successfully: $orderPaymentId")
                                            } else {
                                                android.util.Log.w("OrderConfirmation", "⚠️ WARNING: PaymentId is null! Order may not have paymentId field.")
                                            }
                                            
                                            android.util.Log.d("OrderConfirmation", "🔄 Navigating to CardPaymentScreen")
                                            paymentIntentId = intentId
                                            paymentFlowState = PaymentFlowState.CardPayment(intentId)
                                        } else {
                                            // CASH payment - proceed directly to location sharing
                                            // CASH payments do NOT need payment confirmation
                                            android.util.Log.d("OrderConfirmation", "💵 CASH payment selected")
                                            android.util.Log.d("OrderConfirmation", "✅ Order created for CASH: ${orderResponse._id}")
                                            android.util.Log.d("OrderConfirmation", "🚫 CASH payment - skipping payment confirmation")
                                            android.util.Log.d("OrderConfirmation", "📍 Navigating directly to LocationSharing")
                                            paymentIntentId = null // Clear any payment intent for CASH
                                            paymentFlowState = PaymentFlowState.LocationSharing
                                        }
                                    },
                                    onError = { error ->
                                        android.util.Log.e("OrderConfirmation", "❌ Order creation failed: $error")
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        paymentFlowState = PaymentFlowState.OrderDetails
                                    }
                                )
                            }
                        }
                    )
                }
            }

            is PaymentFlowState.CardPayment -> {
                // Extract paymentIntentId from the state to avoid smart cast issues
                val cardPaymentState = paymentFlowState as PaymentFlowState.CardPayment
                val currentPaymentIntentId = cardPaymentState.paymentIntentId
                
                // Log when CardPayment screen is shown
                LaunchedEffect(Unit) {
                    android.util.Log.d("OrderConfirmation", "🎯 CardPaymentScreen is now displayed")
                    android.util.Log.d("OrderConfirmation", "💳 PaymentIntentId: $currentPaymentIntentId")
                    android.util.Log.d("OrderConfirmation", "💵 Selected payment method: ${selectedPaymentMethod?.name ?: "Unknown"}")
                    android.util.Log.d("OrderConfirmation", "📋 Created order: ${createdOrder?._id}")
                    android.util.Log.d("OrderConfirmation", "💾 Stored orderPaymentId: $orderPaymentId")
                    android.util.Log.d("OrderConfirmation", "📋 Created order paymentId: ${createdOrder?.paymentId}")
                    
                    // Safety check: Only show CardPaymentScreen for CARD payments
                    if (selectedPaymentMethod != PaymentMethod.CARD) {
                        android.util.Log.e("OrderConfirmation", "❌ ERROR: CardPaymentScreen shown for non-CARD payment: ${selectedPaymentMethod?.name}")
                        android.util.Log.e("OrderConfirmation", "🔄 Redirecting to LocationSharing...")
                        paymentFlowState = PaymentFlowState.LocationSharing
                    }
                    
                    // Verify we have paymentId for confirmation
                    if (orderPaymentId == null && createdOrder?.paymentId == null) {
                        android.util.Log.w("OrderConfirmation", "⚠️ WARNING: No paymentId available for payment confirmation!")
                        android.util.Log.w("OrderConfirmation", "  This may cause payment confirmation to fail.")
                    }
                }
                
                // Only show CardPaymentScreen if payment method is actually CARD
                if (selectedPaymentMethod == PaymentMethod.CARD) {
                    CardPaymentScreen(
                        totalPrice = orderTotal,
                        paymentIntentId = currentPaymentIntentId,
                        onBackClick = {
                            android.util.Log.d("OrderConfirmation", "⬅️ Back from CardPaymentScreen")
                            paymentFlowState = PaymentFlowState.PaymentMethodSelection
                        },
                        onPaymentConfirmed = { cardDetails ->
                            android.util.Log.d("OrderConfirmation", "✅ Payment confirmed in CardPaymentScreen")
                            android.util.Log.d("OrderConfirmation", "💳 Card details received:")
                            android.util.Log.d("OrderConfirmation", "  Card Number: ${cardDetails.cardNumber.take(4)}****${cardDetails.cardNumber.takeLast(4)}")
                            android.util.Log.d("OrderConfirmation", "  Expiry: ${cardDetails.expMonth}/${cardDetails.expYear}")
                            android.util.Log.d("OrderConfirmation", "  CVV: ${cardDetails.cvv.length} digits")
                            android.util.Log.d("OrderConfirmation", "  Cardholder: ${cardDetails.cardholderName}")
                            android.util.Log.d("OrderConfirmation", "💳 PaymentIntentId from state: $currentPaymentIntentId")
                            android.util.Log.d("OrderConfirmation", "📋 Created order: ${createdOrder?._id}")
                            android.util.Log.d("OrderConfirmation", "💾 Stored stripePaymentIntentId (Stripe): $stripePaymentIntentId")
                            
                            // CRITICAL: Backend confirmPayment expects STRIPE paymentIntentId, NOT MongoDB paymentId
                            // The backend looks up the payment by Stripe paymentIntentId, not by MongoDB _id
                            val paymentIntentIdToUse = stripePaymentIntentId ?: currentPaymentIntentId
                            
                            android.util.Log.d("OrderConfirmation", "🔍 PAYMENT CONFIRMATION ANALYSIS:")
                            android.util.Log.d("OrderConfirmation", "  - Stripe paymentIntentId: $paymentIntentIdToUse (for PaymentIntent lookup)")
                            android.util.Log.d("OrderConfirmation", "  - Card details: Will be used to create PaymentMethod")
                            android.util.Log.d("OrderConfirmation", "  - Backend will: Create PaymentMethod → Attach to PaymentIntent → Confirm")
                            
                            if (paymentIntentIdToUse == null) {
                                android.util.Log.e("OrderConfirmation", "❌ ERROR: No Stripe paymentIntentId found!")
                                android.util.Log.e("OrderConfirmation", "  Order ID: ${createdOrder?._id}")
                                android.util.Log.e("OrderConfirmation", "  Stored stripePaymentIntentId: $stripePaymentIntentId")
                                android.util.Log.e("OrderConfirmation", "  PaymentIntentId from state: $currentPaymentIntentId")
                                Toast.makeText(
                                    context,
                                    "Error: Payment Intent ID not found. Please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@CardPaymentScreen
                            }
                            
                            android.util.Log.d("OrderConfirmation", "💳 Using Stripe paymentIntentId for confirmation: $paymentIntentIdToUse")
                            android.util.Log.d("OrderConfirmation", "📤 Sending payment confirmation request to backend...")
                            android.util.Log.d("OrderConfirmation", "  Endpoint: POST /orders/payment/confirm")
                            android.util.Log.d("OrderConfirmation", "  Request body:")
                            android.util.Log.d("OrderConfirmation", "    - paymentIntentId: $paymentIntentIdToUse")
                            android.util.Log.d("OrderConfirmation", "    - cardNumber: ${cardDetails.cardNumber.take(4)}****")
                            android.util.Log.d("OrderConfirmation", "    - expMonth: ${cardDetails.expMonth}")
                            android.util.Log.d("OrderConfirmation", "    - expYear: ${cardDetails.expYear}")
                            android.util.Log.d("OrderConfirmation", "    - cvv: ***")
                            
                            // Confirm payment with backend (ONLY for CARD payments)
                            // Backend will create PaymentMethod from card details, then confirm payment
                            cartViewModel.confirmPaymentWithCardDetails(
                                paymentIntentId = paymentIntentIdToUse, // Stripe PaymentIntent ID
                                cardNumber = cardDetails.cardNumber,
                                expMonth = cardDetails.expMonth,
                                expYear = cardDetails.expYear,
                                cvv = cardDetails.cvv,
                                cardholderName = cardDetails.cardholderName,
                                onSuccess = { confirmedOrder ->
                                    android.util.Log.d("OrderConfirmation", "✅ Payment confirmation successful")
                                    android.util.Log.d("OrderConfirmation", "📋 Confirmed order ID: ${confirmedOrder._id}")
                                    createdOrder = confirmedOrder
                                    createdOrderId = confirmedOrder._id
                                    Toast.makeText(
                                        context,
                                        "Payment confirmed successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    paymentFlowState = PaymentFlowState.LocationSharing
                                },
                                onError = { error ->
                                    android.util.Log.e("OrderConfirmation", "❌ Payment confirmation failed: $error")
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                } else {
                    // Safety fallback: If somehow we're here for non-CARD, go to location sharing
                    LaunchedEffect(Unit) {
                        android.util.Log.w("OrderConfirmation", "⚠️ CardPayment state but payment method is not CARD - redirecting")
                        paymentFlowState = PaymentFlowState.LocationSharing
                    }
                    // Show loading or redirect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is PaymentFlowState.LocationSharing -> {
                // Helper function to complete order after location sharing
                val completeOrder: () -> Unit = {
                    createdOrder?.let { order ->
                        selectedCommand?.let { type ->
                            // If user agreed to share location, start tracking
                            if (isLocationSharingEnabled && (type == OrderType.EAT_IN || type == OrderType.TAKEAWAY)) {
                                val orderId = order._id
                                createdOrderId = orderId

                                // Connect to WebSocket and start sharing
                                locationViewModel.connectToOrder(orderId, userId, "user")

                                // Check permission again before starting
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    locationViewModel.startSharingLocation()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Location permission required for tracking",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT)
                            .show()
                        onOrderSuccess()
                    }
                }

                LocationSharingScreen(
                    onBackClick = {
                        // Can't go back after payment
                    },
                    onShareLocation = {
                        isLocationSharingEnabled = true
                        completeOrder()
                    },
                    onSkip = {
                        isLocationSharingEnabled = false
                        completeOrder()
                    }
                )
            }

            is PaymentFlowState.OrderDetails -> {
                // Original order details screen
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Show loading or content
                    when (cartState) {
                        is CartUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is CartUiState.Empty -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cart is empty", color = Color.Gray)
                            }
                        }

                        is CartUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Error loading cart", color = AppPrimaryRed)
                            }
                        }

                        is CartUiState.Success -> {
                            // 1. Cart Summary Card
                            CartSummaryCard(orderItems, orderTotal)

                            Spacer(Modifier.height(24.dp))

                            // 2. Command Type Selection
                            CommandTypeSelection(
                                selectedType = selectedCommand,
                                onSelect = { type ->
                                    selectedCommand = type
                                    // Location sharing will be handled after payment confirmation
                                    isLocationSharingEnabled = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Order Confirmation Helpers ---

@Composable
fun CartSummaryCard(items: List<CartItemResponse>, total: Float) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppCardBackground)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    // Placeholder Image Box (Gray Square)
                    // Image Box
                    val firstItem = items.firstOrNull()
                    AsyncImage(
                        model = if (firstItem?.image.isNullOrEmpty()) null else BASE_URL + firstItem?.image,
                        contentDescription = firstItem?.name ?: "Order Item",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.placeholder),
                        error = painterResource(id = R.drawable.placeholder),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        // Display first item as representative (simplified for this structure)
                        items.firstOrNull()?.let { item ->
                            Text("${item.name} x${item.quantity}", color = AppDarkText)
                            Text(
                                "Total Price",
                                color = AppDarkText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } ?: Text("Cart is Empty", color = AppDarkText)
                    }
                    Text(
                        "${String.format("%.3f", total)} TND",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9333EA)
                    )
                }
            }
        }

@Composable
fun CommandTypeSelection(
    selectedType: OrderType?,
    onSelect: (OrderType) -> Unit
) {
            Column(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    OrderType.TAKEAWAY to "À Emporter",
                    OrderType.EAT_IN to "Sur place",
                    OrderType.DELIVERY to "À livrer"
                ).forEach { (type, label) ->
                    CommandTypeOption(
                        label = label,
                        isSelected = type == selectedType,
                        onClick = { onSelect(type) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

@Composable
fun CommandTypeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppCardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = AppDarkText, fontSize = 16.sp)
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(selectedColor = AppPrimaryRed)
                    )
                }
            }
        }

@Composable
fun ConfirmationBottomBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean
) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Text("Annuler", color = AppDarkText, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = onConfirm,
                    enabled = isConfirmEnabled,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppCartButtonYellow,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text("Confirme", color = AppDarkText, fontWeight = FontWeight.Bold)
                }
            }
        }
    

