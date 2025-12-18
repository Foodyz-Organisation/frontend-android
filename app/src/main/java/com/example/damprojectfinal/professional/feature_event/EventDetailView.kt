package com.example.damprojectfinal.professional.feature_event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // ✅ Import ajouté
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector // ✅ Import ajouté
import coil.compose.AsyncImage
import android.util.Log
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.Event
import com.example.damprojectfinal.feature_event.EventStatus
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                // Vérifier si c'est une image Base64
                val isBase64 = event.image.startsWith("data:image") || 
                               (event.image.length > 100 && !event.image.startsWith("http"))
                
                if (isBase64) {
                    // Convertir Base64 en ImageBitmap
                    val base64String = if (event.image.startsWith("data:image")) {
                        event.image.substringAfter(",")
                    } else {
                        event.image
                    }
                    
                    val imageBitmap = remember(base64String) {
                        try {
                            Log.d("EventDetailScreen", "🖼️ Décodage Base64 (${base64String.length} caractères)")
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            if (bitmap != null) {
                                Log.d("EventDetailScreen", "✅ Bitmap décodé: ${bitmap.width}x${bitmap.height}")
                            }
                            bitmap?.asImageBitmap()
                        } catch (e: Exception) {
                            Log.e("EventDetailScreen", "❌ Erreur décodage Base64: ${e.message}", e)
                            null
                        }
                    }
                    
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = event.nom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder si erreur de décodage
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
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                } else {
                    // C'est une URL, utiliser AsyncImage
                    var imageLoadError by remember { mutableStateOf(false) }
                    
                    if (imageLoadError) {
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
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = event.image,
                            contentDescription = event.nom,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(MaterialTheme.shapes.medium),
                            onError = { errorState ->
                                Log.e("EventDetailScreen", "❌ Erreur chargement image URL: ${errorState.result.throwable?.message}")
                                imageLoadError = true
                            },
                            onSuccess = {
                                Log.d("EventDetailScreen", "✅ Image URL chargée avec succès")
                            }
                        )
                    }
                }
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

                val context = LocalContext.current

                DetailInfoCard(
                    Icons.Filled.CalendarToday,
                    "Date de début",
                    formatEventDate(event.date_debut)
                )
                DetailInfoCard(
                    Icons.Filled.CalendarToday,
                    "Date de fin",
                    formatEventDate(event.date_fin)
                )

                // ✅ Carte Lieu cliquable pour itinéraire
                DetailInfoCard(
                    icon = Icons.Filled.LocationOn,
                    title = "Lieu (Cliquez pour l'itinéraire)",
                    value = event.lieu,
                    onClick = {
                        launchMapIntent(context, event.lieu)
                    }
                )

                DetailInfoCard(Icons.Filled.Star, "Catégorie", event.categorie)
                DetailInfoCard(
                    Icons.Filled.Info,
                    "Statut",
                    when(event.statut) {
                        EventStatus.A_VENIR -> "À venir"
                        EventStatus.EN_COURS -> "En cours"
                        EventStatus.TERMINE -> "Terminé"
                        else -> "Inconnu"
                    }
                )
            }
        }
    }
}

// Format ISO 8601 (avec ou sans 'Z') → "14/12/2025 • 20:03"
private fun formatEventDate(raw: String): String {
    return try {
        val formatterOut = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")

        val formatted = when {
            raw.endsWith("Z", ignoreCase = true) -> {
                val odt = OffsetDateTime.parse(raw)
                odt.format(formatterOut)
            }
            else -> {
                // Ex: 2025-12-23T12:44:00
                val ldt = LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ldt.format(formatterOut)
            }
        }
        formatted
    } catch (e: Exception) {
        raw // fallback si le format ne correspond pas
    }
}

// 🆕 Fonction pour lancer Google Maps
private fun launchMapIntent(context: android.content.Context, location: String) {
    try {
        val encodedLocation = java.net.URLEncoder.encode(location, "UTF-8")
        val uri = android.net.Uri.parse("geo:0,0?q=$encodedLocation")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback navigateur si Maps n'est pas installé
            val browserIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedLocation")
            )
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Impossible d'ouvrir la carte", android.widget.Toast.LENGTH_SHORT).show()
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
fun DetailInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null // 🆕 Paramètre optionnel pour le click
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = BrandColors.Cream100,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier) // ✅ Correction ici
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
                    color = if (onClick != null) Color.Blue else BrandColors.TextPrimary // 🔵 Bleu si cliquable
                )
            }

            // 🆕 Indicateur visuel si cliquable
            if (onClick != null) {
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.ArrowForward, // Ou autre icône pertinente
                    contentDescription = "Ouvrir",
                    tint = BrandColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
