# 🧪 Guide de Test - Spam Detection Android

## 🚀 Tests rapides

### Prérequis
- ✅ Backend NestJS démarré sur `http://localhost:3000`
- ✅ Application Android installée sur l'émulateur/appareil
- ✅ Service FastAPI (optionnel) sur `http://localhost:8000`

---

## Test 1: Message normal ✅

### Action
1. Ouvrir une conversation
2. Envoyer: **"Bonjour, comment allez-vous ?"**

### Résultat attendu
- Message affiché normalement
- **Aucun badge** visible
- Texte en blanc (messages reçus) ou gradient jaune (messages envoyés)

---

## Test 2: Message spam modéré (< 90%) ⚠️

### Action
Envoyer: **"Click here for amazing deals"**

### Résultat attendu
- Message affiché avec contenu
- Badge **⚠️ Spam (XX%)** visible
- Pourcentage entre 70% et 90%
- Couleur du badge: **Rouge** (#FF6B6B) pour messages reçus

---

## Test 3: Message spam élevé (> 90%) 🚫

### Action
Envoyer: **"URGENT!!! Click here to win $1000000 NOW!!!"**

### Résultat attendu
- Message **BLOQUÉ** par le backend
- Ne s'affiche PAS dans la conversation
- Notification WebSocket "Message blocked as spam" (si implémenté)

---

## Test 4: Bad words uniquement 🛑

### Action
Envoyer: **"damn this is annoying"**

### Résultat attendu
- Message affiché: **"**** this is annoying"**
- Badge **🛑 Message modéré** visible
- **PAS** de badge spam
- Couleur: Jaune (#f5c42e) pour messages reçus

---

## Test 5: Bad words + Spam 🛑⚠️

### Action
Envoyer: **"damn click here NOW for prizes"**

### Résultat attendu
- Message affiché: **"**** click here NOW for prizes"**
- **DEUX badges** visibles:
  - **🛑 Message modéré**
  - **⚠️ Spam (XX%)**
- Les deux badges alignés horizontalement

---

## Test 6: Messages reçus vs envoyés 🔄

### Messages reçus (blancs)
```
┌──────────────────────────────┐
│ CLICK HERE NOW!!!            │
└──────────────────────────────┘
⚠️ Spam (85%) [Rouge]
```

### Messages envoyés (gradient jaune)
```
                 ┌──────────────────────────────┐
                 │ BUY NOW!!!                   │
                 └──────────────────────────────┘
                 ⚠️ Spam (78%) [Gris foncé]
```

---

## 📊 Tableau de tests

| Message | Bad Words | Spam | Badge Modéré | Badge Spam | Pourcentage |
|---------|-----------|------|--------------|------------|-------------|
| "Bonjour" | ❌ | ❌ | ❌ | ❌ | - |
| "damn words" | ✅ | ❌ | ✅ | ❌ | - |
| "CLICK NOW!!!" | ❌ | ✅ | ❌ | ✅ | 85% |
| "damn CLICK HERE" | ✅ | ✅ | ✅ | ✅ | 78% |
| "FREE $$$" (>90%) | ❌ | ✅ | - | - | Bloqué |

---

## 🔍 Vérifications visuelles

### ✅ Ce que vous devez voir

**1. Badge spam (messages reçus)**
- Emoji: ⚠️
- Texte: "Spam (XX%)"
- Couleur: Rouge (#FF6B6B)
- Taille: 11sp

**2. Badge spam (messages envoyés)**
- Emoji: ⚠️
- Texte: "Spam (XX%)"
- Couleur: Gris foncé (#374151)
- Taille: 11sp

**3. Disposition**
```
Message text
[Badge Modéré] [Badge Spam]
```

---

## 🐛 Debugging

### Le badge spam ne s'affiche pas

**1. Vérifier le backend**
```bash
# Tester manuellement
curl -X POST http://localhost:3000/chat/test/spam-detection \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"CLICK HERE NOW"}' | jq
```

Vérifiez que la réponse contient:
```json
{
  "isSpam": true,
  "confidence": 0.85
}
```

**2. Vérifier le MessageDto**
Ajouter un log dans `ChatDetailScreen.kt`:
```kotlin
httpMessages.forEach { dto ->
    Log.d("SpamTest", "isSpam=${dto.isSpam}, conf=${dto.spamConfidence}")
}
```

**3. Vérifier la conversion**
```kotlin
val messages: List<Message> = remember(httpMessages, currentUserId) {
    httpMessages.mapIndexed { index, dto ->
        Log.d("SpamTest", "Mapping: isSpam=${dto.isSpam}")
        // ...
    }
}
```

### Le pourcentage ne s'affiche pas correctement

Vérifier la conversion:
```kotlin
if (message.isSpam) {
    val spamPercentage = (message.spamConfidence * 100).toInt()
    Log.d("SpamTest", "Confidence: ${message.spamConfidence}, %: $spamPercentage")
}
```

### Service FastAPI indisponible

Si FastAPI n'est pas démarré:
- Tous les messages retournent `isSpam: false`
- Aucun badge spam ne s'affiche
- C'est le comportement **attendu** (dégradation gracieuse)

---

## 📱 Tests sur différents appareils

### Émulateur Android
```bash
# Démarrer l'émulateur
emulator -avd Pixel_5_API_33

# Installer l'APK
cd frontend-android
./gradlew installDebug

# Voir les logs
adb logcat | grep -E "SpamTest|ChatDetail"
```

### Appareil physique
1. Activer le mode développeur
2. Connecter en USB
3. `adb devices` pour vérifier
4. `./gradlew installDebug`

---

## ✅ Checklist de validation

Avant de valider l'intégration:

- [ ] Message normal s'affiche sans badge
- [ ] Message spam affiche badge avec pourcentage
- [ ] Badge rouge pour messages reçus
- [ ] Badge gris foncé pour messages envoyés
- [ ] Double badge (modéré + spam) fonctionne
- [ ] Pourcentage correct (0-100)
- [ ] Messages > 90% bloqués par backend
- [ ] Pas de crash avec messages null
- [ ] Logs backend confirment l'analyse spam

---

## 🎯 Scénario complet

### Test de bout en bout

1. **Démarrer le backend**
   ```bash
   cd backend-nest
   npm run start:dev
   ```

2. **Optionnel: Démarrer FastAPI**
   ```bash
   cd spam-detection-service
   python main.py
   ```

3. **Installer l'app Android**
   ```bash
   cd frontend-android
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ./gradlew installDebug
   ```

4. **Se connecter à l'app**
   - Email: votre@email.com
   - Password: votrepassword

5. **Ouvrir une conversation**

6. **Tester les 5 scénarios**
   - Message normal
   - Spam modéré (70-90%)
   - Spam élevé (>90%)
   - Bad words seul
   - Bad words + spam

7. **Vérifier visuellement**
   - Badges affichés correctement
   - Couleurs appropriées
   - Pourcentages exacts

---

## 📖 Documentation

Pour plus de détails:
- [ANDROID_SPAM_INTEGRATION.md](./ANDROID_SPAM_INTEGRATION.md) - Documentation complète
- [Backend SPAM_DETECTION_INTEGRATION.md](../backend-nest/SPAM_DETECTION_INTEGRATION.md) - Backend

---

## 🎉 Validation finale

L'intégration est réussie si:

✅ Tous les badges s'affichent correctement
✅ Les pourcentages sont précis
✅ Les couleurs sont cohérentes
✅ Pas de crash ou d'erreur
✅ Backend confirme l'analyse spam

**L'intégration Spam Detection Android est complète ! 🚀**
