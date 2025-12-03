package com.example.damprojectfinal.professional.feature_deals

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.damprojectfinal.feature_deals.Deal
import com.example.damprojectfinal.feature_deals.DealsUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel

private const val TAG = "ProDealsManagementScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProDealsManagementScreen(
    viewModel: DealsViewModel,
    onAddDealClick: () -> Unit,
    onEditDealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🎨 ProDealsManagementScreen composé")
    Log.d(TAG, "  → ViewModel: ${viewModel.hashCode()}")

    val dealsState by viewModel.dealsState.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Log de l'état actuel
    LaunchedEffect(dealsState) {
        when (val state = dealsState) {
            is DealsUiState.Loading -> Log.d(TAG, "📊 État: Loading")
            is DealsUiState.Success -> Log.d(TAG, "📊 État: Success avec ${state.deals.size} deals")
            is DealsUiState.Error -> Log.d(TAG, "📊 État: Error - ${state.message}")
        }
    }

    // Afficher un Snackbar pour les résultats d'opération
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = {
                    Text("Gestion des Deals")
                    Log.d(TAG, "🎯 TopAppBar affichée")
                },
                actions = {
                    IconButton(onClick = {
                        Log.d(TAG, "🔄 Bouton rafraîchir cliqué")
                        viewModel.loadDeals()
                    }) {
                        Icon(Icons.Default.Refresh, "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d(TAG, "➕ FAB Ajouter cliqué")
                onAddDealClick()
            }) {
                Icon(Icons.Default.Add, "Ajouter un deal")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Log.d(TAG, "📦 Contenu principal affiché - padding: $padding")

        when (val state = dealsState) {
            is DealsUiState.Loading -> {
                Log.d(TAG, "⏳ Affichage: Loading")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chargement des deals...")
                    }
                }
            }

            is DealsUiState.Success -> {
                Log.d(TAG, "✅ Affichage: Success avec ${state.deals.size} deals")

                if (state.deals.isEmpty()) {
                    Log.d(TAG, "📭 Liste vide - Affichage écran vide")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun deal créé",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                Log.d(TAG, "➕ Bouton 'Créer un deal' cliqué")
                                onAddDealClick()
                            }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Créer un deal")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "📋 Affichage LazyColumn avec ${state.deals.size} deals")
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.deals) { deal ->
                            Log.d(TAG, "  → Affichage deal: ${deal._id} - ${deal.restaurantName}")
                            ProDealCard(
                                deal = deal,
                                onEditClick = {
                                    Log.d(TAG, "✏️ Édition deal: ${deal._id}")
                                    onEditDealClick(deal._id)
                                },
                                onDeleteClick = {
                                    Log.d(TAG, "🗑️ Demande suppression deal: ${deal._id}")
                                    showDeleteDialog = deal._id
                                }
                            )
                        }
                    }
                }
            }

            is DealsUiState.Error -> {
                Log.e(TAG, "❌ Affichage: Error - ${state.message}")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            Log.d(TAG, "🔄 Bouton 'Réessayer' cliqué")
                            viewModel.loadDeals()
                        }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
        }

        // Dialog de confirmation de suppression
        showDeleteDialog?.let { dealId ->
            Log.d(TAG, "⚠️ Dialog de suppression affiché pour: $dealId")
            AlertDialog(
                onDismissRequest = {
                    Log.d(TAG, "❌ Dialog annulé")
                    showDeleteDialog = null
                },
                title = { Text("Supprimer le deal") },
                text = { Text("Êtes-vous sûr de vouloir supprimer ce deal ?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.d(TAG, "✅ Confirmation suppression: $dealId")
                            viewModel.deleteDeal(dealId)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        Log.d(TAG, "❌ Annulation suppression")
                        showDeleteDialog = null
                    }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }

    Log.d(TAG, "🏁 ProDealsManagementScreen fin de composition")
}

@Composable
fun ProDealCard(
    deal: Deal,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deal.restaurantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = deal.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Expire: ${formatDate(deal.endDate)}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!deal.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("Inactif") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erreur formatage date: ${e.message}")
        dateString
    }
}