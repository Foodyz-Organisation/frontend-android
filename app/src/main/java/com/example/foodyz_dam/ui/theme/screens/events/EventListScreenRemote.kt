package com.example.foodyz_dam.ui.theme.screens.events

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
<<<<<<< Updated upstream:app/src/main/java/com/example/foodyz_dam/ui/theme/screens/events/EventListScreenRemote.kt
import kotlinx.coroutines.launch
=======
import com.example.damprojectfinal.feature_event.BrandColors
import com.example.damprojectfinal.feature_event.Event
import com.example.damprojectfinal.user.feature_event.EmptyState
import com.example.damprojectfinal.user.feature_event.EventCard
>>>>>>> Stashed changes:app/src/main/java/com/example/damprojectfinal/professional/feature_event/EventListScreenRemote.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreenRemote(
    events: List<Event>,
    isLoading: Boolean,
    error: String?,
    onEventClick: (Event) -> Unit,
    onBackClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onEditEventClick: (Event) -> Unit = {},
    onDeleteEventClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Événements", color = BrandColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                error != null -> {
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
                                text = error ?: "Erreur inconnue",
                                color = BrandColors.TextSecondary
                            )
                            Text(
                                "Veuillez réessayer",
                                color = BrandColors.TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
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
                            EventCardWithActions(
                                event = event,
                                onClick = { onEventClick(event) },
                                onEdit = { onEditEventClick(event) },
                                onDelete = {
                                    event._id?.let { eventId ->
                                        onDeleteEventClick(eventId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCardWithActions(
    event: Event,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        EventCard(
            event = event,
            onClick = onClick
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Modifier l'événement",
                    tint = BrandColors.Yellow,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer l'événement",
                    tint = BrandColors.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}