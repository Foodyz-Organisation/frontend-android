package com.example.damprojectfinal.feature_relamation


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                title = {
                    Text(
                        "Points de Fidélité",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Retour",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        if (loyaltyData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF59E0B))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6))
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 💰 Carte Points Totaux - Design Premium
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFF59E0B),
                                            Color(0xFFFBBF24),
                                            Color(0xFFFCD34D)
                                        )
                                    )
                                )
                                .padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Icône étoile avec cercle
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "${loyaltyData.loyaltyPoints}",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Text(
                                    "Points Fidélité",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // 📊 Statistiques - Design Moderne
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernStatCard(
                            title = "Réclamations\nValides",
                            value = "${loyaltyData.validReclamations}",
                            icon = Icons.Default.CheckCircle,
                            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A)),
                            modifier = Modifier.weight(1f)
                        )

                        ModernStatCard(
                            title = "Score\nFiabilité",
                            value = "${loyaltyData.reliabilityScore}%",
                            icon = Icons.Default.Shield,
                            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF42A5F5)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 🎁 Section Récompenses
                if (loyaltyData.availableRewards.isNotEmpty()) {
                    item {
                        Text(
                            "🎁 Récompenses Disponibles",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(loyaltyData.availableRewards) { reward ->
                        ModernRewardCard(reward)
                    }
                }

                // 📜 Historique
                if (loyaltyData.history.isNotEmpty()) {
                    item {
                        Text(
                            "📜 Historique des Points",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    items(loyaltyData.history) { transaction ->
                        ModernTransactionCard(transaction)
                    }
                } else {
                    item {
                        EmptyStateCard()
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        gradientColors.map { it.copy(alpha = 0.1f) }
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    null,
                    tint = gradientColors[0],
                    modifier = Modifier.size(36.dp)
                )

                Column {
                    Text(
                        value,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gradientColors[0]
                    )
                    Text(
                        title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ModernRewardCard(reward: Reward) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        reward.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        "${reward.pointsCost} points",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Button(
                onClick = { /* TODO */ },
                enabled = reward.available,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (reward.available) "Échanger" else "Indisponible")
            }
        }
    }
}

@Composable
fun ModernTransactionCard(transaction: PointsTransaction) {
    val isPositive = transaction.points > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPositive) Color(0xFF10B981).copy(alpha = 0.2f)
                            else Color(0xFFEF4444).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.reason,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        transaction.date,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Text(
                "${if (isPositive) "+" else ""}${transaction.points}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.History,
                null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Aucun historique",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            Text(
                "Vos transactions apparaîtront ici",
                fontSize = 14.sp,
                color = Color(0xFFD1D5DB)
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