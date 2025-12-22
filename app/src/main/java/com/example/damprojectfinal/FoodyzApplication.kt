package com.example.damprojectfinal

import android.app.Application
import com.stripe.android.PaymentConfiguration

class FoodyzApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()

        val stripePublishableKey = com.example.damprojectfinal.BuildConfig.STRIPE_PUBLISHABLE_KEY
        
        if (stripePublishableKey.isEmpty()) {
            android.util.Log.e("FoodyzApplication", "❌ ERROR: STRIPE_PUBLISHABLE_KEY is not set in local.properties!")
            android.util.Log.e("FoodyzApplication", "  Add this line to local.properties:")
            android.util.Log.e("FoodyzApplication", "  STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here")
            throw IllegalStateException("STRIPE_PUBLISHABLE_KEY not configured")
        }
        
        PaymentConfiguration.init(
            context = this,
            publishableKey = stripePublishableKey
        )
        
        android.util.Log.d("FoodyzApplication", "✅ Stripe SDK initialized")
        android.util.Log.d("FoodyzApplication", "🔑 Publishable Key: ${stripePublishableKey.take(20)}...")
    }
}

