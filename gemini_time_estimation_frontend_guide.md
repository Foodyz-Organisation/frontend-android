# Gemini Time Estimation - Frontend Implementation Guide

## Overview

Your backend now has a **Gemini AI-powered time estimation system** that calculates realistic order preparation times based on:
- Base preparation time of menu items
- Current order queue for the professional
- Kitchen efficiency patterns
- Time of day considerations

This guide explains what you need to implement on the frontend to integrate this feature.

---

## Backend API Endpoints

### 1. **Pre-Order Time Estimation** (Before Order Placement)

**Endpoint:** `POST /orders/estimate-time`

**Purpose:** Get a time estimate BEFORE the user confirms their order (e.g., show on cart/checkout screen)

**Request Body:**
```json
{
  "professionalId": "507f1f77bcf86cd799439011",
  "items": [
    {
      "menuItemId": "507f1f77bcf86cd799439012",
      "quantity": 2
    },
    {
      "menuItemId": "507f1f77bcf86cd799439013",
      "quantity": 1
    }
  ]
}
```

**Response:**
```json
{
  "estimatedMinutes": 25,
  "baseMinutes": 15,
  "queuePosition": 3,
  "currentQueueSize": 2,
  "explanation": "Estimated preparation time: 25 minutes.\n\nBased on current kitchen load with 2 orders ahead, your order will be ready in approximately 25 minutes. The base preparation time is 15 minutes, with an additional 10 minutes due to the current queue."
}
```

**When to Call:**
- When user views their cart
- On the checkout/order confirmation screen
- When user changes cart items (debounced)

---

### 2. **Order Creation** (Automatic Time Estimation)

**Endpoint:** `POST /orders`

**Purpose:** Create an order - time estimation is calculated automatically

**Request Body:**
```json
{
  "userId": "507f1f77bcf86cd799439011",
  "professionalId": "507f1f77bcf86cd799439012",
  "orderType": "takeaway",
  "items": [
    {
      "menuItemId": "507f1f77bcf86cd799439013",
      "name": "Margherita Pizza",
      "quantity": 2,
      "chosenIngredients": [],
      "chosenOptions": [],
      "calculatedPrice": 24.00
    }
  ],
  "totalPrice": 24.00,
  "paymentMethod": "CARD",
  "comment": "Extra crispy please"
}
```

**Response:**
```json
{
  "_id": "507f1f77bcf86cd799439014",
  "userId": "507f1f77bcf86cd799439011",
  "professionalId": "507f1f77bcf86cd799439012",
  "items": [...],
  "totalPrice": 24.00,
  "status": "pending",
  "basePreparationMinutes": 15,
  "estimatedPreparationMinutes": 25,
  "queuePosition": 3,
  "comment": "Extra crispy please",
  "createdAt": "2026-01-04T14:30:00.000Z"
}
```

**Note:** The backend automatically:
- Fetches menu items to get `preparationTimeMinutes`
- Queries current pending/confirmed orders
- Calls Gemini AI for intelligent estimation
- Saves time estimation fields in the order

---

## Frontend Implementation Steps

### Step 1: Create API Service Functions

#### For iOS (Swift)

```swift
// In your OrderService.swift or APIService.swift

struct TimeEstimationRequest: Codable {
    let professionalId: String
    let items: [EstimateItem]
    
    struct EstimateItem: Codable {
        let menuItemId: String
        let quantity: Int
    }
}

struct TimeEstimationResponse: Codable {
    let estimatedMinutes: Int
    let baseMinutes: Int
    let queuePosition: Int
    let currentQueueSize: Int
    let explanation: String
}

func getTimeEstimation(
    professionalId: String,
    items: [(menuItemId: String, quantity: Int)]
) async throws -> TimeEstimationResponse {
    let url = URL(string: "\(baseURL)/orders/estimate-time")!
    
    let requestBody = TimeEstimationRequest(
        professionalId: professionalId,
        items: items.map { TimeEstimationRequest.EstimateItem(
            menuItemId: $0.menuItemId,
            quantity: $0.quantity
        )}
    )
    
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.httpBody = try JSONEncoder().encode(requestBody)
    
    let (data, _) = try await URLSession.shared.data(for: request)
    return try JSONDecoder().decode(TimeEstimationResponse.self, from: data)
}
```

#### For Android (Kotlin)

```kotlin
// In your OrderApiService.kt

data class TimeEstimationRequest(
    val professionalId: String,
    val items: List<EstimateItem>
) {
    data class EstimateItem(
        val menuItemId: String,
        val quantity: Int
    )
}

data class TimeEstimationResponse(
    val estimatedMinutes: Int,
    val baseMinutes: Int,
    val queuePosition: Int,
    val currentQueueSize: Int,
    val explanation: String
)

interface OrderApiService {
    @POST("orders/estimate-time")
    suspend fun getTimeEstimation(
        @Body request: TimeEstimationRequest
    ): TimeEstimationResponse
}
```

---

### Step 2: Update Order Schema/Models

Add the new time estimation fields to your Order model:

#### iOS (Swift)

```swift
struct Order: Codable, Identifiable {
    let id: String
    let userId: String
    let professionalId: String
    let items: [OrderItem]
    let totalPrice: Double
    let orderType: String
    let status: String
    let comment: String?
    
    // ✅ NEW: Time Estimation Fields
    let basePreparationMinutes: Int?
    let estimatedPreparationMinutes: Int?
    let queuePosition: Int?
    
    let createdAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case userId, professionalId, items, totalPrice
        case orderType, status, comment
        case basePreparationMinutes
        case estimatedPreparationMinutes
        case queuePosition
        case createdAt
    }
}
```

#### Android (Kotlin)

```kotlin
data class Order(
    @SerializedName("_id") val id: String,
    val userId: String,
    val professionalId: String,
    val items: List<OrderItem>,
    val totalPrice: Double,
    val orderType: String,
    val status: String,
    val comment: String?,
    
    // ✅ NEW: Time Estimation Fields
    val basePreparationMinutes: Int?,
    val estimatedPreparationMinutes: Int?,
    val queuePosition: Int?,
    
    val createdAt: String
)
```

---

### Step 3: Display Time Estimation in Cart/Checkout Screen

Show the estimated preparation time to users before they place their order.

#### Example UI Implementation (iOS SwiftUI)

```swift
struct CartScreen: View {
    @State private var timeEstimation: TimeEstimationResponse?
    @State private var isLoadingEstimate = false
    
    var body: some View {
        VStack {
            // Cart items list
            ForEach(cartItems) { item in
                CartItemRow(item: item)
            }
            
            Divider()
            
            // ✅ Time Estimation Card
            if let estimate = timeEstimation {
                TimeEstimationCard(estimate: estimate)
            } else if isLoadingEstimate {
                ProgressView("Calculating preparation time...")
            }
            
            // Checkout button
            Button("Place Order") {
                placeOrder()
            }
        }
        .onAppear {
            fetchTimeEstimation()
        }
        .onChange(of: cartItems) { _ in
            fetchTimeEstimation()
        }
    }
    
    func fetchTimeEstimation() {
        guard !cartItems.isEmpty else { return }
        
        isLoadingEstimate = true
        
        Task {
            do {
                let items = cartItems.map { (menuItemId: $0.menuItemId, quantity: $0.quantity) }
                timeEstimation = try await orderService.getTimeEstimation(
                    professionalId: currentProfessionalId,
                    items: items
                )
            } catch {
                print("Failed to fetch time estimation: \(error)")
            }
            isLoadingEstimate = false
        }
    }
}

struct TimeEstimationCard: View {
    let estimate: TimeEstimationResponse
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "clock.fill")
                    .foregroundColor(.orange)
                Text("Estimated Preparation Time")
                    .font(.headline)
            }
            
            Text("\(estimate.estimatedMinutes) minutes")
                .font(.title2)
                .fontWeight(.bold)
            
            if estimate.currentQueueSize > 0 {
                Text("Position in queue: #\(estimate.queuePosition)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Text(estimate.explanation)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(3)
        }
        .padding()
        .background(Color.orange.opacity(0.1))
        .cornerRadius(12)
    }
}
```

#### Example UI Implementation (Android Jetpack Compose)

```kotlin
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    professionalId: String,
    viewModel: CartViewModel
) {
    val timeEstimation by viewModel.timeEstimation.collectAsState()
    val isLoadingEstimate by viewModel.isLoadingEstimate.collectAsState()
    
    LaunchedEffect(cartItems) {
        if (cartItems.isNotEmpty()) {
            viewModel.fetchTimeEstimation(professionalId, cartItems)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Cart items
        LazyColumn {
            items(cartItems) { item ->
                CartItemRow(item = item)
            }
        }
        
        Divider()
        
        // ✅ Time Estimation Card
        when {
            isLoadingEstimate -> {
                CircularProgressIndicator()
            }
            timeEstimation != null -> {
                TimeEstimationCard(estimation = timeEstimation!!)
            }
        }
        
        // Checkout button
        Button(
            onClick = { viewModel.placeOrder() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Place Order")
        }
    }
}

@Composable
fun TimeEstimationCard(estimation: TimeEstimationResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estimated Preparation Time",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${estimation.estimatedMinutes} minutes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (estimation.currentQueueSize > 0) {
                Text(
                    text = "Position in queue: #${estimation.queuePosition}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = estimation.explanation,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

---

### Step 4: Display Time Estimation in Order History/Details

Show the estimated time for completed orders.

```swift
// iOS Example
struct OrderDetailView: View {
    let order: Order
    
    var body: some View {
        VStack(alignment: .leading) {
            // Order items, status, etc.
            
            if let estimatedMinutes = order.estimatedPreparationMinutes {
                HStack {
                    Image(systemName: "clock")
                    Text("Estimated: \(estimatedMinutes) min")
                }
                
                if let queuePosition = order.queuePosition {
                    Text("Queue position: #\(queuePosition)")
                        .font(.caption)
                }
            }
        }
    }
}
```

---

## Key Implementation Notes

### 1. **Menu Items Must Have `preparationTimeMinutes`**

Ensure your menu items have this field set in the database:

```javascript
// Example menu item
{
  "_id": "...",
  "name": "Margherita Pizza",
  "price": 12.00,
  "preparationTimeMinutes": 15,  // ✅ Required for time estimation
  // ... other fields
}
```

### 2. **Debounce Time Estimation Calls**

When users modify their cart, debounce the API call to avoid excessive requests:

```swift
// iOS Example with Combine
private var cancellables = Set<AnyCancellable>()

$cartItems
    .debounce(for: .milliseconds(500), scheduler: RunLoop.main)
    .sink { items in
        fetchTimeEstimation()
    }
    .store(in: &cancellables)
```

### 3. **Handle Errors Gracefully**

If the time estimation fails, show a fallback message:

```swift
if timeEstimation == nil {
    Text("Preparation time will be confirmed after order placement")
        .font(.caption)
        .foregroundColor(.secondary)
}
```

### 4. **Real-Time Updates (Optional Enhancement)**

For professional accounts, you could implement WebSocket updates to notify users when their queue position changes or when their order starts being prepared.

---

## Testing Checklist

- [ ] Time estimation shows correctly in cart/checkout screen
- [ ] Estimation updates when cart items change
- [ ] Order creation includes time estimation fields
- [ ] Order history displays estimated time
- [ ] Handles empty queue (no orders ahead)
- [ ] Handles busy queue (multiple orders ahead)
- [ ] Shows queue position correctly
- [ ] Gracefully handles API errors
- [ ] Works with different order types (eat-in, takeaway, delivery)

---

## Summary

**What You Need to Implement:**

1. **API Integration:**
   - Add `POST /orders/estimate-time` endpoint call
   - Update order creation to handle new time fields

2. **UI Components:**
   - Time estimation card in cart/checkout screen
   - Time display in order history/details
   - Loading states and error handling

3. **Data Models:**
   - Add `basePreparationMinutes`, `estimatedPreparationMinutes`, and `queuePosition` to Order model
   - Create `TimeEstimationRequest` and `TimeEstimationResponse` models

4. **User Experience:**
   - Show estimated time before order placement
   - Display queue position
   - Show AI-generated explanation
   - Update estimation when cart changes

The backend handles all the complex logic (Gemini AI integration, queue analysis, fallback calculations), so your frontend just needs to call the API and display the results beautifully! 🎉
