# 🎯 BACKEND FIX: Share Post Metadata

## Problem Summary
Posts are being shared successfully, but they appear as text messages instead of image cards because the backend is NOT including the required metadata in the message.

## Required Backend Changes

### 1. Update Share Endpoint Logic

In your backend's share endpoint (`POST /posts/:id/share`), you need to:

#### Current (Broken) Implementation:
```typescript
// ❌ This creates a text message without metadata
const message = await Message.create({
  conversation: conversationId,
  sender: senderId,
  content: 'Shared a post with you',
  type: 'text',  // Wrong!
  meta: null     // Wrong!
});
```

#### Required (Fixed) Implementation:
```typescript
// ✅ This creates a proper shared post message
async sharePost(postId, recipientId, senderId, messageText) {
  // 1. Get the post being shared
  const post = await Post.findById(postId);
  if (!post) throw new NotFoundException('Post not found');
  
  // 2. Get or create conversation
  let conversation = await this.getOrCreateConversation(senderId, recipientId);
  
  // 3. Determine image URL (use thumbnail for videos, first image for photos)
  const imageUrl = post.mediaType === 'reel' && post.thumbnailUrl
    ? post.thumbnailUrl
    : post.mediaUrls[0];
  
  // 4. ⚠️ CREATE MESSAGE WITH PROPER METADATA
  const message = await Message.create({
    conversation: conversation._id,
    sender: senderId,
    content: messageText || 'Shared a post with you',
    
    // ⚠️ CRITICAL: Set the type to 'shared_post'
    type: 'shared_post',
    
    // ⚠️ CRITICAL: Include metadata with post information
    meta: {
      sharedPostId: postId,
      sharedPostCaption: post.caption,
      sharedPostImage: imageUrl
    }
  });
  
  // 5. Update conversation
  conversation.lastMessage = message._id;
  await conversation.save();
  
  // 6. Emit socket event (recommended)
  this.socketGateway?.emitToUser(recipientId, 'new_message', message);
  
  return {
    success: true,
    message: message
  };
}
```

### 2. Verify Message Schema

Ensure your Message schema allows these fields:

```typescript
// In your Message model
{
  conversation: { type: ObjectId, ref: 'Conversation', required: true },
  sender: { type: ObjectId, refPath: 'senderModel', required: true },
  content: { type: String },
  type: { 
    type: String, 
    enum: ['text', 'image', 'video', 'audio', 'file', 'shared_post', 'post'],
    default: 'text' 
  },
  meta: { 
    type: Map, 
    of: String  // OR: type: mongoose.Schema.Types.Mixed
  },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
}
```

### 3. Test the Fix

#### Step 1: Test with API Client (Postman/Thunder Client)
```bash
POST /posts/{postId}/share
Headers:
  Authorization: Bearer <token>
  x-user-id: <your-user-id>
  x-owner-type: UserAccount

Body:
{
  "recipientId": "recipient_user_id",
  "message": "Shared a post with you"
}
```

#### Step 2: Check the Created Message
Query your database to verify the message structure:
```javascript
// Should see this in MongoDB:
{
  _id: ObjectId("..."),
  conversation: ObjectId("..."),
  sender: ObjectId("..."),
  content: "Shared a post with you",
  type: "shared_post",  // ✅ Check this
  meta: {                // ✅ Check this
    sharedPostId: "694bd45aafc86633448754a0",
    sharedPostCaption: "Check out this amazing dish!",
    sharedPostImage: "uploads/posts/1234567890.jpg"
  },
  createdAt: ISODate("...")
}
```

#### Step 3: Test in Android App
1. Clear app data (or reinstall)
2. Share a post from HomeScreen
3. Open the chat conversation
4. **Expected Result**: Should see a beautiful image card, NOT text
5. Click the image → Should navigate to PostDetailsScreen

---

## Verification Checklist

### Backend Changes:
- [ ] Updated share endpoint to set `type: 'shared_post'`
- [ ] Added `meta` object with three required fields
- [ ] Fetching post details in share endpoint
- [ ] Using thumbnail for reels, first image for photos
- [ ] Message schema allows `shared_post` type
- [ ] Message schema allows `meta` field

### Testing:
- [ ] Tested with Postman - API returns success
- [ ] Checked MongoDB - message has correct structure
- [ ] Tested in app - image card displays
- [ ] Tested clicking - navigates to PostDetailsScreen
- [ ] Tested with images - displays correctly
- [ ] Tested with reels - thumbnail displays correctly

---

## Visual Comparison

### ❌ BEFORE (Current - Wrong):
```
Chat Conversation:
┌──────────────────────────┐
│ yy                       │  ← Old messages
│                          │
│ oo                       │
└──────────────────────────┘
← Missing shared post! Only shows as text in conversation list
```

### ✅ AFTER (Expected - Correct):
```
Chat Conversation:
┌──────────────────────────┐
│ yy                       │
│                          │
│ oo                       │
│                          │
│ ╔════════════════════╗   │  ← NEW: Shared Post Card
│ ║ ┌────────┐         ║   │
│ ║ │        │ Amazing ║   │
│ ║ │ [IMG]  │ dish!   ║   │
│ ║ │        │ Tap to  ║   │
│ ║ └────────┘ view    ║   │
│ ╚════════════════════╝   │
└──────────────────────────┘
```

---

## Critical Requirements Summary

### The THREE required meta fields:
1. **sharedPostId** (string) - The MongoDB `_id` of the post
2. **sharedPostCaption** (string) - The post's caption text
3. **sharedPostImage** (string) - Relative path to image/thumbnail

### Important Notes:
- `meta` must be a **flat object** (not an array)
- Image path should be **relative** (e.g., `"uploads/posts/image.jpg"`)
- The frontend automatically adds the base URL
- For reels, use `thumbnailUrl` if available, otherwise use `mediaUrls[0]`

---

## Expected Time to Fix
**⏱️ 15-30 minutes** to implement and test

---

## Contact & Support
- The Android frontend is **100% complete** and ready
- Beautiful Instagram-style cards are implemented and waiting
- All navigation and click handlers are working
- **Only blocker**: Backend needs to send the metadata

**The ball is in the backend team's court! 🏀**

---

## Status
🔴 **BLOCKING** - Users cannot see shared posts without this fix
🎯 **SOLUTION** - Add metadata to messages created by share endpoint
⏱️ **ETA** - 15-30 minutes once backend team starts

---

Last Updated: January 5, 2026



