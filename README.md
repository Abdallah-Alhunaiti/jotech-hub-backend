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

The backend provides authentication, role-based authorization, event management, approval workflows, saved events, subscriptions, organizer dashboard features, and Google login support.

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

## Main Roles
The system supports these roles:
- **Visitor**
- **Student**
- **Organizer**
- **Admin**

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

## Authentication
The project supports:
- Email and password login
- Google OAuth2 login
- JWT token generation and validation

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

## Default Local Configuration
If not overridden, the project uses these defaults:
- Database URL: `jdbc:postgresql://localhost:5432/jotech_hub_db`
- Database username: `postgres`
- Server port: `8080`

## Running the Project
1. Make sure PostgreSQL is running
2. Create the database
3. Set the required environment variables
4. Run the Spring Boot application from IntelliJ or Maven

## API Groups
Main API groups in the project:

### Auth
- `POST /api/auth/signup/student`
- `POST /api/auth/signup-organization`
- `POST /api/auth/login`
- `GET /oauth2/authorization/google`

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

### Organizer
- `POST /api/organizer/events`
- `GET /api/organizer/events`
- `GET /api/organizer/events/{eventId}`
- `PUT /api/organizer/events/{eventId}`
- `DELETE /api/organizer/events/{eventId}`
- `GET /api/organizer/dashboard`

### Student
- `POST /api/student/saved-events/{eventId}`
- `DELETE /api/student/saved-events/{eventId}`
- `GET /api/student/saved-events`
- `POST /api/student/subscriptions/{eventId}`
- `DELETE /api/student/subscriptions/{eventId}`
- `GET /api/student/subscriptions`

### Admin
- `GET /api/admin/events`
- `GET /api/admin/events/{eventId}`
- `PUT /api/admin/events/{eventId}/approve`
- `PUT /api/admin/events/{eventId}/reject`
- `GET /api/admin/users`
- `GET /api/admin/users/{userId}`
- `PUT /api/admin/users/{userId}/deactivate`

## Security Notes
- Secrets are loaded using environment variables
- JWT is used for securing protected endpoints
- OAuth2 Google login is supported
- Role-based access control is enforced for Student, Organizer, and Admin endpoints

## Notes
- Google OAuth2 requires valid Google Cloud credentials
- Frontend callback URLs should match the frontend application setup
- The repository does not store sensitive runtime secrets directly

## Author
Graduation project backend developed for **JoTech Hub**.