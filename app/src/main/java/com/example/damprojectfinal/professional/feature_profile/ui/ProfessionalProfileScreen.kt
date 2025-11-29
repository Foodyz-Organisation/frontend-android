package com.example.damprojectfinal.professional.feature_profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.R // Ensure this points to your R file

// --- Design Colors (Common) ---
val BrandYellow = Color(0xFFFFC107)
val PrimaryRed = Color(0xFFEF4444)
val DarkText = Color(0xFF1F2937)
val SecondaryText = Color(0xFF6B7280)
val BackgroundLight = Color(0xFFF9FAFB)
val CardBackground = Color(0xFFFFFFFF)
val CategoryBackgroundGray = Color(0xFFE5E7EB)

// --- Data Classes (Should be moved to a common/data module) ---
class EditableRestaurantProfileState(initial: RestaurantDetails) {
    var name by mutableStateOf(initial.name)
    var cuisine by mutableStateOf(initial.cuisine)
    var description by mutableStateOf(initial.description)
    var address by mutableStateOf(initial.address)
    var phone by mutableStateOf(initial.phone)
    var hours by mutableStateOf(initial.hours)
    var imageUrl by mutableStateOf(initial.imageUrl)
    var priceRange by mutableStateOf(initial.priceRange)
}

data class RestaurantDetails(
    val id: String,
    val name: String,
    val imageUrl: Int,
    val rating: Float,
    val reviewCount: Int,
    val priceRange: String,
    val cuisine: String,
    val deliveryTime: String,
    val takeawayTime: String,
    val dineInAvailable: Boolean,
    val address: String,
    val phone: String,
    val hours: String,
    val description: String
)

val mockChilis = RestaurantDetails(
    id = "1",
    name = "Chili's",
    imageUrl = R.drawable.chilis,
    rating = 4.7f,
    reviewCount = 1243,
    priceRange = "$$",
    cuisine = "Italian, Pizza, Pasta",
    deliveryTime = "30-45 min",
    takeawayTime = "Ready in 15 min",
    dineInAvailable = true,
    address = "123 Avenue Habib Bourguiba, Tunis",
    phone = "+216 71 123 456",
    hours = "10:00 AM - 11:00 PM",
    description = "Experience authentic Italian cuisine in the heart of Tunis..."
)

// -----------------------------------------------------------------------------
// MAIN COMPOSABLE: PROFESSIONAL PROFILE SCREEN (EDITABLE)
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    restaurantDetails: RestaurantDetails,
    onBackClick: () -> Unit,
    onSaveClick: (EditableRestaurantProfileState) -> Unit
) {
    val state = remember { EditableRestaurantProfileState(restaurantDetails) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Edit Business Profile", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        // --- Bottom Bar: Save Button ---
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = CardBackground) {
                Button(
                    onClick = { onSaveClick(state) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = DarkText)
                ) {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Image and Name Header (Editable)
            item { HeaderSection(details = restaurantDetails, isEditable = true, state = state) }

            // 2. Ratings and Basic Info (Read-only for Pro, used for context)
            item { InfoSection(restaurantDetails, isEditable = true) }

            // 3. Core Business Details (Editable)
            item {
                ManagementCard(title = "Core Business Details") {
                    EditableTextField(value = state.name, onValueChange = { state.name = it }, label = "Restaurant Name", leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Text)
                    EditableTextField(value = state.cuisine, onValueChange = { state.cuisine = it }, label = "Cuisines", leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Text)
                    EditableTextField(value = state.priceRange, onValueChange = { state.priceRange = it }, label = "Price Range", leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Text)
                }
            }

            // 4. Public Description (Editable)
            item {
                ManagementCard(title = "Public Description") {
                    EditableTextField(value = state.description, onValueChange = { state.description = it }, label = "Detailed Description (Appears on User Profile View)", leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = SecondaryText) }, minLines = 4, maxLines = 6, keyboardType = KeyboardType.Text)
                }
            }

            // 5. Contact and Location Management (Editable)
            item { ContactManagementSection(state = state, details = restaurantDetails, isEditable = true) }

            // 6. Service Toggle Management (Editable)
            item { ServiceToggleSection() }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPONENTS (REQUIRED BY THE MAIN SCREEN)
// -----------------------------------------------------------------------------

@Composable
fun ManagementCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DarkText)
            Divider(color = CategoryBackgroundGray)
            content()
        }
    }
}

@Composable
fun HeaderSection(details: RestaurantDetails, isEditable: Boolean, state: EditableRestaurantProfileState? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = details.imageUrl),
            contentDescription = "Restaurant Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = isEditable) { /* TODO: Trigger Image Picker */ }
        )
        if (isEditable) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { /* TODO: Trigger Image Picker */ }
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Banner", tint = CardBackground, modifier = Modifier.size(24.dp))
            }
            Text(
                text = "Tap to Change Banner Photo",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun InfoSection(details: RestaurantDetails, isEditable: Boolean) {
    ManagementCard(title = "Overview") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RatingChip(details.rating.toString(), Icons.Default.Star)
            RatingChip(details.reviewCount.toString() + " reviews", Icons.Default.ChatBubble)
            RatingChip(details.priceRange, Icons.Default.AttachMoney)
        }
        if (!isEditable) { // This section is for the Client view only (but included here for completeness if needed)
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Delivery: ${details.deliveryTime}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                Text("Takeaway: ${details.takeawayTime}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
        }
    }
}

@Composable
fun ContactManagementSection(state: EditableRestaurantProfileState?, details: RestaurantDetails, isEditable: Boolean) {
    ManagementCard(title = if (isEditable) "Contact and Operating Hours" else "Contact and Location") {
        if (isEditable && state != null) {
            EditableTextField(value = state.address, onValueChange = { state.address = it }, label = "Physical Address (Location Pin)", leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Text)
            EditableTextField(value = state.phone, onValueChange = { state.phone = it }, label = "Public Phone Number", leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Phone)
            EditableTextField(value = state.hours, onValueChange = { state.hours = it }, label = "Operating Hours", leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryText) }, keyboardType = KeyboardType.Text)
        } else {
            ReadOnlyInfoRow(Icons.Default.LocationOn, "Address", details.address)
            ReadOnlyInfoRow(Icons.Default.Call, "Phone Number", details.phone)
            ReadOnlyInfoRow(Icons.Default.Schedule, "Operating Hours", details.hours)
        }
    }
}

@Composable
fun ServiceToggleSection() {
    var deliveryEnabled by remember { mutableStateOf(true) }
    var takeawayEnabled by remember { mutableStateOf(true) }
    var dineInEnabled by remember { mutableStateOf(true) }

    ManagementCard(title = "Service Availability") {
        ServiceSwitchRow("Delivery", deliveryEnabled) { deliveryEnabled = it }
        ServiceSwitchRow("Takeaway", takeawayEnabled) { takeawayEnabled = it }
        ServiceSwitchRow("Dine-in", dineInEnabled) { dineInEnabled = it }
    }
}

@Composable
fun ServiceSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = DarkText, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandYellow,
                checkedTrackColor = BrandYellow.copy(alpha = 0.5f),
                uncheckedThumbColor = SecondaryText,
                uncheckedTrackColor = CategoryBackgroundGray
            )
        )
    }
}

@Composable
fun EditableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SecondaryText) },
        leadingIcon = leadingIcon,
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandYellow,
            unfocusedBorderColor = CategoryBackgroundGray,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground
        )
    )
}

@Composable
fun ReadOnlyInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkText)
        }
    }
}

@Composable
fun RatingChip(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfessionalProfilePreview() {
    ProfessionalProfileScreen(
        restaurantDetails = mockChilis,
        onBackClick = {},
        onSaveClick = {}
    )
}