# Render Backend Setup Guide

## Overview
This guide will help you configure your Android app to consume from your Render-deployed backend instead of the local development server.

---

## ✅ What I've Already Done

I've updated your codebase to support both production (Render) and local development backends. Here's what was changed:

### 1. **BaseUrlProvider.kt** - Main Configuration
- Added `USE_PRODUCTION` flag to switch between Render and local backend
- Added `PRODUCTION_BASE_URL` constant for your Render URL
- Updated logic to use production URL when enabled

### 2. **Network Security Config** - HTTPS Support
- Added support for `onrender.com` domain (HTTPS)
- Maintained local development support (HTTP)

### 3. **Updated Other Retrofit Clients**
- `EventApi.kt` - Now uses BaseUrlProvider
- `RetrofitInstance.kt` - Now uses BaseUrlProvider
- `DealsApiService.kt` - Now uses BaseUrlProvider

---

## 📋 What You Need to Do

### Step 1: Get Your Render Deployment URL

1. Go to your Render dashboard: https://dashboard.render.com
2. Find your backend service
3. Copy the **Service URL** (it should look like: `https://your-app-name.onrender.com`)
4. **Important**: Make sure it starts with `https://` (not `http://`)

### Step 2: Update BaseUrlProvider.kt

Open the file:
```
app/src/main/java/com/example/damprojectfinal/core/api/BaseUrlProvider.kt
```

Find this line (around line 12):
```kotlin
private const val PRODUCTION_BASE_URL = "https://YOUR-APP-NAME.onrender.com"
```

Replace `"https://YOUR-APP-NAME.onrender.com"` with your actual Render URL:
```kotlin
private const val PRODUCTION_BASE_URL = "https://your-actual-app-name.onrender.com"
```

**Example:**
```kotlin
private const val PRODUCTION_BASE_URL = "https://my-backend-api.onrender.com"
```

### Step 3: Enable Production Mode

In the same file, make sure this is set to `true`:
```kotlin
private const val USE_PRODUCTION = true
```

If you want to switch back to local development later, just change it to `false`.

### Step 4: (Optional) Update Network Security Config

If your Render URL uses a custom domain (not `*.onrender.com`), you may need to add it to the network security config.

Open:
```
app/src/main/res/xml/network_security_config.xml
```

If you have a custom domain, add it:
```xml
<domain-config cleartextTrafficPermitted="false">
    <domain includeSubdomains="true">onrender.com</domain>
    <domain includeSubdomains="true">your-custom-domain.com</domain> <!-- Add this if needed -->
</domain-config>
```

---

## 🔄 Switching Between Production and Local

### To Use Render (Production):
```kotlin
private const val USE_PRODUCTION = true
```

### To Use Local Development:
```kotlin
private const val USE_PRODUCTION = false
```

The app will automatically:
- Use Render URL when `USE_PRODUCTION = true`
- Use local URLs (emulator/device) when `USE_PRODUCTION = false`

---

## 🧪 Testing Your Setup

### 1. Verify the URL is Correct

After updating `PRODUCTION_BASE_URL`, check the logs when the app starts:
```
BaseUrlProvider: 🚀 Using PRODUCTION URL (Render): https://your-app.onrender.com
BaseUrlProvider: ✅ Final BASE_URL selected: https://your-app.onrender.com
```

### 2. Test API Calls

Try these actions in your app:
- Login/Register
- Fetch posts
- View reels
- Any API call

If you see network errors, check:
- ✅ Render URL is correct (no typos)
- ✅ Render service is running (check Render dashboard)
- ✅ URL starts with `https://` (not `http://`)
- ✅ No trailing slash in `PRODUCTION_BASE_URL` (I'll add it automatically)

### 3. Check Network Logs

Enable HTTP logging (already enabled) and check Logcat for:
- Request URLs (should show your Render URL)
- Response status codes
- Any SSL/certificate errors

---

## ⚠️ Common Issues

### Issue 1: SSL/Certificate Errors

**Symptom**: `javax.net.ssl.SSLHandshakeException` or certificate errors

**Solution**: 
- Make sure you're using `https://` (not `http://`)
- Check that Render service is running
- Verify network security config includes `onrender.com`

### Issue 2: Connection Timeout

**Symptom**: Requests timeout or fail to connect

**Solution**:
- Check Render dashboard - is the service running?
- Render services may spin down after inactivity - first request might be slow
- Check your internet connection
- Verify the URL is correct

### Issue 3: 404 Not Found

**Symptom**: API calls return 404

**Solution**:
- Verify the Render URL is correct
- Check that your backend routes match
- Make sure you're not including a port number (Render handles that)

### Issue 4: CORS Errors (if applicable)

**Symptom**: CORS-related errors in logs

**Solution**:
- This is usually a backend issue
- Make sure your Render backend has CORS configured to allow your app
- Check backend CORS settings in your NestJS app

---

## 📝 Quick Reference

### Files Modified:
1. ✅ `BaseUrlProvider.kt` - Main configuration
2. ✅ `network_security_config.xml` - HTTPS support
3. ✅ `EventApi.kt` - Uses BaseUrlProvider
4. ✅ `RetrofitInstance.kt` - Uses BaseUrlProvider
5. ✅ `DealsApiService.kt` - Uses BaseUrlProvider

### Files You Need to Update:
1. ⚠️ `BaseUrlProvider.kt` - Replace `PRODUCTION_BASE_URL` with your Render URL

---

## 🎯 Summary

**What to do:**
1. Get your Render URL from Render dashboard
2. Update `PRODUCTION_BASE_URL` in `BaseUrlProvider.kt`
3. Make sure `USE_PRODUCTION = true`
4. Test the app

**That's it!** Your app will now use your Render backend.

---

## 📞 Need Help?

If you encounter issues:
1. Check Render dashboard - is service running?
2. Check Logcat for error messages
3. Verify the URL format: `https://your-app.onrender.com` (no trailing slash)
4. Test the URL in a browser: `https://your-app.onrender.com/api/health` (or your health endpoint)

---

## 🔐 Security Note

- Render uses HTTPS by default (secure)
- Network security config allows HTTPS connections
- Local development still uses HTTP (for development only)
- Production mode uses HTTPS (secure)

---

**Once you provide your Render URL, I can help you update the `PRODUCTION_BASE_URL` constant directly!**

