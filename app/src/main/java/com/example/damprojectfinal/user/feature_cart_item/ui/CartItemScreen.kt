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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Star
import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.example.damprojectfinal.core.dto.cart.IngredientDto

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

// ---------------- Animated Bouncing Icon for Intensity ----------------
@Composable
fun AnimatedBouncingIcon(
    emoji: String,
    fontSize: TextUnit,
    delayMillis: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )
    
    Text(
        text = emoji,
        fontSize = fontSize,
        modifier = Modifier
            .graphicsLayer {
                translationY = bounceOffset
            }
    )
}

// ---------------- Helper: Get Emoji for Intensity Type ----------------
fun getEmojiForIntensityType(type: IntensityType?): String {
    return when (type) {
        IntensityType.COFFEE -> "☕"
        IntensityType.HARISSA -> "🌶️"
        IntensityType.SAUCE -> "🍯"
        IntensityType.SPICE -> "🌿"
        IntensityType.SUGAR -> "🍬"
        IntensityType.SALT -> "🧂"
        IntensityType.PEPPER -> "🫚"
        IntensityType.CHILI -> "🌶️"
        IntensityType.GARLIC -> "🧄"
        IntensityType.LEMON -> "🍋"
        else -> "⭐" // Default for CUSTOM or null
    }
}

// ---------------- Helper: Get Intensity Color ----------------
fun parseColor(hexColor: String?): Color {
    return try {
        hexColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Gray
    } catch (e: IllegalArgumentException) {
        Color.Gray // Fallback
    }
}

fun getIntensityColor(intensityType: IntensityType?, intensityColorHex: String?, intensityValue: Float): Color {
    // If backend provided a color, parse it and adjust by intensity
    if (intensityColorHex != null) {
        try {
            val baseColor = Color(android.graphics.Color.parseColor(intensityColorHex))
            // Adjust brightness based on intensity
            return Color(
                red = (baseColor.red * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                green = (baseColor.green * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                blue = (baseColor.blue * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            // Fall through to default colors
        }
    }
    
    // Default colors based on type
    return when (intensityType) {
        IntensityType.COFFEE -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.15f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.1f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.HARISSA, IntensityType.CHILI -> {
            Color(
                red = (0.6f + intensityValue * 0.4f).coerceIn(0f, 1f),
                green = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SAUCE -> {
            Color(
                red = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SPICE -> {
            Color(
                red = (0.8f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.5f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SUGAR -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.7f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.SALT -> {
            Color(
                red = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.PEPPER -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.GARLIC -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        IntensityType.LEMON -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.4f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        IntensityType.CUSTOM, null -> {
            Color(
                red = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
    }
}

// Custom Slider with Vertical Bar Thumb Design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val trackHeight = 6.dp
    val thumbWidth = 3.dp
    val thumbHeight = 20.dp
    val density = LocalDensity.current
    
    BoxWithConstraints(modifier = modifier.height(thumbHeight)) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        val thumbOffsetPx = value * (trackWidthPx - thumbWidthPx)
        val thumbOffset = with(density) { thumbOffsetPx.toDp() }
        val activeTrackWidth = with(density) { (thumbOffsetPx + thumbWidthPx / 2).toDp() }
        
        // Inactive track (background) - rounded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(inactiveColor)
                .align(Alignment.Center)
        )
        
        // Active track (filled portion) - rounded left side
        Box(
            modifier = Modifier
                .width(activeTrackWidth)
                .height(trackHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = if (value >= 1f) 8.dp else 0.dp,
                        bottomEnd = if (value >= 1f) 8.dp else 0.dp
                    )
                )
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Vertical bar thumb
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .height(thumbHeight)
                .offset(x = thumbOffset)
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Small dot at the end of inactive track
        if (value < 1f) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(inactiveColor)
                    .offset(x = maxWidth - 4.dp)
                    .align(Alignment.CenterEnd)
            )
        }
        
        // Invisible touch target for interaction (disabled for read-only)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxSize(),
            enabled = false,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(modifier = Modifier.size(0.dp))
            },
            track = {
                Box(modifier = Modifier.fillMaxSize())
            }
        )
    }
}

// ---------------- Ingredients Display with Intensity ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListWithIntensity(ingredients: List<IngredientDto>) {
    if (ingredients.isEmpty()) return
    
    Text(
        text = "Ingredients:",
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = DarkText,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
    
    ingredients.forEach { ingredient ->
            if (ingredient.intensityType != null) {
                // Convert Double to Float, or use default if null
                val intensityValue = ingredient.intensityValue?.toFloat() ?: 0.5f
                
                // Debug logging to see actual values
                android.util.Log.d("CartDetails", "Ingredient: ${ingredient.name}, intensityValue (Double): ${ingredient.intensityValue}, intensityValue (Float): $intensityValue")
                
                val primaryColor = getIntensityColor(ingredient.intensityType, ingredient.intensityColor, intensityValue)
                val baseEmoji = getEmojiForIntensityType(ingredient.intensityType)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Ingredient name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryYellow)
                            )
                            Text(
                                text = ingredient.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText
                            )
                            if (ingredient.isDefault) {
                                Text(
                                    text = "(Default)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Intensity slider (read-only display)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIntensitySlider(
                            value = intensityValue,
                            onValueChange = { }, // Read-only
                            activeColor = primaryColor,
                            inactiveColor = primaryColor.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        )
                        // Intensity icons on the right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            when {
                                intensityValue >= 0.8f -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 18.sp, delayMillis = 0)
                                    if (ingredient.intensityType == IntensityType.HARISSA || ingredient.intensityType == IntensityType.CHILI) {
                                        AnimatedBouncingIcon("🔥", fontSize = 18.sp, delayMillis = 100)
                                    }
                                }
                                intensityValue >= 0.3f -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 16.sp, delayMillis = 0)
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 16.sp, delayMillis = 150)
                                }
                                else -> {
                                    AnimatedBouncingIcon(baseEmoji, fontSize = 16.sp, delayMillis = 0)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Regular ingredient without intensity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "• ${ingredient.name}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (ingredient.isDefault) {
                    Text(
                        text = " (Default)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
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
    
    // Calculate savings if deal is applied
    val hasDeal = item.discountPercentage != null && item.discountPercentage > 0 && item.originalPrice != null
    val originalTotalPrice = if (hasDeal) (item.originalPrice!!.toFloat() * itemQuantity) else null
    val savings = if (hasDeal && originalTotalPrice != null) (originalTotalPrice - totalPrice) else null

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
        // Image with discount badge overlay
        Box {
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
            
            // Discount badge overlay
            if (hasDeal) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF5722),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "-${item.discountPercentage}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name ?: "Item", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            
            // Show price with deal info
            if (hasDeal && originalTotalPrice != null) {
                // Original price (strikethrough)
                Text(
                    "${"%.3f".format(originalTotalPrice)} TND",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                )
                
                // Current price (discounted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${"%.3f".format(totalPrice)} TND",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = PrimaryYellow
                    )
                    
                    if (savings != null && savings > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "(-${"%.3f".format(savings)} TND)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50) // Green for savings
                        )
                    }
                }
            } else {
                // Normal price (no deal)
                Text(
                    "${"%.3f".format(totalPrice)} TND",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PrimaryYellow
                )
            }
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
            // Show ingredients with intensity information
            if (item.chosenIngredients.isNotEmpty()) {
                IngredientsListWithIntensity(item.chosenIngredients)
            }
            if (item.chosenOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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
    
    // Calculate total savings from deals
    val totalSavings = cartItems.sumOf { item ->
        val hasDeal = item.discountPercentage != null && item.discountPercentage > 0 && item.originalPrice != null
        if (hasDeal) {
            val originalTotal = item.originalPrice!!.toDouble() * item.quantity
            val discountedTotal = item.calculatedPrice.toDouble() * item.quantity
            originalTotal - discountedTotal
        } else {
            0.0
        }
    }.toFloat()

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
                        
                        // Show savings if any deals are applied
                        if (totalSavings > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Savings",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "You saved:",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    "-${"%.3f".format(totalSavings)} TND",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
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
                            // Display ingredients with intensity information
                            IngredientsListWithIntensity(selectedItem!!.chosenIngredients)
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
