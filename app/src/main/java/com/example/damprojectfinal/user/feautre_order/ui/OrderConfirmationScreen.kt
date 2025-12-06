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
    
    // Load cart state from ViewModel
    val cartState by cartViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }
    
    // Get cart items from state
    val orderItems = when (cartState) {
        is CartUiState.Success -> (cartState as CartUiState.Success).cart.items
        else -> emptyList()
    }

    // Calculate Order Total
    val orderTotal = orderItems.sumOf { (it.quantity * it.calculatedPrice) }.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Votre Commande", color = AppDarkText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ConfirmationBottomBar(
                onCancel = onBackClick,
                onConfirm = {
                    selectedCommand?.let { type ->
                        cartViewModel.checkout(
                            professionalId = professionalId,
                            orderType = type,
                            onSuccess = {
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                                onOrderSuccess()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                isConfirmEnabled = selectedCommand != null
            )
        },
        containerColor = AppBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Show loading or content
            when (cartState) {
                is CartUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CartUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cart is empty", color = Color.Gray)
                    }
                }
                is CartUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        onSelect = { selectedCommand = it }
                    )
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
            Box(
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
                    Text("Total Price", color = AppDarkText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } ?: Text("Cart is Empty", color = AppDarkText)
            }
            Text(
                "${String.format("%.2f", total)} DT",
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

