# 🛠️ ResolveIT
### Smart Grievance Management System

A full-stack grievance management platform built with **Java Spring Boot** that streamlines complaint submission, tracking, and resolution across **Users**, **Staff**, and **Admin** roles — with JWT-based security, role-based dashboards, SLA monitoring, escalation handling, email notifications, and analytics.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?style=flat-square&logo=springsecurity)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-green?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=flat-square&logo=apachemaven)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Roles & Access Control](#-roles--access-control)
- [Complaint Workflow](#-complaint-workflow)
- [Database Schema](#-database-schema)
- [Key Modules](#-key-modules)
- [Security](#-security)
- [Common Issues & Fixes](#-common-issues--fixes)
- [Future Enhancements](#-future-enhancements)
- [Author](#-author)
- [License](#-license)

---

## 🌟 Overview

**ResolveIT** is a web-based grievance management system built with Java Spring Boot. It enables organizations to efficiently manage and resolve user complaints through a structured, role-based workflow — from initial submission to final resolution or denial.

Key highlights:

- 🔐 JWT-secured, role-based dashboards for Users, Staff, and Admins
- 📋 End-to-end complaint lifecycle tracking with full audit history
- ⏱️ SLA monitoring with delay and overdue tracking
- 📬 Email + in-app notifications for every status change
- 📊 Analytics dashboard with staff performance metrics
- 🔺 Escalation management for unresolved or critical complaints
- 💬 Admin messaging system for direct communication
- 📅 Staff attendance tracking

---

## ✨ Features

### 👤 User Features

| Feature | Description |
|---|---|
| Submit Complaints | File complaints with title, description, and optional attachments |
| Track Status | Real-time status: `NEW` → `UNDER REVIEW` → `RESOLVED` / `DENIED` |
| View History | Full timeline of each complaint with timestamps |
| Contact Admin | Send and receive messages directly with admin |
| Email Notifications | Get notified on every status change |
| Feedback | Rate and comment on resolved complaints |
| Profile Management | Update personal information and profile picture |

### 👨‍💼 Staff Features

| Feature | Description |
|---|---|
| My Tasks | View and manage complaints assigned to you |
| Status Updates | Move complaints through the resolution workflow |
| SLA Monitoring | Track delays and overdue complaints |
| Raise Escalations | Escalate critical or unresolved issues to admin |
| Activity Log & Timeline | Full history and audit trail per complaint |
| Notifications | Alerts for newly assigned or updated complaints |
| User Feedback | View ratings and comments submitted by users |

### 🔐 Admin Features

| Feature | Description |
|---|---|
| User Management | Create, update, and manage user and staff accounts |
| Complaint Assignment | Assign and reassign complaints to staff members |
| Full Complaint View | Access, filter, and act on all complaints system-wide |
| Escalation Monitor | Track and respond to all escalated complaints |
| Analytics Dashboard | Stats on total, resolved, pending, and denied complaints |
| Staff Performance | Metrics and activity reports per staff member |
| Feedback Oversight | View all user feedback across complaints |
| Admin Messaging | Send broadcast or direct messages to users and staff |
| Reports | Generate and export complaint reports |
| System Settings | Configure system-wide settings and preferences |
| Attendance Management | Track staff attendance records |

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.0 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Templating | Thymeleaf |
| Frontend | HTML5, CSS3, JavaScript |
| Build Tool | Maven |
| Email | Spring Mail (SMTP / Mailtrap) |
| Version Control | Git & GitHub |

---

## 🗂️ Project Structure

```
resolveit/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/resolveit/
│   │   │   ├── config/
│   │   │   │   ├── JwtFilter.java            # JWT request filter
│   │   │   │   ├── JwtUtil.java              # JWT token utilities
│   │   │   │   ├── SecurityConfig.java       # Spring Security configuration
│   │   │   │   └── WebMvcConfig.java         # MVC configuration
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AdminEscalationController.java
│   │   │   │   ├── AdminFeedbackController.java
│   │   │   │   ├── AdminMessageController.java
│   │   │   │   ├── AttendanceController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ComplaintController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── PageController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── StaffController.java
│   │   │   │   ├── StaffEscalationController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── UserSupportController.java
│   │   │   │   └── GlobalControllerAdvice.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Complaint.java
│   │   │   │   ├── ComplaintHistory.java
│   │   │   │   ├── Escalation.java
│   │   │   │   ├── Feedback.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── AdminMessage.java
│   │   │   │   ├── Attendance.java
│   │   │   │   └── SystemSetting.java
│   │   │   │
│   │   │   ├── repository/                   # Spring Data JPA repositories
│   │   │   ├── service/                      # Business logic layer
│   │   │   ├── exception/                    # Global exception handling
│   │   │   └── ResolveitApplication.java     # Main entry point
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── admin/                    # Admin Thymeleaf pages
│   │       │   ├── staff/                    # Staff Thymeleaf pages
│   │       │   ├── fragments/                # Shared layout, sidebars, topbars
│   │       │   └── *.html                    # User-facing pages
│   │       ├── static/
│   │       │   ├── css/                      # Stylesheets per role/page
│   │       │   └── js/                       # Frontend scripts
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/example/resolveit/
│
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- ☕ Java 21+
- 📦 Maven 3.8+
- 🗄️ MySQL 8.0+
- 🌐 Git

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/resolveit.git
cd resolveit
```

### 2️⃣ Set Up the Database

```sql
CREATE DATABASE resolveit_db;
```

### 3️⃣ Configure the Application

Open `src/main/resources/application.properties` and update with your credentials:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/resolveit_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server
server.port=8080

# Email (configure for notifications)
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> ⚠️ **Security Warning:** Never commit real credentials to version control. Use environment variables or a `.env` file in production. Add `application.properties` to your `.gitignore`.

### 4️⃣ Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Open your browser and navigate to:

```
http://localhost:8080
```

---

## ⚙️ Configuration

### Default Credentials (Development Only)

> ⚠️ Change these before deploying to production.

| Role | Email | Password |
|---|---|---|
| Admin | admin@resolveit.com | admin123 |
| Staff | staff@resolveit.com | staff123 |
| User | user@resolveit.com | user123 |

### File Upload Limits

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads
```

### Timezone

```properties
spring.jackson.time-zone=Asia/Kolkata
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Kolkata
```

---

## 🔐 Roles & Access Control

| Role | Description |
|---|---|
| `USER` | End users who submit and track their own complaints |
| `STAFF` | Staff members who review, update, and resolve complaints |
| `ADMIN` | Full system access — user management, analytics, and settings |

Authentication is handled via **Spring Security + JWT**. Each role is redirected to its respective dashboard after login, and all protected routes are enforced at the security layer.

---

## 🔄 Complaint Workflow

```
┌─────────┐     ┌──────────────┐     ┌──────────┐
│   NEW   │────▶│ UNDER REVIEW │────▶│ RESOLVED │
└─────────┘     └──────────────┘     └──────────┘
                        │
                        ▼
                   ┌────────┐
                   │ DENIED │
                   └────────┘
                        │
                        ▼
                  ┌───────────┐
                  │ ESCALATED │
                  └───────────┘
```

Every status transition is recorded in the `complaint_history` table with:
- The new status
- Who made the update
- A full timestamp

This creates a complete, auditable timeline for every complaint in the system.

---

## 🗄️ Database Schema

### `users`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| name | VARCHAR | Full name |
| email | VARCHAR (UNIQUE) | Login email |
| password | VARCHAR | BCrypt-hashed password |
| role | ENUM | `USER` / `STAFF` / `ADMIN` |
| profile_pic | VARCHAR | Optional profile picture path |

### `complaints`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| title | VARCHAR | Short complaint title |
| description | TEXT | Detailed description |
| user_id | BIGINT (FK) | References `users.id` |
| status | ENUM | `NEW` / `UNDER_REVIEW` / `RESOLVED` / `DENIED` |
| assigned_staff | BIGINT (FK) | References `users.id` (staff) |
| created_at | DATETIME | Submission timestamp |

### `complaint_history`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| complaint_id | BIGINT (FK) | References `complaints.id` |
| status | ENUM | Status at this point in time |
| updated_by | BIGINT (FK) | References `users.id` |
| timestamp | DATETIME | When the update occurred |

### `escalations`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| complaint_id | BIGINT (FK) | References `complaints.id` |
| raised_by | BIGINT (FK) | References `users.id` (staff) |
| reason | TEXT | Reason for escalation |
| status | ENUM | `OPEN` / `RESOLVED` |
| created_at | DATETIME | Escalation timestamp |

### `feedback`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| complaint_id | BIGINT (FK) | References `complaints.id` |
| user_id | BIGINT (FK) | References `users.id` |
| rating | INT | 1–5 star rating |
| comment | TEXT | Optional written feedback |

### `notifications`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| user_id | BIGINT (FK) | References `users.id` |
| message | TEXT | Notification content |
| is_read | BOOLEAN | Read/unread status |
| created_at | DATETIME | Notification timestamp |

### `admin_messages`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| sender_id | BIGINT (FK) | References `users.id` |
| receiver_id | BIGINT (FK) | References `users.id` |
| content | TEXT | Message body |
| sent_at | DATETIME | Sent timestamp |

### `attendance`

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| staff_id | BIGINT (FK) | References `users.id` |
| date | DATE | Attendance date |
| status | ENUM | `PRESENT` / `ABSENT` |

---

## 📊 Key Modules

### 📌 Dashboard
- Role-specific dashboards rendered immediately after login
- Real-time counts: total, pending, resolved, and denied complaints
- Admin sees a system-wide view; Staff sees their personal assigned queue

### 📌 Complaint Management
- Users submit complaints with optional file attachments (up to 10MB)
- Staff updates status, adds resolution notes, and moves through workflow
- Admin assigns, reassigns, and monitors all complaints across the system

### 📌 Escalation System
- Staff can raise escalations for unresolved or critical complaints
- Admin monitors all open escalations from a dedicated dashboard
- Each escalation includes the reason and the full linked complaint context

### 📌 Notifications
- Email alerts triggered on complaint submission and every status change
- In-app UI notifications for staff and admin with read/unread tracking

### 📌 Feedback System
- Users can leave a star rating (1–5) and a comment after resolution
- Staff and Admin can view all feedback associated with their complaints

### 📌 SLA Tracking
- Tracks time elapsed since a complaint was assigned to staff
- Flags and highlights overdue complaints on the staff dashboard

### 📌 Admin Messaging
- Direct messaging between admin and individual users or staff
- Threaded message history for each conversation

### 📌 Reports & Analytics
- Visual breakdown of complaint categories and status distribution
- Staff performance metrics: resolution counts, average handling time
- Exportable report views from the admin panel

### 📌 Attendance Management
- Admin can record and view daily attendance for all staff members

---

## 🔒 Security

Security is implemented using **Spring Security + JWT** with the following route protection:

```
/admin/**            → ADMIN only
/staff/**            → STAFF only
/my-complaints/**    → USER only
/auth/**             → Public (login, register)
/                    → Public (home page)
```

Additional protections:

- 🔑 **JWT Authentication** — stateless token-based auth via `JwtFilter` and `JwtUtil`
- 🔒 **BCrypt Password Hashing** — all passwords are hashed before storage
- 🛡️ **CSRF Protection** — enabled by Spring Security
- 🚦 **Role Enforcement** — via Spring Security's `HttpSecurity` configuration
- 🌐 **CORS Configuration** — managed through `WebMvcConfig`
- ⚠️ **Global Exception Handling** — via `GlobalExceptionHandler` and `GlobalControllerAdvice`

---

## 🐛 Common Issues & Fixes

**❌ Application fails to start — database connection refused**
> Make sure MySQL is running and the `resolveit_db` database has been created. Verify your username and password in `application.properties`.

**❌ Port 8080 already in use**
> Change the port in `application.properties`:
> ```properties
> server.port=9090
> ```

**❌ Email notifications not sending**
> Confirm your SMTP credentials in `application.properties`. For testing, use [Mailtrap](https://mailtrap.io). For production, switch to Gmail or another SMTP provider with valid app credentials.

**❌ File upload fails**
> Check that the `app.upload.dir=uploads` directory exists and that the application has write permissions to it.

**❌ Thymeleaf template changes not reflecting**
> Thymeleaf cache is disabled by default (`spring.thymeleaf.cache=false`). If changes still don't appear, do a hard browser refresh (`Ctrl + Shift + R`).

---

## 🚀 Future Enhancements

- [ ] REST API layer for mobile app integration
- [ ] Real-time notifications via WebSocket
- [ ] Two-factor authentication (2FA)
- [ ] AI-based automatic complaint categorization and routing
- [ ] CSV / PDF export for complaint and analytics reports
- [ ] Dark mode UI
- [ ] Docker containerization and deployment guide
- [ ] Multi-language / i18n support

---

## 👤 Author

**Thikash J**

> Developed as an internship project — a full-stack web application built from scratch using Java Spring Boot.

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2026 Thikash J

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```
