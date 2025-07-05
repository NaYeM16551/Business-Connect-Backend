# Group API Documentation

This document describes all the APIs available for group management features similar to Facebook groups.

## Base URL
```
/api/groups
```

## Authentication
All endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Group Management APIs

### 1. Create Group
**POST** `/api/groups`

Create a new group.

**Request Body:**
```json
{
  "name": "My Group Name",
  "description": "Group description",
  "privacy": "PUBLIC",
  "coverImage": "https://example.com/cover.jpg"
}
```

**Privacy Options:**
- `PUBLIC` - Anyone can see and join
- `CLOSED` - Anyone can see, but requires approval to join
- `PRIVATE` - Only members can see

**Response:**
```json
{
  "id": 1,
  "name": "My Group Name",
  "description": "Group description",
  "privacy": "PUBLIC",
  "ownerId": 123,
  "ownerName": "John Doe",
  "createdAt": "2024-01-01T10:00:00Z",
  "coverImage": "https://example.com/cover.jpg",
  "memberCount": 1,
  "postCount": 0,
  "isMember": true,
  "userRole": "OWNER"
}
```

### 2. Get Group Details
**GET** `/api/groups/{groupId}`

Get detailed information about a specific group.

**Response:**
```json
{
  "id": 1,
  "name": "My Group Name",
  "description": "Group description",
  "privacy": "PUBLIC",
  "ownerId": 123,
  "ownerName": "John Doe",
  "createdAt": "2024-01-01T10:00:00Z",
  "coverImage": "https://example.com/cover.jpg",
  "memberCount": 5,
  "postCount": 10,
  "isMember": true,
  "userRole": "MEMBER"
}
```

### 3. Get User's Groups
**GET** `/api/groups/my-groups`

Get all groups that the authenticated user is a member of.

**Response:**
```json
[
  {
    "id": 1,
    "name": "My Group Name",
    "description": "Group description",
    "privacy": "PUBLIC",
    "ownerId": 123,
    "ownerName": "John Doe",
    "createdAt": "2024-01-01T10:00:00Z",
    "coverImage": "https://example.com/cover.jpg",
    "memberCount": 5,
    "postCount": 10,
    "isMember": true,
    "userRole": "MEMBER"
  }
]
```

### 4. Search Groups
**GET** `/api/groups/search?query={searchTerm}`

Search for groups by name or description.

**Response:**
```json
[
  {
    "id": 1,
    "name": "My Group Name",
    "description": "Group description",
    "privacy": "PUBLIC",
    "ownerId": 123,
    "ownerName": "John Doe",
    "createdAt": "2024-01-01T10:00:00Z",
    "coverImage": "https://example.com/cover.jpg",
    "memberCount": 5,
    "postCount": 10,
    "isMember": false,
    "userRole": null
  }
]
```

## Group Membership APIs

### 5. Join Group
**POST** `/api/groups/{groupId}/join`

Join a group (only works for PUBLIC groups).

**Response:**
```json
"Successfully joined the group"
```

### 6. Leave Group
**POST** `/api/groups/{groupId}/leave`

Leave a group (owners cannot leave).

**Response:**
```json
"Successfully left the group"
```

### 7. Get Group Members
**GET** `/api/groups/{groupId}/members`

Get list of all members in a group.

**Response:**
```json
[
  {
    "userId": 123,
    "userName": "John Doe",
    "userEmail": "john@example.com",
    "role": "OWNER",
    "joinedAt": "2024-01-01T10:00:00Z",
    "profileImage": "https://example.com/profile.jpg"
  },
  {
    "userId": 124,
    "userName": "Jane Smith",
    "userEmail": "jane@example.com",
    "role": "MEMBER",
    "joinedAt": "2024-01-02T10:00:00Z",
    "profileImage": "https://example.com/profile2.jpg"
  }
]
```

### 8. Update Member Role
**PUT** `/api/groups/{groupId}/members/{memberId}/role?role={newRole}`

Update a member's role (only owners and admins can do this).

**Role Options:**
- `OWNER` - Full control
- `ADMIN` - Can manage members and posts
- `MODERATOR` - Can moderate posts
- `MEMBER` - Regular member
- `BANNED` - Cannot post or interact

**Response:**
```json
"Member role updated successfully"
```

### 9. Remove Member
**DELETE** `/api/groups/{groupId}/members/{memberId}`

Remove a member from the group (only owners and admins can do this).

**Response:**
```json
"Member removed successfully"
```

## Group Management APIs

### 10. Update Group Settings
**PUT** `/api/groups/{groupId}`

Update group settings (only owners and admins can do this).

**Request Body:**
```json
{
  "name": "Updated Group Name",
  "description": "Updated description",
  "privacy": "CLOSED",
  "coverImage": "https://example.com/new-cover.jpg"
}
```

**Response:**
```json
"Group updated successfully"
```

### 11. Delete Group
**DELETE** `/api/groups/{groupId}`

Delete a group (only owner can do this).

**Response:**
```json
"Group deleted successfully"
```

## Group Posts APIs

### 12. Create Group Post
**POST** `/api/groups/{groupId}/posts`

Create a new post in a group.

**Request Parameters:**
- `content` (required) - Post content
- `files` (optional) - Array of media files

**Response:**
```json
123
```
(Post ID)

### 13. Get Group Posts
**GET** `/api/groups/{groupId}/posts?page=0&size=10`

Get posts from a group with pagination.

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Number of posts per page

**Response:**
```json
[
  {
    "id": 123,
    "content": "This is a group post",
    "createdAt": "2024-01-01T10:00:00Z",
    "media": [
      {
        "mediaUrl": "https://example.com/image.jpg",
        "mediaType": "image/jpeg"
      }
    ],
    "likeCount": 5,
    "commentCount": 3,
    "shareCount": 1
  }
]
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "User is not a member of this group"
}
```

### 401 Unauthorized
```json
{
  "error": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Only owners and admins can change member roles"
}
```

### 404 Not Found
```json
{
  "error": "Group not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error"
}
```

## Group Roles and Permissions

### Owner
- Can delete the group
- Can change any member's role
- Can remove any member
- Can update group settings
- Cannot leave the group

### Admin
- Can change member roles (except owner)
- Can remove members (except owner)
- Can update group settings
- Can moderate posts

### Moderator
- Can moderate posts
- Can remove comments
- Cannot change member roles

### Member
- Can post in the group
- Can comment on posts
- Can like posts

### Banned
- Cannot post or interact
- Can only view content

## Group Privacy Settings

### Public Groups
- Anyone can see the group
- Anyone can join without approval
- Posts are visible to everyone

### Closed Groups
- Anyone can see the group
- Requires approval to join
- Posts are visible to members only

### Private Groups
- Only members can see the group
- Requires invitation to join
- Posts are visible to members only 