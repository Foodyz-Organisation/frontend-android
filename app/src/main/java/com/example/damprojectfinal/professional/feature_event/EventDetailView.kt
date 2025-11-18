package com.example.damprojectfinal.professional.feature_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodyz_dam.ui.theme.screens.events.BrandColors
import com.example.foodyz_dam.ui.theme.screens.events.Event
import com.example.foodyz_dam.ui.theme.screens.events.EventStatus

// ✅ Pour éviter les avertissements Material3 expérimentaux
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'événement", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Image d'en-tête
            if (!event.image.isNullOrEmpty()) {
                AsyncImage(
                    model = event.image,
                    contentDescription = event.nom,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(BrandColors.Yellow, BrandColors.YellowPressed)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatusBadge(status = event.statut)

                Text(
                    text = event.nom,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary
                )

                Text(
                    text = event.description,
                    fontSize = 16.sp,
                    color = BrandColors.TextSecondary,
                    lineHeight = 24.sp
                )

                Divider(color = BrandColors.Cream200)

                DetailInfoCard(Icons.Filled.CalendarToday, "Date de début", event.date_debut)
                DetailInfoCard(Icons.Filled.CalendarToday, "Date de fin", event.date_fin)
                DetailInfoCard(Icons.Filled.LocationOn, "Lieu", event.lieu)
                DetailInfoCard(Icons.Filled.Star, "Catégorie", event.categorie)
                DetailInfoCard(
                    Icons.Filled.Info,
                    "Statut",
                    when(event.statut) {
                        EventStatus.A_VENIR -> "À venir"
                        EventStatus.EN_COURS -> "En cours"
                        EventStatus.TERMINE -> "Terminé"
                        // ✅ else ajouté pour éviter erreur si l'enum change
                        else -> "Inconnu"
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: EventStatus) {
    val color = when(status) {
        EventStatus.A_VENIR -> Color(0xFF4CAF50)  // vert
        EventStatus.EN_COURS -> Color(0xFFFFC107) // jaune
        EventStatus.TERMINE -> Color(0xFFF44336)  // rouge
        else -> Color.Gray
    }

    Text(
        text = when(status) {
            EventStatus.A_VENIR -> "À venir"
            EventStatus.EN_COURS -> "En cours"
            EventStatus.TERMINE -> "Terminé"
            else -> "Inconnu"
        },
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color = color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
fun DetailInfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = BrandColors.Cream100,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandColors.Yellow,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandColors.TextSecondary
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandColors.TextPrimary
                )
            }
        }
    }
}
