CREATE TABLE IF NOT EXISTS dining_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    number INT,
    seats INT
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    login VARCHAR(45) NOT NULL,
    password VARCHAR(45) NOT NULL,
    first_name VARCHAR(45),
    last_name VARCHAR(45),
    email VARCHAR(320) NOT NULL,
    phone_number VARCHAR(20) NOT NULL
);

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
