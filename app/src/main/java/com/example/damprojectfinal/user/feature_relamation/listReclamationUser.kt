package com.example.damprojectfinal.user.feature_relamation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.feature_relamation.BrandColors
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.ReclamationStatus
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Liste des Réclamations
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationListUserScreen(
    reclamations: List<Reclamation>,
    onReclamationClick: (Reclamation) -> Unit,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Réclamations", color = BrandColors.TextPrimary) },
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
        containerColor = BrandColors.Background
    ) { padding ->
        if (reclamations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Aucune réclamation",
                        color = BrandColors.TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reclamations) { reclamation ->
                    ReclamationCard(
                        reclamation = reclamation,
                        onClick = { onReclamationClick(reclamation) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReclamationCard(
    reclamation: Reclamation,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Item name (burger, pizza, ...) + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = BrandColors.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        // Show the item name coming from the order (fallback to complaint type)
                        text = reclamation.itemName ?: reclamation.complaintType ?: "Réclamation",
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.TextPrimary,
                        fontSize = 16.sp
                    )
                }
                StatusBadgee(status = reclamation.status)
            }

            // Complaint Type
            Text(
                text = reclamation.complaintType ?: "Type inconnu",
                fontWeight = FontWeight.SemiBold,
                color = BrandColors.TextPrimary,
                fontSize = 14.sp
            )

            // Description
            Text(
                text = reclamation.description ?: "Pas de description",
                color = BrandColors.TextSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Date
            Text(
                text = reclamation.date?.let { dateFormat.format(it) } ?: "Date inconnue",
                color = BrandColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// ✅ FIX : Ajouter IN_PROGRESS
@Composable
fun StatusBadgee(status: ReclamationStatus?) {
    val (color, icon, text) = when (status) {
        ReclamationStatus.PENDING -> Triple(
            BrandColors.Orange,
            Icons.Default.Refresh,
            "En attente"
        )
        ReclamationStatus.IN_PROGRESS -> Triple(
            Color(0xFF2196F3), // Bleu
            Icons.Default.Refresh,
            "En cours"
        )
        ReclamationStatus.RESOLVED -> Triple(
            BrandColors.Green,
            Icons.Default.CheckCircle,
            "Résolue"
        )
        ReclamationStatus.REJECTED -> Triple(
            BrandColors.Red,
            Icons.Default.Clear,
            "Rejetée"
        )
        null -> Triple(
            BrandColors.Red,
            Icons.Default.Add,
            "Inconnu"
        )
    }

    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}