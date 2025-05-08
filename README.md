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
- Testcontainers (Integration and end-to-end Testing)
- JUnit 5 & MockMvc
- Maven

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/k-wolk/booking-table-app
cd booking-table-app
```

### 2. Configure the database
Edit src/main/resources/application.properties:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tables_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.app.jwtSecret=YOUR_SECRET_KEY
spring.app.jwtExpirationMs=86400000
```
⚠️ Make sure to create tables before running the application. You can find schemas below.

⚠️ Make sure your local MySQL server is running.

### 3. Build and run the application
```bash
mvn clean install
mvn spring-boot:run
```
The app will be available at http://localhost:8080.

---

## 🗄️ Database

This application requires a relational database MySQL with the following three tables:

- `dining_table` – stores information about dining tables, such as table number and number of seats.
- `user` – stores user data including login credentials, personal information, role, and account status.
- `reservation` – stores reservation records, each linked to a specific user and dining table, with date, time, and duration.

### 💾 SQL Schema

```sql
CREATE TABLE IF NOT EXISTS dining_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    number INT,
    seats INT,
    active BOOLEAN
);
```
```sql
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    login VARCHAR(45) NOT NULL,
    password VARCHAR(60) NOT NULL,
    first_name VARCHAR(45),
    last_name VARCHAR(45),
    email VARCHAR(320) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    active BOOLEAN,
    role VARCHAR(20)
);
```
```sql
CREATE TABLE IF NOT EXISTS reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_id INT NOT NULL,
    user_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    duration INT NOT NULL,
    FOREIGN KEY (table_id) REFERENCES dining_table(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```



---

## 🔐 Authentication

Authentication is based on JWT tokens.

After creating a user account, sign in to receive a token:

POST /signin

```json
{
  "login": "john",
  "password": "yourpassword"
}
```

Example Authorization Header for requests:

```bash
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
| POST   | `/reservations`                               | Admin / user    | Create a new reservation                            |
| GET    | `/reservations/users/{userId}`                | Resource owner  | View own reservations                               |
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

The project includes unit, integration and end-to-end tests.

Run all tests:

```bash
mvn test
```

Testing technologies:

- JUnit 5
  
- Mockito

- MockMvc

- Testcontainers for MySQL integration

---

## 🛠️ Project Structure

```bash
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





