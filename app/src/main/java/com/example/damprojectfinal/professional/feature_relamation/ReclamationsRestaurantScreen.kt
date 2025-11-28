package com.example.damprojectfinal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodyz_dam.ui.theme.screens.reclamation.Reclamation
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationStatus

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
                title = { Text("Réclamations Restaurant") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                reclamations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucune réclamation",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Nom du client
            Text(
                text = reclamation.nomClient ?: "Client inconnu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email
            Text(
                text = reclamation.emailClient ?: "Email non renseigné",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type de réclamation
            reclamation.complaintType?.let {
                Text(
                    text = "Type: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = reclamation.description ?: "Pas de description",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Statut - ✅ FIXED: Use reclamation.status instead of reclamation.statut
            StatusBadge(status = reclamation.status)
        }
    }
}

@Composable
fun StatusBadge(status: ReclamationStatus) {
    val (color, text) = when (status) {
        ReclamationStatus.PENDING -> Color(0xFFFFA726) to "En attente"
        ReclamationStatus.RESOLVED -> Color(0xFF66BB6A) to "Résolue"
        ReclamationStatus.IN_PROGRESS -> Color(0xFF42A5F5) to "En cours"
        ReclamationStatus.REJECTED -> Color(0xFFEF5350) to "Rejetée"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}