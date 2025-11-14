package com.example.damprojectfinal

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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

// User + Pro
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro

// Réclamation
import com.example.foodyz_dam.ui.screens.reclamation.ReclamationTemplateScreen
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
            LoginScreen(
                navController = navController,
                authApiService = authApiService,
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

        // 4️⃣ Forgot Password (Envoyer OTP)
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

        // 6️⃣ Reset Password (✅ SANS Deep Link)
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

        // 🔟 Liste réclamations
        composable("reclamation_list") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val repository = ReclamationRepository(ReclamationRetrofitClient.reclamationApi)

            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, repository)
            )

            val reclamations by vm.reclamations.collectAsState()
            val error by vm.errorMessage.collectAsState()

            LaunchedEffect(Unit) { vm.loadReclamations() }

            ReclamationListScreen(
                reclamations = reclamations,
                onReclamationClick = {},
                onBackClick = { navController.popBackStack() }
            )

            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // 1️⃣1️⃣ Créer réclamation
        composable("create_reclamation") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val repository = ReclamationRepository(ReclamationRetrofitClient.reclamationApi)

            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, repository)
            )

            ReclamationTemplateScreen(
                restaurantNames = listOf("Jean Dupont", "Marie Martin", "Pierre Dubois"),
                complaintTypes = listOf("Late delivery", "Missing item", "Quality issue", "Other"),
                commandeconcernees = listOf("Commande #12345", "Commande #12346", "Commande #12347"),
                onSubmit = { nomClient, commandeConcernee, complaintType, desc, photos ->
                    val request = CreateReclamationRequest(
                        nomClient = nomClient,
                        commandeConcernee = commandeConcernee,
                        complaintType = complaintType,
                        description = desc,
                        photos = photos.map { it.toString() }
                    )

                    vm.createReclamation(request) {
                        Toast.makeText(context, "Réclamation créée !", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}