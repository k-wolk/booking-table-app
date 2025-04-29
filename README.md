# 🍽️ Booking Table App

_A simple restaurant table reservation system._

---

## 📝 Project Description

**Booking Table App** is a Spring Boot-based REST API that allows users to reserve dining tables at a restaurant.  
It supports user authentication and authorization using JWT tokens and role-based access control (Admin / User).

---

## ⚙️ Technologies Used

- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- MySQL
- Testcontainers (Integration Testing)
- JUnit 5 & MockMvc
- Maven

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/booking-table-app.git
cd booking-table-app
```

### 2. Configure the database
Edit src/main/resources/application.properties:
```
spring.datasource.url=jdbc:mysql://localhost:3306/booking_table
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your_secret_key
jwt.expirationMs=86400000
```
⚠️ Make sure your local MySQL server is running.

### 3. Build and run the application
```
mvn clean install
mvn spring-boot:run
```
The app will be available at http://localhost:8080.

---

## 🔐 Authentication

Authentication is based on JWT tokens.

First, sign in to receive a token:

POST /signin

```
{
  "login": "john",
  "password": "yourpassword"
}
```

Example Authorization Header for requests:

```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 📚 API Endpoints Overview

| Method | Endpoint                                      | Access          | Description                                         |
|:------:|-----------------------------------------------|:---------------:|-----------------------------------------------------|
| POST   | `/signin`                                     | Public          | Authenticate and receive JWT                        |
| POST   | `/users`                                      | Public          | Register a new user                                 |
| GET    | `/users`                                      | Admin           | List of all users                                   |
| GET    | `/users/{userId}`                             | Admin or owner  | View user's details                                 |
| PATCH  | `/users/{userId}`                             | User            | Update user's own details                           |
| PUT    | `/users/role/{userId}`                        | Admin           | Update user's role                                  |
| PATCH  | `/users/deactivate/{userId}`                  | Admin or owner  | Deactivate user                                     |
| GET    | `/users/search`                               | Admin           | Search users by query                               |
| GET    | `/reservations`                               | Admin           | List all reservations                               |
| GET    | `/reservations/{id}`                          | Admin or owner  | View reservation's details                          |
| POST   | `/reservations`                               | Admin or user   | Create a new reservation                            |
| GET    | `/reservations/users/{userId}`                | User            | View all user's own reservations                    |
| PATCH  | `/reservations/{id}`                          | Admin or owner  | Update reservation                                  |
| DELETE | `/reservations/{id}`                          | Admin or owner  | Delete reservation                                  |
| GET    | `/reservations/date/{date}/table/{tableId}`   | Admin           | View all reservations for a specific date and table |
| GET    | `/reservations/date/{date}`                   | Admin           | View all reservations for a specific date           |
| GET    | `/reservations/search`                        | Admin           | Search reservations by query                        |
| GET    | `/tables/available`                           | Public          | View available dining tables for a specific date, time, duration and number of seats |                       |
| POST   | `/tables`                | Admin  | Add a new dining table           |

---

## 🧪 Running Tests

The project includes unit and integration tests.

Run all tests:

```
mvn test
```

Testing technologies:

- JUnit 5

- MockMvc

- Testcontainers for MySQL integration

---

## 🛠️ Project Structure

```
src/main/java/com/proinwest/booking_table_app/
│
├── auth/                # Authentication and JWT handling
├── diningTable/          # Dining tables management
├── reservation/          # Reservation management
├── user/                 # User management
├── jwt/                  # JWT security filters, utils
└── BookingTableApplication.java
```

---

## 📎 Notes

- Database schema is auto-generated (Hibernate ddl-auto=update).

- Only active users can authenticate.

- Tokens have an expiration time configured via jwt.expirationMs.

---

## 🚀 Quick Test Flow (Postman)

Register a new user via POST /users

Login via POST /signin to get a JWT token

Authorize your requests by adding the Authorization: Bearer TOKEN header

Create a reservation via POST /reservations

---

## 👨‍💻 Author

Developed by Krzysztof Wołk





