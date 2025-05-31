# Business-Connect API

All endpoints are prefixed with:

```
https://localhost:8001/api/v1
```

> **Note:** Authenticated requests require a valid JWT in the `Authorization` header:
>
> ```http
> Authorization: Bearer <JWT_TOKEN>
> ```

---

## Table of Contents

1. [Authentication & Profile](#1-authentication--profile)
2. [Posts & Interactions](#2-posts--interactions)
3. [Hackathons](#3-hackathons)
4. [Legal Assistance](#4-legal-assistance)
5. [Mentorship](#5-mentorship)
6. [Messaging & Notifications](#6-messaging--notifications)
7. [Learning](#7-learning)
8. [Groups & Community](#8-groups--community)
9. [User Following](#9-user-following)
10. [Versioning & Changelog](#versioning--changelog)

---

Here’s the corrected Markdown snippet with proper fencing and spacing:

# Auth section API

---

## 1-authentication--profile

## Table of Contents

1. [Email Verification](#1-email-verification)  
   1.1 [Request Verification Code](#11-request-verification-code)  
   1.2 [Verify Email Token](#12-verify-email-token)
2. [User Registration & Login](#2-user-registration--login)  
   2.1 [Register User](#21-register-user)  
   2.2 [Login](#22-login)
3. [Password Management](#3-password-management)  
   3.1 [Forgot Password](#31-forgot-password)  
   3.2 [Reset Password](#32-reset-password)  
   3.3 [Change Password](#33-change-password)
4. [Profile Management](#4-profile-management)  
   4.1 [Update Profile](#41-update-profile)
5. [Account Management](#5-account-management)  
   5.1 [Delete Account](#51-delete-account)

---

## 1. Email Verification

### 1.1 Request Verification Code

- **Method:** `POST`
- **Endpoint:** `/register-verify`
- **Description:** Send a one-time verification email to the given address before registration.

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Verification email sent",
    "verificationToken": "<token>" // optional, if you return it
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Email is required"
  }
  ```

- **409 Conflict**

  ```json
  {
    "error": "Email already registered."
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to send verification email. Please try resending."
  }
  ```

---

### 1.2 Verify Email Token

- **Method:** `GET`
- **Endpoint:** `/verify-email?token=<emailToken>`
- **Description:** Confirm the user’s email by validating the token sent in the verification email.

**Query Parameters:**

| Name  | Type   | Required | Description                  |
| ----- | ------ | -------- | ---------------------------- |
| token | String | Yes      | The email verification token |

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Email verified successfully."
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Invalid or expired verification token."
  }
  ```

---

## 2. User Registration & Login

### 2.1 Register User

- **Method:** `POST`
- **Endpoint:** `/register`
- **Description:** Create a new user account after email has been verified. Requires `Authorization` header with the Bearer token from **1.1**.

**Headers:**

```
Authorization: Bearer <verificationToken>
```

**Request Body:**

```json
{
  "username": "M.M.Nabil",
  "email": "nabil2005@gmail.com",
  "password": "nayem123",
  "industry": ["Technology", "Finance"],
  "interests": ["Machine Learning", "Blockchain"],
  "achievements": ["Published a research paper", "Speaker at TechConf 2024"]
}
```

**Responses:**

- **201 Created**

  ```json
  {
    "userId": "u_12345",
    "email": "user@example.com",
    "token": "<JWT_TOKEN>"
  }
  ```

- **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header (Did you verify your email?)"
  }
  ```

- **409 Conflict**

  ```json
  {
    "error": "User with this email already exists."
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Registration failed. Please try again later."
  }
  ```

---

### 2.2 Login

- **Method:** `POST`
- **Endpoint:** `/login`
- **Description:** Authenticate a user and return a JWT.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "StrongPass!23"
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "userId": "u_12345",
    "email": "user@example.com",
    "token": "<JWT_TOKEN>"
  }
  ```

- **401 Unauthorized**

  ```json
  {
    "error": "Invalid email or password."
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Login failed. Please try again later."
  }
  ```

---

## 3. Password Management

### 3.1 Forgot Password

- **Method:** `POST`
- **Endpoint:** `/forgot-password`
- **Description:** Send a password-reset email with a one-time token.

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Password reset email sent successfully"
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Email is required"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to send password reset email"
  }
  ```

---

### 3.2 Reset Password

- **Method:** `POST`
- **Endpoint:** `/reset-password?token=<resetToken>`
- **Description:** Update the user’s password using the reset token.

**Query Parameters:**

| Name  | Type   | Required | Description              |
| ----- | ------ | -------- | ------------------------ |
| token | String | Yes      | The password reset token |

**Request Body:**

```json
{
  "newPassword": "NewStrongPass!45"
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Password updated successfully"
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Invalid or expired reset token."
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to update password"
  }
  ```

---

### 3.3 Change Password

- **Method:** `POST`
- **Endpoint:** `/change-password`
- **Description:** Change password for an authenticated user. Requires JWT.

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "oldPassword": "OldPass!23",
  "newPassword": "NewPass!45"
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Password changed successfully"
  }
  ```

- **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to change password"
  }
  ```

---

## 4. Profile Management

### 4.1 Update Profile

- **Method:** `PATCH`
- **Endpoint:** `/update-profile`
- **Description:** Update user’s profile details. Requires JWT.

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "email": "new-email@example.com", // optional
  "password": "OptionalNewPass!99" // optional
  // any other updatable profile fields...
}
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Profile updated successfully"
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Invalid request data"
  }
  ```

- **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to update profile"
  }
  ```

---

## 5. Account Management

### 5.1 Delete Account

- **Method:** `DELETE`
- **Endpoint:** `/delete-account`
- **Description:** Permanently delete the authenticated user’s account. Requires JWT.

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
```

**Responses:**

- **200 OK**

  ```json
  {
    "message": "Account deleted successfully"
  }
  ```

- **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Failed to delete account"
  }
  ```

---

## 2. Posts & Interactions

### Table of Contents

1. [Create & Manage Posts](#21-create-post)  
   1.1 [Create Post](#21-create-post)  
   1.2 [Edit Post](#212-edit-post)  
   1.3 [Get Post by ID](#22-get-post-by-id)  
   1.4 [Get User's Own Posts](#23-get-users-own-posts)  
   1.5 [Get Posts by User ID](#24-get-posts-by-user-id)  
   1.6 [Delete Post](#25-delete-post)  
   1.7 [Share Post](#210-share-post)

2. [Post Interactions](#26-like-post)  
   2.1 [Like Post](#26-like-post)  
   2.2 [Comment on Post](#27-comment-on-post)  
   2.3 [Get Post Comments](#28-get-post-comments)  
   2.4 [Delete Comment](#29-delete-comment)  
   2.5 [Edit Comment](#30-edit-comment)

All post-related endpoints are prefixed with `/api/v1/posts`

### 2.1 Create Post

- **Method:** `POST`
- **Endpoint:** `/create-post`
- **Description:** Create a new post with content and optional media files
- **Content-Type:** `multipart/form-data`

**Request Parameters:**

- `content` (string, required): The text content of the post
- `files` (array of files, optional): Media files to be attached to the post

**Response:** `200 OK`

```json
{
  "message": "Post created",
  "postId": 123
}
```

**Error Responses:**

| Status Code | Description                           | Response Body                         |
| ----------- | ------------------------------------- | ------------------------------------- |
| 401         | Unauthorized - User not authenticated | `{"error": "User not authenticated"}` |
| 500         | Internal Server Error                 | `{"error": "Error message"}`          |

### 2.1.2 Edit post

- **Method:** `PUT`
- **Endpoint:** `/{postId}`
- **Description:** Edit an existing post by its ID
- **Content-Type:** `multipart/form-data`

**Request Parameters:**

- `content` (string, required): The updated text content of the post
- `files` (array of files, optional): Updated media files to be attached to the post
  **Response:** `200 OK`

```json
{
  "message": "Post updated successfully"
}
```

**Error Responses:**
| Status Code | Description | Response Body |
| ----------- | ------------------------------------- | ------------------------------------- |
| 401 | Unauthorized - User not authenticated | `{"error": "User not authenticated"}` |
| 500 | Internal Server Error | `{"error": "Error message"}` |

### 2.2 Get Post by ID

- **Method:** `GET`
- **Endpoint:** `/{postId}`
- **Description:** Retrieve a specific post by its ID

**Response:** `200 OK`

```json
{
  "id": 252,
  "content": "Ami Nayem,BUET,CSE-2020",
  "media": [
    {
      "mediaUrl": "https://res.cloudinary.com/dv7lfz0nc/image/upload_v1748601330/qtx5lltyhdfoyy45h1lx.jpg",
      "mediaType": "image/jpeg"
    }
  ],
  "createdAt": "2025-05-30T16:35:22.146274"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                |
| ----------- | --------------------- | ---------------------------- |
| 500         | Internal Server Error | `{"error": "Error message"}` |

### 2.3 Get User's Own Posts

- **Method:** `GET`
- **Endpoint:** `/me`
- **Description:** Get all posts created by the authenticated user

**Response:** `200 OK`

```json
[
  {
    "id": 202,
    "content": "Ami Nayem,BUET,CSE",
    "media": [
      {
        "mediaUrl": "https://res.cloudinary.com/dv7lfz0nc/image/upload_v1748600416/mpeilbmyflk8pxkhlt3q.jpg",
        "mediaType": "image/jpeg"
      }
    ],
    "createdAt": "2025-05-30T16:19:49.44687"
  },
  {
    "id": 252,
    "content": "Ami Nayem,BUET,CSE-2020",
    "media": [
      {
        "mediaUrl": "https://res.cloudinary.com/dv7lfz0nc/image/upload_v1748601330/qtx5lltyhdfoyy45h1lx.jpg",
        "mediaType": "image/jpeg"
      }
    ],
    "createdAt": "2025-05-30T16:35:22.146274"
  }
]
```

**Error Responses:**

| Status Code | Description           | Response Body                         |
| ----------- | --------------------- | ------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`          |

### 2.4 Get Posts by User ID

- **Method:** `GET`
- **Endpoint:** `/user/{userId}`
- **Description:** Get all posts created by a specific user

**Response:** `200 OK`

```json
[
  {
    "id": 202,
    "content": "Ami Nayem,BUET,CSE",
    "media": [
      {
        "mediaUrl": "https://res.cloudinary.com/dv7lfz0nc/image/upload_v1748600416/mpeilbmyflk8pxkhlt3q.jpg",
        "mediaType": "image/jpeg"
      }
    ],
    "createdAt": "2025-05-30T16:19:49.44687"
  },
  {
    "id": 252,
    "content": "Ami Nayem,BUET,CSE-2020",
    "media": [
      {
        "mediaUrl": "https://res.cloudinary.com/dv7lfz0nc/image/upload_v1748601330/qtx5lltyhdfoyy45h1lx.jpg",
        "mediaType": "image/jpeg"
      }
    ],
    "createdAt": "2025-05-30T16:35:22.146274"
  }
]
```

**Error Responses:**

| Status Code | Description           | Response Body                |
| ----------- | --------------------- | ---------------------------- |
| 500         | Internal Server Error | `{"error": "Error message"}` |

### 2.5 Delete Post

- **Method:** `DELETE`
- **Endpoint:** `/{postId}`
- **Description:** Delete a specific post

**Response:** `200 OK`

```json
{
  "message": "Post deleted successfully"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                         |
| ----------- | --------------------- | ------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`          |

### 2.6 Like Post

- **Method:** `POST`
- **Endpoint:** `/like/{postId}`
- **Description:** Like/React to a post with different reaction types

**Request Body:**

```json
{
  "likeType": 1 // 0: no-react, 1: love, 2: like, 3: wow, 4: angry, 5: haha
}
```

**Response:** `200 OK`

```json
{
  "message": "Post liked successfully"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                               |
| ----------- | --------------------- | ------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`       |
| 400         | Bad Request           | `{"error": "Invalid user ID in Principal"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                |

### 2.7 Comment on Post

- **Method:** `POST`
- **Endpoint:** `/comment/{postId}`
- **Description:** Add a comment to a post, supports nested comments

**Request Body:**

```json
{
  "comment": "Comment text",
  "parentCommentId": "123" // Optional, for nested comments
}
```

**Response:** `200 OK`

```json
{
  "commentId": 123
}
```

**Error Responses:**

| Status Code | Description           | Response Body                               |
| ----------- | --------------------- | ------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`       |
| 400         | Bad Request           | `{"error": "Invalid user ID in Principal"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                |

### 2.8 Get Post Comments

- **Method:** `GET`
- **Endpoint:** `/comments/{postId}`
- **Description:** Get all comments for a specific post

**Response:** `200 OK`

```json
[
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:28:28.213684",
    "content": "Nice picture vaya",
    "id": 1,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:29:02.742114",
    "content": "Nice picture vaya",
    "id": 2,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:34:13.823938",
    "content": "Nice picture vaya",
    "id": 52,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:35:16.603443",
    "content": "Nice picture vaya",
    "id": 53,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:37:24.619591",
    "content": "Nice picture vaya",
    "id": 102,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:38:01.262217",
    "content": "Nice picture vaya",
    "id": 152,
    "parentCommentId": null,
    "replies": []
  },
  {
    "authorName": "nayem78",
    "commentedAt": "2025-05-31T01:51:35.486207",
    "content": "Nice picture vaya",
    "id": 253,
    "parentCommentId": null,
    "replies": []
  }
]
```

**Error Responses:**

| Status Code | Description           | Response Body                |
| ----------- | --------------------- | ---------------------------- |
| 500         | Internal Server Error | `{"error": "Error message"}` |

### 2.9 Delete Comment

- **Method:** `DELETE`
- **Endpoint:** `/{postId}/comment/{commentId}`
- **Description:** Delete a specific comment from a post(supports nested comments,if parentComment is deleted, all child comments will be deleted)
- **Note:** Requires the user to be the author of the comment or an admin

**Response:** `200 OK`

```json
{
  "message": "Comment deleted successfully"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                               |
| ----------- | --------------------- | ------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`       |
| 400         | Bad Request           | `{"error": "Invalid user ID in Principal"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                |

### 3.0 Edit Comment

- **Method:** `PUT`
- **Endpoint:** `/{postId}/comment/{commentId}`
- **Description:** Edit a specific comment on a post
- **Request Body:**

```json
{
  "comment": "Updated comment text"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                               |
| ----------- | --------------------- | ------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`       |
| 400         | Bad Request           | `{"error": "Invalid user ID in Principal"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                |

### 2.10 Share Post

- **Method:** `POST`
- **Endpoint:** `/share/{postId}`
- **Description:** Share an existing post with optional additional content

**Request Body:**

```json
{
  "content": "Optional comment on the shared post"
}
```

**Response:** `200 OK`

```json
{
  "message": "Post shared successfully",
  "sharedPostId": 123
}
```

**Error Responses:**

| Status Code | Description           | Response Body                               |
| ----------- | --------------------- | ------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`       |
| 400         | Bad Request           | `{"error": "Invalid user ID in Principal"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                |

## 3. Hackathons

### 3.1 List Open Hackathons

- **Endpoint:** `GET /hackathons?type=open`

**Responses:**

| Status | Description                            | Example Body                                                                                                                                                                  |
| ------ | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 200    | List of open hackathons                | `json<br>[<br>  { "id": "h_001", "title": "AI for Good", "deadline": "2025-08-01" },<br>  { "id": "h_002", "title": "Blockchain Innovators", "deadline": "2025-09-10" }<br>]` |
| 400    | Bad Request – invalid `type` parameter | `{ "error": "Invalid type. Must be 'open'." }`                                                                                                                                |
| 401    | Unauthorized – missing/invalid JWT     | `{ "error": "Missing or invalid JWT." }`                                                                                                                                      |
| 500    | Internal Server Error                  | `{ "error": "Failed to retrieve hackathons. Please try again later." }`                                                                                                       |

---

### 3.2 Join / Register Team

- **Endpoint:** `POST /hackathons/{hackId}/join`

**Request Body:**

```json
{
  "teamName": "Data Wizards",
  "members": ["u_01", "u_02"]
}
```

**Responses:**

| Status | Description                             | Example Body                                                      |
| ------ | --------------------------------------- | ----------------------------------------------------------------- |
| 200    | Registration successful                 | `{ "message": "Registered successfully" }`                        |
| 400    | Bad Request – missing/invalid team data | `{ "error": "Team name and members are required." }`              |
| 401    | Unauthorized – missing/invalid JWT      | `{ "error": "Missing or invalid JWT." }`                          |
| 403    | Forbidden – registration closed         | `{ "error": "Registration for this hackathon is closed." }`       |
| 404    | Not Found – hackathon does not exist    | `{ "error": "Hackathon not found." }`                             |
| 500    | Internal Server Error                   | `{ "error": "Failed to register team. Please try again later." }` |

---

### 3.3 Submit Prototype

- **Endpoint:** `POST /hackathons/{hackId}/submission`

**Request Body:**

```json
{
  "repoUrl": "https://github.com/…",
  "demoUrl": "https://demo…",
  "description": "Brief description of the prototype"
}
```

**Responses:**

| Status | Description                                   | Example Body                                                         |
| ------ | --------------------------------------------- | -------------------------------------------------------------------- |
| 201    | Submission received                           | `{ "submissionId": "s_789", "status": "received" }`                  |
| 400    | Bad Request – missing/invalid submission data | `{ "error": "repoUrl, demoUrl, and description are required." }`     |
| 401    | Unauthorized – missing/invalid JWT            | `{ "error": "Missing or invalid JWT." }`                             |
| 404    | Not Found – hackathon does not exist          | `{ "error": "Hackathon not found." }`                                |
| 415    | Unsupported Media Type – bad URL or format    | `{ "error": "Unsupported media type in submission." }`               |
| 500    | Internal Server Error                         | `{ "error": "Failed to submit prototype. Please try again later." }` |

---

### 3.4 Create Sponsored Hackathon

- **Endpoint:** `POST /hackathons/sponsored`

**Request Body:**

```json
{
  "title": "InsurTech Challenge",
  "description": "Solve insurance fraud detection with AI",
  "prizes": ["$10k", "$5k"],
  "deadline": "2025-09-15",
  "managers": ["u_admin1", "u_admin2"]
}
```

**Responses:**

| Status | Description                                  | Example Body                                                                               |
| ------ | -------------------------------------------- | ------------------------------------------------------------------------------------------ |
| 201    | Sponsored hackathon created                  | `{ "hackathonId": "h_s01" }`                                                               |
| 400    | Bad Request – missing/invalid hackathon data | `{ "error": "All fields (title, description, prizes, deadline, managers) are required." }` |
| 401    | Unauthorized – missing/invalid JWT           | `{ "error": "Missing or invalid JWT." }`                                                   |
| 500    | Internal Server Error                        | `{ "error": "Failed to create sponsored hackathon. Please try again later." }`             |

---

## 4. Legal Assistance

### 4.1 Generate NDA Template

- **Endpoint:** `POST /legal/nda`

**Request Body:**

```json
{
  "counterparty": "Acme Corp",
  "scope": "fintech prototype"
}
```

**Response:** `200 OK`

```json
{ "ndaUrl": "https://…/nda_001.docx" }
```

Alternate flow: `503 Service Unavailable` if AI generation fails.

---

### 4.2 Book Lawyer Review

- **Endpoint:** `POST /legal/nda/book`

**Request Body:**

```json
{
  "ndaUrl": "https://…/nda_001.docx",
  "slot": "2025-05-20T15:00Z"
}
```

**Response:** `200 OK`

```json
{ "appointmentId": "a_321" }
```

---

## 5. Mentorship

### 5.1 Find Mentor Matches

- **Endpoint:** `POST /mentorship/matches`

**Request Body:**

```json
{ "goals": ["Business strategy", "Fundraising"] }
```

**Response:** `200 OK`

```json
[
  { "mentorId":"m_11","name":"Anita","expertise":["FinTech"] },
  …
]
```

Alternate flow: empty array → suggest broadening criteria.

---

### 5.2 Request & Chat

- **Request Session:** `POST /mentorship/{mentorId}/request` → `200 OK` `{ "status": "pending" }`
- **Open Chat:** `GET /mentorship/{mentorId}/chat` → `200 OK` `{ "messages": [ … ] }`

---

---

## 6. Messaging & Notifications

### 6.1 Send Private Message

- **Endpoint:** `POST /messages`
- **Request Body:**

  ```json
  {
    "toUserId": "u_02",
    "text": "Congrats on your pitch!"
  }
  ```

- **Responses:**

| Status | Description                               | Example Body                                                     |
| ------ | ----------------------------------------- | ---------------------------------------------------------------- |
| 201    | Message sent successfully                 | `{ "messageId": "msg_501" }`                                     |
| 401    | Unauthorized – missing/invalid JWT        | `{ "error": "Missing or invalid JWT." }`                         |
| 404    | Not Found – recipient user does not exist | `{ "error": "Recipient user not found." }`                       |
| 500    | Internal Server Error                     | `{ "error": "Failed to send message. Please try again later." }` |

---

### 6.2 List Conversations

- **Endpoint:** `GET /messages/conversations`
- **Responses:**

| Status | Description                        | Example Body                                                                                              |
| ------ | ---------------------------------- | --------------------------------------------------------------------------------------------------------- |
| 200    | List of conversation summaries     | `json<br>[<br>  { "userId": "u_02", "unreadCount": 2 },<br>  { "userId": "u_05", "unreadCount": 0 }<br>]` |
| 401    | Unauthorized – missing/invalid JWT | `{ "error": "Missing or invalid JWT." }`                                                                  |
| 500    | Internal Server Error              | `{ "error": "Failed to fetch conversations. Please try again later." }`                                   |

---

## 7. Learning

### 7.1 Browse Events & Library

- **Endpoints:**

  - `GET /learning/events?type=live`
  - `GET /learning/events?type=recording`

- **Responses:**

| Status | Description                            | Example Body                                                                                                                                                      |
| ------ | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 200    | Array of events                        | `json<br>[<br>  { "id": "e_101", "title": "Intro to Blockchain", "type": "live" },<br>  { "id": "e_102", "title": "Scaling Startups", "type": "recording" }<br>]` |
| 400    | Bad Request – invalid `type` parameter | `{ "error": "Invalid event type. Must be 'live' or 'recording'." }`                                                                                               |
| 401    | Unauthorized – missing/invalid JWT     | `{ "error": "Missing or invalid JWT." }`                                                                                                                          |
| 500    | Internal Server Error                  | `{ "error": "Failed to retrieve events. Please try again later." }`                                                                                               |

---

### 7.2 Register & Watch

#### Register for Live Event

- **Endpoint:** `POST /learning/events/{eventId}/register`
- **Responses:**

| Status | Description                              | Example Body                                                    |
| ------ | ---------------------------------------- | --------------------------------------------------------------- |
| 200    | Registration successful                  | `{ "message": "Registered successfully." }`                     |
| 401    | Unauthorized – missing/invalid JWT       | `{ "error": "Missing or invalid JWT." }`                        |
| 404    | Not Found – event does not exist         | `{ "error": "Event not found." }`                               |
| 409    | Conflict – event full, added to waitlist | `{ "error": "Event full. You've been added to the waitlist." }` |
| 500    | Internal Server Error                    | `{ "error": "Registration failed. Please try again later." }`   |

#### Fetch Recording

- **Endpoint:** `GET /learning/events/{eventId}/recording`
- **Responses:**

| Status | Description                                         | Example Body                                                 |
| ------ | --------------------------------------------------- | ------------------------------------------------------------ |
| 200    | Recording URL returned                              | `{ "url": "https://cdn.example.com/rec_e102.mp4" }`          |
| 401    | Unauthorized – missing/invalid JWT                  | `{ "error": "Missing or invalid JWT." }`                     |
| 404    | Not Found – recording not available / event missing | `{ "error": "Recording not found." }`                        |
| 500    | Internal Server Error                               | `{ "error": "Failed to fetch recording. Try again later." }` |

---

## 8. Groups & Community

### 8.1 Create or Follow Group

#### Create Group

- **Endpoint:** `POST /groups`
- **Responses:**

| Status | Description                           | Example Body                             |
| ------ | ------------------------------------- | ---------------------------------------- |
| 201    | Group created                         | `{ "groupId": "g_09" }`                  |
| 400    | Bad Request – invalid or missing data | `{ "error": "Group name is required." }` |
| 401    | Unauthorized – missing/invalid JWT    | `{ "error": "Missing or invalid JWT." }` |
| 500    | Internal Server Error                 | `{ "error": "Failed to create group." }` |

#### Follow Group

- **Endpoint:** `POST /groups/{groupId}/follow`
- **Responses:**

| Status | Description                        | Example Body                                 |
| ------ | ---------------------------------- | -------------------------------------------- |
| 200    | Now following the group            | `{ "message": "Now following group g_09." }` |
| 401    | Unauthorized – missing/invalid JWT | `{ "error": "Missing or invalid JWT." }`     |
| 404    | Not Found – group does not exist   | `{ "error": "Group not found." }`            |
| 500    | Internal Server Error              | `{ "error": "Failed to follow group." }`     |

---

### 8.2 Join & Post in Group

#### Join Group

- **Endpoint:** `POST /groups/{groupId}/join`
- **Responses:**

| Status | Description                        | Example Body                             |
| ------ | ---------------------------------- | ---------------------------------------- |
| 200    | Successfully joined                | `{ "message": "Joined group g_09." }`    |
| 401    | Unauthorized – missing/invalid JWT | `{ "error": "Missing or invalid JWT." }` |
| 404    | Not Found – group does not exist   | `{ "error": "Group not found." }`        |
| 500    | Internal Server Error              | `{ "error": "Failed to join group." }`   |

#### Post in Group

- **Endpoint:** `POST /groups/{groupId}/posts`
- **Request Body:**

  ```json
  { "content": "Check out our first meetup!" }
  ```

- **Responses:**

| Status | Description                            | Example Body                                         |
| ------ | -------------------------------------- | ---------------------------------------------------- |
| 201    | Post created in group                  | `{ "postId": "gp_321", "message": "Post created." }` |
| 400    | Bad Request – empty or invalid content | `{ "error": "Content cannot be empty." }`            |
| 401    | Unauthorized – missing/invalid JWT     | `{ "error": "Missing or invalid JWT." }`             |
| 404    | Not Found – group does not exist       | `{ "error": "Group not found." }`                    |
| 500    | Internal Server Error                  | `{ "error": "Failed to create post." }`              |

---

## 9. User Following


### Table of Contents

1. [Connection Management](#91-connection-management)  
   1.1 [Follow User](#91-follow-user)  
   1.2 [Unfollow User](#92-unfollow-user)

2. [Connection Lists](#93-get-followers--following)  
   2.1 [Get Following List](#get-following)  
   2.2 [Get Followers List](#get-followers)

All connection-related endpoints are prefixed with `/api/v1/connections`

### 9.1 Follow User

- **Method:** `POST`
- **Endpoint:** `/follow/{userID}`
- **Description:** Follow another user

**Response:** `200 OK`

```json
{
  "message": "Successfully followed user {userID}"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                         |
| ----------- | --------------------- | ------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}` |
| 403         | Forbidden             | `{"error": "Cannot follow yourself"}` |
| 404         | Not Found             | `{"error": "User not found"}`         |
| 500         | Internal Server Error | `{"error": "Error message"}`          |

### 9.2 Unfollow User

- **Method:** `DELETE`
- **Endpoint:** `/unfollow/{userID}`
- **Description:** Unfollow a previously followed user

**Response:** `200 OK`

```json
{
  "message": "Successfully unfollowed user {userID}"
}
```

**Error Responses:**

| Status Code | Description           | Response Body                         |
| ----------- | --------------------- | ------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}` |
| 404         | Not Found             | `{"error": "User not found"}`         |
| 500         | Internal Server Error | `{"error": "Error message"}`          |

### Get Following

- **Method:** `GET`
- **Endpoint:** `/following/{userID}`
- **Description:** Get list of users that the specified user is following
- **Note:** Users can only view their own following list

**Response:** `200 OK`

```json
[
  {
    "id": 5,
    "username": "omarSbmc",
    "profilePictureUrl": null
  },
  {
    "id": 7,
    "username": "remonKMC",
    "profilePictureUrl": null
  }
]
```

**Error Responses:**

| Status Code | Description           | Response Body                                            |
| ----------- | --------------------- | -------------------------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}`                    |
| 403         | Forbidden             | `{"error": "You can only view your own following list"}` |
| 500         | Internal Server Error | `{"error": "Error message"}`                             |

### Get Followers

- **Method:** `GET`
- **Endpoint:** `/followers/{userID}`
- **Description:** Get list of users who follow the specified user

**Response:** `200 OK`

```json
[
  {
    "id": 5,
    "username": "omarSbmc",
    "profilePictureUrl": null
  },
  {
    "id": 7,
    "username": "remonKMC",
    "profilePictureUrl": null
  }
]
```

**Error Responses:**

| Status Code | Description           | Response Body                         |
| ----------- | --------------------- | ------------------------------------- |
| 401         | Unauthorized          | `{"error": "User not authenticated"}` |
| 404         | Not Found             | `{"error": "User not found"}`         |
| 500         | Internal Server Error | `{"error": "Error message"}`          |

## Versioning & Changelog

- **v1.0** – Initial release covering UC01–UC12
