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
10. [Common Response Errors](#common-response-errors)
11. [Versioning & Changelog](#versioning--changelog)

---

Here’s the corrected Markdown snippet with proper fencing and spacing:

# Auth section API


---

## Table of Contents

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

* **200 OK**

  ```json
  {
    "message": "Verification email sent",
    "verificationToken": "<token>"      // optional, if you return it
  }
  ```
* **400 Bad Request**

  ```json
  {
    "error": "Email is required"
  }
  ```
* **409 Conflict**

  ```json
  {
    "error": "Email already registered."
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to send verification email. Please try resending."
  }
  ```

---

### 1.2 Verify Email Token

* **Method:** `GET`
* **Endpoint:** `/verify-email?token=<emailToken>`
* **Description:** Confirm the user’s email by validating the token sent in the verification email.

**Query Parameters:**

| Name  | Type   | Required | Description                  |
| ----- | ------ | -------- | ---------------------------- |
| token | String | Yes      | The email verification token |

**Responses:**

* **200 OK**

  ```json
  {
    "message": "Email verified successfully."
  }
  ```
* **400 Bad Request**

  ```json
  {
    "error": "Invalid or expired verification token."
  }
  ```

---

## 2. User Registration & Login

### 2.1 Register User

* **Method:** `POST`
* **Endpoint:** `/register`
* **Description:** Create a new user account after email has been verified. Requires `Authorization` header with the Bearer token from **1.1**.

**Headers:**

```
Authorization: Bearer <verificationToken>
```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "StrongPass!23",
  // any additional registration fields your service requires
}
```

**Responses:**

* **201 Created**

  ```json
  {
    "userId": "u_12345",
    "email": "user@example.com",
    "token": "<JWT_TOKEN>"
  }
  ```
* **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header (Did you verify your email?)"
  }
  ```
* **409 Conflict**

  ```json
  {
    "error": "User with this email already exists."
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Registration failed. Please try again later."
  }
  ```

---

### 2.2 Login

* **Method:** `POST`
* **Endpoint:** `/login`
* **Description:** Authenticate a user and return a JWT.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "StrongPass!23"
}
```

**Responses:**

* **200 OK**

  ```json
  {
    "userId": "u_12345",
    "email": "user@example.com",
    "token": "<JWT_TOKEN>"
  }
  ```
* **401 Unauthorized**

  ```json
  {
    "error": "Invalid email or password."
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Login failed. Please try again later."
  }
  ```

---

## 3. Password Management

### 3.1 Forgot Password

* **Method:** `POST`
* **Endpoint:** `/forgot-password`
* **Description:** Send a password-reset email with a one-time token.

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

**Responses:**

* **200 OK**

  ```json
  {
    "message": "Password reset email sent successfully"
  }
  ```
* **400 Bad Request**

  ```json
  {
    "error": "Email is required"
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to send password reset email"
  }
  ```

---

### 3.2 Reset Password

* **Method:** `POST`
* **Endpoint:** `/reset-password?token=<resetToken>`
* **Description:** Update the user’s password using the reset token.

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

* **200 OK**

  ```json
  {
    "message": "Password updated successfully"
  }
  ```
* **400 Bad Request**

  ```json
  {
    "error": "Invalid or expired reset token."
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to update password"
  }
  ```

---

### 3.3 Change Password

* **Method:** `POST`
* **Endpoint:** `/change-password`
* **Description:** Change password for an authenticated user. Requires JWT.

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

* **200 OK**

  ```json
  {
    "message": "Password changed successfully"
  }
  ```
* **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to change password"
  }
  ```

---

## 4. Profile Management

### 4.1 Update Profile

* **Method:** `PATCH`
* **Endpoint:** `/update-profile`
* **Description:** Update user’s profile details. Requires JWT.

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "email": "new-email@example.com",         // optional
  "password": "OptionalNewPass!99",          // optional
  // any other updatable profile fields...
}
```

**Responses:**

* **200 OK**

  ```json
  {
    "message": "Profile updated successfully"
  }
  ```
* **400 Bad Request**

  ```json
  {
    "error": "Invalid request data"
  }
  ```
* **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to update profile"
  }
  ```

---

## 5. Account Management

### 5.1 Delete Account

* **Method:** `DELETE`
* **Endpoint:** `/delete-account`
* **Description:** Permanently delete the authenticated user’s account. Requires JWT.

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
```

**Responses:**

* **200 OK**

  ```json
  {
    "message": "Account deleted successfully"
  }
  ```
* **401 Unauthorized**

  ```json
  {
    "error": "Missing or invalid Authorization header"
  }
  ```
* **500 Internal Server Error**

  ```json
  {
    "error": "Failed to delete account"
  }
  ```

---

```
```


## 2. Posts & Interactions

### 2.1 Create Post

- **Endpoint:** `POST /posts`
- **Description:** Publish a new idea with optional media.

**Request Body:**

```json
{
  "title": "Micro-loan App for SMBs",
  "description": "A scalable P2P lending platform...",
  "category": "FinTech",
  "mediaUrls": ["https://.../pitch.pdf"] //optional
}
```

**Response:** `201 Created`

```json
{ "postId": "p_123", "status": "live" }
```

| Status Code | Description                        | Example Body                                                             |
| ----------- | ---------------------------------- | ------------------------------------------------------------------------ |
| 400         | Bad Request – policy violation     | `{ "error": "Policy violation: please revise your content." }`           |
| 502         | Bad Gateway – media upload failure | `{ "error": "Media upload failed: service unavailable, please retry." }` |

### 2.2 Like & Comment

#### Like a Post

- **Endpoint:** `POST /posts/{postId}/like`
- **Response:** `200 OK`

```json
{ "likes": 42 }
```

#### Comment on a Post

- **Endpoint:** `POST /posts/{postId}/comments`

**Request Body:**

```json
{ "text": "Great idea—how do you handle compliance?" }
```

**Response:** `201 Created`

```json
{ "commentId": "c_456", "message": "Comment added" }
```

---

---

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

### 9.1 Follow / Unfollow User

#### Follow User

- **Endpoint:** `POST /users/{userId}/follow`
- **Responses:**

| Status | Description                            | Example Body                                |
| ------ | -------------------------------------- | ------------------------------------------- |
| 200    | Now following the user                 | `{ "message": "Now following user u_02." }` |
| 401    | Unauthorized – missing/invalid JWT     | `{ "error": "Missing or invalid JWT." }`    |
| 404    | Not Found – target user does not exist | `{ "error": "User not found." }`            |
| 500    | Internal Server Error                  | `{ "error": "Failed to follow user." }`     |

#### Unfollow User

- **Endpoint:** `DELETE /users/{userId}/follow`
- **Responses:**

| Status | Description                            | Example Body                              |
| ------ | -------------------------------------- | ----------------------------------------- |
| 204    | Unfollowed successfully (no content)   | _empty body_                              |
| 401    | Unauthorized – missing/invalid JWT     | `{ "error": "Missing or invalid JWT." }`  |
| 404    | Not Found – target user does not exist | `{ "error": "User not found." }`          |
| 500    | Internal Server Error                  | `{ "error": "Failed to unfollow user." }` |

---

### 9.2 Get Followers & Following

#### Get Followers

- **Endpoint:** `GET /users/{userId}/followers`
- **Responses:**

| Status | Description                        | Example Body                                                                                  |
| ------ | ---------------------------------- | --------------------------------------------------------------------------------------------- |
| 200    | List of followers                  | `json<br>[<br>  { "id": "u_05", "name": "Anita" },<br>  { "id": "u_08", "name": "Jin" }<br>]` |
| 401    | Unauthorized – missing/invalid JWT | `{ "error": "Missing or invalid JWT." }`                                                      |
| 404    | Not Found – user does not exist    | `{ "error": "User not found." }`                                                              |
| 500    | Internal Server Error              | `{ "error": "Failed to fetch followers." }`                                                   |

#### Get Following

- **Endpoint:** `GET /users/{userId}/following`
- **Responses:**

| Status | Description                        | Example Body                                                                                 |
| ------ | ---------------------------------- | -------------------------------------------------------------------------------------------- |
| 200    | List of users being followed       | `json<br>[<br>  { "id": "u_02", "name": "Sara" },<br>  { "id": "u_07", "name": "Lee" }<br>]` |
| 401    | Unauthorized – missing/invalid JWT | `{ "error": "Missing or invalid JWT." }`                                                     |
| 404    | Not Found – user does not exist    | `{ "error": "User not found." }`                                                             |
| 500    | Internal Server Error              | `{ "error": "Failed to fetch following list." }`                                             |

---

## Common Response Errors

| Status | Description                              |
| ------ | ---------------------------------------- |
| 400    | Validation failed (missing/invalid data) |
| 401    | Missing or invalid JWT                   |
| 403    | Forbidden (e.g., feature disabled)       |
| 404    | Resource not found                       |
| 409    | Conflict (e.g., duplicate registration)  |
| 500    | Server error                             |

---

## Versioning & Changelog

- **v1.0** – Initial release covering UC01–UC12

---
