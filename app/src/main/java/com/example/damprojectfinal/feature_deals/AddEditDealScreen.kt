package com.example.damprojectfinal.feature_deals

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.deals.CreateDealDto
import com.example.damprojectfinal.core.dto.deals.UpdateDealDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.text.SimpleDateFormat
import java.util.*

// ============== Couleurs de la marque ==============
object BrandColors {
    val Yellow = Color(0xFFFFC107)
    val YellowPressed = Color(0xFFFFB300)
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF757575)
    val FieldFill = Color(0xFFF5F5F5)
    val Dashed = Color(0xFFE0E0E0)
}

// ============== Data Classes ==============
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)

// ============== Écran principal ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDealScreen(
    dealId: String? = null,
    viewModel: DealsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var restaurantName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf("") }

    // 🔥 Séparation Date & Heure pour Début et Fin
    var startDate by remember { mutableStateOf("") } // YYYY-MM-DD
    var startTime by remember { mutableStateOf("") } // HH:mm

    var endDate by remember { mutableStateOf("") }   // YYYY-MM-DD
    var endTime by remember { mutableStateOf("") }   // HH:mm

    var isActive by remember { mutableStateOf(true) }

    // 📅 États pour les pickers Material3
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // 🔥 État pour la carte
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LocationData?>(null) }

    val operationResult by viewModel.operationResult.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isEditMode = dealId != null

    // Image picker
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    // Catégories prédéfinies
    val categories = listOf(
        "Street Food",
        "Gastronomie",
        "Fast Food",
        "Cuisine Traditionnelle",
        "Cuisine Internationale",
        "Desserts & Pâtisserie"
    )

    // Charger les données si mode édition
    LaunchedEffect(dealId) {
        if (dealId != null) {
            viewModel.loadDealById(dealId)
        }
    }

    // Observer les résultats
    LaunchedEffect(operationResult) {
        operationResult?.onSuccess {
            showSuccessDialog = true
        }
    }

    // Remplir les champs en mode édition
    val dealDetailState by viewModel.dealDetailState.collectAsState()
    LaunchedEffect(dealDetailState) {
        if (dealDetailState is DealDetailUiState.Success && isEditMode) {
            val deal = (dealDetailState as DealDetailUiState.Success).deal
            restaurantName = deal.restaurantName
            description = deal.description
            category = deal.category

            // Parsing des dates ISO
            val (dStart, tStart) = parseIsoDate(deal.startDate)
            startDate = dStart
            startTime = tStart

            val (dEnd, tEnd) = parseIsoDate(deal.endDate)
            endDate = dEnd
            endTime = tEnd

            isActive = deal.isActive
            // Note: L'image URL ne peut pas être convertie en Uri facilement
        }
    }

    val isValid by remember {
        derivedStateOf {
            restaurantName.isNotBlank() &&
                    description.isNotBlank() &&
                    (imageUri != null || isEditMode) &&
                    category.isNotBlank() &&
                    startDate.isNotBlank() && startTime.isNotBlank() &&
                    endDate.isNotBlank() && endTime.isNotBlank() &&
                    selectedLocation != null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Modifier le deal" else "Nouveau deal",
                        color = BrandColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Retour",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Nom du restaurant
            FieldLabel("Nom du restaurant")
            StyledTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                placeholder = "Ex: Restaurant Le Gourmet",
                leadingIcon = {
                    Icon(Icons.Default.Store, contentDescription = null, tint = BrandColors.TextSecondary)
                }
            )

            // Description
            FieldLabel("Description du deal")
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .heightIn(min = 140.dp),
                placeholder = { Text("Décrivez votre offre spéciale...", color = BrandColors.TextSecondary) },
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = BrandColors.TextPrimary
                )
            )

            Text(
                "${description.length}/500",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = BrandColors.TextSecondary,
                fontSize = 12.sp
            )

            // Image
            FieldLabel("Image du deal")
            ImageSection(
                imageUri = imageUri,
                onAddImage = { pickImage.launch("image/*") },
                onRemoveImage = { imageUri = null }
            )

            // Catégorie
            FieldLabel("Catégorie")
            ExposedDropdown(
                selected = category,
                placeholder = "Sélectionnez une catégorie",
                options = categories,
                onSelected = { category = it },
                leading = { Icon(Icons.Default.Category, contentDescription = null, tint = BrandColors.TextSecondary) }
            )

            // Lieu avec carte OSM
            FieldLabel("Lieu du restaurant")
            OutlinedButton(
                onClick = { showMapPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BrandColors.FieldFill
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = selectedLocation?.name ?: "Sélectionnez un lieu",
                            color = if (selectedLocation != null) BrandColors.TextPrimary else BrandColors.TextSecondary,
                            textAlign = TextAlign.Start
                        )
                        selectedLocation?.let {
                            Text(
                                text = String.format("%.4f, %.4f", it.latitude, it.longitude),
                                color = BrandColors.TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            }

            // 📅 Date & Heure de DÉBUT
            FieldLabel("Date de début")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Date Picker
                ClickableTextField(
                    value = formatDateForDisplay(startDate),
                    placeholder = "JJ/MM/AAAA",
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f),
                    onClick = { showStartDatePicker = true }
                )
                // Time Picker
                ClickableTextField(
                    value = startTime,
                    placeholder = "HH:mm",
                    icon = Icons.Default.Schedule, // Icône Horloge
                    modifier = Modifier.weight(0.8f),
                    onClick = { showStartTimePicker = true }
                )
            }

            // 📅 Date & Heure de FIN
            FieldLabel("Date de fin")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Date Picker
                ClickableTextField(
                    value = formatDateForDisplay(endDate),
                    placeholder = "JJ/MM/AAAA",
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f),
                    onClick = { showEndDatePicker = true }
                )
                // Time Picker
                ClickableTextField(
                    value = endTime,
                    placeholder = "HH:mm",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(0.8f),
                    onClick = { showEndTimePicker = true }
                )
            }

            // Switch actif/inactif (uniquement en mode édition)
            if (isEditMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = BrandColors.FieldFill),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Deal actif",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandColors.TextPrimary
                            )
                            Text(
                                text = if (isActive) "Visible par les clients" else "Masqué",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandColors.TextSecondary
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandColors.Yellow,
                                checkedTrackColor = BrandColors.Yellow.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton de validation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isValid) {
                            Brush.horizontalGradient(
                                colors = listOf(BrandColors.Yellow, BrandColors.YellowPressed)
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    BrandColors.Yellow.copy(alpha = 0.6f),
                                    BrandColors.YellowPressed.copy(alpha = 0.6f)
                                )
                            )
                        }
                    )
                    .clickable(enabled = isValid) {
                        if (isValid) {
                            val formattedStart = combineDateAndTime(startDate, startTime)
                            val formattedEnd = combineDateAndTime(endDate, endTime)
                            val imageUrl = imageUri?.toString() ?: "https://via.placeholder.com/400"

                            if (isEditMode) {
                                viewModel.updateDeal(
                                    dealId!!,
                                    UpdateDealDto(
                                        restaurantName = restaurantName,
                                        description = description,
                                        image = imageUrl,
                                        category = category,
                                        startDate = formattedStart,
                                        endDate = formattedEnd,
                                        isActive = isActive
                                    )
                                )
                            } else {
                                viewModel.createDeal(
                                    CreateDealDto(
                                        restaurantName = restaurantName,
                                        description = description,
                                        image = imageUrl,
                                        category = category,
                                        startDate = formattedStart,
                                        endDate = formattedEnd
                                    )
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isValid) {
                        if (isEditMode) "Mettre à jour le deal" else "Créer le deal"
                    } else {
                        "Remplissez tous les champs"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = BrandColors.TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Dialog de succès
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearOperationResult()
                onBackClick()
            },
            title = { Text("Succès", color = BrandColors.TextPrimary) },
            text = {
                Text(
                    if (isEditMode)
                        "Le deal a été mis à jour avec succès"
                    else
                        "Le deal a été créé avec succès",
                    color = BrandColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearOperationResult()
                        onBackClick()
                    }
                ) {
                    Text("OK", color = BrandColors.Yellow)
                }
            },
            containerColor = Color.White
        )
    }

    // Dialog pour la carte
    if (showMapPicker) {
        MapPickerDialog(
            initialLocation = selectedLocation,
            onLocationSelected = { location ->
                selectedLocation = location
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }

    // 📅 Date Picker pour Date de début
    if (showStartDatePicker) {
        DealDatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
                showStartTimePicker = true // Automatically show time picker after date selection
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // ⏰ Time Picker pour Date de début
    if (showStartTimePicker) {
        DealTimePickerDialog(
            onTimeSelected = { time ->
                startTime = time
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // 📅 Date Picker pour Date de fin
    if (showEndDatePicker) {
        DealDatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
                showEndTimePicker = true // Automatically show time picker after date selection
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // ⏰ Time Picker pour Date de fin
    if (showEndTimePicker) {
        DealTimePickerDialog(
            onTimeSelected = { time ->
                endTime = time
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

// ============== Composants & Helpers réutilisables ==============

@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        color = BrandColors.TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        placeholder = { Text(placeholder, color = BrandColors.TextSecondary) },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = BrandColors.FieldFill,
            unfocusedContainerColor = BrandColors.FieldFill,
            disabledContainerColor = BrandColors.FieldFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = BrandColors.TextPrimary
        )
    )
}

// 🔥 Nouveau composant cliquable pour Date/Heure
@Composable
fun ClickableTextField(
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() } // Le Box intercepte le clic
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Read-only
            readOnly = true,
            enabled = false, // empêche le clavier mais garde le style
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = BrandColors.TextSecondary) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = BrandColors.TextSecondary) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BrandColors.FieldFill,
                unfocusedContainerColor = BrandColors.FieldFill,
                disabledContainerColor = BrandColors.FieldFill,
                disabledTextColor = BrandColors.TextPrimary,
                disabledPlaceholderColor = BrandColors.TextSecondary,
                disabledLeadingIconColor = BrandColors.TextSecondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdown(
    selected: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    leading: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (options.isNotEmpty()) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            placeholder = { Text(placeholder, color = BrandColors.TextSecondary) },
            leadingIcon = leading,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BrandColors.FieldFill,
                unfocusedContainerColor = BrandColors.FieldFill,
                disabledContainerColor = BrandColors.FieldFill,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = BrandColors.TextPrimary) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ImageSection(
    imageUri: Uri?,
    onAddImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    if (imageUri == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = BrandColors.Dashed,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onAddImage() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = BrandColors.TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Ajouter une image", color = BrandColors.TextSecondary)
                Text("JPG, PNG (max 5MB)", color = BrandColors.TextSecondary, fontSize = 12.sp)
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onRemoveImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// ============== Carte OSM ==============

@Composable
fun MapPickerDialog(
    initialLocation: LocationData?,
    onLocationSelected: (LocationData) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MapPickerScreen(
            initialLocation = initialLocation,
            onLocationSelected = onLocationSelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun MapPickerScreen(
    initialLocation: LocationData?,
    onLocationSelected: (LocationData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentLocation by remember {
        mutableStateOf(
            initialLocation ?: LocationData(
                latitude = 36.8065,
                longitude = 10.1815,
                name = "Tunis, Tunisie"
            )
        )
    }

    var locationName by remember { mutableStateOf(currentLocation.name) }
    var isLoadingAddress by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // La carte OSM
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(
                        GeoPoint(currentLocation.latitude, currentLocation.longitude)
                    )

                    addMapListener(object : org.osmdroid.events.MapListener {
                        override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                            val center = mapCenter as GeoPoint
                            currentLocation = LocationData(
                                latitude = center.latitude,
                                longitude = center.longitude
                            )

                            scope.launch {
                                kotlinx.coroutines.delay(500)
                                isLoadingAddress = true
                                locationName = reverseGeocode(ctx, center.latitude, center.longitude)
                                isLoadingAddress = false
                            }
                            return true
                        }

                        override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                            return true
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pin fixe au centre
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin",
            tint = Color.Red,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center)
                .offset(y = (-25).dp)
        )

        // Info en haut
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoadingAddress) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recherche de l'adresse...")
                    }
                } else {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(
                            "%.4f, %.4f",
                            currentLocation.latitude,
                            currentLocation.longitude
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Instruction
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "📍 Déplacez la carte pour positionner le restaurant",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Boutons en bas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler")
                }

                Button(
                    onClick = {
                        onLocationSelected(
                            currentLocation.copy(name = locationName)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.Yellow
                    )
                ) {
                    Text("Confirmer", color = BrandColors.TextPrimary)
                }
            }
        }
    }
}

suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val components = mutableListOf<String>()

            address.featureName?.let { components.add(it) }
            address.thoroughfare?.let {
                if (!components.contains(it)) components.add(it)
            }
            address.locality?.let { components.add(it) }
            address.countryName?.let { components.add(it) }

            return@withContext if (components.isNotEmpty()) {
                components.joinToString(", ")
            } else {
                "Lieu sélectionné"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext "Lieu sélectionné"
}

// ============== Dialogs Material3 pour Date & Heure ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().timeInMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = BrandColors.Yellow
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sélectionnez une date",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = BrandColors.TextPrimary
                )
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp,
                color = Color.White
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(formatter.format(calendar.time))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Yellow
                )
            ) {
                Text("Confirmer", color = BrandColors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = BrandColors.TextSecondary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealTimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = BrandColors.Yellow
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sélectionnez une heure",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = BrandColors.TextPrimary
                )
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp,
                color = Color.White
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(formattedTime)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Yellow
                )
            ) {
                Text("Confirmer", color = BrandColors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = BrandColors.TextSecondary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

// ============== Fonctions utilitaires Dates ==============

// Format YYYY-MM-DD to DD/MM/YYYY for display
private fun formatDateForDisplay(dateString: String): String {
    if (dateString.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString // Return original if parsing fails
    }
}

// Combine YYYY-MM-DD + HH:mm -> ISO
private fun combineDateAndTime(date: String, time: String): String {
    if (date.isBlank() || time.isBlank()) return ""
    return try {
        // Input: "yyyy-MM-dd" "HH:mm"
        // Output: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        // Comme on manipule des Strings simples, on peut concaténer pour créer l'ISO
        // Attention au fuseau horaire. Pour simplifier, on envoie telle quelle avec T et Z
        // ou on utilise un formatter pour être propre.

        // Option simple : "${date}T${time}:00.000Z" (Assume UTC)
        // Mais l'utilisateur saisit en LocalTime.

        val localFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val parsedDate = localFormat.parse("$date $time")

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC") // Store as UTC

        parsedDate?.let { isoFormat.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}

// Parse ISO -> Pair(Date, Time)
private fun parseIsoDate(isoString: String): Pair<String, String> {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = isoFormat.parse(isoString)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (date != null) {
            Pair(dateFormat.format(date), timeFormat.format(date))
        } else {
            Pair("", "")
        }
    } catch (e: Exception) {
        // Fallback si format incorrect
        Pair("", "")
    }
}