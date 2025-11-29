package com.example.damprojectfinal.user.feature_menu.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.damprojectfinal.R
import com.example.damprojectfinal.ui.theme.* // Import global colors

// --- Data Models for Menu and Customization ---

data class MenuItem(
    val id: String,
    val name: String,
    val priceDT: Float,
    val imageUrl: Int, // Drawable resource ID
    val defaultIngredients: List<String> = emptyList() // Ingredients for customization
)

private val mockMenuItems = listOf(
    MenuItem("1", "Cheese Burger", 38.0f, R.drawable.burger, listOf("Tomatoes", "Lettuce", "Onions", "Cheese")),
    MenuItem("2", "Pasta", 25.0f, R.drawable.pasta, listOf("Basil", "Parmesan", "Meat Sauce")),
    MenuItem("3", "Pizza", 40.0f, R.drawable.pizza, listOf("Pepperoni", "Mozzarella", "Tomato Sauce")),
)

// --- Data Model for the Cart Item ---
data class OrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val finalPrice: Float
)

// -----------------------------------------------------------------------------
// MAIN COMPOSABLE: Restaurant Menu Screen
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(
    restaurantId: String,
    onBackClick: () -> Unit,
    onViewCartClick: () -> Unit,
    onAddToCartClick: (MenuItem, Float, Int) -> Unit
) {
    var selectedItemForCustomization by remember { mutableStateOf<MenuItem?>(null) }
    var totalOrderPrice by remember { mutableStateOf(0.0f) }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                restaurantName = "Chili's",
                onBackClick = onBackClick,
                onCartClick = onViewCartClick
            )
        },
        bottomBar = {
            MenuBottomBar(
                totalOrderPrice = totalOrderPrice,
                onCheckoutClick = onViewCartClick
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackgroundLight
    ) { paddingValues ->

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
            }
            items(mockMenuItems) { item ->
                MenuItemCard(
                    item = item,
                    onAddClick = { selectedItemForCustomization = item }
                )
            }
        }
    }

    selectedItemForCustomization?.let { item ->
        ItemCustomizationOverlay(
            item = item,
            onDismiss = { selectedItemForCustomization = null },
            onConfirmAddToCart = { confirmedItem, finalPrice, quantity ->
                onAddToCartClick(confirmedItem, finalPrice, quantity)
                totalOrderPrice += finalPrice * quantity
                selectedItemForCustomization = null
            }
        )
    }
}

// -----------------------------------------------------------------------------
// ITEM CUSTOMIZATION OVERLAY (The Pop-up Screen)
// -----------------------------------------------------------------------------

@Composable
fun ItemCustomizationOverlay(
    item: MenuItem,
    onDismiss: () -> Unit,
    onConfirmAddToCart: (MenuItem, Float, Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var ingredientsToRemove by remember { mutableStateOf(setOf<String>()) }

    val finalTotal = remember(item.priceDT, quantity, ingredientsToRemove) {
        item.priceDT * quantity
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundLight)
        ) {
            // ⭐ ERROR FIXED: CustomizationHeader is now defined below.
            CustomizationHeader(item = item, onDismiss = onDismiss)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))

                    // ⭐ ERROR FIXED: QuantitySelector is now defined below.
                    QuantitySelector(
                        quantity = quantity,
                        onQuantityChange = { quantity = it }
                    )
                    Spacer(Modifier.height(24.dp))

                    // ⭐ ERROR FIXED: IngredientsCustomizer is now defined below.
                    IngredientsCustomizer(
                        ingredients = item.defaultIngredients,
                        ingredientsToRemove = ingredientsToRemove,
                        // ⭐ ERROR FIXED: Lambda parameter 'ingredient' is implicitly typed by the function signature
                        onToggleIngredient = { ingredient ->
                            ingredientsToRemove = if (ingredientsToRemove.contains(ingredient)) {
                                ingredientsToRemove - ingredient
                            } else {
                                ingredientsToRemove + ingredient
                            }
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ⭐ ERROR FIXED: CustomizationFooter is now defined below.
            CustomizationFooter(
                total = finalTotal,
                onAddToCart = { onConfirmAddToCart(item, item.priceDT, quantity) }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// ⭐ CUSTOMIZATION HELPER COMPONENTS (THESE WERE MISSING) ⭐
// -----------------------------------------------------------------------------

@Composable
fun CustomizationHeader(item: MenuItem, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Image(
            painter = painterResource(id = item.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        // Item Name and Price overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(item.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${item.priceDT} DT", color = Color.White, fontSize = 18.sp)
        }
        // Close Button (Top Right)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.White)
                .size(32.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = AppDarkText)
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppDarkText)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Minus Button
            Button(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                colors = ButtonDefaults.buttonColors(containerColor = AppCardBackground, contentColor = AppDarkText),
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
            }
            Spacer(Modifier.width(16.dp))
            Text(quantity.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppDarkText)
            Spacer(Modifier.width(16.dp))
            // Plus Button
            FloatingActionButton(
                onClick = { onQuantityChange(quantity + 1) },
                containerColor = AppCartButtonYellow,
                contentColor = AppDarkText,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
            }
        }
    }
}

@Composable
fun IngredientsCustomizer(
    ingredients: List<String>,
    ingredientsToRemove: Set<String>,
    onToggleIngredient: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Customize Ingredients",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppDarkText
        )
        Text(
            "Remove any ingredients you don't like",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ingredients.forEach { ingredient ->
            val isSelected = !ingredientsToRemove.contains(ingredient)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppCardBackground)
                    .clickable { onToggleIngredient(ingredient) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onToggleIngredient(ingredient) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppPrimaryRed,
                        unselectedColor = Color.LightGray
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(ingredient, color = AppDarkText, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun CustomizationFooter(total: Float, onAddToCart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBackground)
            .padding(16.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppDarkText)
            Text(
                String.format("%.2f DT", total),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppCartButtonYellow
            )
        }

        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AppCartButtonYellow)
        ) {
            Text(
                text = "Add to Cart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppDarkText
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppDarkText)
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLES FOR MENU LIST
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopAppBar(restaurantName: String, onBackClick: () -> Unit, onCartClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppDarkText)
                }
                Text(
                    text = "Back to search",
                    color = AppDarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }

            IconButton(onClick = onCartClick) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "View Cart", tint = AppDarkText)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = restaurantName.first().toString(),
                    color = AppDarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = restaurantName,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppDarkText
            )
        }
        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppCardBackground)
            .clickable { /* Show customization overlay */ onAddClick() }
            .height(120.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = item.imageUrl),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .width(120.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppDarkText
            )
            Text(
                text = "${item.priceDT} DT",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = AppDarkText
            )
        }
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(48.dp),
            containerColor = AppCartButtonYellow,
            contentColor = AppDarkText
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add to Cart")
        }
    }
}

@Composable
fun MenuBottomBar(totalOrderPrice: Float, onCheckoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Price:",
                fontSize = 18.sp,
                color = AppDarkText
            )
            Text(
                text = String.format("%.2f DT", totalOrderPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9333EA)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onCheckoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryRed)
        ) {
            Text(
                text = "View Cart & Confirm Order",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW
// -----------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun RestaurantMenuPreview() {
    RestaurantMenuScreen(
        restaurantId = "1",
        onBackClick = {},
        onViewCartClick = {},
        onAddToCartClick = { _, _, _ -> }
    )
}
