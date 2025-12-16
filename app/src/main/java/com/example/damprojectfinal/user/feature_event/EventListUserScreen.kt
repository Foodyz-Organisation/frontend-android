package com.example.damprojectfinal.user.feature_event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.Event
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image

// ------------------ Liste des événements
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    onEventClick: (com.example.damprojectfinal.feature_event.Event) -> Unit,
    onBackClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onEditClick: (com.example.damprojectfinal.feature_event.Event) -> Unit = {},
    onDeleteClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Événements", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        containerColor = BrandColors.Cream100
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(
                onAddClick = onAddEventClick,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        onEditClick = { onEditClick(event) },
                        onDeleteClick = {
                            if (event._id != null) {
                                onDeleteClick(event._id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: com.example.damprojectfinal.feature_event.Event,
    onClick: () -> Unit,
    onEditClick: (com.example.damprojectfinal.feature_event.Event) -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image avec actions overlay
            Box {
                if (event.image != null && event.image.isNotBlank()) {
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
                                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                bitmap?.asImageBitmap()
                            } catch (e: Exception) {
                                android.util.Log.e("EventCard", "❌ Erreur décodage Base64: ${e.message}")
                                null
                            }
                        }
                        
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = event.nom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder si erreur de décodage
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(BrandColors.Yellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
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
                                    .height(180.dp)
                                    .background(BrandColors.Yellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            AsyncImage(
                                model = event.image,
                                contentDescription = event.nom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop,
                                onError = { errorState ->
                                    android.util.Log.e("EventCard", "❌ Erreur chargement image: ${errorState.result.throwable?.message}")
                                    imageLoadError = true
                                },
                                onSuccess = {
                                    android.util.Log.d("EventCard", "✅ Image URL chargée avec succès pour: ${event.nom}")
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(BrandColors.Yellow, BrandColors.YellowPressed)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Action buttons overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton éditer
                    IconButton(
                        onClick = { onEditClick(event) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = BrandColors.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bouton supprimer
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = BrandColors.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Statut badge
                StatusBadge(status = event.statut)

                // Titre
                Text(
                    text = event.nom,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = BrandColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = BrandColors.Cream200
                )

                // Infos
                EventInfoRow(
                    icon = Icons.Default.DateRange,
                    text = event.date_debut
                )
                EventInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = event.lieu
                )
                EventInfoRow(
                    icon = Icons.Default.Star,
                    text = event.categorie
                )
            }
        }
    }

    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Supprimer l'événement",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer \"${event.nom}\" ? Cette action est irréversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BrandColors.Red
                    )
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EventInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = BrandColors.TextSecondary
        )
    }
}

@Composable
fun StatusBadge(status: com.example.damprojectfinal.feature_event.EventStatus) {
    val (color, text) = when (status) {
        com.example.damprojectfinal.feature_event.EventStatus.A_VENIR -> BrandColors.Orange to "À venir"
        com.example.damprojectfinal.feature_event.EventStatus.EN_COURS -> BrandColors.Green to "En cours"
        com.example.damprojectfinal.feature_event.EventStatus.TERMINE -> BrandColors.Red to "Terminé"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.TextSecondary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun événement",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Créez votre premier événement",
            fontSize = 14.sp,
            color = BrandColors.TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColors.Yellow
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Créer un événement", color = BrandColors.TextPrimary)
        }
    }
}

// ------------------ Détail d'un événement
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreenUser(
    event: com.example.damprojectfinal.feature_event.Event,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                actions = {
                    // Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = BrandColors.Yellow
                        )
                    }
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = BrandColors.Red
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
        ) {
            // Image d'en-tête
            if (event.image != null) {
                AsyncImage(
                    model = event.image,
                    contentDescription = event.nom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
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
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Badge statut
                StatusBadge(status = event.statut)

                // Titre
                Text(
                    text = event.nom,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary
                )

                // Description
                Text(
                    text = event.description,
                    fontSize = 16.sp,
                    color = BrandColors.TextSecondary,
                    lineHeight = 24.sp
                )

                Divider(color = BrandColors.Cream200)

                // Informations détaillées
                DetailInfoCard(
                    icon = Icons.Default.DateRange,
                    title = "Date de début",
                    value = event.date_debut
                )

                DetailInfoCard(
                    icon = Icons.Default.DateRange,
                    title = "Date de fin",
                    value = event.date_fin
                )

                DetailInfoCard(
                    icon = Icons.Default.LocationOn,
                    title = "Lieu",
                    value = event.lieu
                )

                DetailInfoCard(
                    icon = Icons.Default.Star,
                    title = "Catégorie",
                    value = event.categorie
                )

                DetailInfoCard(
                    icon = Icons.Default.Info,
                    title = "Statut",
                    value = when (event.statut) {
                        com.example.damprojectfinal.feature_event.EventStatus.A_VENIR -> "À venir"
                        com.example.damprojectfinal.feature_event.EventStatus.EN_COURS -> "En cours"
                        com.example.damprojectfinal.feature_event.EventStatus.TERMINE -> "Terminé"
                    }
                )
            }
        }
    }

    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Supprimer l'événement",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer \"${event.nom}\" ? Cette action est irréversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BrandColors.Red
                    )
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DetailInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BrandColors.Cream100,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    color = BrandColors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = BrandColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ------------------ Helpers
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
    return formatter.format(date)
}

private fun formatDateTimeFull(date: Date): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH)
    return formatter.format(date)
}