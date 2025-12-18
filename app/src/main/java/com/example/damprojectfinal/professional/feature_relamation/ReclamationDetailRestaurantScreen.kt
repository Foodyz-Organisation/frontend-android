package com.example.damprojectfinal.professional.feature_relamation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.ReclamationStatus
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ReclamationDetailRestaurant"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationDetailRestaurantScreen(
    reclamation: Reclamation,
    onBackClick: () -> Unit,
    onRespond: (String) -> Unit
) {
    Log.e(TAG, "═══════════════════════════════════════════════════════")
    Log.e(TAG, "🎬 ECRAN OUVERT - ReclamationDetailRestaurantScreen")
    Log.e(TAG, "═══════════════════════════════════════════════════════")

    var responseText by remember { mutableStateOf("") }
    var showResponseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.e(TAG, "📡 BASE_URL = ${BaseUrlProvider.BASE_URL}")
        Log.e(TAG, "📋 Reclamation ID = ${reclamation.id}")
        Log.e(TAG, "📸 Photos count = ${reclamation.photos?.size ?: 0}")

        reclamation.photos?.forEachIndexed { index, photo ->
            Log.e(TAG, "Photo $index original: $photo")
        }
    }

    val statusColor = when (reclamation.status) {
        ReclamationStatus.PENDING -> Color(0xFFFFA726)
        ReclamationStatus.RESOLVED -> Color(0xFF66BB6A)
        ReclamationStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        ReclamationStatus.REJECTED -> Color(0xFFEF5350)
    }

    val statusText = when (reclamation.status) {
        ReclamationStatus.PENDING -> "En attente"
        ReclamationStatus.RESOLVED -> "Résolue"
        ReclamationStatus.IN_PROGRESS -> "En cours"
        ReclamationStatus.REJECTED -> "Rejetée"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Détail de la réclamation", color = Color(0xFF1A1A1A)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statut
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
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
                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Informations client
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informations client",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Nom", reclamation.nomClient ?: "Non renseigné")
                        InfoRow("Email", reclamation.emailClient ?: "Non renseigné")
                        InfoRow("Date", reclamation.createdAt?.let { formatDate(it) } ?: "N/A")
                    }
                }
            }

            // Détails réclamation
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Détails de la réclamation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Afficher le nom de l'item si disponible, sinon l'ID de commande
                        InfoRow(
                            "Commande concernée",
                            reclamation.itemName ?: reclamation.orderNumber ?: "N/A"
                        )
                        InfoRow("Type de réclamation", reclamation.complaintType ?: "N/A")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reclamation.description ?: "Aucune description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            // Photos avec chargement manuel
            if (!reclamation.photos.isNullOrEmpty()) {
                item {
                    Text(
                        text = "Photos jointes (${reclamation.photos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(reclamation.photos) { index, photoPath ->
                    val fullImageUrl =
                        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
                            photoPath
                        } else {
                            // Utiliser BaseUrlProvider pour construire l'URL complète du fichier
                            BaseUrlProvider.getFullImageUrl(photoPath)
                        }

                    Log.e(TAG, "🖼️ Photo $index URL: $fullImageUrl")

                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Photo jointe $index",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Réponse du restaurant
            reclamation.responseMessage?.let { response ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
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
                                color = Color(0xFF1A1A1A)
                            )
                            reclamation.respondedAt?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Répondu le ${formatDate(it)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }

            // Bouton répondre
            if (reclamation.responseMessage == null) {
                item {
                    Button(
                        onClick = { showResponseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        )
                    ) {
                        Text(
                            "Répondre à la réclamation",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog de réponse à la réclamation
    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Répondre à la réclamation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Expliquez la situation au client de manière claire et professionnelle.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        placeholder = { Text("Votre réponse détaillée...") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFFF59E0B),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Text(
                        text = "${responseText.length}/500",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
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
                    enabled = responseText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF111827)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResponseDialog = false }) {
                    Text("Annuler", color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF757575)
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