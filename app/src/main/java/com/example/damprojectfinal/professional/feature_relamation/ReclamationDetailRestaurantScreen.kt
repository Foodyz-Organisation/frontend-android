package com.example.damprojectfinal.professional.feature_relamation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foodyz_dam.ui.theme.screens.reclamation.Reclamation
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationDetailRestaurantScreen(
    reclamation: Reclamation,
    onBackClick: () -> Unit,
    onRespond: (String) -> Unit
) {
    var responseText by remember { mutableStateOf("") }
    var showResponseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail de la réclamation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Statut
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (reclamation.status) {
                            ReclamationStatus.PENDING -> Color(0xFFFFF3E0)
                            ReclamationStatus.IN_PROGRESS -> Color(0xFFE3F2FD)
                            ReclamationStatus.RESOLVED -> Color(0xFFE8F5E9)
                            ReclamationStatus.REJECTED -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Statut",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(status = reclamation.status)
                    }
                }
            }

            // 2. Informations du client
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informations client",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("Nom", reclamation.nomClient ?: "Non renseigné")
                        InfoRow("Email", reclamation.emailClient ?: "Non renseigné")
                        InfoRow(
                            "Date",
                            reclamation.createdAt?.let { formatDate(it) } ?: "Non renseignée"
                        )
                    }
                }
            }

            // 3. Détails de la réclamation
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Détails de la réclamation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // ✅ Utilise orderNumber (votre champ existant)
                        reclamation.orderNumber?.let {
                            InfoRow("Commande concernée", it)
                        }

                        reclamation.complaintType?.let {
                            InfoRow("Type de réclamation", it)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reclamation.description ?: "Aucune description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 4. Photos uploadées
            if (!reclamation.photos.isNullOrEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Photos (${reclamation.photos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // ✅ Affichage des photos
                items(reclamation.photos) { photoUrl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Photo de réclamation",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 5. Réponse du restaurant (si existe)
            reclamation.responseMessage?.let { response ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Votre réponse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = response,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // ✅ Date de réponse
                            reclamation.respondedAt?.let { respondedDate ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Répondu le ${formatDate(respondedDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 6. Bouton de réponse
            if (reclamation.responseMessage == null &&
                reclamation.status != ReclamationStatus.RESOLVED) {
                item {
                    Button(
                        onClick = { showResponseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Répondre à la réclamation")
                    }
                }
            }
        }
    }

    // Dialog pour répondre
    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = { Text("Répondre à la réclamation") },
            text = {
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Votre réponse...") },
                    minLines = 4,
                    maxLines = 8
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (responseText.isNotBlank()) {
                            onRespond(responseText)
                            showResponseDialog = false
                            responseText = ""
                        }
                    },
                    enabled = responseText.isNotBlank()
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResponseDialog = false }) {
                    Text("Annuler")
                }
            }
        )
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

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

private fun formatDate(date: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(date)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }
}