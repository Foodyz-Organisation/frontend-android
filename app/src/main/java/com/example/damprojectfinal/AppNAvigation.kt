package com.example.damprojectfinal

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.utils.ForgotPasswordViewModelFactory
import com.example.damprojectfinal.core.utils.ResetPasswordViewModelFactory
import com.example.damprojectfinal.core.utils.VerifyOtpViewModelFactory
import com.example.damprojectfinal.feature_deals.AddEditDealScreen
import com.example.damprojectfinal.feature_deals.DealsViewModel
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModel
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModelFactory
import com.example.damprojectfinal.feature_relamation.ReclamationViewModelFactory
import com.example.damprojectfinal.professional.feature_relamation.ReclamationDetailRestaurantScreen

// User + Pro
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_deals.ProDealsManagementScreen
import com.example.damprojectfinal.professional.feature_event.EventDetailScreen
import com.example.damprojectfinal.user.feature_deals.DealDetailScreen
import com.example.damprojectfinal.user.feature_deals.DealsListScreen

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

    // ✅ IMPORTANT: Créer le ViewModel UNE SEULE FOIS
    val dealsViewModel: DealsViewModel = viewModel()

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
            ForgotPasswordScreen(navController = navController, viewModel = vm)
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
            VerifyOtpScreen(email = email, navController = navController, viewModel = vm)
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
            ProSignupScreen(navController = navController, authApiService = authApiService)
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
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )
            val reclamations by vm.reclamations.collectAsState()
            val error by vm.errorMessage.collectAsState()

            LaunchedEffect(Unit) { vm.loadReclamations() }

            ReclamationListUserScreen(
                reclamations = reclamations,
                onReclamationClick = fun(reclamation) {
                    val reclamationId = reclamation.id ?: return
                    navController.navigate("reclamation_detail/$reclamationId")
                },
                onBackClick = { navController.popBackStack() }
            )
            error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
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
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )

            LaunchedEffect(reclamationId) { vm.loadReclamationById(reclamationId) }
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
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )

            ReclamationTemplateScreen(
                complaintTypes = listOf("Livraison en retard", "Article manquant", "Problème de qualité", "Autre"),
                commandeconcernees = listOf("Commande #12345", "Commande #12346", "Commande #12347", "Commande #12348"),
                onSubmit = { commandeConcernee, complaintType, description, photos ->
                    vm.createReclamation(
                        commandeConcernee = commandeConcernee,
                        complaintType = complaintType,
                        description = description,
                        photoUris = photos
                    ) { reclamation ->
                        Toast.makeText(context, "Réclamation créée avec succès !", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }
            )
        }

        // 1️⃣3️⃣ Liste événements
        composable("event_list") {
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) { eventViewModel.loadEvents() }

            EventListScreen(
                events = events,
                onEventClick = { event -> navController.navigate("event_detail/${event._id}") },
                onAddEventClick = {},
                onEditClick = { event -> },
                onDeleteClick = { eventId -> eventViewModel.deleteEvent(eventId) },
                onBackClick = { navController.popBackStack() }
            )
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
                EventDetailScreen(event = selectedEvent, onBackClick = { navController.popBackStack() })
            }
        }

        // 1️⃣5️⃣ Créer événement
        composable("create_event") {
            val context = LocalContext.current
            val eventViewModel: EventViewModel = viewModel()

            CreateEventScreen(
                navController = navController,
                onSubmit = { nom, description, dateDebut, dateFin, image, lieu, categorie, statut ->
                    eventViewModel.createEvent(nom, description, dateDebut, dateFin, image, lieu, categorie, statut)
                    Toast.makeText(context, "Événement créé avec succès!", Toast.LENGTH_SHORT).show()
                    navController.navigate("event_list") { popUpTo("create_event") { inclusive = true } }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 1️⃣6️⃣ Liste réclamations (RESTAURANT)
        composable("restaurant_reclamations") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager, context)
            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )
            val reclamations by vm.reclamations.collectAsState()
            val isLoading by vm.isLoading.collectAsState()

            LaunchedEffect(Unit) { vm.loadMyRestaurantReclamations() }

            ReclamationListRestaurantScreen(
                reclamations = reclamations,
                isLoading = isLoading,
                onReclamationClick = { rec ->
                    vm.selectReclamation(rec)
                    navController.navigate("restaurant_reclamation_detail/${rec.id}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 1️⃣7️⃣ Détail réclamation (RESTAURANT)
        composable(
            route = "restaurant_reclamation_detail/{reclamationId}",
            arguments = listOf(navArgument("reclamationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId") ?: return@composable
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager, context)
            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )
            val reclamations by vm.reclamations.collectAsState()
            val selectedReclamation by vm.selected.collectAsState()

            LaunchedEffect(reclamationId) {
                if (reclamations.isEmpty()) vm.loadMyRestaurantReclamations()
            }

            LaunchedEffect(reclamations) {
                if (reclamations.isNotEmpty() && selectedReclamation == null) {
                    reclamations.find { it.id == reclamationId }?.let { vm.selectReclamation(it) }
                }
            }

            if (selectedReclamation != null) {
                ReclamationDetailRestaurantScreen(
                    reclamation = selectedReclamation!!,
                    onBackClick = {
                        vm.clearSelected()
                        navController.popBackStack()
                    },
                    onRespond = { responseMessage ->
                        vm.respond(reclamationId, responseMessage) {
                            Toast.makeText(context, "Réponse envoyée!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // ============================================
        // 📌 SECTION DEALS - ROUTES PROFESSIONNELLES
        // ============================================

        // 1️⃣8️⃣ Gestion PRO des deals
        composable("pro_deals") {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                Log.d("AppNavigation", "📊 Chargement écran pro_deals")
                dealsViewModel.loadDeals()
            }

            ProDealsManagementScreen(
                viewModel = dealsViewModel,
                onAddDealClick = {
                    Log.d("AppNavigation", "➕ Navigation vers deal_add")
                    navController.navigate("deal_add")
                },
                onEditDealClick = { dealId ->
                    Log.d("AppNavigation", "✏️ Navigation vers deal_edit/$dealId")
                    navController.navigate("deal_edit/$dealId")
                }
            )
        }

        // 1️⃣9️⃣ Ajouter un deal
        composable("deal_add") {
            Log.d("AppNavigation", "➕ Écran d'ajout de deal")

            AddEditDealScreen(
                dealId = null,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis deal_add")
                    navController.popBackStack()
                }
            )
        }

        // 2️⃣0️⃣ Modifier un deal
        composable(
            route = "deal_edit/{dealId}",
            arguments = listOf(navArgument("dealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dealId = backStackEntry.arguments?.getString("dealId")

            Log.d("AppNavigation", "✏️ Écran d'édition de deal: $dealId")

            AddEditDealScreen(
                dealId = dealId,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis deal_edit")
                    navController.popBackStack()
                }
            )
        }

        // 2️⃣1️⃣ Liste des deals (pour les clients)
        composable("deals") {
            LaunchedEffect(Unit) {
                Log.d("AppNavigation", "📋 Chargement liste des deals")
                dealsViewModel.loadDeals()
            }

            DealsListScreen(
                viewModel = dealsViewModel,
                onDealClick = { dealId ->
                    Log.d("AppNavigation", "🔍 Navigation vers dealDetail/$dealId")
                    navController.navigate("dealDetail/$dealId")
                }
            )
        }

        // 2️⃣2️⃣ Détail d'un deal
        composable(
            route = "dealDetail/{dealId}",
            arguments = listOf(navArgument("dealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dealId = backStackEntry.arguments?.getString("dealId") ?: ""

            Log.d("AppNavigation", "🔍 Détail du deal: $dealId")

            DealDetailScreen(
                dealId = dealId,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis dealDetail")
                    navController.popBackStack()
                }
            )
        }
    }
}

// Extension function pour EventViewModel
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
    // La logique métier est dans EventViewModel
}