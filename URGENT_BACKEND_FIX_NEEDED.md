# 🚨 URGENT: Backend Fix Needed for Share Post Feature

## Current Issue

The share post feature is **partially working**:
- ✅ Posts are being shared successfully
- ✅ Messages appear in chat
- ❌ Messages show as text "Shared a post with you" instead of image cards
- ❌ Metadata is not being included in the message

## What's Happening Now

When a post is shared, the backend creates a message like this:

```json
{
  "conversation": "xxx",
  "sender": "xxx",
  "content": "Shared a post with you",
  "type": "text",  // ❌ Should be "shared_post"
  "meta": null     // ❌ Should contain post metadata
}
```

## What Should Happen

The backend should create a message like this:

```json
{
  "conversation": "xxx",
  "sender": "xxx", 
  "content": "Shared a post with you",
  "type": "shared_post",  // ✅ Set type to identify shared posts
  "meta": {               // ✅ Include post metadata
    "sharedPostId": "694bd45aafc86633448754a0",
    "sharedPostCaption": "Check out this amazing dish!",
    "sharedPostImage": "uploads/posts/1234567890.jpg"
  }
}
```

## Required Backend Changes

### In your share endpoint (`POST /posts/:id/share`):

```typescript
// 1. Get the post being shared
const post = await Post.findById(postId);

// 2. Determine the image URL
const imageUrl = post.mediaType === 'reel' && post.thumbnailUrl 
  ? post.thumbnailUrl 
  : post.mediaUrls[0];

// 3. Create message with metadata
const message = await Message.create({
  conversation: conversationId,
  sender: senderId,
  content: requestBody.message || 'Shared a post with you',
  type: 'shared_post',  // ⚠️ IMPORTANT: Set type
  meta: {               // ⚠️ IMPORTANT: Include metadata
    sharedPostId: postId,
    sharedPostCaption: post.caption,
    sharedPostImage: imageUrl
  }
});
```

## Critical Requirements

### 1. Message Type
```typescript
type: 'shared_post'  // Must be exactly this string
```

### 2. Meta Field Structure
```typescript
meta: {
  sharedPostId: string,      // The MongoDB _id of the post
  sharedPostCaption: string, // The post's caption
  sharedPostImage: string    // Relative path to image (e.g., "uploads/posts/image.jpg")
}
```

**Important**: 
- `meta` must be a **flat object**, NOT an array
- All three fields are required
- Image path should be relative (frontend adds base URL)

### 3. Image URL Priority
```typescript
// For reels: use thumbnail if available
const imageUrl = post.mediaType === 'reel' && post.thumbnailUrl 
  ? post.thumbnailUrl 
  : post.mediaUrls[0];  // Otherwise use first image
```

## Testing the Fix

### Backend Test:
1. Share a post via API
2. Query the created message
3. Verify `type === 'shared_post'`
4. Verify `meta` contains all three fields
5. Check logs for the exact structure

### Frontend Test:
1. Share a post from the app
2. Open the conversation
3. **Should see**: Image card with post thumbnail
4. **Should NOT see**: Yellow bubble with text
5. Click the image → Should navigate to PostDetailsScreen

## Current Frontend Behavior

The frontend is **ready** and will automatically:
- ✅ Detect messages with `type: 'shared_post'`
- ✅ Display them as image cards
- ✅ Show post thumbnail and caption
- ✅ Navigate to PostDetailsScreen on click

**But it needs the backend to provide the correct message structure!**

## Debugging

### Check Backend Logs:
```
POST /posts/:id/share
- Request body: { recipientId, message }
- Created message: { ... check type and meta fields ... }
```

### Check Frontend Logs (after fix):
```
D/ChatDetailScreen: Processing message xxx: type=shared_post, content=Shared a post with you, meta={...}
D/ChatDetailScreen: Meta is Map with keys: [sharedPostId, sharedPostCaption, sharedPostImage]
D/ChatDetailScreen: Extracted: postId=xxx, caption=xxx, image=xxx
```

## Example Backend Implementation

```typescript
// In your PostsController or similar

async sharePost(postId: string, recipientId: string, senderId: string, message: string) {
  try {
    // 1. Validate post exists
    const post = await this.postModel.findById(postId);
    if (!post) {
      throw new NotFoundException('Post not found');
    }

    // 2. Get or create conversation
    let conversation = await this.conversationModel.findOne({
      participants: { $all: [senderId, recipientId] }
    });

    if (!conversation) {
      conversation = await this.conversationModel.create({
        kind: 'direct',
        participants: [senderId, recipientId]
      });
    }

    // 3. Determine image URL
    const imageUrl = post.mediaType === 'reel' && post.thumbnailUrl
      ? post.thumbnailUrl
      : post.mediaUrls[0];

    // 4. Create message with metadata ⚠️ THIS IS THE KEY PART
    const newMessage = await this.messageModel.create({
      conversation: conversation._id,
      sender: senderId,
      content: message || 'Shared a post with you',
      type: 'shared_post',  // ⚠️ MUST SET THIS
      meta: {               // ⚠️ MUST INCLUDE THIS
        sharedPostId: postId,
        sharedPostCaption: post.caption,
        sharedPostImage: imageUrl
      }
    });

    // 5. Update conversation's last message
    conversation.lastMessage = newMessage._id;
    await conversation.save();

    // 6. Emit socket event (optional but recommended)
    this.socketGateway.emitToUser(recipientId, 'new_message', newMessage);

    // 7. Return success
    return {
      success: true,
      message: newMessage
    };
  } catch (error) {
    console.error('Error sharing post:', error);
    throw error;
  }
}
```

## Quick Fix Checklist

- [ ] Update share endpoint to set `type: 'shared_post'`
- [ ] Add `meta` object with three required fields
- [ ] Test with Postman/curl to verify structure
- [ ] Check database to confirm message saved correctly
- [ ] Test in app - should see image cards
- [ ] Verify clicking navigates to PostDetailsScreen

## Contact

If you need help implementing this, the frontend code is ready and waiting. The only blocker is the backend message structure.

**Time estimate**: 15-30 minutes to implement and test

## Visual Comparison

### ❌ Current (Wrong):
```
┌──────────────────────────┐
│ Shared a post with you   │  ← Yellow text bubble
└──────────────────────────┘
```

### ✅ Expected (Correct):
```
┌──────────────────────────┐
│ ┌────────┐               │
│ │        │ Amazing dish! │  ← Image card
│ │ [IMG]  │ Tap to view   │
│ └────────┘               │
└──────────────────────────┘
```

---

**Status**: Waiting for backend to include `type` and `meta` fields in share endpoint response.


