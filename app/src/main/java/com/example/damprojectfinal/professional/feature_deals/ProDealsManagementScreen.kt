package com.example.damprojectfinal.professional.feature_deals

import android.util.Log
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
import com.example.damprojectfinal.feature_deals.Deal
import com.example.damprojectfinal.feature_deals.DealsUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ProDealsManagementScreen"

// Couleurs de la marque
object BrandColors {
    val Yellow = Color(0xFFFFD700)
    val YellowPressed = Color(0xFFFFC700)
    val Orange = Color(0xFFFF9800)
    val Green = Color(0xFF4CAF50)
    val Red = Color(0xFFF44336)
    val TextPrimary = Color(0xFF2C2C2C)
    val TextSecondary = Color(0xFF757575)
    val Cream100 = Color(0xFFFFFBF5)
    val Cream200 = Color(0xFFFFE6CC)
}

// ------------------ Liste des Deals
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProDealsManagementScreen(
    viewModel: DealsViewModel,
    onAddDealClick: () -> Unit,
    onEditDealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🎨 ProDealsManagementScreen composé")

    val dealsState by viewModel.dealsState.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gestion des résultats d'opération
    LaunchedEffect(operationResult) {
        operationResult?.let { result ->
            result.onSuccess { message ->
                Log.d(TAG, "✅ Opération réussie: $message")
                snackbarHostState.showSnackbar(message)
            }.onFailure { error ->
                Log.e(TAG, "❌ Opération échouée: ${error.message}")
                snackbarHostState.showSnackbar(
                    error.message ?: "Une erreur est survenue"
                )
            }
            viewModel.clearOperationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Deals", color = BrandColors.TextPrimary) },
                actions = {
                    IconButton(onClick = {
                        Log.d(TAG, "🔄 Actualisation")
                        viewModel.loadDeals()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d(TAG, "➕ Ajout d'un deal")
                    onAddDealClick()
                },
                containerColor = BrandColors.Yellow,
                contentColor = BrandColors.TextPrimary
            ) {
                Icon(Icons.Default.Add, "Ajouter un deal")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BrandColors.Cream100
    ) { padding ->
        when (val state = dealsState) {
            is DealsUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }

            is DealsUiState.Success -> {
                if (state.deals.isEmpty()) {
                    EmptyDealsState(
                        onAddClick = onAddDealClick,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.deals) { deal ->
                            DealCard(
                                deal = deal,
                                onClick = { /* Navigation vers détails si nécessaire */ },
                                onEditClick = {
                                    Log.d(TAG, "✏️ Édition: ${deal._id}")
                                    onEditDealClick(deal._id)
                                },
                                onDeleteClick = {
                                    Log.d(TAG, "🗑️ Suppression: ${deal._id}")
                                    viewModel.deleteDeal(deal._id)
                                }
                            )
                        }
                    }
                }
            }

            is DealsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadDeals() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun DealCard(
    deal: Deal,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            // Image avec gradient overlay
            Box {
                if (deal.image != null) {
                    AsyncImage(
                        model = deal.image,
                        contentDescription = deal.restaurantName,
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
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Boutons d'action en overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton éditer
                    IconButton(
                        onClick = onEditClick,
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
                // Badge statut
                DealStatusBadge(isActive = deal.isActive)

                // Nom du restaurant
                Text(
                    text = deal.restaurantName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                Text(
                    text = deal.description,
                    fontSize = 14.sp,
                    color = BrandColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = BrandColors.Cream200
                )

                // Informations
                DealInfoRow(
                    icon = Icons.Default.Category,
                    text = deal.category
                )
                DealInfoRow(
                    icon = Icons.Default.CalendarToday,
                    text = "Expire: ${formatDate(deal.endDate)}"
                )
                DealInfoRow(
                    icon = Icons.Default.Percent,
                    text = "Réduction: %"
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
                    text = "Supprimer le deal",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer \"${deal.restaurantName}\" ? Cette action est irréversible.")
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
fun DealInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
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
fun DealStatusBadge(isActive: Boolean) {
    val (color, text) = if (isActive) {
        BrandColors.Green to "Actif"
    } else {
        BrandColors.Red to "Inactif"
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
fun EmptyDealsState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocalOffer,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.TextSecondary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun deal créé",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Créez votre premier deal",
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
            Text("Créer un deal", color = BrandColors.TextPrimary)
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = BrandColors.Yellow)
            Text(
                "Chargement des deals...",
                fontSize = 16.sp,
                color = BrandColors.TextSecondary
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = BrandColors.Red.copy(alpha = 0.7f)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = BrandColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Yellow
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Réessayer", color = BrandColors.TextPrimary)
            }
        }
    }
}

// ------------------ Helper pour formater les dates
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erreur formatage date: ${e.message}")
        dateString
    }
}