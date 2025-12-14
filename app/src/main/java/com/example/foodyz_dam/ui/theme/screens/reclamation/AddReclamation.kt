package com.example.foodyz_dam.ui.screens.reclamation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
//import com.example.foodyz_dam.ui.theme.screens.reclamation.BrandColors

// ------------------ Brand palette
private object BrandColors {
    val Cream100 = Color(0xFFFFFEF7)
    val Cream200 = Color(0xFFFFFAEB)
    val FieldFill = Color(0xFFFFFFFF)
    val FieldStroke = Color(0xFFE8E6E0)
    val TextPrimary = Color(0xFF2C2F36)
    val TextSecondary = Color(0xFF6C7382)
    val Yellow = Color(0xFFFFD54F)
    val YellowPressed = Color(0xFFF4BE25)
    val Dashed = Color(0xFFD7D3CB)
}

// ------------------ Écran principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationTemplateScreen(
    restaurantNames: List<String>, // Liste des noms de clients
    complaintTypes: List<String>,
    commandeconcernees: List<String>,
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/reclamation/AddReclamation.kt
    onSubmit: (nomClient: String, commandeConcernee: String, complaintType: String, description: String, photos: List<Uri>) -> Unit = { _, _, _, _, _ -> }
=======
    onSubmit: (commandeConcernee: String, complaintType: String, description: String, photos: List<Uri>) -> Unit,
    initialOrderId: String? = null
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/user/feature_relamation/AddReclamation.kt
) {
    val context = LocalContext.current

    var nomClient by remember { mutableStateOf("") }
    var complaintType by remember { mutableStateOf("") }
    var commandeconcernee by remember { mutableStateOf(initialOrderId ?: "") }
    var description by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            photos = (photos + uris).distinct().take(4)
        }
    }

    val isValid by remember {
        derivedStateOf {
            nomClient.isNotBlank() &&
                    complaintType.isNotBlank() &&
                    description.isNotBlank() &&
                    commandeconcernee.isNotBlank() &&
                    agree
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle Réclamation", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { /* action de retour */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
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

            // Nom du client
            FieldLabel("Nom du Client")
            ExposedDropdown(
                selected = nomClient,
                placeholder = "Sélectionner le nom du client",
                options = restaurantNames,
                onSelected = { nomClient = it },
                leading = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandColors.TextSecondary) }
            )

            // Commande concernée
            FieldLabel("Commande Concernée")
            ExposedDropdown(
                selected = commandeconcernee,
                placeholder = "Sélectionner la commande",
                options = commandeconcernees,
                onSelected = { commandeconcernee = it },
                leading = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BrandColors.TextSecondary) }
            )

            // Type de réclamation
            FieldLabel("Type de Réclamation")
            ExposedDropdown(
                selected = complaintType,
                placeholder = "Sélectionner le type",
                options = complaintTypes,
                onSelected = { complaintType = it },
                leading = { Icon(Icons.Default.Clear, contentDescription = null, tint = BrandColors.TextSecondary) }
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
                placeholder = { Text("Décrivez votre problème...", color = BrandColors.TextSecondary) },
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

            PhotosSection(
                photos = photos,
                onAddPhoto = { pickImages.launch("image/*") },
                onRemovePhoto = { uri -> photos = photos.filterNot { it == uri } }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = agree, onCheckedChange = { agree = it })
                Text(
                    "J'accepte les conditions générales",
                    color = BrandColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isValid) Brush.horizontalGradient(
                            colors = listOf(BrandColors.Yellow, BrandColors.YellowPressed)
                        ) else Brush.horizontalGradient(
                            colors = listOf(Color.Gray, Color.Gray)
                        )
                    )
                    .clickable(enabled = isValid) {
                        if (isValid) {
                            android.util.Log.d("ReclamationForm", "=== Submit Form ===")
                            android.util.Log.d("ReclamationForm", "Nom Client: '$nomClient'")
                            android.util.Log.d("ReclamationForm", "Commande: '$commandeconcernee'")
                            android.util.Log.d("ReclamationForm", "Type: '$complaintType'")
                            android.util.Log.d("ReclamationForm", "Description: '$description'")
                            android.util.Log.d("ReclamationForm", "Photos: ${photos.size}")

                            onSubmit(nomClient, commandeconcernee, complaintType, description.trim(), photos)
                            Toast.makeText(context, "Réclamation envoyée", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Soumettre la Réclamation",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isValid) BrandColors.TextPrimary else Color.White
                )
            }

            Text(
                "Vous recevrez une réponse sous 24 heures",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                color = BrandColors.TextSecondary
            )
        }
    }
}

// ------------------ Composants réutilisables

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = BrandColors.TextPrimary, fontWeight = FontWeight.SemiBold)
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
private fun AddTile(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 120.dp)
            .clip(shape)
            .border(width = 2.dp, color = BrandColors.Dashed, shape = shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = BrandColors.TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text("Ajouter", color = BrandColors.TextSecondary)
        }
    }
}

@Composable
fun PhotosSection(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    FieldLabel("Joindre des Photos (Optionnel)")
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AddTile(onClick = onAddPhoto)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(photos) { uri ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .padding(6.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onRemovePhoto(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }
            if (photos.size < 4) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AddTile(onClick = onAddPhoto)
                    }
                }
            }
        }
    }
}

// ------------------ Preview
@Composable
fun ReclamationTemplatePreview() {
    val clientNames = listOf("Jean Dupont", "Marie Martin", "Pierre Dubois")
    val types = listOf("Late delivery", "Missing item", "Quality issue", "Other")
    val commandes = listOf("Commande #12345", "Commande #12346", "Commande #12347")
    MaterialTheme {
        ReclamationTemplateScreen(clientNames, types, commandeconcernees = commandes)
    }
}