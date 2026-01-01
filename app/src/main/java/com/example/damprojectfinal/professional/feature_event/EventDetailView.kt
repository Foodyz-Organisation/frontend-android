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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector 
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import android.util.Log
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
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
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Détails de l'événement", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = BrandColors.TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrandColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Share action */ },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Partager",
                            tint = BrandColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
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
            // Image d'en-tête avec ombre et coins arrondis en bas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            ) {
                if (!event.image.isNullOrEmpty()) {
                    val isBase64 = event.image.startsWith("data:image") || 
                                   (event.image.length > 100 && !event.image.startsWith("http"))
                    
                    if (isBase64) {
                        val base64String = if (event.image.startsWith("data:image")) {
                            event.image.substringAfter(",")
                        } else {
                            event.image
                        }
                        
                        val imageBitmap = remember(base64String) {
                            try {
                                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                bitmap?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = event.nom,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        AsyncImage(
                            model = event.image,
                            contentDescription = event.nom,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                
                // Overlay gradient sur l'image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                                startY = 150f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = event.statut)
                    
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = BrandColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Pro",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.TextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = event.nom,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = BrandColors.TextPrimary
                )

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        color = Color(0xFF4B5563)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                val context = LocalContext.current

                DetailInfoCard(
                    icon = Icons.Filled.Event,
                    iconBg = Color(0xFFEFF6FF),
                    iconColor = Color(0xFF3B82F6),
                    title = "Date de début",
                    value = formatEventDate(event.date_debut)
                )
                
                DetailInfoCard(
                    icon = Icons.Filled.EventAvailable,
                    iconBg = Color(0xFFFFF7ED),
                    iconColor = Color(0xFFF97316),
                    title = "Date de fin",
                    value = formatEventDate(event.date_fin)
                )

                DetailInfoCard(
                    icon = Icons.Filled.LocationOn,
                    iconBg = Color(0xFFF0FDF4),
                    iconColor = Color(0xFF22C55E),
                    title = "Lieu (Cliquez pour l'itinéraire)",
                    value = event.lieu,
                    isClickable = true,
                    onClick = { launchMapIntent(context, event.lieu) }
                )

                DetailInfoCard(
                    icon = Icons.Filled.Category,
                    iconBg = Color(0xFFFAF5FF),
                    iconColor = Color(0xFFA855F7),
                    title = "Catégorie",
                    value = event.categorie
                )
                
                Spacer(modifier = Modifier.height(32.dp))
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
    val (color, backgroundColor) = when(status) {
        EventStatus.A_VENIR -> Color(0xFF10B981) to Color(0xFFECFDF5)  // Green
        EventStatus.EN_COURS -> Color(0xFFF59E0B) to Color(0xFFFFFBEB) // Yellow/Amber
        EventStatus.TERMINE -> Color(0xFFEF4444) to Color(0xFFFEF2F2)  // Red
        else -> Color.Gray to Color(0xFFF3F4F6)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = when(status) {
                EventStatus.A_VENIR -> "À venir"
                EventStatus.EN_COURS -> "En cours"
                EventStatus.TERMINE -> "Terminé"
                else -> "Inconnu"
            },
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun DetailInfoCard(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF9FAFB),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon container with circle background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isClickable) Color(0xFF2563EB) else Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isClickable) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Ouvrir",
                    tint = Color(0xFFD1D5DB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
