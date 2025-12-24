# Foodyz Android App - Setup Guide

## 🔐 Security & Configuration

This project contains sensitive API keys and credentials that are **NOT included in version control**.

### Required Setup Files

Before building the project, you need to create these files:

#### 1. Firebase Configuration
**File:** `app/google-services.json`

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Download `google-services.json`
4. Place it in `/app/google-services.json`

See `app/google-services.json.example` for the structure.

#### 2. API Keys
**File:** `local.properties` (in project root)

```properties
STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_KEY_HERE
```

See `local.properties.example` for the template.

## 🚀 Quick Start

1. Clone the repository
2. Create `app/google-services.json` from Firebase Console
3. Create `local.properties` with your API keys
4. Open in Android Studio
5. Build and run

## 📱 Features

- User authentication (Email/Password, Google Sign-In)
- Real-time chat with push notifications (FCM)
- Food posts and reels
- Restaurant menu browsing
- Order management
- Deals and promotions
- Payment integration (Stripe)

## 🔧 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Networking:** Ktor, Retrofit
- **Image Loading:** Coil
- **Real-time:** Socket.IO
- **Push Notifications:** Firebase Cloud Messaging
- **Payments:** Stripe Android SDK

## ⚠️ Important Notes

- Never commit `google-services.json` or `local.properties` to Git
- These files are protected by `.gitignore`
- Each developer needs their own Firebase project configuration

## 📄 License

[Your License Here]
