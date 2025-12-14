package com.example.foodyz_dam.ui.theme.screens.events

import android.net.Uri
import android.widget.Toast
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodyz_dam.ui.theme.screens.events.BrandColors
=======
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.EventStatus
import java.text.SimpleDateFormat
import java.util.*
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt

// ------------------ Écran principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavHostController,
    categories: List<String> = listOf("cuisine française", "cuisine tunisienne", "cuisine japonaise"),
    statuts: List<String> = listOf("À venir", "En cours", "Terminé"),
    onSubmit: (
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

    var nom by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var lieu by remember { mutableStateOf("") }
=======
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt
    var categorie by remember { mutableStateOf("") }
    var statut by remember { mutableStateOf("À venir") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
=======
    // États pour la carte
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LocationData?>(null) }

    // États pour les dates et heures
    var startDate by remember { mutableStateOf<Long?>(null) }
    var startTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var endTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // États pour afficher les pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    // Fonction pour formater la date/heure complète en ISO 8601
    fun formatDateTime(dateMillis: Long?, time: Pair<Int, Int>?): String {
        if (dateMillis == null || time == null) return ""
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    // Fonction pour afficher la date/heure de manière lisible
    fun displayDateTime(dateMillis: Long?, time: Pair<Int, Int>?): String {
        if (dateMillis == null) return "Sélectionnez une date"
        val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        val dateStr = dateSdf.format(Date(dateMillis))

        return if (time != null) {
            "$dateStr à ${String.format("%02d:%02d", time.first, time.second)}"
        } else {
            "$dateStr - Sélectionnez l'heure"
        }
    }

    val isValid by remember {
        derivedStateOf {
            nom.isNotBlank() &&
                    description.isNotBlank() &&
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
                    dateDebut.isNotBlank() &&
                    dateFin.isNotBlank() &&
                    lieu.isNotBlank() &&
=======
                    startDate != null &&
                    startTime != null &&
                    endDate != null &&
                    endTime != null &&
                    selectedLocation != null &&
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt
                    categorie.isNotBlank()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un Événement", color = BrandColors.TextPrimary) },
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
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandColors.TextSecondary)
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

            // Date et heure de début
            FieldLabel("Date et heure de début")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date de début
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandColors.FieldFill
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (startDate != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(startDate!!))
                        } else {
                            "Date"
                        },
                        color = if (startDate != null) BrandColors.TextPrimary else BrandColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }

                // Heure de début
                OutlinedButton(
                    onClick = {
                        if (startDate != null) {
                            showStartTimePicker = true
                        } else {
                            Toast.makeText(context, "Sélectionnez d'abord une date", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandColors.FieldFill
                    )
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (startTime != null) {
                            String.format("%02d:%02d", startTime!!.first, startTime!!.second)
                        } else {
                            "Heure"
                        },
                        color = if (startTime != null) BrandColors.TextPrimary else BrandColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
            // Lieu
=======
            // Affichage complet de la date/heure de début
            if (startDate != null || startTime != null) {
                Text(
                    text = "Début: ${displayDateTime(startDate, startTime)}",
                    color = BrandColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Date et heure de fin
            FieldLabel("Date et heure de fin")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date de fin
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandColors.FieldFill
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (endDate != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(endDate!!))
                        } else {
                            "Date"
                        },
                        color = if (endDate != null) BrandColors.TextPrimary else BrandColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }

                // Heure de fin
                OutlinedButton(
                    onClick = {
                        if (endDate != null) {
                            showEndTimePicker = true
                        } else {
                            Toast.makeText(context, "Sélectionnez d'abord une date", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandColors.FieldFill
                    )
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (endTime != null) {
                            String.format("%02d:%02d", endTime!!.first, endTime!!.second)
                        } else {
                            "Heure"
                        },
                        color = if (endTime != null) BrandColors.TextPrimary else BrandColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Affichage complet de la date/heure de fin
            if (endDate != null || endTime != null) {
                Text(
                    text = "Fin: ${displayDateTime(endDate, endTime)}",
                    color = BrandColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Lieu avec carte OSM
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt
            FieldLabel("Lieu")
            StyledTextField(
                value = lieu,
                onValueChange = { lieu = it },
                placeholder = "Ex: Parc de la ville",
                leadingIcon = {
                    Icon(Icons.Default.Place, contentDescription = null, tint = BrandColors.TextSecondary)
                }
            )

            // Catégorie
            FieldLabel("Catégorie")
            ExposedDropdown(
                selected = categorie,
                placeholder = "Sélectionnez une catégorie",
                options = categories,
                onSelected = { categorie = it },
                leading = { Icon(Icons.Default.Add, contentDescription = null, tint = BrandColors.TextSecondary) }
            )

            // Statut
            FieldLabel("Statut")
            ExposedDropdown(
                selected = statut,
                placeholder = "Sélectionnez un statut",
                options = statuts,
                onSelected = { statut = it },
                leading = { Icon(Icons.Default.Info, contentDescription = null, tint = BrandColors.TextSecondary) }
            )

            // Image
            FieldLabel("Image (Optionnelle)")
            ImageSection(
                imageUri = imageUri,
                onAddImage = { pickImage.launch("image/*") },
                onRemoveImage = { imageUri = null }
            )

            // Bouton de soumission
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(BrandColors.Yellow, BrandColors.YellowPressed)
                        )
                    )
                    .clickable(enabled = isValid) {
                        if (isValid) {
                            val dateDebutFormatted = formatDateTime(startDate, startTime)
                            val dateFinFormatted = formatDateTime(endDate, endTime)

                            onSubmit(
                                nom.trim(),
                                description.trim(),
                                dateDebutFormatted,
                                dateFinFormatted,
                                imageUri?.toString(),
                                lieu.trim(),
                                categorie.trim(),
                                when (statut) {
                                    "à venir" -> EventStatus.A_VENIR
                                    "en cours" -> EventStatus.EN_COURS
                                    "terminé" -> EventStatus.TERMINE
                                    else -> EventStatus.A_VENIR
                                }
                            )
                            Toast.makeText(context, "Événement créé avec succès!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Créer l'événement",
                    fontWeight = FontWeight.SemiBold,
                    color = BrandColors.TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/CreateEventScreen.kt
}

// ------------------ Composants réutilisables

=======

    // Pickers de date et heure
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDate = datePickerState.selectedDateMillis
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK", color = BrandColors.Yellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Annuler", color = BrandColors.TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandColors.Yellow,
                    todayContentColor = BrandColors.Yellow,
                    todayDateBorderColor = BrandColors.Yellow
                )
            )
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime?.first ?: 10,
            initialMinute = startTime?.second ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startTime = Pair(timePickerState.hour, timePickerState.minute)
                        showStartTimePicker = false
                    }
                ) {
                    Text("OK", color = BrandColors.Yellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Annuler", color = BrandColors.TextSecondary)
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = BrandColors.FieldFill,
                    selectorColor = BrandColors.Yellow,
                    timeSelectorSelectedContainerColor = BrandColors.Yellow,
                    timeSelectorSelectedContentColor = Color.White
                )
            )
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate ?: startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDate = datePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK", color = BrandColors.Yellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Annuler", color = BrandColors.TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandColors.Yellow,
                    todayContentColor = BrandColors.Yellow,
                    todayDateBorderColor = BrandColors.Yellow
                )
            )
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime?.first ?: (startTime?.first?.plus(2) ?: 12),
            initialMinute = endTime?.second ?: (startTime?.second ?: 0),
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endTime = Pair(timePickerState.hour, timePickerState.minute)
                        showEndTimePicker = false
                    }
                ) {
                    Text("OK", color = BrandColors.Yellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Annuler", color = BrandColors.TextSecondary)
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = BrandColors.FieldFill,
                    selectorColor = BrandColors.Yellow,
                    timeSelectorSelectedContainerColor = BrandColors.Yellow,
                    timeSelectorSelectedContentColor = Color.White
                )
            )
        }
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
}

// Composant pour le dialog du TimePicker
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = dismissButton,
        confirmButton = confirmButton,
        text = { content() }
    )
}

// Dialog pour la carte OSM
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

// Composants réutilisables
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/CreateEventScreen.kt
@Composable
 fun FieldLabel(text: String) {
    Text(text, color = BrandColors.TextPrimary, fontWeight = FontWeight.SemiBold)
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
                .border(width = 2.dp, color = BrandColors.Dashed, shape = RoundedCornerShape(16.dp))
                .clickable { onAddImage() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = BrandColors.TextSecondary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("Ajouter une image", color = BrandColors.TextSecondary)
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize())
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
@Preview(showBackground = true)
@Composable
fun CreateEventScreenPreview() {
    CreateEventScreen(
        navController = rememberNavController(),
        onSubmit = { _, _, _, _, _, _, _, _ -> },
        onBack = {}
    )
}
