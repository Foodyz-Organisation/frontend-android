package com.example.damprojectfinal.feature_relamation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.ReclamationStatus
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Détails de la Réclamation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationDetailScreen(
    reclamation: Reclamation,
    onBackClick: () -> Unit = {},
    onStatusChange: ((ReclamationStatus) -> Unit)? = null
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails Réclamation", color = BrandColors.TextPrimary) },
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
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Status Card - CLICKABLE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .clickable(enabled = onStatusChange != null) {
                        showStatusDialog = true
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Statut",
                                fontWeight = FontWeight.SemiBold,
                                color = BrandColors.TextPrimary
                            )
                            if (onStatusChange != null) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier le statut",
                                    tint = BrandColors.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        StatusBadge(status = reclamation.status)
                    }

                    Divider(color = BrandColors.TextSecondary.copy(alpha = 0.1f))

                    fun formatDate(date: Date?): String {
                        return date?.let {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                        } ?: "Non disponible"
                    }

                    Text(text = "Date: ${formatDate(reclamation.date)}")
                    DetailRow("Commande", reclamation.orderNumber ?: "N/A")
                }
            }

            // Complaint Details
            SectionLabel("Type de réclamation")
            InfoCard(reclamation.complaintType ?: "Non spécifié")

            SectionLabel("Description")
            InfoCard(reclamation.description ?: "Aucune description")

            // Photos
            reclamation.photos?.takeIf { it.isNotEmpty() }?.let { photosList ->
                SectionLabel("Photos (${photosList.size})")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(photosList) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            // Response - ✅ FIX : Utiliser une variable locale
            reclamation.response?.let { responseText ->
                SectionLabel("Réponse")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandColors.Yellow.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandColors.Yellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Réponse de l'équipe",
                                fontWeight = FontWeight.SemiBold,
                                color = BrandColors.TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            responseText, // ✅ Variable locale au lieu de reclamation.response
                            color = BrandColors.TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Status Change Dialog
    if (showStatusDialog && onStatusChange != null) {
        StatusChangeDialog(
            currentStatus = reclamation.status,
            onDismiss = { showStatusDialog = false },
            onConfirm = { newStatus ->
                onStatusChange(newStatus)
                showStatusDialog = false
            }
        )
    }
}

@Composable
private fun StatusChangeDialog(
    currentStatus: ReclamationStatus,
    onDismiss: () -> Unit,
    onConfirm: (ReclamationStatus) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Changer le statut",
                fontWeight = FontWeight.Bold,
                color = BrandColors.TextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Sélectionnez le nouveau statut :",
                    color = BrandColors.TextSecondary,
                    fontSize = 14.sp
                )

                ReclamationStatus.values().forEach { status ->
                    StatusOption(
                        status = status,
                        isSelected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Yellow
                ),
                enabled = selectedStatus != currentStatus
            ) {
                Text("Confirmer", color = BrandColors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = BrandColors.TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun StatusOption(
    status: ReclamationStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                BrandColors.Yellow.copy(alpha = 0.2f)
            else
                Color.White
        ),
        border = if (isSelected)
            BorderStroke(2.dp, BrandColors.Yellow)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    getStatusLabel(status),
                    fontWeight = FontWeight.SemiBold,
                    color = BrandColors.TextPrimary,
                    fontSize = 16.sp
                )
                Text(
                    getStatusDescription(status),
                    color = BrandColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            StatusBadge(status = status)
        }
    }
}

// ✅ FIX : Ajouter IN_PROGRESS
private fun getStatusLabel(status: ReclamationStatus): String {
    return when (status) {
        ReclamationStatus.PENDING -> "En attente"
        ReclamationStatus.IN_PROGRESS -> "En cours"
        ReclamationStatus.RESOLVED -> "Résolue"
        ReclamationStatus.REJECTED -> "Rejetée"
    }
}

// ✅ FIX : Ajouter IN_PROGRESS
private fun getStatusDescription(status: ReclamationStatus): String {
    return when (status) {
        ReclamationStatus.PENDING -> "La réclamation est en cours de traitement"
        ReclamationStatus.IN_PROGRESS -> "La réclamation est actuellement traitée"
        ReclamationStatus.RESOLVED -> "La réclamation a été résolue"
        ReclamationStatus.REJECTED -> "La réclamation a été rejetée"
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.SemiBold,
        color = BrandColors.TextPrimary,
        fontSize = 16.sp
    )
}

@Composable
private fun InfoCard(content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            content,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = BrandColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = BrandColors.TextSecondary,
            fontSize = 14.sp
        )
        Text(
            value,
            color = BrandColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ✅ Composant StatusBadge manquant
@Composable
private fun StatusBadge(status: ReclamationStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        ReclamationStatus.PENDING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "En attente"
        )
        ReclamationStatus.IN_PROGRESS -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        ReclamationStatus.RESOLVED -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF388E3C),
            "Résolue"
        )
        ReclamationStatus.REJECTED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Rejetée"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}