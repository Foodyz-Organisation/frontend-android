package com.example.damprojectfinal.professional.feature_event

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.navigation.NavHostController
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.Event
import com.example.damprojectfinal.feature_event.EventStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavHostController,
    event: Event,
    categories: List<String> = listOf("cuisine française", "cuisine tunisienne", "cuisine japonaise"),
    statuts: List<String> = listOf("À venir", "En cours", "Terminé"),
    onUpdate: (
        id: String,
        nom: String,
        description: String,
        dateDebut: String,
        dateFin: String,
        image: String?,
        lieu: String,
        categorie: String,
        statut: EventStatus
    ) -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nom by remember { mutableStateOf(event.nom) }
    var description by remember { mutableStateOf(event.description) }
    var dateDebut by remember { mutableStateOf(event.date_debut) }
    var dateFin by remember { mutableStateOf(event.date_fin) }
    var categorie by remember { mutableStateOf(event.categorie) }
    var statut by remember {
        mutableStateOf(
            when (event.statut) {
                EventStatus.A_VENIR -> "À venir"
                EventStatus.EN_COURS -> "En cours"
                EventStatus.TERMINE -> "Terminé"
            }
        )
    }
    var imageUri by remember { mutableStateOf<Uri?>(event.image?.let { Uri.parse(it) }) }

    // 🔥 NOUVEAU : État pour la carte
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LocationData?>(null) }

    // 📅 États pour les DatePickers (comme dans CreateEventScreen)
    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showTimePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }

    var selectedDateDebut by remember { mutableStateOf<Calendar?>(null) }
    var selectedTimeDebut by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedDateFin by remember { mutableStateOf<Calendar?>(null) }
    var selectedTimeFin by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // 🔥 Parsing du lieu existant
    LaunchedEffect(event.lieu) {
        if (event.lieu.isNotBlank()) {
            // Tenter d'extraire les coordonnées du format "latitude,longitude"
            val parts = event.lieu.split(",")
            if (parts.size == 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lon = parts[1].trim().toDoubleOrNull()
                if (lat != null && lon != null) {
                    selectedLocation =
                        LocationData(
                            lat,
                            lon,
                            ""
                        )
                    // Géocodage inversé pour obtenir le nom
                    scope.launch {
                        val name =
                            reverseGeocode(
                                context,
                                lat,
                                lon
                            )
                        selectedLocation =
                            LocationData(
                                lat,
                                lon,
                                name
                            )
                    }
                } else {
                    // Si ce n'est pas des coordonnées, c'est un nom de lieu
                    selectedLocation =
                        LocationData(
                            0.0,
                            0.0,
                            event.lieu
                        )
                }
            } else {
                // Lieu textuel simple
                selectedLocation =
                    LocationData(
                        0.0,
                        0.0,
                        event.lieu
                    )
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val isValid by remember {
        derivedStateOf {
            nom.isNotBlank() &&
                    description.isNotBlank() &&
                    dateDebut.isNotBlank() &&
                    dateFin.isNotBlank() &&
                    selectedLocation != null &&
                    categorie.isNotBlank()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier l'Événement", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Nom
            FieldLabel("Nom de l'événement")
            StyledTextField(
                value = nom,
                onValueChange = { nom = it },
                placeholder = "Ex: Festival Street Food Ramadan",
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            )

            // Description
            FieldLabel("Description")
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .heightIn(min = 140.dp),
                placeholder = { Text("Décrivez l'événement...", color = BrandColors.TextSecondary) },
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

            // Dates avec DatePicker + TimePicker (comme création)
            FieldLabel("Date de début")
            DatePickerField(
                value = dateDebut,
                placeholder = "Sélectionnez la date et l'heure de début",
                onDateClick = { showDatePickerDebut = true }
            )

            FieldLabel("Date de fin")
            DatePickerField(
                value = dateFin,
                placeholder = "Sélectionnez la date et l'heure de fin",
                onDateClick = { showDatePickerFin = true }
            )

            // 🔥 NOUVEAU : Lieu avec carte OSM - Identique à CreateEventScreen
            FieldLabel("Lieu")
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
                            text = selectedLocation?.name?.ifBlank { "Sélectionnez un lieu" }
                                ?: "Sélectionnez un lieu",
                            color = if (selectedLocation != null && selectedLocation?.name?.isNotBlank() == true)
                                BrandColors.TextPrimary else BrandColors.TextSecondary,
                            textAlign = TextAlign.Start
                        )
                        selectedLocation?.let {
                            if (it.latitude != 0.0 && it.longitude != 0.0) {
                                Text(
                                    text = String.format("%.4f, %.4f", it.latitude, it.longitude),
                                    color = BrandColors.TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            }

            // Catégorie
            FieldLabel("Catégorie")
            ExposedDropdown(
                selected = categorie,
                placeholder = "Sélectionnez une catégorie",
                options = categories,
                onSelected = { categorie = it },
                leading = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            )

            // Statut
            FieldLabel("Statut")
            ExposedDropdown(
                selected = statut,
                placeholder = "Sélectionnez un statut",
                options = statuts,
                onSelected = { statut = it },
                leading = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            )

            // Image
            FieldLabel("Image (Optionnelle)")
            ImageSection(
                imageUri = imageUri,
                onAddImage = { pickImage.launch("image/*") },
                onRemoveImage = { imageUri = null }
            )

            // Bouton Modifier
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
                        if (isValid && event._id != null) {
                            val statutEnum = when (statut.lowercase().trim()) {
                                "à venir" -> EventStatus.A_VENIR
                                "en cours" -> EventStatus.EN_COURS
                                "terminé" -> EventStatus.TERMINE
                                else -> EventStatus.A_VENIR
                            }

                            // 🔥 Gestion du lieu avec coordonnées ou texte
                            val lieuString = if (selectedLocation != null) {
                                if (selectedLocation!!.name.isNotBlank()) {
                                    selectedLocation!!.name
                                } else if (selectedLocation!!.latitude != 0.0 && selectedLocation!!.longitude != 0.0) {
                                    "${selectedLocation!!.latitude},${selectedLocation!!.longitude}"
                                } else {
                                    "Lieu non défini"
                                }
                            } else {
                                "Lieu non défini"
                            }

                            // ✅ Logs détaillés pour débogage
                            android.util.Log.d("EditEventScreen", "🔧 Début de la mise à jour")
                            android.util.Log.d("EditEventScreen", "📝 ID: ${event._id}")
                            android.util.Log.d("EditEventScreen", "📝 Nom: ${nom.trim()}")
                            android.util.Log.d("EditEventScreen", "📝 Description: ${description.trim()}")
                            android.util.Log.d("EditEventScreen", "📝 Lieu: $lieuString")
                            android.util.Log.d("EditEventScreen", "📝 Statut: $statutEnum")

                            onUpdate(
                                event._id!!,
                                nom.trim(),
                                description.trim(),
                                dateDebut.trim(),
                                dateFin.trim(),
                                imageUri?.toString(),
                                lieuString,
                                categorie.trim(),
                                statutEnum
                            )

                            android.util.Log.d("EditEventScreen", "✅ onUpdate() appelé")

                            // ❌ NE PAS afficher le toast ici - il sera affiché dans AppNavigation
                        } else {
                            Toast.makeText(
                                context,
                                "Erreur: ID manquant ou données invalides",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isValid) "Enregistrer les modifications" else "Remplissez tous les champs",
                    fontWeight = FontWeight.SemiBold,
                    color = BrandColors.TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // 🔥 NOUVEAU : Dialog pour la carte
    if (showMapPicker) {
        _root_ide_package_.com.example.damprojectfinal.professional.feature_event.MapPickerDialog(
            initialLocation = selectedLocation,
            onLocationSelected = { location ->
                selectedLocation = location
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }

    // 📅 DatePicker + TimePicker pour Date de début
    if (showDatePickerDebut) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDateDebut = date
                showDatePickerDebut = false
                showTimePickerDebut = true
            },
            onDismiss = { showDatePickerDebut = false }
        )
    }

    if (showTimePickerDebut) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                selectedTimeDebut = Pair(hour, minute)
                showTimePickerDebut = false

                selectedDateDebut?.let { calendar ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    dateDebut = dateFormat.format(calendar.time)
                } ?: run { dateDebut = "" }
            },
            onDismiss = {
                showTimePickerDebut = false
                selectedDateDebut = null
            }
        )
    }

    // 📅 DatePicker + TimePicker pour Date de fin
    if (showDatePickerFin) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDateFin = date
                showDatePickerFin = false
                showTimePickerFin = true
            },
            onDismiss = { showDatePickerFin = false }
        )
    }

    if (showTimePickerFin) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                selectedTimeFin = Pair(hour, minute)
                showTimePickerFin = false

                selectedDateFin?.let { calendar ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    dateFin = dateFormat.format(calendar.time)
                } ?: run { dateFin = "" }
            },
            onDismiss = {
                showTimePickerFin = false
                selectedDateFin = null
            }
        )
    }
}