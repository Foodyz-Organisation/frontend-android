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
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodyz_dam.ui.theme.screens.events.BrandColors

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
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var lieu by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf("") }
    var statut by remember { mutableStateOf("À venir") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val isValid by remember {
        derivedStateOf {
            nom.isNotBlank() &&
                    description.isNotBlank() &&
                    dateDebut.isNotBlank() &&
                    dateFin.isNotBlank() &&
                    lieu.isNotBlank() &&
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

            // Dates
            FieldLabel("Date de début")
            StyledTextField(
                value = dateDebut,
                onValueChange = { dateDebut = it },
                placeholder = "2025-11-15T10:00:00",
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandColors.TextSecondary)
                }
            )

            FieldLabel("Date de fin")
            StyledTextField(
                value = dateFin,
                onValueChange = { dateFin = it },
                placeholder = "2025-11-15T18:00:00",
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandColors.TextSecondary)
                }
            )

            // Lieu
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

            // Bouton
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
                            onSubmit(
                                nom.trim(),
                                description.trim(),
                                dateDebut.trim(),
                                dateFin.trim(),
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
}

// ------------------ Composants réutilisables

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
