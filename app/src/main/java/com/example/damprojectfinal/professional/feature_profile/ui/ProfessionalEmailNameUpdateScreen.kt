package com.example.damprojectfinal.professional.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel
import kotlinx.coroutines.launch

// Yellow accent color matching the app theme
private val ProYellow = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalEmailNameUpdateScreen(
    navController: NavHostController,
    professionalId: String,
    viewModel: ProfessionalProfileViewModel = viewModel(
        factory = ProfessionalProfileViewModel.Factory(
            tokenManager = TokenManager(androidx.compose.ui.platform.LocalContext.current),
            professionalApiService = RetrofitClient.professionalApiService,
            postsApiService = RetrofitClient.postsApiService
        )
    )
) {
    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val updateError by viewModel.updateError.collectAsState()
    
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var fullName by remember { mutableStateOf(profile?.fullName ?: "") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(profile) {
        email = profile?.email ?: ""
        fullName = profile?.fullName ?: ""
    }
    
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Email and name updated successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.resetUpdateStatus()
            viewModel.loadProfessionalProfile(professionalId)
            kotlinx.coroutines.delay(1000)
            navController.popBackStack()
        }
    }
    
    LaunchedEffect(updateError) {
        updateError?.let { error ->
            snackbarHostState.showSnackbar(
                message = "Error: $error",
                duration = SnackbarDuration.Long
            )
            viewModel.resetUpdateStatus()
        }
    }
    
    LaunchedEffect(professionalId) {
        viewModel.loadProfessionalProfile(professionalId)
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (updateError != null) Color(0xFFEF4444) else ProYellow,
                        contentColor = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Email & Name", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // TODO: Implement email and name update
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Email and name update coming soon",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.CheckCircle, "Save Changes", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProYellow,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Email",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter email address") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, null, tint = ProYellow)
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProYellow,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Business Name",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter business name") },
                            leadingIcon = {
                                Icon(Icons.Filled.Business, null, tint = ProYellow)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProYellow,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}

