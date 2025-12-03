package com.example.damprojectfinal.feature_deals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDealScreen(
    dealId: String? = null, // null = mode ajout, non-null = mode édition
    viewModel: DealsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var restaurantName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val operationResult by viewModel.operationResult.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isEditMode = dealId != null

    // Charger les données si mode édition
    LaunchedEffect(dealId) {
        if (dealId != null) {
            viewModel.loadDealById(dealId)
        }
    }

    // Observer les résultats pour afficher le dialog de succès
    LaunchedEffect(operationResult) {
        operationResult?.onSuccess {
            showSuccessDialog = true
        }
    }

    // Remplir les champs si données chargées
    val dealDetailState by viewModel.dealDetailState.collectAsState()
    LaunchedEffect(dealDetailState) {
        if (dealDetailState is DealDetailUiState.Success && isEditMode) {
            val deal = (dealDetailState as DealDetailUiState.Success).deal
            restaurantName = deal.restaurantName
            description = deal.description
            imageUrl = deal.image
            category = deal.category
            startDate = formatDateForInput(deal.startDate)
            endDate = formatDateForInput(deal.endDate)
            isActive = deal.isActive
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Modifier le deal" else "Nouveau deal")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom du restaurant
            OutlinedTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = { Text("Nom du restaurant *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // URL de l'image
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de l'image *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://exemple.com/image.jpg") }
            )

            // Catégorie
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Catégorie *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Street Food, Gastronomie, etc.") }
            )

            // Date de début
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Date de début *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") },
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, "Calendrier")
                }
            )

            // Date de fin
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Date de fin *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") },
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, "Calendrier")
                }
            )

            // Switch actif/inactif (uniquement en mode édition)
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Deal actif",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton de validation
            Button(
                onClick = {
                    if (validateForm(restaurantName, description, imageUrl, category, startDate, endDate)) {
                        val formattedStartDate = formatDateForApi(startDate)
                        val formattedEndDate = formatDateForApi(endDate)

                        if (isEditMode) {
                            viewModel.updateDeal(
                                dealId!!,
                                UpdateDealDto(
                                    restaurantName = restaurantName,
                                    description = description,
                                    image = imageUrl,
                                    category = category,
                                    startDate = formattedStartDate,
                                    endDate = formattedEndDate,
                                    isActive = isActive
                                )
                            )
                        } else {
                            viewModel.createDeal(
                                CreateDealDto(
                                    restaurantName = restaurantName,
                                    description = description,
                                    image = imageUrl,
                                    category = category,
                                    startDate = formattedStartDate,
                                    endDate = formattedEndDate
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = restaurantName.isNotBlank() &&
                        description.isNotBlank() &&
                        imageUrl.isNotBlank() &&
                        category.isNotBlank() &&
                        startDate.isNotBlank() &&
                        endDate.isNotBlank()
            ) {
                Text(if (isEditMode) "Mettre à jour" else "Créer le deal")
            }
        }
    }

    // Dialog de succès
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            title = { Text("Succès") },
            text = {
                Text(
                    if (isEditMode)
                        "Le deal a été mis à jour avec succès"
                    else
                        "Le deal a été créé avec succès"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

private fun validateForm(
    restaurantName: String,
    description: String,
    imageUrl: String,
    category: String,
    startDate: String,
    endDate: String
): Boolean {
    return restaurantName.isNotBlank() &&
            description.isNotBlank() &&
            imageUrl.isNotBlank() &&
            category.isNotBlank() &&
            startDate.isNotBlank() &&
            endDate.isNotBlank()
}

private fun formatDateForInput(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}

private fun formatDateForApi(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}