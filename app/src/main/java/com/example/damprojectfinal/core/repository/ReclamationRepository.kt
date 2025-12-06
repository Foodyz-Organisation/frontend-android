package com.example.damprojectfinal.core.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.reclamation.CreateReclamationRequest
import com.example.damprojectfinal.core.dto.reclamation.Reclamation
import com.example.damprojectfinal.core.dto.reclamation.RespondReclamationRequest
import java.io.ByteArrayOutputStream

class ReclamationRepository(
    private val tokenManager: TokenManager,
    private val context: Context
) {
    private val TAG = "ReclamationRepo"

    /**
     * ✅ Convertit un URI en Base64
     */
    private fun uriToBase64(uri: Uri): String? {
        return try {
            Log.e(TAG, "🖼️ Conversion: $uri")

            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return null

            Log.e(TAG, "   Original: ${originalBitmap.width}x${originalBitmap.height}")

            // Redimensionner si nécessaire
            val maxSize = 1200
            val bitmap = if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
                val ratio = minOf(
                    maxSize.toFloat() / originalBitmap.width,
                    maxSize.toFloat() / originalBitmap.height
                )
                val newWidth = (originalBitmap.width * ratio).toInt()
                val newHeight = (originalBitmap.height * ratio).toInt()

                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                    originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            // Compresser en JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()

            Log.e(TAG, "   Compressé: ${imageBytes.size / 1024} KB")

            // Convertir en Base64
            val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            bitmap.recycle()
            outputStream.close()

            Log.e(TAG, "   ✅ Base64: ${base64String.length} chars")

            "data:image/jpeg;base64,$base64String"

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
            null
        }
    }

    /**
     * ✅ Crée une réclamation avec photos en Base64
     */
    suspend fun createReclamation(
        commandeConcernee: String,
        complaintType: String,
        description: String,
        photoUris: List<Uri>
    ): Reclamation {
        Log.e(TAG, "════════════════════════════════")
        Log.e(TAG, "🚀 Création réclamation")
        Log.e(TAG, "   Commande: $commandeConcernee")
        Log.e(TAG, "   Type: $complaintType")
        Log.e(TAG, "   Photos: ${photoUris.size}")

        val token = tokenManager.getAccessTokenAsync()
            ?: throw Exception("Token manquant")

        // Convertir les photos en Base64
        val photosBase64 = photoUris.mapIndexedNotNull { index, uri ->
            Log.e(TAG, "📷 Photo ${index + 1}/${photoUris.size}")
            uriToBase64(uri)
        }

        Log.e(TAG, "✅ Photos converties: ${photosBase64.size}/${photoUris.size}")

        val request = CreateReclamationRequest(
            description = description.trim(),
            commandeConcernee = commandeConcernee.trim(),
            complaintType = complaintType,
            photos = photosBase64
        )

        val api = ReclamationRetrofitClient.createClient(token)

        return try {
            val result = api.createReclamation(request)
            Log.e(TAG, "✅ Succès: ${result.id}")
            Log.e(TAG, "════════════════════════════════")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
            throw e
        }
    }

    suspend fun getAllReclamations(): List<Reclamation> {
        val token = tokenManager.getAccessTokenAsync() ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.getAllReclamations()
    }

    suspend fun getMyRestaurantReclamations(): List<Reclamation> {
        val token = tokenManager.getAccessTokenAsync() ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.getMyRestaurantReclamations()
    }

    suspend fun getReclamationsByRestaurant(restaurantId: String): List<Reclamation> {
        val token = tokenManager.getAccessTokenAsync() ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.getReclamationsByRestaurant(restaurantId)
    }

    suspend fun respondToReclamation(
        id: String,
        request: RespondReclamationRequest
    ): Reclamation {
        val token = tokenManager.getAccessTokenAsync() ?: throw Exception("Token manquant")
        val api = ReclamationRetrofitClient.createClient(token)
        return api.respondToReclamation(id, request)
    }
}