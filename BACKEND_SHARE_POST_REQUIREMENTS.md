# Backend Requirements for Share Post Feature

## API Endpoint: `POST /posts/:id/share`

### Request Format

**URL**: `POST /posts/:id/share`

**Headers**:
```
Authorization: Bearer <token>
x-user-id: <senderId>
x-owner-type: UserAccount or ProfessionalAccount
```

**Body**:
```json
{
  "recipientId": "user_or_professional_id",
  "message": "Shared a post with you"
}
```

### Expected Backend Behavior

When this endpoint is called, the backend should:

1. **Validate the post exists** using `:id` parameter
2. **Validate the recipient exists** using `recipientId`
3. **Get or create a conversation** between sender (from x-user-id header) and recipient
4. **Fetch the post details** to include in message metadata
5. **Create a message** in the conversation with the following structure:

### Message Structure to Create

```json
{
  "conversation": "conversation_id",
  "sender": "sender_user_id",
  "content": "Shared a post with you",
  "type": "shared_post",
  "meta": {
    "sharedPostId": "694bd45aafc86633448754a0",
    "sharedPostCaption": "The caption of the shared post",
    "sharedPostImage": "uploads/posts/image123.jpg"
  },
  "createdAt": "2025-12-30T19:41:08.000Z"
}
```

**Important**: The `meta` field should be a **flat object** with string key-value pairs, NOT a nested structure or array.

### Response Format

**Success Response** (200):
```json
{
  "success": true,
  "message": {
    "_id": "message_id",
    "conversation": "conversation_id",
    "sender": "sender_id",
    "content": "Shared a post with you",
    "type": "shared_post",
    "meta": {
      "sharedPostId": "post_id",
      "sharedPostCaption": "caption",
      "sharedPostImage": "image_url"
    },
    "createdAt": "2025-12-30T19:41:08.000Z"
  }
}
```

**Error Response** (400):
```json
{
  "success": false,
  "message": "Error description",
  "statusCode": 400
}
```

## Key Points

### 1. Meta Field Structure
✅ **Correct**:
```json
"meta": {
  "sharedPostId": "string",
  "sharedPostCaption": "string",
  "sharedPostImage": "string"
}
```

❌ **Incorrect**:
```json
"meta": [
  {"key": "sharedPostId", "value": "string"}
]
```

### 2. Image URL Format
The `sharedPostImage` should contain:
- **For reels**: Use `post.thumbnailUrl` if available
- **For images**: Use `post.mediaUrls[0]` (first image)
- **Path format**: Just the relative path (e.g., `uploads/posts/image.jpg`)
- The frontend will handle adding the base URL via `BaseUrlProvider`

### 3. Message Type
- Set `type: "shared_post"` so the frontend can identify it
- This allows special rendering with image preview and clickable navigation

### 4. Content Field
- The `content` field should contain a user-friendly message
- Default: "Shared a post with you"
- Can be customized by the sender in future enhancements

## Frontend Behavior

When a shared post message is received:

1. **In Chat List**: Shows "📷 Shared a post" as last message
2. **In Conversation**: Displays a card with:
   - Post image/thumbnail
   - Post caption (truncated to 100 chars)
   - "Tap to view full post" hint
3. **On Click**: Navigates to `PostDetailsScreen` with the `sharedPostId`

## Example Backend Implementation (Pseudo-code)

```typescript
async sharePost(postId: string, recipientId: string, senderId: string, message: string) {
  // 1. Get post details
  const post = await Post.findById(postId);
  if (!post) throw new NotFoundException('Post not found');
  
  // 2. Get or create conversation
  const conversation = await getOrCreateConversation(senderId, recipientId);
  
  // 3. Determine image URL
  const imageUrl = post.mediaType === 'reel' && post.thumbnailUrl 
    ? post.thumbnailUrl 
    : post.mediaUrls[0];
  
  // 4. Create message with metadata
  const newMessage = await Message.create({
    conversation: conversation._id,
    sender: senderId,
    content: message || 'Shared a post with you',
    type: 'shared_post',
    meta: {
      sharedPostId: postId,
      sharedPostCaption: post.caption,
      sharedPostImage: imageUrl
    }
  });
  
  // 5. Update conversation's last message
  conversation.lastMessage = newMessage._id;
  await conversation.save();
  
  // 6. Emit socket event (optional)
  this.socketService.emitToUser(recipientId, 'new_message', newMessage);
  
  // 7. Return response
  return {
    success: true,
    message: newMessage
  };
}
```

## Testing Checklist

### Backend Tests
- [ ] Endpoint validates post exists
- [ ] Endpoint validates recipient exists
- [ ] Conversation is created if doesn't exist
- [ ] Message is created with correct structure
- [ ] Meta field is a flat object (not array)
- [ ] Response includes all required fields
- [ ] Socket event is emitted to recipient

### Integration Tests
- [ ] Share post from HomeScreen
- [ ] Post appears in recipient's chat list
- [ ] Post displays with image in conversation
- [ ] Clicking post navigates to PostDetailsScreen
- [ ] Works for both user-to-user and user-to-professional
- [ ] Works for posts with images and reels

## Common Issues & Solutions

### Issue 1: "message must be a string" validation error
**Cause**: Backend validation expecting message field  
**Solution**: Include `message` field in request body with default value

### Issue 2: JSON parsing error "Expected string but was BEGIN_ARRAY"
**Cause**: Meta field is returned as array instead of object  
**Solution**: Ensure meta is a flat object `{key: value}` not `[{key, value}]`

### Issue 3: Shared post shows text instead of image
**Cause**: Meta fields not populated correctly in message  
**Solution**: Ensure all three meta fields are included:
- `sharedPostId`
- `sharedPostCaption` 
- `sharedPostImage`

### Issue 4: Image doesn't load in shared post
**Cause**: Image URL format incorrect  
**Solution**: Use relative path without base URL. Frontend handles base URL via BaseUrlProvider.

## Additional Notes

- The frontend supports both incoming and outgoing shared posts with different styling
- Images are loaded asynchronously with Coil library
- Failed image loads show a gray placeholder
- All shared posts are clickable and navigate to full post view
- The feature works seamlessly with existing chat system


