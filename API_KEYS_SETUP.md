# 🔐 Configuration des Clés API

## ⚠️ IMPORTANT: Ne jamais commit les clés API!

Ce projet utilise `local.properties` pour stocker les clés API de manière sécurisée.

### Configuration

1. **Copier le fichier exemple:**
   ```bash
   cp local.properties.example local.properties
   ```

2. **Obtenir votre clé Stripe:**
   - Aller sur https://dashboard.stripe.com/test/apikeys
   - Copier votre **Publishable key** (commence par `pk_test_`)

3. **Éditer `local.properties`:**
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   STRIPE_PUBLISHABLE_KEY=pk_test_VOTRE_CLE_ICI
   ```

4. **Sync Gradle et rebuild le projet**

### Sécurité

✅ **À FAIRE:**
- Utiliser la clé **PUBLISHABLE** (`pk_test_...` ou `pk_live_...`)
- Garder `local.properties` dans `.gitignore`
- Partager `local.properties.example` avec l'équipe

❌ **NE JAMAIS FAIRE:**
- Commit `local.properties` sur Git
- Utiliser la clé **SECRET** (`sk_test_...`) dans l'app Android
- Hardcoder les clés dans le code

### Vérification

Vérifiez que `local.properties` est bien ignoré par Git:
```bash
git check-ignore local.properties
# Devrait afficher: local.properties
```

Si ce n'est pas le cas, vérifiez votre `.gitignore`:
```
# Android local properties (should already be there)
local.properties
```

### Pour les nouveaux développeurs

Quand un nouveau développeur clone le projet:

1. Copier `local.properties.example` → `local.properties`
2. Ajouter sa propre clé Stripe
3. Sync Gradle
4. Run l'app

### Cartes de test Stripe

Pour tester les paiements:
- **Succès:** `4242 4242 4242 4242`
- **Déclinée:** `4000 0000 0000 0002`
- Expiry: `12/2025`
- CVV: `123`
- Nom: n'importe quel nom

---

**Documentation Stripe:** https://stripe.com/docs/testing

