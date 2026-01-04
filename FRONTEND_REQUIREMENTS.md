# Frontend Requirement Updates - Gemini Suggestions & Order Logic

## 🚀 New Feature: AI Menu Suggestions

We have added a new feature that uses Google Gemini AI to suggest the best way to customize a menu item.

### 1. New API Endpoint
- **URL**: `GET /menu-items/:id/suggestions`
- **When to call**: When the user clicks a specific "Ask AI" or "Suggestions" button on the **Item Details** screen.
- **Response**:
  ```json
  {
    "bestCombination": "Double patty with Swiss cheese...",
    "popularChoice": "Classic Cheddar Burger with...",
    "reasoning": "The Swiss cheese adds a distinct flavor..."
  }
  ```

### 2. UI Requirements
- **Button**: Add an "✨ Ask AI" (or similar) button on the Menu Item screen.
- **Display**: show the results in a **Popup / Modal** or **Bottom Sheet**.
  - Show **Best Combination** clearly.
  - Show **Popular Choice**.
  - Show the **Reasoning** text to explain why these were picked.
- **Optional**: The feature is optional for the user. They can view suggestions and close the popup.

---

## 🛠️ Data Model Updates (Breaking Changes)

The `Order` object structure has changed regarding time estimation.

### 1. Removed Fields
The following fields have been **removed** from the Order object and will no longer be sent by the backend:
- ❌ `estimatedPreparationMinutes` (The predictive AI time)
- ❌ `queuePosition` (The position in the kitchen queue)

### 2. Updated Logic: Preparation Time
- ✅ `basePreparationMinutes`: This field remains but is now strictly the **sum of the base prep time** for all items in the order.
- **Frontend Action**: If you were displaying "Estimated Wait Time" based on the AI field, switch to displaying `basePreparationMinutes` labeled as **"Standard Prep Time"** or similar. Do not promise an exact delivery time based on queue depth anymore.

---

## Summary Checklist
- [ ] Implement "Ask AI" button on Menu Item Details.
- [ ] Create Modal/Popup to display AI suggestions.
- [ ] Connect new button to `GET /menu-items/:id/suggestions`.
- [ ] Remove UI references to "Queue Position" or "AI Estimated Wait".
- [ ] Update Order Details content to show `basePreparationMinutes` if needed.
