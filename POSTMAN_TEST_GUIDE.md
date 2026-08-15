# GDS Portal — Complete API Testing Guide (Postman)

Everything you need to test **every endpoint**, plus **authentication** and **authorization**, using Postman.

> Roles are already seeded in your DB, so you only need to create ONE account by hand (the first ADMIN). After that you create all other accounts through the API itself.

---

## Legend used in this guide

| Symbol | Meaning |
|---|---|
| `A` | ADMINISTRATOR |
| `P` | PRINCIPAL |
| `T` | TEACHER |
| `S` | STUDENT |
| `Pa` | PARENT |
| `Self` | the logged-in account is the owner of the resource |
| `Any` | any authenticated user (any role) |

### Global facts about every request

- **Base URL** (Tomcat default): `http://localhost:8080/gds-potal`
- **Everything** under `/api/*` requires `Authorization: Bearer <token>` EXCEPT:
  - `POST /api/auth/login`
  - `POST /api/auth/request-password-reset`
  - `POST /api/auth/reset-password`
  - `POST /api/auth/refresh`
  - `POST /api/auth/logout`
- **Response envelope** — every response (success AND error) looks like:
  ```json
  { "success": true|false, "message": "...", "data": {...}, "errors": [], "status": 200 }
  ```
- **Dates** = `YYYY-MM-DD` (e.g. `2026-08-07`). **Times** = `HH:MM:SS` (e.g. `08:30:00`).
- **Enums** are sent as plain strings (e.g. `"gender": "MALE"`, `"status": "PRESENT"`).
- **Booleans** in JSON accept BOTH `"active"` and `"isActive"` (same for `current`/`isCurrent`, `read`/`isRead`, `locked`/`isLocked`). Responses always use the short form (`active`, `current`, `read`, `locked`).
- `GET /api/students` and `GET /api/students/` are the same thing (trailing slash is optional).
- Email **must end in `@gmail.com`** and is stored lower-cased. Passwords need: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char (e.g. `Admin@123`).

### IMPORTANT status-code quirk (affects authorization testing)

`RoleGuard` returns **401** (not 403) when a user has a valid token but the **wrong role**. So:

| Scenario | Code |
|---|---|
| Missing / malformed / expired token | `401` |
| Valid token, wrong role (access denied) | `401` (message says "Access denied. Required role: ...") |
| Account is disabled | `403` |
| Invalid JSON body | `400` |
| Validation / business-rule failure | `400` |
| Not found | `404` |
| Duplicate (already exists) | `409` |
| DB error / unexpected | `500` |

---

## Part 0 — Prerequisites

1. Java 17+ installed.
2. MySQL running with the `gds_portal` schema created (your existing schema).
3. `roles` table already seeded (you said it is).
4. `.env` file at the repo root with `DB_URL`, `DB_USER`, `DB_PASS`, `JWT_SECRET` set. If you run the WAR in standalone Tomcat, put a copy of `.env` in Tomcat's `bin` folder (the loader looks in `user.dir` and `CATALINA_BASE`) or start Tomcat with `-Denv.file=C:\path\to\.env`.
5. The `revoked_tokens` table exists (used to blacklist access tokens on logout). Create it if missing:
   ```sql
   CREATE TABLE IF NOT EXISTS revoked_tokens (
       jti         VARCHAR(64) PRIMARY KEY,
       expires_at  DATETIME    NOT NULL,
       created_at  DATETIME    NOT NULL
   );
   ```
6. The app is built (`mvn -o package -DskipTests`) and the WAR `target/gds-potal.war` is deployed, OR you run it from your IDE on a Tomcat 10/11 embedded server.
7. Open `http://localhost:8080/gds-potal/` in a browser — the endpoint `/api/...` is what Postman calls.

---

## Part 1 — Insert the first ADMIN (do this ONCE, via SQL)

The app has no "register" endpoint, and creating an admin requires an admin token (chicken-and-egg). So the very first admin is inserted straight into the database.

Run this in MySQL (Workbench / CLI / phpMyAdmin):

```sql
-- 1) Create the login account (role pulled from your seeded roles table)
INSERT INTO users (role_id, username, email, password_hash, profile_picture_url, is_active, last_login_at, created_at, updated_at)
VALUES (
  (SELECT role_id FROM roles WHERE role_name = 'ADMINISTRATOR'),
  'admin',
  'admin@gmail.com',
  '$2a$10$nnYqLn8vqj.6pv97czlDl.UnacfJQjpTdEkeKpKPUvXWNYd7lZbdO',
  NULL,
  TRUE,
  NULL,
  NOW(),
  NOW()
);

-- 2) Link an Administrator profile to that user
INSERT INTO administrators (user_id, employee_id, first_name, last_name, phone)
VALUES (
  (SELECT user_id FROM users WHERE email = 'admin@gmail.com'),
  'EMP-001',
  'System',
  'Admin',
  '923211234567'
);
```

**You can now log in with:**
```
email:    admin@gmail.com
password: Admin@123
```

Notes:
- The hash above is a verified BCrypt hash of `Admin@123`.
- `phone` is stored in the app's canonical format: `03XXXXXXXXX` → `92XXXXXXXXX`. `923211234567` is already canonical.
- Once this admin works, **create every other account through the API** (see Part 4). You never need SQL again.

### (Optional) Generate your own password hash

Save this as `GenHash.java` in any folder, then run it from a terminal (Command Prompt):

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GenHash {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode(args[0]));
    }
}
```

```
set CRYPTO=%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.5.1\spring-security-crypto-6.5.1.jar
set COMMONS=%USERPROFILE%\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar
"%JAVA_HOME%\bin\java" -cp "%CRYPTO%;%COMMONS%" GenHash.java YourPassword123!
```

Copy the printed hash into the SQL above.

---

## Part 2 — Postman setup (do once)

1. **Create a collection** — e.g. "GDS Portal".
2. **Create an environment** — e.g. "Local". Add these initial variables:
   | Variable | Initial value |
   |---|---|
   | `baseUrl` | `http://localhost:8080/gds-potal` |
   | `accessToken` | *(empty)* |
   | `refreshToken` | *(empty)* |
   | `userId` | *(empty)* |
3. **Add the Login request** to the collection:
   - Method `POST`, URL `{{baseUrl}}/api/auth/login`
   - Body (JSON):
     ```json
     { "email": "admin@gmail.com", "password": "Admin@123" }
     ```
   - **Tests** tab:
     ```js
     const json = pm.response.json();
     if (json.success) {
       pm.environment.set("accessToken", json.data.token);
       pm.environment.set("refreshToken", json.data.refreshToken);
       pm.environment.set("userId", String(json.data.user.userId));
       pm.environment.set("role", json.data.profile.role.roleName);
     }
     ```
   - Hit **Send**. The variables are now filled automatically.
4. **Reuse the token everywhere**: for every protected request, add this header:
   ```
   Authorization: Bearer {{accessToken}}
   ```
   (Tip: set it once at the **Collection → Authorization** level, set type to `Bearer Token` and the token to `{{accessToken}}`, then every request inherits it.)
5. **Add the Logout request** to the collection:
   - Method `POST`, URL `{{baseUrl}}/api/auth/logout`
   - Body (JSON) — send BOTH tokens so the access token is blacklisted too:
     ```json
     { "accessToken": "{{accessToken}}", "refreshToken": "{{refreshToken}}" }
     ```
   - After it runs, the access token is revoked. Any later request with `Authorization: Bearer {{accessToken}}` → `401` until you login again.

---

## Part 3 — Authentication & token flow (test these first)

All six are `POST /api/auth/...`.

| # | Path | Body | Public? | Notes |
|---|---|---|---|---|
| 1 | `/login` | `{ "email", "password" }` | ✅ | Returns `data.token`, `data.refreshToken`, `data.user`, `data.profile` |
| 2 | `/users/me` (GET) | — | JWT only | Use it to confirm the token works. Open to any role. |
| 3 | `/change-password` | `{ "currentPassword", "newPassword" }` | JWT only | Applies to the logged-in user. New password must satisfy policy. |
| 4 | `/refresh` | `{ "refreshToken" }` | ✅ | Returns a **new** `token` and **new** `refreshToken`. The old refresh token is consumed — reuse it again → `401` (that's correct!). |
| 5 | `/logout` | `{ "accessToken", "refreshToken" }` | ✅ | Burns the refresh token **and** blacklists the access token (via its `jti`). Refreshing after logout → `401`; using the logged-out access token on any endpoint → `401`. |
| 6 | `/request-password-reset` | `{ "email" }` | ✅ | Returns success even if the email doesn't exist. **No email is actually sent** — the reset token is stored in the `password_reset_tokens` table. |
| 7 | `/reset-password` | `{ "token", "newPassword" }` | ✅ | The `token` is the **raw** value you read from `password_reset_tokens.token_hash` is a hash — read the raw token from your code only if you implement email. For manual testing, generate a reset via the app and copy the raw token before hashing, or create a row with a known hash (hash = sha256(raw)). |

> Manual reset-token tip: `token_hash` is `SHA-256(rawToken)`. To test `/reset-password` by hand, generate any string, SHA-256 it (e.g. `echo -n "MyRawToken" | openssl dgst -sha256`), insert a `password_reset_tokens` row with that hash for your user, then call `/reset-password` with `"token": "MyRawToken"`.

**Authentication negative tests:**
- Call any `/api/...` endpoint with **no** `Authorization` header → `401`.
- Call with `Authorization: Bearer garbage` → `401`.
- Call with an expired/invalid token → `401`.
- Call with an access token that was blacklisted by `/logout` → `401` (`Token has been revoked.`).
- Call `/api/auth/login` with wrong password → `401`.
- Login of a deactivated account → `403`.

---

## Part 4 — Build your test data (happy path, as ADMIN)

Do this in this order so every later endpoint has data to work with. All calls below are ADMIN (the `{{accessToken}}` you saved).

> After each create, copy the returned `id` (e.g. `data.academicYearId`) into an environment variable or just note it. The examples below assume ids 1, 2, 3... in creation order.

### 4.1 Reference data (admin creates these)

**Academic year** — `POST {{baseUrl}}/api/academic-years`
```json
{ "yearName": "2026-2027", "startDate": "2026-09-01", "endDate": "2027-06-30", "isCurrent": true }
```
Then `PUT /api/academic-years/set-current/1` (or whatever id it got).

**Class** — `POST {{baseUrl}}/api/classes`
```json
{ "className": "Grade 9", "numericLevel": 9, "description": "Ninth grade" }
```

**Section** — `POST {{baseUrl}}/api/sections`
```json
{ "classId": 1, "academicYearId": 1, "sectionName": "A", "capacity": 40, "roomNumber": "201" }
```

**Subject** — `POST {{baseUrl}}/api/subjects`
```json
{ "subjectName": "Mathematics", "subjectCode": "MATH-101", "description": "Algebra & Geometry" }
```

**Period** — `POST {{baseUrl}}/api/periods`
```json
{ "periodNumber": 1, "startTime": "08:30:00", "endTime": "09:15:00" }
```
(create a few more: periods 2 and 3)

### 4.2 Create the accounts (each creates its `users` row + profile row)

**Teacher** — `POST {{baseUrl}}/api/teachers`
```json
{
  "email": "teacher1@gmail.com",
  "username": "teacher1",
  "password": "Teacher@123",
  "teacher": {
    "employeeId": "EMP-1001",
    "firstName": "Sara",
    "lastName": "Ahmed",
    "phone": "03211234567",
    "gender": "FEMALE",
    "dateOfBirth": "1990-03-15",
    "hireDate": "2020-09-01",
    "qualification": "MSc Mathematics"
  }
}
```
> Phone formats accepted: `03XXXXXXXXX` (11 digits) or `92XXXXXXXXX` (12 digits).

**Student** — `POST {{baseUrl}}/api/students`
```json
{
  "email": "student1@gmail.com",
  "username": "student1",
  "password": "Student@123",
  "student": {
    "registrationNumber": "REG-2026-001",
    "firstName": "Ali",
    "lastName": "Khan",
    "dateOfBirth": "2010-05-12",
    "gender": "MALE",
    "admissionDate": "2026-08-01"
  }
}
```

**Parent** — `POST {{baseUrl}}/api/parents`
```json
{
  "email": "parent1@gmail.com",
  "username": "parent1",
  "password": "Parent@123",
  "parent": {
    "firstName": "Imran",
    "lastName": "Khan",
    "phone": "03219876543",
    "occupation": "Engineer"
  }
}
```

**Principal** — `POST {{baseUrl}}/api/principals`
```json
{
  "email": "principal@gmail.com",
  "username": "principal",
  "password": "Principal@123",
  "principal": {
    "employeeId": "EMP-2001",
    "firstName": "Ayesha",
    "lastName": "Malik",
    "phone": "03214567890"
  }
}
```

**A second Admin** (to prove admin creation works) — `POST {{baseUrl}}/api/administrators`
```json
{
  "email": "admin2@gmail.com",
  "username": "admin2",
  "password": "Admin2@123",
  "administrator": {
    "employeeId": "EMP-0002",
    "firstName": "Second",
    "lastName": "Admin",
    "phone": "03215551234"
  }
}
```

### 4.3 Link + enroll + timetables

**Enroll student** — `POST {{baseUrl}}/api/enrollments/enroll`
```json
{ "studentId": 1, "classId": 1, "sectionId": 1, "academicYearId": 1, "rollNumber": "01" }
```

**Link parent** — `POST {{baseUrl}}/api/parents/link`
```json
{ "studentId": 1, "parentId": 1, "relationshipType": "FATHER", "primaryContact": true }
```

**Assign teacher → class** — `POST {{baseUrl}}/api/teacher-classes`
```json
{ "teacherId": 1, "classId": 1, "sectionId": 1, "academicYearId": 1 }
```

**Assign teacher → subject** — `POST {{baseUrl}}/api/teacher-subjects`
```json
{ "teacherId": 1, "subjectId": 1, "sectionId": 1, "academicYearId": 1 }
```

**Assign class teacher** — `POST {{baseUrl}}/api/class-teachers`
```json
{ "teacherId": 1, "sectionId": 1, "academicYearId": 1 }
```

**Timetable entry** — `POST {{baseUrl}}/api/timetable`
```json
{ "sectionId": 1, "subjectId": 1, "teacherId": 1, "periodId": 1, "dayOfWeek": "MON", "academicYearId": 1 }
```
> `dayOfWeek` values: `MON, TUE, WED, THU, FRI, SAT, SUN`.

### 4.4 Academic work

**Examination** — `POST {{baseUrl}}/api/examinations`
```json
{
  "examName": "Midterm",
  "subjectId": 1,
  "sectionId": 1,
  "academicYearId": 1,
  "examDate": "2026-10-15",
  "startTime": "09:00:00",
  "endTime": "11:00:00",
  "maxMarks": 100,
  "passingMarks": 40,
  "status": "SCHEDULED"
}
```
> `status` values: `SCHEDULED, ONGOING, COMPLETED, PUBLISHED`.

**Assignment** — `POST {{baseUrl}}/api/assignments`
```json
{
  "teacherId": 1,
  "subjectId": 1,
  "sectionId": 1,
  "title": "Algebra Homework",
  "description": "Solve exercises 1-10",
  "maxMarks": 20,
  "deadline": "2026-09-20T23:59:00",
  "status": "CREATED"
}
```
> ⚠️ `teacherId` is the **teacher profile id** (`data.teacherId` from `GET /api/teachers/me`), not the user id. When a TEACHER creates an assignment and omits `teacherId`, the app substitutes their user id, which may not match — so always pass `teacherId` explicitly when testing.

**Marks** — `POST {{baseUrl}}/api/marks`
```json
{
  "marks": [
    { "examinationId": 1, "studentId": 1, "marksObtained": 85, "grade": "A", "remarks": "Good" }
  ]
}
```
> `marksObtained` cannot exceed `maxMarks` of the examination (else `400`). `enteredBy` is auto-filled from the token.

### 4.5 Attendance

**Student attendance** — `POST {{baseUrl}}/api/attendance/students`
```json
{
  "records": [
    { "studentClassId": 1, "attendanceDate": "2026-08-07", "status": "PRESENT", "periodId": 1, "remarks": "" }
  ]
}
```
> `status` values: `PRESENT, ABSENT, LATE, LEAVE`. `markedBy` is auto-filled. A second record for the same student+date+period → `400` (duplicate). Adding the **same date but a different `periodId`** is allowed (multi-period attendance).

**Lock a day** — `POST {{baseUrl}}/api/attendance/students/lock`
```json
{ "date": "2026-08-07", "sectionId": 1 }
```
> After locking, changing/adding attendance for that date is rejected.

**Teacher attendance** — `POST {{baseUrl}}/api/attendance/teachers`
```json
{
  "records": [
    { "teacherId": 1, "attendanceDate": "2026-08-07", "status": "PRESENT", "checkInTime": "08:20:00", "checkOutTime": null }
  ]
}
```
> `markedAt` is auto-filled.

### 4.6 Communication

**Announcement** — `POST {{baseUrl}}/api/announcements`
```json
{ "title": "Exam schedule", "content": "Midterms start Oct 15.", "targetRoleId": null, "classId": null, "sectionId": null }
```
> `createdBy`/`createdAt` auto-filled. Target fields are nullable (`Integer`).

**Notification (single)** — `POST {{baseUrl}}/api/notifications`
```json
{ "userId": 1, "notificationType": "NEW_ANNOUNCEMENT", "title": "Welcome", "message": "Welcome to the portal" }
```
> `notificationType` values: `NEW_ASSIGNMENT, NEW_MESSAGE, RESULT_PUBLISHED, ATTENDANCE_ALERT, NEW_ANNOUNCEMENT`.

**Notification (bulk)** — `POST {{baseUrl}}/api/notifications/bulk`
```json
{
  "notifications": [
    { "userId": 1, "notificationType": "NEW_ANNOUNCEMENT", "title": "T1", "message": "M1" },
    { "userId": 2, "notificationType": "NEW_ASSIGNMENT", "title": "T2", "message": "M2" }
  ]
}
```

---

## Part 5 — Complete endpoint matrix (every single endpoint)

Legend: `A`=ADMINISTRATOR, `P`=PRINCIPAL, `T`=TEACHER, `S`=STUDENT, `Pa`=PARENT, `Self`=owner.

> Rule meanings:
> - **Self** — you may touch your own record/notifications/submissions.
> - **Pa (linked)** — parent can only access the students they are linked to.
> - **T self** — teacher can only read their own teacher data (`/teacher/{teacherId}` where the id is *their own profile id*).

### 5.1 Auth — `POST /api/auth/*`

| Path | Roles | Body / Notes |
|---|---|---|
| `/login` | Public | `{email, password}` → tokens |
| `/refresh` | Public | `{refreshToken}` → new tokens |
| `/logout` | Public | `{accessToken, refreshToken}` → burns the refresh token and blacklists the access token |
| `/request-password-reset` | Public | `{email}` |
| `/reset-password` | Public | `{token, newPassword}` |
| `/change-password` | Any (JWT) | `{currentPassword, newPassword}` |

### 5.2 Users — `/api/users/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/me` | Any | your own profile |
| GET | `/` | A, P | list all |
| GET | `/role/{roleId}` | A, P | |
| GET | `/email/{email}` | A, P | |
| GET | `/username/{username}` | A, P | |
| GET | `/{userId}` | A, P | |
| POST | `/` | A | `CreateUserRequest {email, username, password, roleId}` |
| PUT | `/profile-picture/{userId}` | Self or A | `{profilePictureUrl}` |
| PUT | `/status` | A | `{userId, active}` |
| PUT | `/{userId}` | A | `{roleId, username, email, profilePictureUrl}` |
| DELETE | `/{userId}` | A | deletes user |

### 5.3 Roles — `/api/roles/*`

| Method | Path | Roles |
|---|---|---|
| GET | `/` | Any |
| GET | `/name/{name}` | Any |
| GET | `/{id}` | Any |

### 5.4 Academic years — `/api/academic-years/*`

| Method | Path | Roles | Body |
|---|---|---|---|
| GET | `/current` | Any | |
| GET | `/` | A, P, T | |
| GET | `/{id}` | A, P, T | |
| POST | `/` | A | `AcademicYear` |
| PUT | `/set-current/{id}` | A | |
| PUT | `/{id}` | A | `AcademicYear` |

### 5.5 Classes — `/api/classes/*`

| Method | Path | Roles | Body |
|---|---|---|---|
| GET | `/` | A, P, T | |
| GET | `/level/{level}` | A, P, T | |
| GET | `/{id}` | A, P, T | |
| POST | `/` | A | `{className, numericLevel, description}` |
| PUT | `/{id}` | A | `Class` |
| DELETE | `/{id}` | A | |

### 5.6 Sections — `/api/sections/*`

| Method | Path | Roles | Body |
|---|---|---|---|
| GET | `/` | A, P, T | |
| GET | `/class/{classId}/{academicYearId}` | A, P, T | |
| GET | `/{id}` | A, P, T | |
| POST | `/` | A | `{classId, academicYearId, sectionName, capacity, roomNumber}` |
| PUT | `/{id}` | A | `Section` |
| DELETE | `/{id}` | A | |

### 5.7 Subjects — `/api/subjects/*`

| Method | Path | Roles | Body |
|---|---|---|---|
| GET | `/` | A, P, T | |
| GET | `/{id}` | A, P, T | |
| POST | `/` | A | `{subjectName, subjectCode, description}` |
| PUT | `/{id}` | A | `Subject` |
| DELETE | `/{id}` | A | |

### 5.8 Periods — `/api/periods/*`

| Method | Path | Roles | Body |
|---|---|---|---|
| GET | `/` | A, P, T | |
| GET | `/{id}` | A, P, T | |
| POST | `/` | A | `{periodNumber, startTime, endTime}` (no overlapping ranges) |
| PUT | `/{id}` | A | `Period` |
| DELETE | `/{id}` | A | |

### 5.9 Teachers — `/api/teachers/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/me` | Self or A, P | |
| GET | `/` | A, P | list |
| GET | `/class-teachers` | A, P | list class teachers |
| GET | `/search/{term}` | A, P | |
| GET | `/subject/{subjectId}` | A, P | |
| GET | `/section/{sectionId}/{academicYearId}` | A, P | |
| GET | `/user/{userId}` | A, P | |
| GET | `/employee/{employeeId}` | A, P | |
| GET | `/email/{email}` | A, P | |
| GET | `/count` | A, P | |
| GET | `/{teacherId}` | Self or A, P | |
| POST | `/` | A | `CreateTeacherRequest` |
| PUT | `/{teacherId}` | A | `Teacher` |
| DELETE | `/{teacherId}` | A | deactivate |

### 5.10 Students — `/api/students/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/me` | Self or A, P | |
| GET | `/` | A, P | list |
| GET | `/class/{classId}/{academicYearId}` | A, P, T | |
| GET | `/section/{sectionId}` | A, P, T | students of the section's current year |
| GET | `/parent/{parentId}` | Pa (own) or A, P | parent may only use their own parentId |
| GET | `/search/{term}` | A, P, T | |
| GET | `/registration/{regNumber}` | A, P, T | |
| GET | `/user/{userId}` | Self, Pa(linked), A, P, T | |
| GET | `/{studentId}` | Self, Pa(linked), A, P, T | |
| POST | `/` | A | `CreateStudentRequest` |
| PUT | `/{studentId}` | A | `Student` |
| DELETE | `/{studentId}` | A | deactivate |

### 5.11 Parents — `/api/parents/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/me` | Self or A, P | |
| GET | `/` | A, P | ⚠️ **always returns 400** ("not supported") — expected |
| GET | `/student/{studentId}` | Pa (linked) or A, P | |
| GET | `/user/{userId}` | Self or A, P | |
| GET | `/{parentId}` | Self or A, P | |
| POST | `/link` | A | `{studentId, parentId, relationshipType, primaryContact}` |
| POST | `/` | A | `CreateParentRequest` |
| PUT | `/{parentId}` | A | `Parent` |
| DELETE | `/link/{parentId}/{studentId}` | A | unlink |
| DELETE | `/{parentId}` | A | deactivate |

### 5.12 Principals — `/api/principals/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/` | A, P | |
| GET | `/user/{userId}` | A, P | |
| GET | `/{principalId}` | A, P | |
| POST | `/` | A | `CreatePrincipalRequest` |
| PUT | `/{principalId}` | A | `Principal` |
| DELETE | `/{principalId}` | A | deactivate |

### 5.13 Administrators — `/api/administrators/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/` | A, P | |
| GET | `/user/{userId}` | A, P | |
| GET | `/{adminId}` | A, P | |
| POST | `/` | A | `CreateAdministratorRequest` |
| PUT | `/{adminId}` | A | `Administrator` |
| DELETE | — | — | (no delete endpoint) |

### 5.14 Enrollments — `/api/enrollments/*`

| Method | Path | Roles | Body / Notes |
|---|---|---|---|
| GET | `/student/{studentId}` | A, P, T | history |
| GET | `/current/{studentId}` | A, P, T | |
| GET | `/section/{sectionId}/{academicYearId}` | A, P, T | |
| POST | `/enroll` | A | `{studentId, classId, sectionId, academicYearId, rollNumber}` |
| POST | `/transfer` | A | `{studentId, academicYearId, newSectionId, rollNumber}` |
| POST | `/promote` | A, P | `{sourceSectionId, sourceAcademicYearId, targetSectionId, targetAcademicYearId}` |
| PUT | `/end/{studentId}/{academicYearId}` | A | end enrollment |
| PUT | `/roll-number/{studentId}/{academicYearId}` | A | `{newRollNumber}` |

### 5.15 Timetable — `/api/timetable/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/section/{sectionId}/{academicYearId}` | A, P, T | |
| GET | `/teacher/{teacherId}/{academicYearId}` | A, P, T | **T self** — teacher sees only their own timetable |
| GET | `/day/{sectionId}/{day}` | A, P, T | `day` = `MON`..`SUN` |
| POST | `/` | A | `Timetable` |
| PUT | `/{timetableId}` | A | `Timetable` |
| DELETE | `/{timetableId}` | A | |

### 5.16 Examinations — `/api/examinations/*`

| Method | Path | Roles |
|---|---|---|
| GET | `/id/{id}` | A, P, T |
| GET | `/section/{sectionId}/year/{academicYearId}` | A, P, T |
| GET | `/section/{sectionId}/type/{examName}/year/{academicYearId}` | A, P, T |
| GET | `/teacher/{teacherId}/year/{academicYearId}` | A, P, T |
| GET | `/teacher/{teacherId}/type/{examName}/year/{academicYearId}` | A, P, T |
| GET | `/teacher/{teacherId}/section/{sectionId}/year/{academicYearId}` | A, P, T |
| GET | `/teacher/{teacherId}/section/{sectionId}/type/{examName}/year/{academicYearId}` | A, P, T |
| POST | `/` | A, P, T |
| PUT | `/status/{id}` | A, P, T — body `{status}` |
| PUT | `/{id}` | A, P, T |
| DELETE | — | (no delete endpoint) |

### 5.17 Assignments — `/api/assignments/*`

| Method | Path | Roles   | Notes |
|---|---|---------|---|
| GET | `/id/{id}` | A, P, T | |
| GET | `/section/{sectionId}/{academicYearId}` | A, P, T | |
| GET | `/teacher/{teacherId}/{academicYearId}` | A, P, T | **T self** |
| POST | `/` | T       | always pass `teacherId` explicitly |
| PUT | `/publish/{id}` | T       | |
| PUT | `/{id}` | T       | |
| DELETE | `/{id}` | A, P, T | |

### 5.18 Submissions — `/api/submissions/*`

| Method | Path | Roles | Notes                                                        |
|---|---|---|--------------------------------------------------------------|
| GET | `/count/{assignmentId}` | A, P, T |                                                              |
| GET | `/id/{submissionId}` | S(own), A, P, T |                                                              |
| GET | `/assignment/{assignmentId}` | A, P, T |                                                              |
| GET | `/assignment/{assignmentId}/student/{studentId}` | S(own), A, P, T |                                                              |
| GET | `/student/{studentId}` | S(own), A, P, T |                                                              |
| GET | `/status/{status}` | A, P, T | `status` = `SUBMITTED, LATE, GRADED`                         |
| POST | `/` | S | **TEACHER, ADMIN, PRINCIPAL not allowed**                    |
| PUT | `/grade/{submissionId}` | A, P, T | `{marksAwarded, feedback, gradedBy}`                         |
| PUT | `/late/{submissionId}` | A, P, T | mark as late                                                 |
| PUT | `/{submissionId}` | S(own), A, P | student can edit only their own, grading fields are stripped |
| DELETE | `/{submissionId}` | S(own), A, P |                                                              |

**Submission create body (as STUDENT):**
```json
{ "assignmentId": 1, "fileUrl": "https://drive.example.com/hw1.pdf", "status": "SUBMITTED" }
```
> `studentId` is auto-set to the logged-in student. As S/P you need `studentId` for lookups — use your student profile id from `GET /api/students/me`.

### 5.19 Marks — `/api/marks/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/id/{id}` | A, P, T | |
| GET | `/student/{studentId}/examination/{examinationId}` | S(own), Pa(linked), A, P, T | |
| GET | `/student/{studentId}/year/{academicYearId}` | S(own), Pa(linked), A, P, T | |
| GET | `/student/{studentId}/type/{examName}/year/{academicYearId}` | S(own), Pa(linked), A, P, T | |
| GET | `/examination/{examinationId}` | A, P, T | |
| POST | `/` | A, P, T | `{marks:[...]}` |
| PUT | `/{markId}` | A, P, T | partial body OK (missing fields restored) |
| DELETE | `/{markId}` | A, P | |

### 5.20 Announcements — `/api/announcements/*`

| Method | Path | Roles |
|---|---|---|
| GET | `/` | Any |
| GET | `/id/{id}` | Any |
| GET | `/role/{roleId}` | Any |
| GET | `/class/{classId}` | Any |
| GET | `/section/{sectionId}` | Any |
| POST | `/` | A, P |
| PUT | `/disable/{id}` | A, P |
| PUT | `/{id}` | A, P |

### 5.21 Notifications — `/api/notifications/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/me/unread-count` | Any | |
| GET | `/me/unread` | Any | |
| GET | `/me?limit=20&offset=0` | Any | query params optional |
| GET | `/id/{notificationId}` | Self or A | |
| POST | `/bulk` | A, P | `{notifications:[...]}` |
| POST | `/` | A, P | `Notification` |
| PUT | `/read-all` | Any | |
| PUT | `/read/{notificationId}` | Self or A | |
| DELETE | `/me/all` | Any | |
| DELETE | `/{notificationId}` | Self or A | |

### 5.22 Student attendance — `/api/attendance/students/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/id/{id}` | A, P, T | |
| GET | `/section/{sectionId}/date/{date}` | A, P, T | `date` = `YYYY-MM-DD` |
| GET | `/student/{studentId}?startDate=...&endDate=...` | S(own), Pa(linked), A, P, T | **both query params required** |
| POST | `/lock` | A, P, T | `{date, sectionId}` |
| POST | `/` | A, P, T | `{records:[...]}` |
| PUT | `/status/{attendanceId}` | A, P, T | `{status}` |

### 5.23 Teacher attendance — `/api/attendance/teachers/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/id/{id}` | A, P, T | |
| GET | `/date/{date}` | A, P | |
| GET | `/teacher/{teacherId}?startDate=...&endDate=...` | A, P, T | **T self**; both query params required |
| POST | `/` | A, P | `{records:[...]}` (TEACHER not allowed) |
| PUT | `/status/{attendanceId}` | A, P, T | `{status}` |
| PUT | `/check-in/{attendanceId}` | A, P, T | `{time: "08:30:00"}` |
| PUT | `/check-out/{attendanceId}` | A, P, T | `{time: "16:00:00"}` |

### 5.24 Class teacher assignments — `/api/class-teachers/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/section/{sectionId}` | A, P, T | current class teacher |
| GET | `/section/{sectionId}/history` | A, P | |
| GET | `/teacher/{teacherId}/year/{academicYearId}` | A, P, T | |
| GET | `/teacher/{teacherId}/assigned?academicYearId=...` | A, P, T | **query param required** |
| GET | `/teacher/{teacherId}/history` | A, P | |
| POST | `/` | A, P | `{teacherId, sectionId, academicYearId}` |
| DELETE | `/section/{sectionId}/year/{academicYearId}` | A, P | |

### 5.25 Teacher classes — `/api/teacher-classes/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/teacher/{teacherId}/{academicYearId}` | A, P, T | **T self** |
| POST | `/` | A, P | `{teacherId, classId, sectionId, academicYearId}` |
| DELETE | `/{teacherClassId}` | A, P | |

### 5.26 Teacher subjects — `/api/teacher-subjects/*`

| Method | Path | Roles | Notes |
|---|---|---|---|
| GET | `/teacher/{teacherId}/{academicYearId}` | A, P, T | **T self** |
| POST | `/` | A, P | `{teacherId, subjectId, sectionId, academicYearId}` |
| DELETE | `/{teacherSubjectId}` | A, P | |

### 5.27 Reports — `/api/reports/*` (all GET, A/P only)

| Path | Params |
|---|---|
| `/student-performance/{studentId}/{academicYearId}` | |
| `/teacher-attendance/{teacherId}/{month}/{year}` | month 1-12 |
| `/class-attendance/{sectionId}/{month}/{year}` | |
| `/teacher-performance/{teacherId}/{academicYearId}` | |
| `/examination/{examinationId}` | |
| `/student-attendance-summary/{studentId}/{academicYearId}` | |

---

## Part 6 — Authorization tests (negative cases)

Goal: confirm each role is **blocked** from what it shouldn't touch. Wrong-role responses come back as `401` with "Access denied. Required role: ..." (see the quirk in Part 0).

| Call (as TEACHER token) | Expected |
|---|---|
| `GET /api/students` | `401` Access denied |
| `GET /api/teachers` | `401` |
| `POST /api/classes` | `401` |
| `POST /api/attendance/teachers` | `401` (only A/P) |
| `GET /api/reports/student-performance/1/1` | `401` (only A/P) |
| `GET /api/teachers/{someoneElseTeacherId}` | `401` (not self) |
| `GET /api/timetable/teacher/{someoneElseTeacherId}/1` | `401` (T self check) |

| Call (as STUDENT token) | Expected |
|---|---|
| `GET /api/students` | `401` |
| `POST /api/submissions` | `200` (allowed) |
| `PUT /api/submissions/grade/1` | `401` |
| `POST /api/attendance/students` | `401` |
| `GET /api/students/{someoneElseStudentId}` | `401` (not self) |
| `GET /api/marks/student/{someoneElseId}/year/1` | `401` (not self, not linked) |

| Call (as PARENT token) | Expected |
|---|---|
| `GET /api/students/2` (a student you're NOT linked to) | `401` |
| `GET /api/parents/student/2` (not linked) | `401` |
| `POST /api/students` | `401` |
| `GET /api/students/1` (your linked child) | `200` |

| Call (as PRINCIPAL token) | Expected |
|---|---|
| `GET /api/students` | `200` (allowed) |
| `POST /api/classes` | `401` (create = admin only) |
| `DELETE /api/sections/1` | `401` |
| `PUT /api/students/1` | `401` (update = admin only) |

| No token / bad token (any endpoint) | Expected |
|---|---|
| No `Authorization` header | `401` "Missing or invalid Authorization header." |
| `Bearer not.a.jwt` | `401` "Invalid or expired token." |
| `POST /api/auth/login` with wrong password | `401` "Invalid email or password." |
| Login of deactivated account | `403` |

**Self-access checks to verify:**
- Teacher views own timetable/classes/subjects/assignments/attendance → `200`; any other teacher's → `401`.
- Student views own marks/submissions/attendance/profile → `200`; another student's → `401`.
- Parent views linked child's marks/attendance/student record → `200`; non-linked → `401`.
- `GET /api/users/me`, `GET /api/teachers/me`, `GET /api/students/me`, `GET /api/parents/me` all work with their own token.
- Notifications: a user can read/delete their own; another user's → `401`; ADMIN can read anyone's.

---

## Part 7 — Suggested full test order (checklist)

1. ✅ Seed admin via SQL → login as admin → token saved.
2. ✅ `GET /api/users/me` with admin token → `200`.
3. ✅ Auth negatives: no token, bad token, wrong password, deactivated account.
4. ✅ Refresh-token flow: login → refresh → old refresh reused → `401`; logout → refresh → `401`; logout → reuse the logged-out **access** token on `GET /api/users/me` → `401`.
5. ✅ Change password as admin → login again with new password (restore old or remember both).
6. ✅ Reference data: academic year (set-current), classes, sections, subjects, periods.
7. ✅ Create: teacher, student, parent, principal, second admin. Login as each and hit `/me` + `GET /api/users/me`.
8. ✅ Enroll student, link parent, assign teacher-class / teacher-subject / class-teacher, timetable.
9. ✅ Create examination, assignment; student submits; teacher grades (PUT grade, PUT late).
10. ✅ Enter marks as teacher/admin; verify student & parent can read their marks; verify the various marks GET routes.
11. ✅ Student + teacher attendance (record, query by section/date and by student/teacher with date range, update status, lock).
12. ✅ Announcements (create, list by role/class/section, disable, update).
13. ✅ Notifications (create single + bulk, read/unread, unread-count, read-all, delete all).
14. ✅ Reports (all 6, as ADMIN and PRINCIPAL; confirm TEACHER gets 401).
15. ✅ Full negative/authorization matrix from Part 6 for every role.
16. ✅ Edge cases: malformed JSON → `400`; duplicate enroll → `409`; marks above max → `400`; missing query params (`startDate`/`endDate`) → `400`.

---

## Part 8 — Troubleshooting

| Symptom | Fix |
|---|---|
| `500 A database error occurred` on login | DB not reachable, `.env` wrong, or `JWT_SECRET` not set. Check `EnvLoader` reads your `.env` (put it in the Tomcat working dir or use `-Denv.file=...`). |
| `IllegalStateException: JWT_SECRET is not configured` | Set a base64 `JWT_SECRET` (>= 32 bytes) in `.env`. |
| `Invalid Email.` | Emails must end in `@gmail.com` and be sent as valid format. |
| Login returns `Invalid email or password.` | Wrong password, or the seeded user's `password_hash` isn't a valid BCrypt hash. |
| `401 Access denied` when you expect `200` | Wrong role, or (for `teacherId`/`studentId` routes) you used the wrong id — the teacher/student **profile id** is required, not the user id. |
| CORS issues from the browser | `OPTIONS` requests bypass auth automatically (the Android app won't care). |
