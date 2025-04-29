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

After creating a user account, sign in to receive a token:

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
| GET    | `/users`                                      | Admin           | View a list of all users                            |
| GET    | `/users/{userId}`                             | Admin / resource owner | View a user's details                        |
| PATCH  | `/users/{userId}`                             | User            | Update own user details                             |
| PUT    | `/users/role/{userId}`                        | Admin           | Update a user's role                                |
| PATCH  | `/users/deactivate/{userId}`                  | Admin / resource owner | Deactivate a user                            |
| GET    | `/users/search`                               | Admin           | Search users by query                               |
| GET    | `/reservations`                               | Admin           | View a list of all reservations                     |
| GET    | `/reservations/{id}`                          | Admin / resource owner | View reservation details                     |
| POST   | `/reservations`                               | Admin / resource user  | Create a new reservation                     |
| GET    | `/reservations/users/{userId}`                | User            | View own reservations                               |
| PATCH  | `/reservations/{id}`                          | Admin / resource owner | Update a reservation                         |
| DELETE | `/reservations/{id}`                          | Admin / resource owner | Delete a reservation                         |
| GET    | `/reservations/date/{date}/table/{tableId}`   | Admin           | View reservations for a specific date and table     |
| GET    | `/reservations/date/{date}`                   | Admin           | View reservations for a specific date               |
| GET    | `/reservations/search`                        | Admin           | Search reservations by query                        |
| GET    | `/tables/available`                           | Public          | View available dining tables for a specific date, time, duration and number of seats | 
| GET    | `/tables/getactive`                           | Public          | View a list of all active dining tables             |
| GET    | `/tables`                                     | Admin           | View a list of all dining tables                    | 
| GET    | `/tables/{tableId}`                           | Public (active) / Admin (all) | View details of a specific dining table | 
| POST   | `/tables`                                     | Admin           | Add a new dining table                              |
| POST   | `/tables/deactivate/{tableId}`                | Admin           | Deactivate a dining table                           |
| POST   | `/tables/activate/{tableId}`                  | Admin           | Activate a dining table                             |
| PATCH  | `/tables/{tableId}`                           | Admin           | Update a dining table                               |
| DELETE | `/tables/{tableId}`                           | Admin           | Delete a dining table                               |
| GET    | `/tables/{tableId}/date/{date}`               | Public          | View available hours for specific dining table and date |

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
├── diningTable/ # Dining tables management (controllers, services, entities, repositories, validators)
├── reservation/ # Reservation management
├── user/ # User management
│
├── security/
│    ├── auth/ # Login logic (LoginController, LoginRequest, LoginResponse)
│    ├── config/ # Spring Security config (SecurityConfig, AuthEntryPointJwt, filters)
│    ├── jwt/ # JWT utilities (JwtUtils, SecurityUtils)
│    └── userdetails/ # CustomUserDetails and UserDetailsService
│
├── exception/
│    ├── handler/ # Global exception handler
│    └── types/ # Custom exception classes (e.g., NotFoundException, InvalidInputException)
│
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





