package com.example.damprojectfinal.professional.feature_relamation

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.ReclamationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ReclamationDetail"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclamationDetailRestaurantScreen(
    reclamation: Reclamation,
    onBackClick: () -> Unit,
    onRespond: (String) -> Unit
) {
    Log.e(TAG, "═══════════════════════════════════════════════════════")
    Log.e(TAG, "🎬 ECRAN OUVERT - ReclamationDetailRestaurantScreen")
    Log.e(TAG, "═══════════════════════════════════════════════════════")

    var responseText by remember { mutableStateOf("") }
    var showResponseDialog by remember { mutableStateOf(false) }

    val BASE_URL = "http://192.168.1.7:3000"

    LaunchedEffect(Unit) {
        Log.e(TAG, "📡 BASE_URL = $BASE_URL")
        Log.e(TAG, "📋 Reclamation ID = ${reclamation.id}")
        Log.e(TAG, "📸 Photos count = ${reclamation.photos?.size ?: 0}")

        reclamation.photos?.forEachIndexed { index, photo ->
            Log.e(TAG, "Photo $index original: $photo")
        }
    }

    val statusColor = when (reclamation.status) {
        ReclamationStatus.PENDING -> Color(0xFFFFA726)
        ReclamationStatus.RESOLVED -> Color(0xFF66BB6A)
        ReclamationStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        ReclamationStatus.REJECTED -> Color(0xFFEF5350)
    }

    val statusText = when (reclamation.status) {
        ReclamationStatus.PENDING -> "En attente"
        ReclamationStatus.RESOLVED -> "Résolue"
        ReclamationStatus.IN_PROGRESS -> "En cours"
        ReclamationStatus.REJECTED -> "Rejetée"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Détail de la réclamation", color = Color(0xFF1A1A1A)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statut
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Statut",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Informations client
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informations client",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Nom", reclamation.nomClient ?: "Non renseigné")
                        InfoRow("Email", reclamation.emailClient ?: "Non renseigné")
                        InfoRow("Date", reclamation.createdAt?.let { formatDate(it) } ?: "N/A")
                    }
                }
            }

            // Détails réclamation
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Détails de la réclamation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Commande concernée", reclamation.orderNumber ?: "N/A")
                        InfoRow("Type de réclamation", reclamation.complaintType ?: "N/A")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reclamation.description ?: "Aucune description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            // Photos avec chargement manuel
            if (!reclamation.photos.isNullOrEmpty()) {
                item {
                    Text(
                        text = "Photos jointes (${reclamation.photos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(reclamation.photos) { index, photoPath ->
                    val fullImageUrl = when {
                        photoPath.startsWith("http://") || photoPath.startsWith("https://") -> {
                            photoPath
                        }
                        photoPath.startsWith("/uploads/reclamations/") -> {
                            "$BASE_URL$photoPath"
                        }
                        else -> {
                            val filename = photoPath.substringAfterLast("/")
                            "$BASE_URL/uploads/reclamations/$filename"
                        }
                    }

                    Log.e(TAG, "🖼️ Photo $index URL: $fullImageUrl")

                    ManualImageLoader(
                        imageUrl = fullImageUrl,
                        index = index
                    )
                }
            }

            // Réponse du restaurant
            reclamation.responseMessage?.let { response ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Votre réponse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = response,
                                color = Color(0xFF1A1A1A)
                            )
                            reclamation.respondedAt?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Répondu le ${formatDate(it)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }

            // Bouton répondre
            if (reclamation.responseMessage == null) {
                item {
                    Button(
                        onClick = { showResponseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        )
                    ) {
                        Text(
                            "Répondre à la réclamation",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog
    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = { Text("Répondre à la réclamation") },
            text = {
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Votre réponse...") },
                    minLines = 4,
                    maxLines = 8
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (responseText.isNotBlank()) {
                            onRespond(responseText)
                            showResponseDialog = false
                            responseText = ""
                        }
                    },
                    enabled = responseText.isNotBlank()
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResponseDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// 🔥 NOUVEAU : Composant de chargement manuel d'image
@Composable
fun ManualImageLoader(
    imageUrl: String,
    index: Int
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUrl) {
        Log.e(TAG, "🔄 Chargement manuel de l'image $index...")
        isLoading = true
        errorMessage = null

        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.doInput = true
                connection.requestMethod = "GET"

                Log.e(TAG, "📡 Connexion à: $imageUrl")
                connection.connect()

                val responseCode = connection.responseCode
                Log.e(TAG, "📥 Code réponse: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val loadedBitmap = BitmapFactory.decodeStream(inputStream)

                    if (loadedBitmap != null) {
                        bitmap = loadedBitmap
                        Log.e(TAG, "✅ Image $index chargée! Taille: ${loadedBitmap.width}x${loadedBitmap.height}")
                    } else {
                        errorMessage = "Impossible de décoder l'image"
                        Log.e(TAG, "❌ Erreur décodage bitmap")
                    }

                    inputStream.close()
                } else {
                    errorMessage = "Erreur HTTP: $responseCode"
                    Log.e(TAG, "❌ Erreur HTTP $responseCode")
                }

                connection.disconnect()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erreur inconnue"
                Log.e(TAG, "❌ Exception: ${e.message}")
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Image display (sans debug card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFEB3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "⏳ Chargement manuel...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFEBEE))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("❌", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Erreur de chargement",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    errorMessage ?: "Erreur inconnue",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Photo ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
    }
}

private fun formatDate(date: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(date)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }
}