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
import com.example.damprojectfinal.ui.theme.*

data class OrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val finalPrice: Float
)

enum class CommandType { TAKEAWAY, DINE_IN, DELIVERY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    orderItems: List<OrderItem>,
    onBackClick: () -> Unit,
    onConfirmOrder: (CommandType) -> Unit
) {
    // State for command type selection
    var selectedCommand by remember { mutableStateOf<CommandType?>(null) }

    // Calculate Order Total
    val orderTotal = orderItems.sumOf { (it.quantity * it.finalPrice).toDouble() }.toFloat()

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
                onConfirm = { selectedCommand?.let { onConfirmOrder(it) } },
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

// --- Order Confirmation Helpers ---

@Composable
fun CartSummaryCard(items: List<OrderItem>, total: Float) {
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
    selectedType: CommandType?,
    onSelect: (CommandType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        listOf(
            CommandType.TAKEAWAY to "À Emporter",
            CommandType.DINE_IN to "Sur place",
            CommandType.DELIVERY to "À livrer"
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

@Preview(showBackground = true)
@Composable
fun OrderConfirmationPreview() {
    val mockItems = listOf(
        OrderItem("1", "Cheese Burger", 1, 38.0f),
        OrderItem("2", "Pizza", 1, 40.0f)
    )
    OrderConfirmationScreen(
        orderItems = mockItems,
        onBackClick = {},
        onConfirmOrder = {}
    )
}