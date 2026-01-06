package com.example.damprojectfinal.user.feautre_order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.ui.theme.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Test card data
data class TestCard(
    val name: String,
    val description: String,
    val cardNumber: String,
    val month: String = "12",
    val year: String = "2028",
    val cvv: String = "123"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentScreen(
    totalPrice: Float,
    paymentIntentId: String,
    onBackClick: () -> Unit,
    onPaymentConfirmed: (String) -> Unit // Now receives PaymentMethod ID (pm_xxx) from Stripe
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var cardNumber by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Test cards
    val testCards = remember {
        listOf(
            TestCard("Visa Success", "Succeeds immediately", "4242424242424242"),
            TestCard("Visa Decline", "Declines immediately", "4000000000000002"),
            TestCard("Visa 3D Secure", "Requires authentication", "4000002500003155"),
            TestCard("Mastercard Success", "Succeeds immediately", "5555555555554444")
        )
    }

    // Format card number (add spaces every 4 digits)
    val formattedCardNumber = remember(cardNumber) {
        cardNumber.replace(" ", "").chunked(4).joinToString(" ")
    }

    // Month options
    val months = remember {
        (1..12).map { month ->
            String.format("%02d - %s", month, getMonthName(month))
        }
    }

    // Year options (current year to 10 years ahead)
    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
    val years = remember {
        (currentYear..currentYear + 10).map { it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", color = AppDarkText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            CardPaymentBottomBar(
                totalPrice = totalPrice,
                isLoading = isLoading,
                onCancel = onBackClick,
                onConfirm = {
                    // Validate inputs
                    if (cardNumber.replace(" ", "").length < 16) {
                        errorMessage = "Invalid card number"
                        return@CardPaymentBottomBar
                    }
                    if (selectedMonth.isEmpty() || selectedYear.isEmpty()) {
                        errorMessage = "Please select expiry date"
                        return@CardPaymentBottomBar
                    }
                    if (cvv.length < 3) {
                        errorMessage = "Invalid CVV"
                        return@CardPaymentBottomBar
                    }
                    if (cardholderName.isBlank()) {
                        errorMessage = "Cardholder name is required"
                        return@CardPaymentBottomBar
                    }

                    errorMessage = null
                    isLoading = true

                    // Extract card details for backend to create PaymentMethod
                    val cleanCardNumber = cardNumber.replace(" ", "")
                    val expMonthInt = selectedMonth.toIntOrNull() ?: 0
                    // Convert 4-digit year to 2-digit year for Stripe SDK (2025 -> 25)
                    val expYearInt = selectedYear.toIntOrNull()?.let { 
                        if (it >= 2000) it - 2000 else it 
                    } ?: 0
                    
                    if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
                        errorMessage = "Invalid card number length"
                        isLoading = false
                        return@CardPaymentBottomBar
                    }
                    
                    if (expMonthInt < 1 || expMonthInt > 12) {
                        errorMessage = "Invalid expiry month"
                        isLoading = false
                        return@CardPaymentBottomBar
                    }
                    
                    android.util.Log.d("CardPaymentScreen", "💳 ========== CREATING PAYMENT METHOD WITH STRIPE SDK ==========")
                    android.util.Log.d("CardPaymentScreen", "  Card Number: ${cleanCardNumber.take(4)}****${cleanCardNumber.takeLast(4)}")
                    android.util.Log.d("CardPaymentScreen", "  Expiry: $expMonthInt/$expYearInt")
                    android.util.Log.d("CardPaymentScreen", "  CVV: ${cvv.length} digits")
                    android.util.Log.d("CardPaymentScreen", "  Cardholder: $cardholderName")
                    android.util.Log.d("CardPaymentScreen", "  ✅ Using Stripe Android SDK to create PaymentMethod")

                    // ⭐ Create PaymentMethod using Stripe Android SDK
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            android.util.Log.d("CardPaymentScreen", "🚀 Creating Stripe PaymentMethod...")
                            
                            // Create CardParams
                            val cardParams = CardParams(
                                number = cleanCardNumber,
                                expMonth = expMonthInt,
                                expYear = expYearInt,
                                cvc = cvv
                            )
                            
                            // Create PaymentMethodCreateParams with CardParams
                            val paymentMethodParams = PaymentMethodCreateParams.createCard(cardParams)
                            
                            android.util.Log.d("CardPaymentScreen", "📤 Sending PaymentMethod creation request to Stripe...")
                            
                            // Create Stripe instance
                            val stripe = Stripe(
                                context = context,
                                publishableKey = PaymentConfiguration.getInstance(context).publishableKey
                            )
                            
                            // Create PaymentMethod (API call to Stripe)
                            val paymentMethod = withContext(Dispatchers.IO) {
                                stripe.createPaymentMethodSynchronous(paymentMethodParams)
                            }
                            
                            if (paymentMethod != null) {
                                val paymentMethodId = paymentMethod.id ?: run {
                                    android.util.Log.e("CardPaymentScreen", "❌ PaymentMethod created but ID is null")
                                    errorMessage = "Payment method creation failed"
                                    isLoading = false
                                    return@launch
                                }
                                
                                android.util.Log.d("CardPaymentScreen", "✅ PaymentMethod created successfully!")
                                android.util.Log.d("CardPaymentScreen", "  PaymentMethod ID: $paymentMethodId")
                                android.util.Log.d("CardPaymentScreen", "  Card brand: ${paymentMethod.card?.brand}")
                                android.util.Log.d("CardPaymentScreen", "  Last 4 digits: ${paymentMethod.card?.last4}")
                                android.util.Log.d("CardPaymentScreen", "")
                                android.util.Log.d("CardPaymentScreen", "🔒 Card details are now securely stored by Stripe")
                                android.util.Log.d("CardPaymentScreen", "📤 Sending ONLY PaymentMethod ID to backend")
                                
                                isLoading = false
                                onPaymentConfirmed(paymentMethodId)
                            } else {
                                android.util.Log.e("CardPaymentScreen", "❌ PaymentMethod creation failed - response is null")
                                errorMessage = "Payment method creation failed"
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CardPaymentScreen", "❌ Stripe PaymentMethod creation error: ${e.message}")
                            android.util.Log.e("CardPaymentScreen", "  Error type: ${e.javaClass.simpleName}")
                            e.printStackTrace()
                            
                            errorMessage = e.message ?: "Payment method creation failed"
                            isLoading = false
                        }
                    }
                },
                isConfirmEnabled = cardNumber.isNotBlank() &&
                        selectedMonth.isNotBlank() &&
                        selectedYear.isNotBlank() &&
                        cvv.isNotBlank() &&
                        cardholderName.isNotBlank() &&
                        !isLoading
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Payment Summary Card
            PaymentSummaryCard(totalPrice = totalPrice)

            Spacer(Modifier.height(16.dp))

            // Card Payment Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Section Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Card Payment",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDarkText
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Enter your card details to complete the payment",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(20.dp))

                    // Test Cards Section
                    Text(
                        "Test Cards (Tap to fill)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppDarkText
                    )

                    Spacer(Modifier.height(12.dp))

                    testCards.forEach { testCard ->
                        TestCardOption(
                            testCard = testCard,
                            onClick = {
                                cardNumber = testCard.cardNumber
                                selectedMonth = testCard.month
                                selectedYear = testCard.year
                                cvv = testCard.cvv
                                cardholderName = "John Doe"
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    // Cardholder Name
                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it },
                        label = { Text("Cardholder Name") },
                        placeholder = { Text("John Doe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Card Number
                    OutlinedTextField(
                        value = formattedCardNumber,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.replace(" ", "")
                            if (digitsOnly.length <= 16 && digitsOnly.all { it.isDigit() }) {
                                cardNumber = digitsOnly
                            }
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("1234 5678 9012 3456") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Expiry Date Row (Month and Year)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month Dropdown
                        MonthYearDropdown(
                            selected = selectedMonth,
                            placeholder = "Month",
                            options = months,
                            modifier = Modifier.weight(1f),
                            onSelected = { month ->
                                selectedMonth = month.take(2) // Extract "01" from "01 - Jan"
                            }
                        )

                        // Year Dropdown
                        MonthYearDropdown(
                            selected = selectedYear,
                            placeholder = "Year",
                            options = years,
                            modifier = Modifier.weight(1f),
                            onSelected = { year ->
                                selectedYear = year
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // CVV Field
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                cvv = newValue
                            }
                        },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            errorMessage!!,
                            color = AppPrimaryRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Payment Intent ID
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Payment Intent ID",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppDarkText
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        paymentIntentId,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentSummaryCard(totalPrice: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "Payment Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppDarkText
                    )
                    Text(
                        "Complete your payment",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "Total",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "${String.format("%.2f", totalPrice)} DT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9333EA)
                )
            }
        }
    }
}

@Composable
fun TestCardOption(
    testCard: TestCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    testCard.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppDarkText
                )
                Text(
                    testCard.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                testCard.cardNumber.chunked(4).joinToString(" "),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppDarkText,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearDropdown(
    selected: String,
    placeholder: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = modifier.menuAnchor(),
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CardPaymentBottomBar(
    totalPrice: Float,
    isLoading: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
        ) {
            Text("Cancel", color = AppDarkText, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onConfirm,
            enabled = isConfirmEnabled && !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E9E9E),
                disabledContainerColor = Color.LightGray
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Pay ${String.format("%.2f", totalPrice)} DT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}
