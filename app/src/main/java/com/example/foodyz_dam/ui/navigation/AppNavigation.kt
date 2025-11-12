package com.example.foodyz_dam.ui.navigation
//package com.example.foodyz_dam.ui.theme.screens.reclamation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodyz_dam.ui.screens.reclamation.ReclamationTemplateScreen
import com.example.foodyz_dam.ui.theme.screens.events.*
import com.example.foodyz_dam.ui.theme.screens.reclamation.Reclamation
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationDetailScreen
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationListScreen
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationRepository
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationViewModel
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationViewModelFactory
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationStatus
import com.example.foodyz_dam.ui.theme.screens.reclamation.CreateReclamationRequest
import java.util.Date
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationApi
import com.example.foodyz_dam.ui.theme.screens.reclamation.ReclamationRetrofitClient

@Composable
fun AppNavigation(navController: NavHostController) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.events.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val error by eventViewModel.error.collectAsState()
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val context = LocalContext.current
    var selectedReclamation by remember { mutableStateOf<Reclamation?>(null) }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "createReclamation",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("eventList") {
                when {
                    isLoading -> LoadingScreen()
                    error != null -> ErrorScreen(error = error ?: "") { eventViewModel.loadEvents() }
                    else -> EventListScreen(
                        events = events,
                        onEventClick = { event ->
                            selectedEvent = event
                            navController.navigate("eventDetail")
                        },
                        onBackClick = { navController.popBackStack() },
                        onAddEventClick = { navController.navigate("createEvent") },
                        onEditClick = { event ->
                            selectedEvent = event
                            navController.navigate("editEvent")
                        }
,
                        onDeleteClick = { eventId -> // ✅ Changé: reçoit l'ID directement
                            android.util.Log.d("AppNavigation", "Delete event with ID: $eventId")
                            eventViewModel.deleteEvent(eventId)
                            Toast.makeText(context, "Événement supprimé", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            composable("eventDetail") {
                selectedEvent?.let { event ->
                    EventDetailScreenUser(
                        event = event,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = {
                            // TODO: Navigation vers édition
                            Toast.makeText(context, "Édition de ${event.nom}", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = {
                            // ✅ Utilise event._id au lieu de event.id
                            if (event._id != null) {
                                eventViewModel.deleteEvent(event._id)
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Erreur: ID manquant", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } ?: run {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    LoadingScreen()
                }
            }

            composable("createEvent") {
                CreateEventScreen(
                    navController = navController,
                    categories = listOf("cuisine française", "cuisine tunisienne", "cuisine japonaise"),
                    statuts = listOf(" à venir", "en cours", "terminé"),
                    onSubmit = { nom, desc, debut, fin, img, lieu, cat, statut ->
                        val newEvent = Event(
                            id = "",
                            nom = nom,
                            description = desc,
                            date_debut = debut,
                            date_fin = fin,
                            image = img,
                            lieu = lieu,
                            categorie = cat,
                            statut = statut
                        )
                        eventViewModel.addEvent(newEvent)
                        Toast.makeText(context, "Événement créé avec succès !", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("editEvent") {
                selectedEvent?.let { event ->
                    EditEventScreen(
                        navController = navController,
                        event = event,
                        categories = listOf("cuisine française", "cuisine tunisienne", "cuisine japonaise"),
                        statuts = listOf("À venir", "En cours", "Terminé"),
                        onUpdate = { id, nom, desc, debut, fin, img, lieu, cat, statut ->
                            eventViewModel.updateEvent(
                                event = event,
                                nom = nom,
                                description = desc,
                                dateDebut = debut,
                                dateFin = fin,
                                image = img,
                                lieu = lieu,
                                categorie = cat,
                                statut = statut
                            )
                            Toast.makeText(context, "Événement modifié avec succès !", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                } ?: run {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    LoadingScreen()
                }
            }






            composable("reclamationList") {
                val reclamationViewModel: ReclamationViewModel = viewModel(
                    factory = ReclamationViewModelFactory(
                        ReclamationRepository(ReclamationRetrofitClient.reclamationApi)
                    )
                )
                val reclamations by reclamationViewModel.reclamations.collectAsState()
                val errorMessage by reclamationViewModel.errorMessage.collectAsState()

                LaunchedEffect(Unit) {
                    reclamationViewModel.loadReclamations()
                }

                ReclamationListScreen(
                    reclamations = reclamations,
                    onReclamationClick = { reclamation ->
                        selectedReclamation = reclamation
                        navController.navigate("reclamationDetail")
                    },
                    onBackClick = { navController.popBackStack() }
                )

                errorMessage?.let { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            }

            composable("reclamationDetail") {
                selectedReclamation?.let { reclamation ->
                    ReclamationDetailScreen(
                        reclamation = reclamation,
                        onBackClick = { navController.popBackStack() }
                    )
                } ?: run {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    LoadingScreen()
                }
            }

            composable("createReclamation") {
                val localContext = LocalContext.current

                val reclamationViewModel: ReclamationViewModel = viewModel(
                    factory = ReclamationViewModelFactory(
                        ReclamationRepository(ReclamationRetrofitClient.reclamationApi)
                    )
                )

                // Liste des noms de clients (ou récupérer depuis l'utilisateur connecté)
                val clientNames = listOf("Jean Dupont", "Marie Martin", "Pierre Dubois")

                // Types de réclamation
                val complaintTypes = listOf("Late delivery", "Missing item", "Quality issue", "Other")

                // Commandes disponibles
                val commandes = listOf("Commande #12345", "Commande #12346", "Commande #12347")

                ReclamationTemplateScreen(
                    restaurantNames = clientNames, // On réutilise ce paramètre pour les noms clients
                    complaintTypes = complaintTypes,
                    commandeconcernees = commandes,
                    onSubmit = { nomClient, commandeConcernee, complaintType, description, photos ->
                        android.util.Log.d("CreateReclamation", "=== DEBUT onSubmit ===")
                        android.util.Log.d("CreateReclamation", "Nom Client: '$nomClient'")
                        android.util.Log.d("CreateReclamation", "Commande: '$commandeConcernee'")
                        android.util.Log.d("CreateReclamation", "Type: '$complaintType'")
                        android.util.Log.d("CreateReclamation", "Description: '$description'")
                        android.util.Log.d("CreateReclamation", "Photos count: ${photos.size}")

                        val request = CreateReclamationRequest(
                            nomClient = nomClient,
                            emailClient = "client@example.com", // TODO: Récupérer l'email du client connecté
                            description = description,
                            commandeConcernee = commandeConcernee,
                            complaintType = complaintType,
                            image = null // Les images ne sont pas gérées pour l'instant
                        )

                        android.util.Log.d("CreateReclamation", "Request JSON: $request")
                        android.util.Log.d("CreateReclamation", "Appel createReclamation...")

                        try {
                            reclamationViewModel.createReclamation(request) { newReclamation ->
                                android.util.Log.d("CreateReclamation", "Success! New reclamation: $newReclamation")
                                Toast.makeText(
                                    localContext,
                                    "Réclamation créée avec succès!",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CreateReclamation", "Erreur: ${e.message}", e)
                            Toast.makeText(
                                localContext,
                                "Erreur: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BrandColors.Yellow)
            Spacer(Modifier.height(16.dp))
            Text("Chargement des événements...", color = BrandColors.TextSecondary)
        }
    }
}

@Composable
fun ErrorScreen(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = BrandColors.Red
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Erreur de connexion",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = BrandColors.TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
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