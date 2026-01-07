# Professional Signup Formulary Implementation Guide - Android to iOS

## 📋 Overview

This document explains how the professional signup formulary (form submission) is implemented in the Android app and provides a complete guide for implementing the same functionality in iOS using Xcode.

## 🎯 What is the "Formulary"?

The "formulary" refers to the **complete form submission process** for professional (restaurant) signup, which includes:
1. Multi-step form navigation (3 steps)
2. Image upload and compression
3. Base64 encoding
4. API request submission
5. Response handling and validation
6. Error handling and user feedback

---

## 📱 Android Implementation Overview

### Architecture

The Android implementation follows **MVVM (Model-View-ViewModel)** architecture:

```
┌─────────────────┐
│   UI Layer      │  SignupScreenPro.kt (Compose UI)
│   (View)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ViewModel      │  ProSignupViewModel.kt (State Management)
│  (Logic)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  API Service    │  AuthApiService.kt (Network Calls)
│  (Data)         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Backend API   │  POST /auth/signup/professional
└─────────────────┘
```

### Key Components

1. **ViewModel** (`ProSignupViewModel.kt`) - Manages form state and business logic
2. **UI Screen** (`SignupScreenPro.kt`) - Multi-step form UI
3. **Image Compressor** (`ImageCompressor.kt`) - Compresses images to < 800 KB
4. **API Service** (`AuthApiService.kt`) - Handles HTTP requests
5. **DTOs** (`ProSingupRequest.kt`) - Data transfer objects

---

## 🔄 Complete Form Submission Flow

### Step-by-Step Process

```
1. User fills Step 1: Personal Details
   ├─ Email
   ├─ Password
   ├─ Confirm Password
   └─ Full Name (Restaurant Name)
   ↓
2. User clicks "Next" → Validates Step 1
   ├─ Email format validation
   ├─ Password strength validation
   └─ Password match validation
   ↓
3. User reaches Step 2: Business Verification
   ├─ Optional: Upload Restaurant Permit Image
   ├─ Image compression (if uploaded)
   └─ Base64 encoding
   ↓
4. User clicks "Next" → Proceeds to Step 3
   ↓
5. User fills Step 3: Location (Optional)
   ├─ Select location on map
   └─ Confirm location
   ↓
6. User clicks "Complete Registration"
   ↓
7. ViewModel.signup() is called
   ├─ Builds ProfessionalSignupRequest
   ├─ Includes base64 image (if uploaded)
   ├─ Includes location data (if provided)
   └─ Sends POST request to backend
   ↓
8. Backend Processing (5-30 seconds)
   ├─ Validates document (if image provided)
   ├─ Performs OCR extraction
   ├─ Extracts permit number
   └─ Creates professional account
   ↓
9. Response Handling
   ├─ Success: Shows permit number, navigates to login
   └─ Error: Shows error message, navigates back to appropriate step
```

---

## 📦 Android Implementation Details

### 1. ViewModel State Management

**File**: `ProSignupVeiwModel.kt`

```kotlin
// Form fields
val email = mutableStateOf("")
val password = mutableStateOf("")
val confirmPassword = mutableStateOf("")
val fullName = mutableStateOf("")

// Image upload states
val permitImageUri = mutableStateOf<Uri?>(null)
val permitImageBase64 = mutableStateOf<String?>(null)
val isCompressingImage = mutableStateOf(false)
val permitFileName = mutableStateOf<String?>(null)
val permitFileSize = mutableStateOf<String?>(null)

// Location data
val selectedLocation = mutableStateOf<LocationData?>(null)

// Multi-step flow
val currentStep = mutableStateOf(1) // 1, 2, or 3

// UI states
val isLoading = mutableStateOf(false)
val errorMessage = mutableStateOf<String?>(null)
val isSignupSuccess = mutableStateOf(false)
val permitNumberExtracted = mutableStateOf<String?>(null)
val showSuccessDialog = mutableStateOf(false)
```

### 2. Image Processing Flow

**Function**: `convertImageToBase64(context: Context, uri: Uri)`

```kotlin
fun convertImageToBase64(context: Context, uri: Uri) {
    viewModelScope.launch {
        try {
            isCompressingImage.value = true
            
            // Extract file metadata
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                // Get file name and size
                permitFileName.value = it.getString(nameIndex)
            }
            
            // Compress image in background thread
            val compressedBase64 = withContext(Dispatchers.IO) {
                ImageCompressor.compressImageToBase64(context, uri)
            }
            
            // Update state
            permitImageBase64.value = compressedBase64
            permitImageUri.value = uri
            
        } catch (e: Exception) {
            errorMessage.value = "Failed to process image. Please try another photo."
            clearPermitImage()
        } finally {
            isCompressingImage.value = false
        }
    }
}
```

**Key Points**:
- Runs on background thread (`Dispatchers.IO`)
- Shows loading state during compression
- Handles errors gracefully
- Stores base64 string with data URI prefix: `"data:image/jpeg;base64,{base64String}"`

### 3. Form Submission

**Function**: `signup()`

```kotlin
fun signup() {
    if (email.value.isBlank() || password.value.isBlank() || fullName.value.isBlank()) {
        errorMessage.value = "Please fill in all required fields."
        return
    }

    isLoading.value = true
    errorMessage.value = null

    viewModelScope.launch {
        try {
            // Build locations array if location is selected
            val locations = selectedLocation.value?.let { location ->
                listOf(
                    LocationDto(
                        name = null,
                        address = location.name.ifEmpty { null },
                        lat = location.latitude,
                        lon = location.longitude
                    )
                )
            }

            // Build request
            val request = ProfessionalSignupRequest(
                email = email.value,
                password = password.value,
                fullName = fullName.value,
                licenseImage = permitImageBase64.value, // Base64 image
                locations = locations
            )

            // Call API
            val response = authApiService.professionalSignup(request)
            
            // Handle success
            permitNumberExtracted.value = response.permitNumber
            isSignupSuccess.value = true
            showSuccessDialog.value = true
            
            // Navigate to login after delay
            delay(3000)
            navController.navigate(AuthRoutes.LOGIN) {
                popUpTo(AuthRoutes.PRO_SIGNUP) { inclusive = true }
            }

        } catch (e: Exception) {
            // Handle errors (see Error Handling section)
            handleSignupError(e)
        } finally {
            isLoading.value = false
        }
    }
}
```

### 4. Request DTO Structure

**File**: `ProSingupRequest.kt`

```kotlin
@Serializable
data class ProfessionalSignupRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val licenseImage: String? = null,  // Base64: "data:image/jpeg;base64,..."
    val linkedUserId: String? = null,
    val locations: List<LocationDto>? = null
)

@Serializable
data class LocationDto(
    val name: String? = null,
    val address: String? = null,
    val lat: Double,
    val lon: Double
)
```

### 5. API Service Implementation

**File**: `AuthApiService.kt`

```kotlin
suspend fun professionalSignup(request: ProfessionalSignupRequest): ProfessionalSignupResponse {
    val url = "$BASE_URL/auth/signup/professional"
    
    val response: HttpResponse = client.post(url) {
        contentType(ContentType.Application.Json)
        setBody(request)
        // Extended timeout for OCR processing
        timeout {
            requestTimeoutMillis = 30000 // 30 seconds
        }
    }
    
    if (!response.status.isSuccess()) {
        // Parse error response
        val errorBody = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val jsonObj = json.parseToJsonElement(errorBody).jsonObject
        
        val errorMsg = jsonObj["message"]?.jsonPrimitive?.content 
            ?: jsonObj["reason"]?.jsonPrimitive?.content 
            ?: "Signup failed"
        
        throw Exception(errorMsg)
    }
    
    return response.body<ProfessionalSignupResponse>()
}
```

**Key Configuration**:
- **Endpoint**: `POST /auth/signup/professional`
- **Content-Type**: `application/json`
- **Timeout**: 30 seconds (for OCR processing)
- **Error Handling**: Parses JSON error responses

### 6. Response DTO Structure

```kotlin
@Serializable
data class ProfessionalSignupResponse(
    val message: String? = null,
    val professionalId: String? = null,
    val permitNumber: String? = null,  // Extracted from OCR
    val confidence: String? = null,     // OCR confidence: high/medium/low
    val id: String? = null,
    val role: String? = null,
    val token: String? = null
)
```

### 7. Error Handling

```kotlin
private fun handleSignupError(e: Exception) {
    val errorMsg = e.message ?: "Professional signup failed."
    
    // Check if it's a permit validation error
    val isPermitValidationError = errorMsg.contains("does not appear to be a Tunisian restaurant", ignoreCase = true) ||
                                 errorMsg.contains("Could not extract permit number", ignoreCase = true) ||
                                 errorMsg.contains("permit", ignoreCase = true) ||
                                 errorMsg.contains("validation", ignoreCase = true) ||
                                 errorMsg.contains("Image quality too low", ignoreCase = true)
    
    when {
        isPermitValidationError -> {
            errorMessage.value = "We were not able to validate the document provided, please provide another one"
            clearPermitImage()
            currentStep.value = 2 // Go back to Step 2
        }
        errorMsg.contains("email", ignoreCase = true) -> {
            errorMessage.value = "This mail already exist"
            currentStep.value = 1 // Go back to Step 1
        }
        else -> {
            errorMessage.value = errorMsg.replace("Technical Details: ", "")
        }
    }
}
```

---

## 🍎 iOS Implementation Guide

### Step 1: Create ViewModel (ObservableObject)

**File**: `ProSignupViewModel.swift`

```swift
import SwiftUI
import Combine

class ProSignupViewModel: ObservableObject {
    // MARK: - Form Fields
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var confirmPassword: String = ""
    @Published var fullName: String = ""
    
    // MARK: - Image Upload States
    @Published var permitImage: UIImage? = nil
    @Published var permitImageBase64: String? = nil
    @Published var isCompressingImage: Bool = false
    @Published var permitFileName: String? = nil
    @Published var permitFileSize: String? = nil
    
    // MARK: - Location Data
    @Published var selectedLocation: LocationData? = nil
    
    // MARK: - Multi-step Flow
    @Published var currentStep: Int = 1 // 1, 2, or 3
    
    // MARK: - UI States
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var isSignupSuccess: Bool = false
    @Published var permitNumberExtracted: String? = nil
    @Published var showSuccessDialog: Bool = false
    
    // MARK: - Dependencies
    private let authApiService: AuthApiService
    
    init(authApiService: AuthApiService) {
        self.authApiService = authApiService
    }
    
    // MARK: - Step Validation
    func canProceedFromStep1() -> Bool {
        // Email validation
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format:"SELF MATCHES %@", emailRegex)
        let isEmailValid = emailPredicate.evaluate(with: email)
        
        // Password validation (at least 8 chars, uppercase, lowercase, number, special char)
        let passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        let passwordPredicate = NSPredicate(format:"SELF MATCHES %@", passwordRegex)
        let isPasswordValid = passwordPredicate.evaluate(with: password)
        
        return !fullName.isEmpty &&
               isEmailValid &&
               isPasswordValid &&
               password == confirmPassword
    }
    
    func canProceedFromStep2() -> Bool {
        // Permit image is optional
        return true
    }
    
    func canProceedFromStep3() -> Bool {
        // Location is optional
        return true
    }
    
    // MARK: - Image Processing
    func convertImageToBase64(_ image: UIImage, fileName: String? = nil) {
        isCompressingImage = true
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            do {
                print("📸 Image selected: \(fileName ?? "unknown")")
                
                DispatchQueue.main.async {
                    self?.permitFileName = fileName
                }
                
                // Compress image
                let compressedBase64 = try ImageCompressor.compressImageToBase64(image)
                
                // Calculate compressed size
                if let data = Data(base64Encoded: compressedBase64.replacingOccurrences(of: "data:image/jpeg;base64,", with: "")) {
                    let sizeKB = data.count / 1024
                    let formattedSize = self?.formatFileSize(sizeKB * 1024) ?? "\(sizeKB) KB"
                    
                    DispatchQueue.main.async {
                        self?.permitImageBase64 = compressedBase64
                        self?.permitImage = image
                        self?.permitFileSize = formattedSize
                        self?.isCompressingImage = false
                        print("✅ Image compressed and ready! Size: \(formattedSize)")
                    }
                }
                
            } catch {
                DispatchQueue.main.async {
                    self?.errorMessage = "Failed to process image. Please try another photo."
                    self?.clearPermitImage()
                    print("❌ Image compression failed: \(error.localizedDescription)")
                }
            }
        }
    }
    
    func clearPermitImage() {
        permitImage = nil
        permitImageBase64 = nil
        permitFileName = nil
        permitFileSize = nil
        isCompressingImage = false
    }
    
    private func formatFileSize(_ size: Int) -> String {
        let kb = Double(size) / 1024.0
        let mb = kb / 1024.0
        
        if mb >= 1.0 {
            return String(format: "%.2f MB", mb)
        } else if kb >= 1.0 {
            return String(format: "%.2f KB", kb)
        } else {
            return "\(size) B"
        }
    }
    
    // MARK: - Navigation
    func nextStep() {
        switch currentStep {
        case 1:
            if canProceedFromStep1() {
                errorMessage = nil
                currentStep = 2
            } else {
                // Set appropriate error message
                if fullName.isEmpty {
                    errorMessage = "Please enter your full name"
                } else if !isValidEmail(email) {
                    errorMessage = "Please enter a valid email address"
                } else if !isValidPassword(password) {
                    errorMessage = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
                } else if password != confirmPassword {
                    errorMessage = "Passwords do not match"
                }
            }
        case 2:
            errorMessage = nil
            currentStep = 3
        case 3:
            // Final step - trigger signup
            signup()
        default:
            break
        }
    }
    
    func previousStep() {
        if currentStep > 1 {
            errorMessage = nil
            currentStep -= 1
        }
    }
    
    // MARK: - Form Submission
    func signup() {
        guard !email.isEmpty, !password.isEmpty, !fullName.isEmpty else {
            errorMessage = "Please fill in all required fields."
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        // Build locations array if location is selected
        let locations = selectedLocation.map { location in
            LocationDto(
                name: nil,
                address: location.name.isEmpty ? nil : location.name,
                lat: location.latitude,
                lon: location.longitude
            )
        }
        
        let request = ProfessionalSignupRequest(
            email: email,
            password: password,
            fullName: fullName,
            licenseImage: permitImageBase64,
            linkedUserId: nil,
            locations: locations.isEmpty ? nil : locations
        )
        
        Task {
            do {
                print("📤 Sending signup request to backend...")
                let response = try await authApiService.professionalSignup(request: request)
                print("✅ Signup response received!")
                
                await MainActor.run {
                    permitNumberExtracted = response.permitNumber
                    isSignupSuccess = true
                    showSuccessDialog = true
                    isLoading = false
                    
                    // Delay navigation to show success animation
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                        // Navigate to login screen (use your navigation coordinator)
                        // navigationCoordinator.navigateToLogin()
                    }
                }
                
            } catch {
                await MainActor.run {
                    handleSignupError(error)
                    isLoading = false
                }
            }
        }
    }
    
    // MARK: - Error Handling
    private func handleSignupError(_ error: Error) {
        let errorMsg = error.localizedDescription
        print("❌ Professional signup failed: \(errorMsg)")
        
        // Check if it's a permit validation error
        let isPermitValidationError = errorMsg.localizedCaseInsensitiveContains("tunisian restaurant") ||
                                     errorMsg.localizedCaseInsensitiveContains("extract permit number") ||
                                     errorMsg.localizedCaseInsensitiveContains("permit") ||
                                     errorMsg.localizedCaseInsensitiveContains("validation") ||
                                     errorMsg.localizedCaseInsensitiveContains("Image quality too low") ||
                                     errorMsg.localizedCaseInsensitiveContains("no readable text") ||
                                     errorMsg.localizedCaseInsensitiveContains("clearer photo")
        
        if isPermitValidationError {
            errorMessage = "We were not able to validate the document provided, please provide another one"
            clearPermitImage()
            currentStep = 2 // Go back to Step 2
        } else if errorMsg.localizedCaseInsensitiveContains("email") ||
                  errorMsg.localizedCaseInsensitiveContains("mail") {
            errorMessage = "This mail already exist"
            currentStep = 1 // Go back to Step 1
        } else {
            errorMessage = errorMsg.replacingOccurrences(of: "Technical Details: ", with: "")
        }
    }
    
    // MARK: - Validation Helpers
    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format:"SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: email)
    }
    
    private func isValidPassword(_ password: String) -> Bool {
        let passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        let passwordPredicate = NSPredicate(format:"SELF MATCHES %@", passwordRegex)
        return passwordPredicate.evaluate(with: password)
    }
}
```

### Step 2: Create API Service

**File**: `AuthApiService.swift`

```swift
import Foundation

// MARK: - Request Models
struct ProfessionalSignupRequest: Codable {
    let email: String
    let password: String
    let fullName: String
    let licenseImage: String?  // Base64: "data:image/jpeg;base64,..."
    let linkedUserId: String?
    let locations: [LocationDto]?
}

struct LocationDto: Codable {
    let name: String?
    let address: String?
    let lat: Double
    let lon: Double
}

// MARK: - Response Models
struct ProfessionalSignupResponse: Codable {
    let message: String?
    let professionalId: String?
    let permitNumber: String?     // Extracted license number
    let confidence: String?       // OCR confidence: high/medium/low
    let id: String?
    let role: String?
    let token: String?
}

struct ErrorResponse: Codable {
    let statusCode: Int?
    let message: String?
    let reason: String?
    let error: String?
}

// MARK: - API Service
class AuthApiService {
    private let baseURL: String = "YOUR_BASE_URL" // Replace with actual base URL
    private let session: URLSession
    
    init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30.0 // 30 seconds for OCR processing
        configuration.timeoutIntervalForResource = 60.0
        self.session = URLSession(configuration: configuration)
    }
    
    func professionalSignup(request: ProfessionalSignupRequest) async throws -> ProfessionalSignupResponse {
        let url = URL(string: "\(baseURL)/auth/signup/professional")!
        
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Add authorization header if needed
        // urlRequest.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let encoder = JSONEncoder()
        urlRequest.httpBody = try encoder.encode(request)
        
        print("🔄 Professional signup request to: \(url.absoluteString)")
        print("📧 Email: \(request.email), Has image: \(request.licenseImage != nil)")
        
        let (data, response) = try await session.data(for: urlRequest)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "AuthApiService", code: -1,
                         userInfo: [NSLocalizedDescriptionKey: "Invalid response"])
        }
        
        print("📡 Response status: \(httpResponse.statusCode)")
        
        guard (200...299).contains(httpResponse.statusCode) else {
            // Parse error response
            let errorBody = String(data: data, encoding: .utf8) ?? ""
            print("❌ Professional signup failed with status \(httpResponse.statusCode)")
            print("❌ Error body: \(errorBody)")
            
            if let errorResponse = try? JSONDecoder().decode(ErrorResponse.self, from: data) {
                let errorMsg = errorResponse.reason ?? errorResponse.message ?? errorResponse.error ?? "Signup failed"
                throw NSError(domain: "AuthApiService", code: httpResponse.statusCode,
                             userInfo: [NSLocalizedDescriptionKey: errorMsg])
            }
            
            throw NSError(domain: "AuthApiService", code: httpResponse.statusCode,
                         userInfo: [NSLocalizedDescriptionKey: "Signup failed: \(httpResponse.statusCode)"])
        }
        
        let decoder = JSONDecoder()
        let successResponse = try decoder.decode(ProfessionalSignupResponse.self, from: data)
        print("✅ Professional signup successful! Permit: \(successResponse.permitNumber ?? "N/A")")
        
        return successResponse
    }
}
```

### Step 3: Image Compression Utility

**File**: `ImageCompressor.swift`

*(See the existing `OCR_LICENSE_EXTRACTION_IOS_IMPLEMENTATION_GUIDE.md` for the complete ImageCompressor implementation)*

### Step 4: SwiftUI View Implementation

**File**: `ProSignupView.swift`

```swift
import SwiftUI

struct ProSignupView: View {
    @StateObject private var viewModel: ProSignupViewModel
    @State private var showImagePicker = false
    @State private var selectedImage: UIImage? = nil
    
    init(authApiService: AuthApiService) {
        _viewModel = StateObject(wrappedValue: ProSignupViewModel(authApiService: authApiService))
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Progress Indicator
            StepProgressIndicator(currentStep: viewModel.currentStep, totalSteps: 3)
                .padding()
            
            // Form Content
            ScrollView {
                VStack(spacing: 24) {
                    switch viewModel.currentStep {
                    case 1:
                        Step1PersonalDetailsView(viewModel: viewModel)
                    case 2:
                        Step2BusinessVerificationView(
                            viewModel: viewModel,
                            showImagePicker: $showImagePicker,
                            selectedImage: $selectedImage
                        )
                    case 3:
                        Step3LocationView(viewModel: viewModel)
                    default:
                        EmptyView()
                    }
                }
                .padding()
            }
            
            // Navigation Buttons
            VStack(spacing: 16) {
                if viewModel.currentStep == 3 {
                    // Final step - Show Login and Create Account buttons
                    Button("Login") {
                        // Navigate to login
                    }
                    .buttonStyle(PrimaryButtonStyle())
                    
                    Button("Create Account") {
                        viewModel.signup()
                    }
                    .buttonStyle(SecondaryButtonStyle())
                } else {
                    // Show Next button
                    Button("Next") {
                        viewModel.nextStep()
                    }
                    .buttonStyle(PrimaryButtonStyle())
                    .disabled(!canProceed())
                }
            }
            .padding()
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(selectedImage: $selectedImage, isPresented: $showImagePicker)
        }
        .onChange(of: selectedImage) { newImage in
            if let image = newImage {
                viewModel.convertImageToBase64(image, fileName: viewModel.permitFileName)
            }
        }
        .alert("Error", isPresented: .constant(viewModel.errorMessage != nil)) {
            Button("OK") {
                viewModel.errorMessage = nil
            }
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
        .alert("Success", isPresented: $viewModel.showSuccessDialog) {
            Button("OK") {
                // Navigate to login
            }
        } message: {
            if let permitNumber = viewModel.permitNumberExtracted {
                Text("Permit Number: \(permitNumber)")
            } else {
                Text("Account created successfully!")
            }
        }
    }
    
    private func canProceed() -> Bool {
        switch viewModel.currentStep {
        case 1:
            return viewModel.canProceedFromStep1()
        case 2:
            return viewModel.canProceedFromStep2()
        case 3:
            return viewModel.canProceedFromStep3()
        default:
            return false
        }
    }
}
```

---

## 🔌 API Request/Response Format

### Request

**Endpoint**: `POST /auth/signup/professional`

**Headers**:
```
Content-Type: application/json
```

**Body**:
```json
{
  "email": "restaurant@example.com",
  "password": "SecurePass123!",
  "fullName": "My Restaurant",
  "licenseImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "linkedUserId": null,
  "locations": [
    {
      "name": null,
      "address": "Tunis, Tunisia",
      "lat": 36.8065,
      "lon": 10.1815
    }
  ]
}
```

### Success Response (200 OK)

```json
{
  "message": "Professional account created successfully",
  "professionalId": "123e4567-e89b-12d3-a456-426614174000",
  "permitNumber": "12345",
  "confidence": "high",
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "role": "professional",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Error Response (400/422)

```json
{
  "statusCode": 400,
  "message": "Could not extract permit number from image",
  "reason": "Image quality too low or no readable text found"
}
```

---

## ✅ Implementation Checklist

### Core Functionality
- [ ] ViewModel with ObservableObject
- [ ] Multi-step form navigation (3 steps)
- [ ] Form field validation
- [ ] Image picker integration
- [ ] Image compression utility
- [ ] Base64 encoding
- [ ] API service with extended timeout
- [ ] Request/Response models
- [ ] Error handling
- [ ] Success dialog with permit number

### UI Components
- [ ] Step progress indicator
- [ ] Step 1: Personal Details form
- [ ] Step 2: Business Verification (image upload)
- [ ] Step 3: Location selection
- [ ] Loading indicators
- [ ] Error message display
- [ ] Success dialog
- [ ] Navigation buttons (Next/Previous)

### Testing
- [ ] Test form validation
- [ ] Test image compression
- [ ] Test API integration
- [ ] Test error scenarios
- [ ] Test success flow
- [ ] Test navigation between steps

---

## 📝 Key Differences: Android vs iOS

| Feature | Android | iOS |
|---------|---------|-----|
| **State Management** | `mutableStateOf` (Compose) | `@Published` (Combine) |
| **Image Picker** | `ActivityResultContracts.GetContent()` | `PHPickerViewController` |
| **Image Compression** | `Bitmap` + `BitmapFactory` | `UIImage` + `UIImageJPEGRepresentation` |
| **Base64 Encoding** | `android.util.Base64` | `Data.base64EncodedString()` |
| **Network** | Ktor Client | URLSession |
| **Async Operations** | `viewModelScope.launch` | `async/await` or `Task` |
| **UI Framework** | Jetpack Compose | SwiftUI |

---

## 🎯 Summary

The professional signup formulary implementation involves:

1. **Multi-step form** with validation at each step
2. **Image upload and compression** to < 800 KB
3. **Base64 encoding** with data URI prefix
4. **API submission** with extended timeout (30 seconds)
5. **Response handling** for success and error cases
6. **Error recovery** by navigating back to appropriate step

The iOS implementation should mirror the Android flow while using iOS-native APIs and SwiftUI patterns.

---

## 🔗 Related Documentation

- `OCR_LICENSE_EXTRACTION_IOS_IMPLEMENTATION_GUIDE.md` - Detailed OCR implementation
- `BUSINESS_VERIFICATION_IMAGE_UPLOAD_IMPLEMENTATION.md` - Image upload details
- `IMAGE_COMPRESSION_IMPLEMENTATION.md` - Compression algorithm details

