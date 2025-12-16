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
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreenRemote(
    navController: NavController
) {
    var events by remember { mutableStateOf(listOf<Event>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("EventListScreenRemote", "➕ Navigation vers create_event (TopBar)")
                            navController.navigate("create_event")
                        }
                    ) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("EventListScreenRemote", "➕ Navigation vers create_event (FAB)")
                    navController.navigate("create_event")
                },
                containerColor = BrandColors.Yellow,
                contentColor = BrandColors.TextPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Créer un événement"
                )
            }
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
                    EmptyState(onAddClick = {
                        Log.d("EventListScreenRemote", "➕ EmptyState - Navigation vers create_event")
                        navController.navigate("create_event")
                    })
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
                                onClick = { 
                                    event._id?.let { eventId ->
                                        Log.d("EventListScreenRemote", "👁️ Navigation vers event_detail/$eventId")
                                        navController.navigate("event_detail/$eventId")
                                    }
                                },
                                onEditClick = { 
                                    event._id?.let { eventId ->
                                        Log.d("EventListScreenRemote", "✏️ Navigation vers edit_event/$eventId")
                                        navController.navigate("edit_event/$eventId")
                                    } ?: run {
                                        Toast.makeText(context, "Erreur: L'événement n'a pas d'ID", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDeleteClick = {
                                    // Le dialog de confirmation est déjà géré dans EventCard
                                    // Ce callback est appelé quand l'utilisateur confirme la suppression
                                    event._id?.let { eventId ->
                                        Log.d("EventListScreenRemote", "🗑️ Suppression confirmée pour l'événement $eventId")
                                        
                                        scope.launch {
                                            try {
                                                EventRetrofitClient.api.deleteEvent(eventId)
                                                Log.d("EventListScreenRemote", "✅ Événement supprimé avec succès")
                                                Toast.makeText(context, "Événement supprimé", Toast.LENGTH_SHORT).show()
                                                
                                                // Recharger la liste
                                                events = EventRetrofitClient.api.getEvents()
                                            } catch (e: Exception) {
                                                Log.e("EventListScreenRemote", "❌ Erreur suppression: ${e.message}", e)
                                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } ?: run {
                                        Toast.makeText(context, "Erreur: L'événement n'a pas d'ID", Toast.LENGTH_SHORT).show()
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