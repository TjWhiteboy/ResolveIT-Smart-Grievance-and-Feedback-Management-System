<div align="center">

# 🚀 ResolveIT

### Smart Grievance Management System

A full-stack grievance management platform that streamlines complaint submission, tracking, and resolution across **Users, Staff, and Admin** roles — with real-time updates, role-based dashboards, SLA monitoring, and analytics.

<br/>

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#️-tech-stack)
- [Project Structure](#️-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#️-configuration)
- [Roles & Access Control](#-roles--access-control)
- [Complaint Workflow](#-complaint-workflow)
- [Database Schema](#️-database-schema)
- [Key Modules](#-key-modules)
- [Security](#-security)
- [Common Issues & Fixes](#-common-issues--fixes)
- [Future Enhancements](#-future-enhancements)
- [Author](#-author)
- [License](#-license)

---

## 🌟 Overview

**ResolveIT** is a web-based grievance management system built with **Java Spring Boot**. It enables organizations to efficiently manage and resolve user complaints through a structured, role-based workflow — from initial submission to final resolution or denial.

Key highlights:
- Role-based dashboards for Users, Staff, and Admins
- End-to-end complaint lifecycle tracking
- SLA monitoring with delay tracking
- Email + UI notifications
- Analytics with staff performance metrics

---

## ✨ Features

### 👤 User Features

| Feature | Description |
|--------|-------------|
| Submit Complaints | File complaints with title, description, and attachments |
| Track Status | Real-time status updates: NEW → UNDER REVIEW → RESOLVED / DENIED |
| Search & Filter | Filter complaints by status, date, or keyword |
| Download Data | Export complaint history as CSV |
| Email Notifications | Get notified on every status change |
| Feedback | Rate and comment on resolved complaints |

---

### 👨‍💼 Staff Features

| Feature | Description |
|--------|-------------|
| My Tasks | View complaints assigned to you |
| Status Updates | Move complaints through the workflow |
| SLA Monitoring | Track delays and overdue complaints |
| Timeline Tracking | Full history of each complaint |
| Notifications | Alerts for newly assigned or updated complaints |
| User Feedback | View ratings and comments from users |

---

### 🛠️ Admin Features

| Feature | Description |
|--------|-------------|
| User Management | Create, update, and deactivate users and staff |
| Complaint Assignment | Assign complaints to specific staff members |
| Full Complaint View | Access and filter all complaints system-wide |
| Analytics Dashboard | Stats on total, resolved, pending complaints |
| Staff Performance | Metrics per staff member |
| Distribution Charts | Visual breakdown of complaint categories |

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| Framework | Spring Boot |
| Security | Spring Security |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Templating | Thymeleaf |
| Frontend | HTML, CSS, JavaScript |
| Build Tool | Maven |
| Version Control | Git & GitHub |

---

## 🗂️ Project Structure

```
resolveit/
│
├── src/
│   ├── main/
│   │   ├── java/com/resolveit/
│   │   │   ├── controller/         # MVC Controllers (User, Staff, Admin, Auth)
│   │   │   ├── service/            # Business logic layer
│   │   │   ├── repository/         # JPA Repositories
│   │   │   ├── model/              # Entity classes
│   │   │   └── config/             # Spring Security & app config
│   │   │
│   │   └── resources/
│   │       ├── templates/          # Thymeleaf HTML templates
│   │       │   ├── user/
│   │       │   ├── staff/
│   │       │   ├── admin/
│   │       │   └── auth/
│   │       ├── static/             # CSS, JS, images
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/resolveit/     # Unit & integration tests
│
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Git

---

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/resolveit.git
cd resolveit
```

---

### 2️⃣ Set Up the Database

Create a MySQL database:

```sql
CREATE DATABASE resolveit_db;
```

---

### 3️⃣ Configure the Application

Update `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/resolveit_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server
server.port=8080

# Email (optional — configure for notifications)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

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

### Default Credentials (Development)

> ⚠️ Change these before deploying to production.

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@resolveit.com | admin123 |
| Staff | staff@resolveit.com | staff123 |
| User | user@resolveit.com | user123 |

---

## 🔐 Roles & Access Control

| Role | Dashboard URL | Description |
|------|--------------|-------------|
| `USER` | `/my-complaints` | End users who submit and track complaints |
| `STAFF` | `/staff-dashboard` | Staff members who review and resolve complaints |
| `ADMIN` | `/admin` | Full system access — management and analytics |

Authentication is handled via **Spring Security** with session-based login. Each role is redirected to its respective dashboard after login using a custom `AuthenticationSuccessHandler`.

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
```

Every status transition is recorded in the `complaint_history` table with:
- The new status
- Who made the update
- A timestamp

This creates a full, auditable timeline for every complaint.

---

## 🗄️ Database Schema

### `users`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-increment primary key |
| name | VARCHAR | Full name |
| email | VARCHAR (UNIQUE) | Login email |
| password | VARCHAR | BCrypt-hashed password |
| role | ENUM | USER / STAFF / ADMIN |
| profile_pic | VARCHAR | Optional profile picture path |

---

### `complaints`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-increment primary key |
| title | VARCHAR | Short complaint title |
| description | TEXT | Detailed description |
| user_id | BIGINT (FK) | References `users.id` |
| status | ENUM | NEW / UNDER REVIEW / RESOLVED / DENIED |
| assigned_staff | BIGINT (FK) | References `users.id` (staff) |
| created_at | DATETIME | Submission timestamp |

---

### `complaint_history`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-increment primary key |
| complaint_id | BIGINT (FK) | References `complaints.id` |
| status | ENUM | Status at this point in time |
| updated_by | BIGINT (FK) | References `users.id` |
| timestamp | DATETIME | When the update occurred |

---

### `feedback`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-increment primary key |
| complaint_id | BIGINT (FK) | References `complaints.id` |
| user_id | BIGINT (FK) | References `users.id` |
| rating | INT | 1–5 star rating |
| comment | TEXT | Optional written feedback |

---

## 📊 Key Modules

### 📌 Dashboard
- Role-specific dashboards rendered on login
- Real-time stats: total, pending, resolved, denied counts
- Admin sees system-wide view; Staff sees their assigned queue

### 📌 Complaint Management
- Users submit complaints with optional file attachments
- Staff updates status and adds notes
- Admin assigns complaints and monitors all activity

### 📌 Notifications
- Email alerts on complaint submission and status changes
- In-app UI notifications for staff and admin

### 📌 Feedback System
- Users can leave a star rating and comment after resolution
- Staff and Admin can view feedback per complaint

### 📌 SLA Tracking
- Tracks time elapsed since complaint was assigned
- Flags overdue complaints on the staff dashboard

### 📌 Pagination
- All complaint lists are paginated for performance
- Configurable page size

---

## 🔒 Security

Security is implemented using **Spring Security** with the following configuration:

```java
/admin/**         → ADMIN only
/staff/**         → STAFF only
/my-complaints/** → USER only
/auth/**          → Public (login, register)
```

Additional protections:
- **BCrypt** password hashing
- **CSRF protection** enabled
- **Session-based authentication** with custom success handler
- Role enforcement via Spring Security's `HttpSecurity` configuration

---

## ⚠️ Common Issues & Fixes

### ❌ Redirected to wrong page after login
**Fix:** Implement a custom `AuthenticationSuccessHandler` that reads the user's role and redirects accordingly:

```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request,
    HttpServletResponse response, Authentication authentication) throws IOException {
    String role = authentication.getAuthorities().iterator().next().getAuthority();
    if (role.equals("ROLE_ADMIN")) response.sendRedirect("/admin");
    else if (role.equals("ROLE_STAFF")) response.sendRedirect("/staff-dashboard");
    else response.sendRedirect("/my-complaints");
}
```

---

### ❌ Logout throws error at `/auth/logout`
**Fix:** Use the default Spring Security logout endpoint `/logout` instead of a custom one:

```java
http.logout().logoutUrl("/logout").logoutSuccessUrl("/auth/login");
```

---

### ❌ Foreign key constraint error on complaint creation
**Fix:** Ensure the `user_id` in the complaints table maps to a valid, existing user before adding the foreign key constraint. Check that `spring.jpa.hibernate.ddl-auto=update` is set and the user entity is persisted first.

---

### ❌ Session conflicts when opening multiple tabs with different roles
**Fix:** Avoid logging in as different roles in the same browser session. Use separate browsers or incognito windows for testing multiple roles simultaneously.

---

## 🚀 Future Enhancements

- [ ] **WebSocket real-time updates** — live status push notifications without page refresh
- [ ] **Mobile-responsive UI** — improved layouts for mobile and tablet users
- [ ] **AI-based complaint categorization** — auto-tag and route complaints using NLP
- [ ] **Chat support** — real-time messaging between user and assigned staff
- [ ] **Advanced analytics** — graphs for resolution time, SLA breaches, and category trends
- [ ] **REST API** — expose endpoints for potential mobile app or third-party integration
- [ ] **Multi-language support** — i18n for regional deployments

---

## 👨‍💻 Author

**Thikash Tj**

- 🐙 GitHub: [@your-username](https://github.com/your-username)
- 💼 LinkedIn: [Add your LinkedIn profile URL]

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit: `git commit -m "Add your feature"`
4. Push to your branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

Please ensure your code follows existing conventions and includes relevant comments.

---

## ⭐ Support

If you found this project helpful:

- ⭐ **Star** the repo to show your support
- 🍴 **Fork** it to build on top of it
- 🐛 **Report bugs** by opening an issue
- 💡 **Suggest features** via the issues tab

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Thikash Tj

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">
  Made with ❤️ by <strong>Thikash Tj</strong>
</div>
