package com.example.damprojectfinal.professional.feature_event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.damprojectfinal.core.api.EventRetrofitClient
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.Event
import com.example.damprojectfinal.user.feature_event.EmptyState
import com.example.damprojectfinal.user.feature_event.EventCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreenRemote(
    onEventClick: (Event) -> Unit,
    onBackClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onEditEventClick: (Event) -> Unit = {},
    onDeleteEventClick: (String) -> Unit = {}
) {
    var events by remember { mutableStateOf(listOf<Event>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Chargement des événements
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                events = EventRetrofitClient.api.getEvents() // ✅ Changé ici
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Erreur: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Événements", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // ✅ Corrigé
                            contentDescription = "Back",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddEventClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Event",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = BrandColors.Cream100
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandColors.Yellow)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = BrandColors.Red
                            )
                            Text(
                                text = errorMessage ?: "Erreur inconnue",
                                color = BrandColors.TextSecondary
                            )
                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            events = EventRetrofitClient.api.getEvents()
                                        } catch (e: Exception) {
                                            errorMessage = "Erreur: ${e.localizedMessage}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandColors.Yellow
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Réessayer", color = BrandColors.TextPrimary)
                            }
                        }
                    }
                }
                events.isEmpty() -> {
                    EmptyState(onAddClick = onAddEventClick)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(events) { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event) },
                                onEditClick = { onEditEventClick(event) },
                                onDeleteClick = {
                                    event._id?.let { onDeleteEventClick(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}