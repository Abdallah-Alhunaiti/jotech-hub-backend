# JoTech Hub Backend

Backend for the **JoTech Hub** graduation project, built with **Spring Boot**.

## Overview

JoTech Hub is a web platform that helps students discover technology-related opportunities such as:

- Workshops
- Hackathons
- Competitions
- Bootcamps
- Conferences
- Webinars
- Other tech events in Jordan

The backend provides authentication, role-based authorization, event management, approval workflows, saved events,
subscriptions, organizer dashboard features, and Google login support.

- Forgot password and reset password flow using email reset links
- Profile image upload for users and organizer logo/image upload during organizer signup
- Event type support: IN_PERSON and ONLINE
- City-based event filtering using cityId, separated from detailed location text
- Student profile editing for full name, gender, university, and city

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JWT Authentication
- OAuth2 Google Login
- Hibernate Validator
- Spring Mail for password reset emails
- Multipart File Upload for profile images and organizer images
- Lombok
- BCrypt password hashing

## Main Roles

The system supports these roles:

- **Visitor**
- **Student**
- **Organizer**
- **Admin**

## Role Description

- Visitor can browse public events and lookup data without logging in
- Student can save events, subscribe to events, manage tickets, and update personal profile data
- Organizer can create, update, delete/cancel, and track submitted events
- Admin can approve, reject, delete/cancel events, and manage users

## Main Features

- Student signup and login
- Organizer signup and login
- Admin login
- Google login
- JWT-based authentication
- Role-based authorization
- Public lookup APIs
- Public event browsing
- Organizer event management
- Admin event approval and rejection
- Saved events
- Event subscriptions and ticket generation
- Capacity control for event registration
- Organizer dashboard
- Admin user management
- Google onboarding flow: new Google users choose Student or Organizer after login
- Admin event rejection reason returned to organizer/user-facing responses
- Admin event delete/cancel endpoint using soft delete
- Organizer event delete/cancel using soft delete
- Deleted/cancelled events are removed from students' saved events
- Forgot password endpoint and reset password endpoint
- Password reset token expiration and optional token exposure for local testing only
- Student gender stored in StudentProfile only, not for organizers or admins
- Student profile update endpoint for fullName, gender, universityId, and cityId
- User profile image upload and delete endpoints
- Organizer image/logo upload during organizer signup using multipart/form-data
- Organizer organization name uniqueness validation
- Event type field: IN_PERSON or ONLINE
- City dropdown support for events using cityId
- Public event city filtering using /api/public/events?cityId={id}
- Event location remains a free-text detailed location, separate from city
- Public uploaded image serving under /uploads/**

## Project Structure

The backend is organized into packages such as:

- `auth`
- `user`
- `role`
- `event`
- `category`
- `tag`
- `subscription`
- `savedevent`
- `admin`
- `organizer`
- `security`
- `config`
- `common`
- `city`
- `university`
- `notification`
- `uploaded files / image storage support`

## Authentication

The project supports:

- Email and password login
- Google OAuth2 login
- JWT token generation and validation
- Google complete signup after choosing account type
- Password reset using secure reset tokens
- Local password login is blocked for Google-only accounts unless a password account exists

## Google OAuth2 Flow

- Existing Google email: user is logged in with the existing role
- New Google email: backend creates a pending signup token
- Frontend redirects user to choose Student or Organizer
- Frontend sends the selected role and required profile fields to complete signup

## Environment Variables

Sensitive values are not stored directly in the project files.

### Required

- `DB_PASSWORD`
- `JWT_SECRET`

### Optional

- `DB_URL`
- `DB_USERNAME`
- `SERVER_PORT`
- `JPA_DDL_AUTO`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `FRONTEND_SUCCESS_URL`
- `FRONTEND_FAILURE_URL`
- `JWT_EXPIRATION`
- `JWT_ISSUER`
- FRONTEND_ONBOARDING_URL
- FRONTEND_PASSWORD_RESET_URL
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD
- MAIL_FROM
- PASSWORD_RESET_EXPIRATION_MINUTES
- EXPOSE_PASSWORD_RESET_TOKEN
- APP_BASE_URL
- file.profile-images-dir
- file.organizer-images-dir

## Default Local Configuration

If not overridden, the project uses these defaults:

- Database URL: `jdbc:postgresql://localhost:5432/jotech_hub_db`
- Database username: `postgres`
- Server port: `8080`
- JWT expiration: 86400000
- JWT issuer: jotech-hub
- Password reset expiration: 15 minutes
- Frontend OAuth2 success URL: http://localhost:3000/oauth2/callback
- Frontend OAuth2 failure URL: http://localhost:3000/login
- Frontend password reset URL: http://localhost:3000/reset-password
- Application base URL: http://localhost:8080
- Profile images directory: uploads/profile-images
- Organizer images directory: uploads/organizers

## Running the Project

1. Make sure PostgreSQL is running
2. Create the database
3. Set the required environment variables
4. Run the Spring Boot application from IntelliJ or Maven
5. For new database columns such as event_type or city_id, make sure old data is migrated before enforcing
   NOT NULL

## API Groups

Main API groups in the project:

### Auth

- `POST /api/auth/signup/student`
- `POST /api/auth/signup-organization`
- `POST /api/auth/login`
- `GET /oauth2/authorization/google`
- `POST /api/auth/oauth2/google/complete`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/signup-organization as multipart/form-data with image`

### Public

- `GET /api/public/cities`
- `GET /api/public/universities`
- `GET /api/public/categories`
- `GET /api/public/tags`
- `GET /api/public/organization-types`
- `GET /api/public/events`
- `GET /api/public/events/upcoming`
- `GET /api/public/events/recent`
- `GET /api/public/events/{eventId}`
- `GET /api/public/events?cityId={cityId}`
- `GET /api/public/events?cityId={cityId}&categoryId={categoryId}&tagId={tagId}`
- `GET /uploads/profile-images/{fileName}`
- `GET /uploads/organizers/{fileName}`

### Organizer

- `POST /api/organizer/events`
- `GET /api/organizer/events`
- `GET /api/organizer/events/{eventId}`
- `PUT /api/organizer/events/{eventId}`
- `DELETE /api/organizer/events/{eventId}`
- `GET /api/organizer/dashboard`
- `Create and update event requests include eventType and cityId`
- `Deleting/cancelling an organizer event removes it from saved events`

### Student

- `POST /api/student/saved-events/{eventId}`
- `DELETE /api/student/saved-events/{eventId}`
- `GET /api/student/saved-events`
- `POST /api/student/subscriptions/{eventId}`
- `DELETE /api/student/subscriptions/{eventId}`
- `GET /api/student/subscriptions`
- `GET /api/student/profile`
- `PUT /api/student/profile`
- `PUT /api/users/me/profile-image`
- `DELETE /api/users/me/profile-image`

### Admin

- `GET /api/admin/events`
- `GET /api/admin/events/{eventId}`
- `PUT /api/admin/events/{eventId}/approve`
- `PUT /api/admin/events/{eventId}/reject`
- `GET /api/admin/users`
- `GET /api/admin/users/{userId}`
- `PUT /api/admin/users/{userId}/deactivate`
- `DELETE /api/admin/events/{eventId}`
- `Admin reject request supports rejectionReason`
- `Admin delete request supports cancellationReason`
- `Admin event deletion removes the event from saved events`

## Event Model Updates

- eventType: IN_PERSON or ONLINE
- cityId and cityName are separate from location
- location remains detailed free text, such as venue name or meeting link
- activeRegistrationsCount can be displayed as activeRegistrationsCount / capacity
- cancelled, cancelledAt, and cancellationReason support soft delete
- rejectionReason stores the reason written by the admin

## Security Notes

- Secrets are loaded using environment variables
- JWT is used for securing protected endpoints
- OAuth2 Google login is supported
- Role-based access control is enforced for Student, Organizer, and Admin endpoints
- Password reset tokens are stored securely and expire
- EXPOSE_PASSWORD_RESET_TOKEN must be false outside local testing
- Uploaded files are validated by content type and size
- Public files are served under /uploads/**
- Organizer names are unique
- Student gender is stored only in student profile
- Protected frontend pages should still validate role, but backend APIs enforce real authorization

## Notes

- Google OAuth2 requires valid Google Cloud credentials
- Frontend callback URLs should match the frontend application setup
- The repository does not store sensitive runtime secrets directly
- Gmail SMTP requires a Google App Password
- Use EXPOSE_PASSWORD_RESET_TOKEN=true only during local Postman testing
- Use EXPOSE_PASSWORD_RESET_TOKEN=false for normal usage
- Event city filter uses cityId, not the free-text location value

## Author

Graduation project backend developed for **JoTech Hub**.