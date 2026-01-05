# Backend Fix: Allow Updating Post Category (foodType)

## Problem

The frontend is trying to update a post's `foodType` using `PATCH /posts/{postId}`, but the backend is rejecting the request with:
```
["property foodType should not exist"]
```

This error indicates that the backend's validation schema for the PATCH endpoint does not include `foodType` as an allowed property.

## Current Frontend Request

**Endpoint:** `PATCH /posts/{postId}`

**Request Body:**
```json
{
  "foodType": "BURGER"
}
```

**Current Backend Response:**
```json
{
  "statusCode": 400,
  "message": ["property foodType should not exist"]
}
```

## Required Backend Changes

### 1. Update the PATCH /posts/:id DTO

The backend's `UpdatePostDto` (or equivalent DTO used for PATCH requests) needs to include `foodType` as an optional property.

**Example (NestJS/TypeScript):**
```typescript
// posts/dto/update-post.dto.ts
export class UpdatePostDto {
  @IsOptional()
  @IsString()
  @IsEnum(FoodType) // Ensure it matches your FoodType enum
  foodType?: string;

  // ... other optional fields like caption, price, etc.
}
```

**Example (Express/Node.js with class-validator):**
```typescript
import { IsOptional, IsString, IsEnum } from 'class-validator';
import { FoodType } from '../enums/food-type.enum';

export class UpdatePostDto {
  @IsOptional()
  @IsString()
  @IsEnum(FoodType)
  foodType?: string;
}
```

### 2. Update the PATCH Endpoint Handler

Ensure the endpoint handler accepts and processes the `foodType` field:

**Example (NestJS):**
```typescript
@Patch(':id')
async updatePost(
  @Param('id') id: string,
  @Body() updatePostDto: UpdatePostDto,
) {
  // Update the post with the new foodType if provided
  return this.postsService.updatePost(id, updatePostDto);
}
```

**Example (Express):**
```typescript
router.patch('/posts/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { foodType, ...otherFields } = req.body;
    
    const updateData = {};
    if (foodType !== undefined) {
      updateData.foodType = foodType;
    }
    // ... handle other fields
    
    const updatedPost = await Post.findByIdAndUpdate(
      id,
      { $set: updateData },
      { new: true }
    );
    
    res.json(updatedPost);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});
```

### 3. Validation Rules

- `foodType` should be **optional** (nullable/undefined allowed)
- `foodType` should be validated against your FoodType enum values
- If `foodType` is provided, it should be a valid enum value (e.g., "BURGER", "PIZZA", "PASTA", etc.)

### 4. Expected Response

After the fix, the endpoint should:
- Accept `foodType` in the request body
- Update the post's `foodType` field in the database
- Return the updated post with the new `foodType` value

**Success Response (200 OK):**
```json
{
  "_id": "695c01395ab14d7663f3212d",
  "caption": "lll",
  "foodType": "BURGER",  // Updated value
  "mediaUrls": [...],
  // ... other post fields
}
```

## Testing

After implementing the fix, test with:

1. **Valid Update:**
   ```bash
   PATCH /posts/695c01395ab14d7663f3212d
   Body: { "foodType": "BURGER" }
   ```
   Expected: 200 OK with updated post

2. **Invalid FoodType:**
   ```bash
   PATCH /posts/695c01395ab14d7663f3212d
   Body: { "foodType": "INVALID_TYPE" }
   ```
   Expected: 400 Bad Request with validation error

3. **Empty Update:**
   ```bash
   PATCH /posts/695c01395ab14d7663f3212d
   Body: {}
   ```
   Expected: 200 OK (no changes, but should not error)

## Alternative: Separate Endpoint

If you prefer to keep the main PATCH endpoint limited, you could create a separate endpoint:

```typescript
@Patch(':id/food-type')
async updatePostFoodType(
  @Param('id') id: string,
  @Body() body: { foodType: string },
) {
  return this.postsService.updatePost(id, { foodType: body.foodType });
}
```

However, this would require frontend changes to use the new endpoint.

## Summary

**The backend needs to:**
1. ✅ Add `foodType` as an optional property to the `UpdatePostDto` (or equivalent DTO)
2. ✅ Update the PATCH `/posts/:id` endpoint to accept and process `foodType`
3. ✅ Validate `foodType` against the FoodType enum
4. ✅ Return the updated post with the new `foodType` value

**Once this is fixed, the frontend will automatically work** - no frontend changes needed!

