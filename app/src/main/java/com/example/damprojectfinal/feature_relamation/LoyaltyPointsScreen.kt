package com.example.damprojectfinal.feature_relamation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyPointsScreen(
    loyaltyData: LoyaltyData?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Points Fidélité") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EA)
                )
            )
        }
    ) { padding ->

        if (loyaltyData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 💰 Carte Points Totaux
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6200EA), Color(0xFF9C27B0))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "${loyaltyData.loyaltyPoints}",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    "Points Fidélité",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // 📊 Statistiques
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Réclamations Valides",
                            value = "${loyaltyData.validReclamations}",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Score Fiabilité",
                            value = "${loyaltyData.reliabilityScore}%",
                            icon = Icons.Default.Shield,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 🎁 Récompenses Disponibles
                item {
                    Text(
                        "Récompenses Disponibles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(loyaltyData.availableRewards) { reward ->
                    RewardCard(reward)
                }

                // 📜 Historique
                item {
                    Text(
                        "Historique des Points",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(loyaltyData.history) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RewardCard(reward: Reward) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, null, tint = Color(0xFFFF9800))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(reward.name, fontWeight = FontWeight.Bold)
                    Text("${reward.pointsCost} points", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = { /* TODO: Échanger récompense */ },
                enabled = reward.available
            ) {
                Text(if (reward.available) "Échanger" else "Indisponible")
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: PointsTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.points > 0)
                Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.reason, fontWeight = FontWeight.Medium)
                Text(
                    transaction.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                "${if (transaction.points > 0) "+" else ""}${transaction.points}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.points > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

// Modèles de données
data class LoyaltyData(
    val loyaltyPoints: Int,
    val validReclamations: Int,
    val invalidReclamations: Int,
    val reliabilityScore: Int,
    val availableRewards: List<Reward>,
    val history: List<PointsTransaction>
)

data class Reward(
    val name: String,
    val pointsCost: Int,
    val available: Boolean
)

data class PointsTransaction(
    val points: Int,
    val reason: String,
    val date: String,
    val reclamationId: String
)