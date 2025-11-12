package com.example.foodyz_dam.ui.theme.screens.events

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Liste des événements
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onBackClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onEditClick: (Event) -> Unit = {},
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
                actions = {
                    IconButton(onClick = onAddEventClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Event",
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
    event: Event,
    onClick: () -> Unit,
    onEditClick: (Event) -> Unit = {},
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
                if (event.image != null) {
                    AsyncImage(
                        model = event.image,
                        contentDescription = event.nom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
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
                    // Edit button
                    IconButton(
                        onClick = {
                            onEditClick(event)
                            android.util.Log.d("EventCard", "Edit clicked for: ${event.nom}")
                        },


                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = BrandColors.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = {
                            showDeleteDialog = true
                            android.util.Log.d("EventCard", "Delete clicked for: ${event.nom} - ID: ${event._id}")
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
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
fun StatusBadge(status: EventStatus) {
    val (color, text) = when (status) {
        EventStatus.A_VENIR -> BrandColors.Orange to "À venir"
        EventStatus.EN_COURS -> BrandColors.Green to "En cours"
        EventStatus.TERMINE -> BrandColors.Red to "Terminé"
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
    event: Event,
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
                        EventStatus.A_VENIR -> "À venir"
                        EventStatus.EN_COURS -> "En cours"
                        EventStatus.TERMINE -> "Terminé"
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