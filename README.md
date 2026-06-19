# 🎬 Book My Ticket

> **An Online Movie Ticket Booking System** built with Spring Boot & Thymeleaf

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red?logo=redis)](https://redis.io/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?logo=thymeleaf)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple?logo=bootstrap)](https://getbootstrap.com/)

---

## 📖 About

**Book My Ticket** is a full-stack web application for online movie ticket booking with separate **Admin** and **User** modules.

- **Users** can browse movies, select shows and seats, make online payments via Razorpay, and receive QR-coded tickets via email.
- **Admins** can manage movies, theaters, screens, seat layouts, shows, and user accounts.

---

## ✨ Features

### 👤 User Module
- Register with **OTP-based email verification**
- Login / Logout with session management
- Browse now-showing movies
- Select theater, show, and seats in real-time
- **Razorpay** payment gateway integration
- QR code generated on successful booking
- View past bookings & ticket history

### 🔐 Admin Module
- Secure admin login
- Add / Edit / Delete Movies (with Cloudinary image upload)
- Add / Edit / Delete Theaters & Screens
- Configure seat layouts per screen
- Manage show timings
- View all registered users (block / unblock)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.4.5 |
| **Frontend** | Thymeleaf, Bootstrap 5, HTML/CSS |
| **Database** | MySQL 8 (via Spring Data JPA / Hibernate) |
| **Cache** | Redis |
| **ORM** | Hibernate 6 |
| **Payment** | Razorpay |
| **Image Storage** | Cloudinary |
| **Email** | JavaMailSender (Gmail SMTP) |
| **QR Code** | Google ZXing |
| **Build Tool** | Maven |

---

## 🏗️ Architecture

```
┌─────────────────────────────────┐
│          Thymeleaf Views        │  ← HTML templates (Bootstrap 5)
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│         Controller Layer        │  ← UserController (Spring MVC)
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│          Service Layer          │  ← Business logic, OTP, Payments
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│        Repository Layer         │  ← Spring Data JPA (8 repos)
└────────────────┬────────────────┘
                 │
        ┌────────┴────────┐
        ▼                 ▼
    MySQL DB          Redis Cache
```

---

## 📦 Project Structure

```
book-my-ticket/
├── src/
│   └── main/
│       ├── java/com/jsp/book/
│       │   ├── config/          # App configuration
│       │   ├── controller/      # MVC controllers
│       │   ├── dto/             # Data transfer objects
│       │   ├── entity/          # JPA entities
│       │   ├── exception/       # Global exception handling
│       │   ├── repository/      # Spring Data JPA repos
│       │   ├── service/         # Business logic
│       │   └── util/            # Helpers (Email, QR, Cloudinary, AES)
│       └── resources/
│           ├── templates/       # Thymeleaf HTML templates (26 pages)
│           └── application.yaml # App configuration
├── pom.xml
└── README.md
```

---

## ⚙️ Prerequisites

Make sure you have the following installed:

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis** (running on `localhost:6379`)
- A **Cloudinary** account
- A **Gmail** account (with App Password enabled)
- A **Razorpay** account (for payment keys)

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/navi-siddu-1103/book-my-ticket.git
cd book-my-ticket
```

### 2. Configure environment variables

Set the following environment variables before running:

| Variable | Description |
|---|---|
| `APP_PASSWORD` | Gmail App Password for sending emails |
| `CLOUDINARY_URL` | Your Cloudinary URL (`cloudinary://api_key:api_secret@cloud_name`) |

**Windows (PowerShell):**
```powershell
$env:APP_PASSWORD="your_gmail_app_password"
$env:CLOUDINARY_URL="cloudinary://your_api_key:your_api_secret@your_cloud_name"
```

### 3. Configure `application.yaml`

Update `src/main/resources/application.yaml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/book-my-ticket?createDatabaseIfNotExist=true
    username: root
    password: your_mysql_password
  mail:
    username: your_email@gmail.com
admin:
  email: admin@gmail.com
  password: admin
```

### 4. Start Redis

```bash
# Windows (if Redis is installed)
redis-server
```

### 5. Run the application

```bash
./mvnw spring-boot:run
```

Or on Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

### 6. Open in browser

```
http://localhost:80
```

> The database and tables are created automatically on first run via Hibernate `ddl-auto: update`.

---

## 🗃️ Database Entities

| Entity | Description |
|---|---|
| `User` | Registered users with roles (USER / ADMIN) |
| `Movie` | Movie details with Cloudinary image |
| `Theater` | Theater with location & image |
| `Screen` | Screen inside a theater |
| `Seat` | Seat layout per screen |
| `Show` | Movie show with date, time & ticket price |
| `ShowSeat` | Seat availability per show |
| `BookedTicket` | Booking record with QR code |

---

## 🔗 Key Endpoints

| URL | Description |
|---|---|
| `/` | Home / Landing page |
| `/login` | User login |
| `/register` | User registration |
| `/otp` | OTP verification |
| `/book/movie/{id}` | Browse theaters for a movie |
| `/select-seats/{showId}` | Seat selection page |
| `/confirm-ticket` | Booking confirmation |
| `/bookings` | User booking history |
| `/manage-movies` | Admin – manage movies |
| `/manage-theaters` | Admin – manage theaters |
| `/manage-users` | Admin – manage users |

---

## 🔒 Security

- **Session-based** authentication (Admin & User roles)
- **AES encryption** used for sensitive data
- **OTP verification** required during registration
- **Razorpay signature verification** before booking confirmation
- Guests clicking **Browse Movies** are redirected to login

---

## 📧 Email Flow

1. User registers → OTP sent via Gmail SMTP
2. OTP verified → Account activated
3. Booking confirmed → QR-coded e-ticket sent to email

---

## 💳 Payment Flow (Razorpay)

1. User selects seats → Order created on server
2. Razorpay checkout opens in browser
3. Payment completed → Signature verified server-side
4. Booking confirmed → QR code generated & emailed

---

## 🙋 FAQ

**Q: Why Spring Boot?**
> Reduces boilerplate, provides auto-configuration, and speeds up development.

**Q: Why Thymeleaf over React?**
> Integrates natively with Spring Boot, supports server-side rendering, ideal for monolithic apps.

**Q: How are double bookings prevented?**
> Seat availability is verified before payment, and seats are locked after successful transaction.

**Q: Why Cloudinary?**
> Secure cloud storage for images reduces server load and storage costs.

---

## 🚧 Future Improvements

- [ ] JWT-based authentication
- [ ] REST API with React frontend
- [ ] Seat locking with Redis TTL
- [ ] Ticket cancellation & refund flow
- [ ] Deployment on AWS / Railway
- [ ] Microservices architecture

---

## 👨‍💻 Author

**Naveen** — Internship Project  
📧 smsiddu2266@gmail.com  
🔗 [GitHub](https://github.com/navi-siddu-1103)

---

## 📄 License

This project is for educational/internship purposes.
