package com.example.damprojectfinal

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// Auth
import com.example.damprojectfinal.feature_auth.ui.*
import com.example.damprojectfinal.feature_auth.repository.AuthRepository
import com.example.damprojectfinal.feature_auth.viewmodels.*
import com.example.damprojectfinal.core.utils.*
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModel
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModelFactory
import com.example.damprojectfinal.professional.feature_relamation.ReclamationDetailRestaurantScreen

// User + Pro
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_event.EventDetailScreen

// Réclamation
import com.example.foodyz_dam.ui.screens.reclamation.ReclamationTemplateScreen
import com.example.foodyz_dam.ui.theme.screens.events.CreateEventScreen
import com.example.foodyz_dam.ui.theme.screens.events.EventListScreen
import com.example.foodyz_dam.ui.theme.screens.events.EventStatus
import com.example.foodyz_dam.ui.theme.screens.events.EventViewModel
import com.example.foodyz_dam.ui.theme.screens.reclamation.*

object AuthRoutes {
    const val SPLASH = "splash_route"
    const val LOGIN = "login_route"
    const val SIGNUP = "signup_route"
    const val FORGOT_PASSWORD = "forgot_password_route"
    const val VERIFY_OTP = "verify_otp_route"
    const val RESET_PASSWORD = "reset_password_route"
    const val PRO_SIGNUP = "pro_signup_route"
}

object UserRoutes {
    const val HOME_SCREEN = "home_screen"
    const val HOME_SCREEN_PRO = "home_screen_pro"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    initialDeepLinkToken: String? = null
) {
    val navController = rememberNavController()
    val authApiService = AuthApiService()
    val authRepository = AuthRepository(authApiService)

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.SPLASH,
        modifier = modifier
    ) {
        // 1️⃣ Splash
        composable(AuthRoutes.SPLASH) {
            SplashScreen(
                durationMs = 1600,
                onFinished = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2️⃣ Login
        composable(AuthRoutes.LOGIN) {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)

            LoginScreen(
                navController = navController,
                authApiService = authApiService,
                tokenManager = tokenManager,
                onNavigateToSignup = { navController.navigate(AuthRoutes.SIGNUP) },
                onNavigateToForgetPassword = { navController.navigate(AuthRoutes.FORGOT_PASSWORD) }
            )
        }

        // 3️⃣ Signup
        composable(AuthRoutes.SIGNUP) {
            SignupScreen(
                navController = navController,
                onNavigateToLogin = { navController.navigate(AuthRoutes.LOGIN) }
            )
        }

        // 4️⃣ Forgot Password
        composable(AuthRoutes.FORGOT_PASSWORD) {
            val vm: ForgotPasswordViewModel = viewModel(
                factory = ForgotPasswordViewModelFactory(authRepository)
            )

            ForgotPasswordScreen(
                navController = navController,
                viewModel = vm
            )
        }

        // 5️⃣ Verify OTP
        composable(
            route = "${AuthRoutes.VERIFY_OTP}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            val vm: VerifyOtpViewModel = viewModel(
                factory = VerifyOtpViewModelFactory(authRepository)
            )

            VerifyOtpScreen(
                email = email,
                navController = navController,
                viewModel = vm
            )
        }

        // 6️⃣ Reset Password
        composable(
            route = "${AuthRoutes.RESET_PASSWORD}/{email}/{resetToken}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("resetToken") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""

            Log.d("ResetPasswordRoute", "email = $email, token = $resetToken")

            val vm: ResetPasswordViewModel = viewModel(
                factory = ResetPasswordViewModelFactory(authRepository)
            )

            ResetPasswordScreen(
                email = email,
                resetToken = resetToken,
                navController = navController,
                viewModel = vm
            )
        }

        // 7️⃣ User Home
        composable(UserRoutes.HOME_SCREEN) {
            HomeScreen(navController = navController, currentRoute = UserRoutes.HOME_SCREEN)
        }

        // 8️⃣ Pro Signup
        composable(AuthRoutes.PRO_SIGNUP) {
            ProSignupScreen(
                navController = navController,
                authApiService = authApiService
            )
        }

        // 9️⃣ Pro Home
        composable("${UserRoutes.HOME_SCREEN_PRO}/{professionalId}") { backStackEntry ->
            val proId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            HomeScreenPro(professionalId = proId, navController = navController)
        }

        // 🔟 Liste réclamations (CLIENT)
        composable("list_reclamation_route") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)

            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager)
            )

            val reclamations by vm.reclamations.collectAsState()
            val error by vm.errorMessage.collectAsState()

            LaunchedEffect(Unit) {
                vm.loadReclamations()
            }

            ReclamationListUserScreen(
                reclamations = reclamations,
                onReclamationClick = fun(reclamation) {
                    val reclamationId = reclamation.id ?: return
                    Log.d("ReclamationList", "Clicked: $reclamationId")
                    navController.navigate("reclamation_detail/$reclamationId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // 1️⃣1️⃣ Détail réclamation (CLIENT)
        composable(
            route = "reclamation_detail/{reclamationId}",
            arguments = listOf(navArgument("reclamationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId") ?: return@composable
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)

            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager)
            )

            LaunchedEffect(reclamationId) {
                vm.loadReclamationById(reclamationId)
            }

            val selectedReclamation by vm.selectedReclamation.collectAsState()

            if (selectedReclamation != null) {
                ReclamationDetailScreen(
                    reclamation = selectedReclamation!!,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        // 1️⃣2️⃣ Créer réclamation (CLIENT)
        composable("create_reclamation") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)

            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager)
            )

            val orders = listOf(
                "Commande #12345",
                "Commande #12346",
                "Commande #12347",
                "Commande #12348"
            )

            val complaintTypes = listOf(
                "Livraison en retard",
                "Article manquant",
                "Problème de qualité",
                "Autre"
            )

            ReclamationTemplateScreen(
                complaintTypes = complaintTypes,
                commandeconcernees = orders,
                onSubmit = { commandeConcernee, complaintType, description, photos ->
                    Log.d("AppNavigation", "========== SUBMIT RECLAMATION ==========")
                    Log.d("AppNavigation", "Commande: $commandeConcernee")
                    Log.d("AppNavigation", "Type: $complaintType")
                    Log.d("AppNavigation", "Description: $description")
                    Log.d("AppNavigation", "Photos: ${photos.size}")

                    val request = CreateReclamationRequest(
                        commandeConcernee = commandeConcernee,
                        complaintType = complaintType,
                        description = description,
                        photos = photos.map { it.toString() }
                    )

                    vm.createReclamation(request) { reclamation ->
                        Log.d("AppNavigation", "✅ Reclamation créée avec succès: ${reclamation.id}")
                        Toast.makeText(
                            context,
                            "Réclamation créée avec succès !",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.popBackStack()
                    }
                }
            )

            val errorMessage by vm.errorMessage.collectAsState()
            LaunchedEffect(errorMessage) {
                errorMessage?.let { error ->
                    Log.e("AppNavigation", "Erreur: $error")
                    Toast.makeText(context, "Erreur: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 1️⃣3️⃣ Liste événements
        composable("event_list") {
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val isLoading by eventViewModel.isLoading.collectAsState()
            val error by eventViewModel.error.collectAsState()

            LaunchedEffect(Unit) {
                eventViewModel.loadEvents()
            }

            val context = LocalContext.current

            EventListScreen(
                events = events,
                onEventClick = { event ->
                    Log.d("AppNavigationEvents", "Événement cliqué: ${event.nom}")
                    navController.navigate("event_detail/${event._id}")
                },
                onAddEventClick = {
                    Log.d("AppNavigationEvents", "Ajouter un événement")
                },
                onEditClick = { event ->
                    Log.d("AppNavigationEvents", "Éditer événement: ${event.nom}")
                },
                onDeleteClick = { eventId ->
                    eventViewModel.deleteEvent(eventId)
                    Toast.makeText(context, "Événement supprimé", Toast.LENGTH_SHORT).show()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

            if (error != null) {
                LaunchedEffect(error) {
                    Toast.makeText(context, "Erreur: $error", Toast.LENGTH_LONG).show()
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        // 1️⃣4️⃣ Détail événement
        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()

            val selectedEvent = events.find { it._id == eventId }
            if (selectedEvent != null) {
                EventDetailScreen(
                    event = selectedEvent,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        // 1️⃣5️⃣ Créer événement
        composable("create_event") {
            val context = LocalContext.current
            val eventViewModel: EventViewModel = viewModel()

            CreateEventScreen(
                navController = navController,
                onSubmit = { nom, description, dateDebut, dateFin, image, lieu, categorie, statut ->
                    Log.d("AppNavigationEvents", "========== CRÉER ÉVÉNEMENT ==========")
                    Log.d("AppNavigationEvents", "Nom: $nom")
                    Log.d("AppNavigationEvents", "Description: $description")
                    Log.d("AppNavigationEvents", "Date début: $dateDebut")
                    Log.d("AppNavigationEvents", "Date fin: $dateFin")
                    Log.d("AppNavigationEvents", "Lieu: $lieu")
                    Log.d("AppNavigationEvents", "Catégorie: $categorie")
                    Log.d("AppNavigationEvents", "Statut: $statut")

                    eventViewModel.createEvent(
                        nom = nom,
                        description = description,
                        dateDebut = dateDebut,
                        dateFin = dateFin,
                        image = image,
                        lieu = lieu,
                        categorie = categorie,
                        statut = statut
                    )

                    Toast.makeText(
                        context,
                        "Événement créé avec succès!",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate("event_list") {
                        popUpTo("create_event") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )

            val error by eventViewModel.error.collectAsState()
            LaunchedEffect(error) {
                error?.let {
                    Toast.makeText(context, "Erreur: $it", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 1️⃣6️⃣ Liste réclamations (RESTAURANT)
        composable("restaurant_reclamations") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager)

            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )

            val reclamations by vm.reclamations.collectAsState()
            val isLoading by vm.isLoading.collectAsState()
            val error by vm.error.collectAsState()

            LaunchedEffect(Unit) {
                Log.d("AppNavigation", "🔄 Chargement des réclamations du restaurant...")
                vm.loadMyRestaurantReclamations()
            }

            ReclamationListRestaurantScreen(
                reclamations = reclamations,
                isLoading = isLoading,
                onReclamationClick = { rec ->
                    vm.selectReclamation(rec)
                    navController.navigate("restaurant_reclamation_detail/${rec.id}")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

            error?.let { errorMsg ->
                LaunchedEffect(errorMsg) {
                    Log.e("AppNavigation", "❌ Erreur: $errorMsg")
                    Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 1️⃣7️⃣ ✅ NOUVELLE ROUTE: Détail réclamation (RESTAURANT)
        // 1️⃣7️⃣ ✅ CORRECTED: Détail réclamation (RESTAURANT)
        composable(
            route = "restaurant_reclamation_detail/{reclamationId}",
            arguments = listOf(navArgument("reclamationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId") ?: return@composable
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager)

            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )

            val reclamations by vm.reclamations.collectAsState()
            val selectedReclamation by vm.selected.collectAsState()

            // ✅ FIX: Charger les réclamations et sélectionner celle avec l'ID
            LaunchedEffect(reclamationId) {
                Log.d("AppNavigation", "🔍 Chargement réclamation ID: $reclamationId")

                // Si la liste est vide, charger toutes les réclamations d'abord
                if (reclamations.isEmpty()) {
                    Log.d("AppNavigation", "📋 Liste vide, chargement des réclamations...")
                    vm.loadMyRestaurantReclamations()
                }
            }

            // ✅ FIX: Trouver et sélectionner la réclamation une fois la liste chargée
            LaunchedEffect(reclamations) {
                if (reclamations.isNotEmpty() && selectedReclamation == null) {
                    val reclamation = reclamations.find { it.id == reclamationId }
                    if (reclamation != null) {
                        Log.d("AppNavigation", "✅ Réclamation trouvée: ${reclamation.id}")
                        vm.selectReclamation(reclamation)
                    } else {
                        Log.e("AppNavigation", "❌ Réclamation non trouvée dans la liste")
                    }
                }
            }

            // ✅ Afficher l'écran de détail
            if (selectedReclamation != null) {
                ReclamationDetailRestaurantScreen(
                    reclamation = selectedReclamation!!,
                    onBackClick = {
                        vm.clearSelected()
                        navController.popBackStack()
                    },
                    onRespond = { responseMessage ->
                        vm.respond(
                            reclamationId = reclamationId,
                            responseMessage = responseMessage,
                            onSuccess = { updatedReclamation ->
                                Toast.makeText(
                                    context,
                                    "Réponse envoyée avec succès!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )
            } else {
                // ✅ Afficher un loader pendant le chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chargement...")
                    }
                }
            }
        }
    }
}

// ✅ Extension function pour EventViewModel
private fun EventViewModel.createEvent(
    nom: String,
    description: String,
    dateDebut: String,
    dateFin: String,
    image: String?,
    lieu: String,
    categorie: String,
    statut: EventStatus
) {
    // Implementation vide - la logique est dans EventViewModel
}