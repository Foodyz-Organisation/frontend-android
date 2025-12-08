package com.example.damprojectfinal.user.feature_cart_item.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.damprojectfinal.core.dto.cart.CartItemResponse
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartUiState
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModel
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R
import com.google.gson.Gson
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

private const val BASE_URL = "http://10.0.2.2:3000/"

// ---------------- Colors ----------------
val PrimaryRed = Color(0xFFEF4444)
val BackgroundLight = Color(0xFFF9FAFB)
val CardBackground = Color(0xFFFFFFFF)
val PrimaryYellow = Color(0xFFFFC107)
val DarkText = Color(0xFF1F2937)
val LightGrayIcon = Color(0xFF9CA3AF)

// ---------------- DetailsList ----------------
@Composable
fun DetailsList(title: String, details: List<String>) {
    if (details.isNotEmpty()) {
        Text(
            text = "$title:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = DarkText,
            modifier = Modifier.padding(top = 4.dp)
        )
        details.forEach { detail ->
            Text(
                text = "• $detail",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// ---------------- Cart Item Card ----------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartItemCard(
    item: CartItemResponse,
    itemIndex: Int,
    onRemoveClick: (Int) -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    onDetailsClick: (CartItemResponse) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val itemQuantity = item.quantity
    val totalPrice = item.calculatedPrice.toFloat() * itemQuantity

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .combinedClickable(
                onClick = { onDetailsClick(item) },
                onLongClick = { isExpanded = !isExpanded }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = if (item.image.isNullOrEmpty()) null else BASE_URL + item.image,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder),
            error = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder),
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name ?: "Item", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${"%.3f".format(totalPrice)} TND", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryYellow)
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(70.dp)
        ) {
            IconButton(onClick = { onRemoveClick(itemIndex) }) {
                Icon(Icons.Filled.Close, contentDescription = "Remove Item", tint = LightGrayIcon)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = DarkText,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { if (itemQuantity > 1) onQuantityChange(itemIndex, itemQuantity - 1) }
                        .padding(4.dp)
                        .size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(itemQuantity.toString(), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkText, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = PrimaryYellow,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onQuantityChange(itemIndex, itemQuantity + 1) }
                        .padding(4.dp)
                        .size(20.dp)
                )
            }
        }
    }

    AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            DetailsList(
                "Ingredients",
                item.chosenIngredients.map { "${it.name} (${if (it.isDefault) "Default" else "Added"})" }
            )
            if (item.chosenOptions.isNotEmpty()) {
                DetailsList(
                    "Options",
                    item.chosenOptions.map { "${it.name} (+${"%.3f".format(it.price)} TND)" }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

// ---------------- Empty Cart Animation ----------------
@Composable
fun EmptyCartAnimation() {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.shoppingcart))
    val progress by animateLottieCompositionAsState(composition.value, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(composition.value, progress = { progress }, modifier = Modifier.size(250.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Cart is Empty!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkText)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Time to find some delicious food.", fontSize = 16.sp, color = Color.Gray)
    }
}

// ---------------- Shopping Cart Screen ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(
    navController: NavController,
    cartVM: CartViewModel,
    professionalId: String // Add this parameter
) {
    val cartState by cartVM.uiState.collectAsState()
    val cartItems = (cartState as? CartUiState.Success)?.cart?.items ?: emptyList()
    val subtotal = cartItems.sumOf { it.calculatedPrice.toDouble() * it.quantity }.toFloat()

    LaunchedEffect(Unit) { cartVM.loadCart() }

    var selectedItem by remember { mutableStateOf<CartItemResponse?>(null) }
    var showPopup by remember { mutableStateOf(false) }

    val onDetailsClick: (CartItemResponse) -> Unit = { item ->
        selectedItem = item
        showPopup = true
    }

    val onRemove: (Int) -> Unit = { index -> cartVM.removeItem(index) }
    val onQuantityChange: (Int, Int) -> Unit = { index, qty -> cartVM.updateQuantity(index, qty) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart (${cartItems.size})", fontWeight = FontWeight.ExtraBold, color = DarkText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(shadowElevation = 16.dp, color = CardBackground) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", color = Color.Gray, fontSize = 16.sp)
                            Text("${"%.3f".format(subtotal)} TND", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Button(
                            onClick = {
                                // Use the passed professionalId
                                navController.navigate("order_confirmation_route/$professionalId")
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Proceed to Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        when (cartState) {
            is CartUiState.Error -> {
                val errorMessage = (cartState as CartUiState.Error).message
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error loading cart: $errorMessage", color = PrimaryRed)
                }
            }
            is CartUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryYellow)
                }
            }
            is CartUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
                    EmptyCartAnimation()
                }
            }
            is CartUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(cartItems, key = { index, item -> "${item.menuItemId}-$index" }) { index, item ->
                        CartItemCard(
                            item = item,
                            itemIndex = index,
                            onRemoveClick = onRemove,
                            onQuantityChange = onQuantityChange,
                            onDetailsClick = onDetailsClick
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (showPopup && selectedItem != null) {
            AlertDialog(
                onDismissRequest = { showPopup = false },
                containerColor = CardBackground,
                title = null, // Custom title inside content
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Popup Image
                        AsyncImage(
                            model = if (selectedItem!!.image.isNullOrEmpty()) null else BASE_URL + selectedItem!!.image,
                            contentDescription = selectedItem!!.name,
                            contentScale = ContentScale.Crop,
                            placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder),
                            error = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder),
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Item Name
                        Text(
                            text = selectedItem!!.name ?: "Item Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Details scrollable area if needed, or just column
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            DetailsList(
                                "Ingredients",
                                selectedItem!!.chosenIngredients.map { "${it.name} (${if (it.isDefault) "Default" else "Added"})" }
                            )
                            if (selectedItem!!.chosenOptions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailsList(
                                    "Options",
                                    selectedItem!!.chosenOptions.map { "${it.name} (+${"%.3f".format(it.price)} TND)" }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Quantity:", fontWeight = FontWeight.SemiBold, color = DarkText)
                                Text("${selectedItem!!.quantity}", fontWeight = FontWeight.Bold, color = DarkText)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Price:", fontWeight = FontWeight.SemiBold, color = DarkText)
                                Text(
                                    "${"%.3f".format(selectedItem!!.calculatedPrice)} TND", 
                                    fontWeight = FontWeight.Bold, 
                                    color = PrimaryYellow,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPopup = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
