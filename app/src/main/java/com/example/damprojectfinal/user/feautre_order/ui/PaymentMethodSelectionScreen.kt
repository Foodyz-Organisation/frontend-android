package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.ui.theme.*

enum class PaymentMethod {
    CASH,
    CARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelectionScreen(
    totalPrice: Float,
    onBackClick: () -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit
) {
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Méthode de Paiement", color = AppDarkText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            PaymentMethodBottomBar(
                totalPrice = totalPrice,
                onConfirm = {
                    selectedMethod?.let { method ->
                        onPaymentMethodSelected(method)
                    }
                },
                isConfirmEnabled = selectedMethod != null
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
            // Total Price Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppCardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total à payer",
                        color = AppDarkText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${String.format("%.3f", totalPrice)} TND",
                        color = Color(0xFF9333EA),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Payment Method Options
            PaymentMethodOption(
                method = PaymentMethod.CASH,
                title = "Espèces",
                description = "Payer en espèces à la livraison",
                icon = Icons.Default.Money,
                isSelected = selectedMethod == PaymentMethod.CASH,
                onClick = { selectedMethod = PaymentMethod.CASH }
            )

            Spacer(Modifier.height(12.dp))

            PaymentMethodOption(
                method = PaymentMethod.CARD,
                title = "Carte Bancaire",
                description = "Payer par carte de crédit/débit",
                icon = Icons.Default.CreditCard,
                isSelected = selectedMethod == PaymentMethod.CARD,
                onClick = { selectedMethod = PaymentMethod.CARD }
            )
        }
    }
}

@Composable
fun PaymentMethodOption(
    method: PaymentMethod,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF3E8FF) else AppCardBackground
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF9333EA))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color(0xFF9333EA) else AppDarkText,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        title,
                        color = AppDarkText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        description,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9333EA))
            )
        }
    }
}

@Composable
fun PaymentMethodBottomBar(
    totalPrice: Float,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Total",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                "${String.format("%.3f", totalPrice)} TND",
                color = AppDarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCartButtonYellow,
                disabledContainerColor = Color.LightGray
            )
        ) {
            Text("Continuer", color = AppDarkText, fontWeight = FontWeight.Bold)
        }
    }
}

