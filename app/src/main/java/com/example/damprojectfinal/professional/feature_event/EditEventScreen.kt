package com.example.foodyz_dam.ui.theme.screens.events


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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

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

    var nom by remember { mutableStateOf(event.nom) }
    var description by remember { mutableStateOf(event.description) }
    var dateDebut by remember { mutableStateOf(event.date_debut) }
    var dateFin by remember { mutableStateOf(event.date_fin) }
    var lieu by remember { mutableStateOf(event.lieu) }
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
                title = { Text("Modifier l'Événement", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandColors.TextPrimary)
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
            // Champs du formulaire (Nom, Description, Dates, Lieu, Catégorie, Statut, Image)
            // ... réutiliser le même code que ton composable existant

            // Bouton Modifier
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(BrandColors.Yellow, BrandColors.YellowPressed)))
                    .clickable(enabled = isValid) {
                        if (isValid && event._id != null) {
                            // ✅ Ici : conversion du texte sélectionné en enum
                            val statutEnum = when (statut.lowercase().trim()) {
                                "à venir" -> EventStatus.A_VENIR
                                "en cours" -> EventStatus.EN_COURS
                                "terminé" -> EventStatus.TERMINE
                                else -> EventStatus.A_VENIR
                            }

                            onUpdate(
                                event._id!!, // ✅ forcer le non-null
                                nom.trim(),
                                description.trim(),
                                dateDebut.trim(),
                                dateFin.trim(),
                                imageUri?.toString(),
                                lieu.trim(),
                                categorie.trim(),
                                statutEnum
                            )

                            Toast.makeText(context, "Événement modifié avec succès!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erreur: ID manquant", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Modifier l'événement", fontWeight = FontWeight.SemiBold, color = BrandColors.TextPrimary)
            }


            Spacer(Modifier.height(20.dp))
        }
    }
}
