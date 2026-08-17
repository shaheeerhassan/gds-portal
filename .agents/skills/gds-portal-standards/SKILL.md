---
name: gds-portal-standards
description: >-
  Custom coding standards, UI guidelines, and workflows for the GDS Portal project,
  including Admin UI patterns, API usage rules, and data modeling quirks.
---

# GDS Portal Coding Standards & Workflows

When working on the GDS Portal project (Android client or Java Backend), always adhere to the following standards, architectural patterns, and workflows.

## 1. UI & Architecture (Android)
- **Pattern Reuse**: Always reuse existing Administrator UI patterns for Directory screens, Detail screens, Create/Edit forms, Loading/Empty/Error states, and Confirmation dialogs. Do not introduce new design systems or alternative layout paradigms.
- **Top App Bar**: Use `AdminTopAppBar` consistently across all Administrator screens.
- **ViewModel Architecture**: Each screen (e.g., Directory, Detail, Create) must have its own dedicated ViewModel handling its specific state and API interactions.
- **Navigation Registration**: When creating new modules, register them in both `AdminLandingScreens.kt` (setting `isImplemented = true`) and wire them correctly in `AdminAppWrapper.kt`.

## 2. Backend API Rules & Restrictions
- **Strict Endpoint Verification**: Only use backend endpoints that have been explicitly verified to exist in the Java controllers. Do not assume or invent endpoints (like Search, Pagination, or Delete) if they haven't been implemented in the backend.
- **Endpoint Scoping (`/me`)**: The `/me` endpoints (e.g., `/api/notifications/me`) specifically represent records belonging to the currently authenticated user. Respect this scoping.
- **Server-Side vs Client-Side Logic**: Do not implement client-side filtering (e.g., for "Unread" items) if the backend already provides a dedicated endpoint (like `/me/unread`). Always prefer server-side operations where available.

## 3. Data Modeling & Mapping Discoveries
- **Gson Serialization Quirks**: Boolean fields starting with "is" (like `isActive` or `isRead`) can cause mapping issues between the Java backend and the Kotlin Android client. Always use `@SerializedName(value = "isActive", alternate = ["active"])` in `Models.kt` to prevent runtime mapping failures.
- **Role ID Mapping Reference**:
  - `1` = Administrator
  - `2` = Principal
  - `3` = Teacher
  - `4` = Student
  - `5` = Parent
- **Bulk Payload Wrapping**: Operations like bulk notification creation require wrapping the payload in specific DTOs (e.g., `BulkNotificationsRequest` containing a `List<Notification>`). Always match the expected backend DTO wrapper.

## 4. Module-Specific Workflows (Announcements vs. Notifications)
- **Announcements**: Act as a public/group bulletin board. They target Scopes (Global, Role, Class, Section) and do not have individual per-user read/unread tracking. Instead of deleting, they are disabled via `PUT /disable/{id}`.
- **Notifications**: Act as a personal inbox. They target specific user IDs, have strict per-user read/unread states, and can be permanently deleted.
- **Contextual Deep Linking**: Always utilize the `referenceTable` and `referenceId` properties in schemas to provide actionable deep links in the UI (e.g., a notification about an announcement should link directly to that announcement's detail screen).
