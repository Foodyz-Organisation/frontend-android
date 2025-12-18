package com.example.damprojectfinal.professional.feature_relamation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.ReclamationStatus

private val ScreenBackground = Color(0xFFF4F5F7)
private val CardBackground = Color(0xFFF9FAFB)
private val TitleColor = Color(0xFF111827)
private val SubtitleColor = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationListRestaurantScreen(
    reclamations: List<Reclamation>,
    isLoading: Boolean = false,
    onReclamationClick: (Reclamation) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Réclamations Restaurant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TitleColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = TitleColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = TitleColor)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Chargement des réclamations...",
                            color = SubtitleColor
                        )
                    }
                }

                reclamations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucune réclamation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TitleColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Vous n'avez reçu aucune réclamation pour le moment.",
                            color = SubtitleColor,
                            fontSize = 13.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reclamations) { rec ->
                            ReclamationCard(
                                reclamation = rec,
                                onClick = { onReclamationClick(rec) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReclamationCard(
    reclamation: Reclamation,
    onClick: () -> Unit
) {
    val (statusColor, _) = statusColorsAndText(reclamation.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: avatar + client infos + statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (reclamation.nomClient?.take(1) ?: "?").uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = TitleColor
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = reclamation.nomClient ?: "Client inconnu",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TitleColor
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = SubtitleColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = reclamation.emailClient ?: "Email non renseigné",
                                fontSize = 12.sp,
                                color = SubtitleColor
                            )
                        }
                    }
                }

                StatusBadge(status = reclamation.status)
            }

            // Type + item name (si dispo)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                reclamation.complaintType?.let {
                    Text(
                        text = "Type: $it",
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Nom de l'item concerné (burger, pizza, etc.)
                reclamation.itemName?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SubtitleColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = SubtitleColor
                        )
                    }
                }
            }

            // Description
            Text(
                text = reclamation.description ?: "Pas de description",
                style = MaterialTheme.typography.bodyMedium,
                color = TitleColor,
                maxLines = 2
            )
        }
    }
}

@Composable
fun StatusBadge(status: ReclamationStatus) {
    val (color, text) = statusColorsAndText(status)

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

private fun statusColorsAndText(status: ReclamationStatus): Pair<Color, String> =
    when (status) {
        ReclamationStatus.PENDING -> Color(0xFFF59E0B) to "En attente"
        ReclamationStatus.RESOLVED -> Color(0xFF22C55E) to "Résolue"
        ReclamationStatus.IN_PROGRESS -> Color(0xFF3B82F6) to "En cours"
        ReclamationStatus.REJECTED -> Color(0xFFEF4444) to "Rejetée"
    }