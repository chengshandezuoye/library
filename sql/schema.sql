CREATE DATABASE IF NOT EXISTS bookstore_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bookstore_db;

DROP TABLE IF EXISTS finance_records;
DROP TABLE IF EXISTS sales_orders;
DROP TABLE IF EXISTS purchase_order_items;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    employee_no VARCHAR(50) NOT NULL UNIQUE,
    gender VARCHAR(10) NOT NULL,
    age INT NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    retail_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    stock_qty INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_publisher (publisher)
);

CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_operator FOREIGN KEY (operator_id) REFERENCES users(id)
);

CREATE TABLE purchase_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    book_id BIGINT NULL,
    isbn VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_purchase_item_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_purchase_item_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE sales_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    operator_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_sale_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_sales_created_at (created_at)
);

CREATE TABLE finance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    related_type VARCHAR(20) NOT NULL,
    related_id BIGINT NOT NULL,
    remark VARCHAR(255) NOT NULL,
    operator_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_finance_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_finance_created_at (created_at),
    INDEX idx_finance_type (type)
);

INSERT INTO users (username, password_hash, real_name, employee_no, gender, age, role)
VALUES
('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 'EMP-ADMIN', '男', 30, 'super_admin'),
('manager', 'e10adc3949ba59abbe56e057f20f883e', '普通管理员', 'EMP-001', '女', 24, 'admin');

INSERT INTO books (isbn, title, author, publisher, retail_price, stock_qty)
VALUES
('9787115428028', 'Java核心技术', 'Cay S. Horstmann', '人民邮电出版社', 98.00, 20),
('9787115546081', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', 59.00, 30),
('9787302517597', '数据结构', '严蔚敏', '清华大学出版社', 45.00, 15),
('9787111612728', '算法导论', 'Thomas H. Cormen', '机械工业出版社', 128.00, 12),
('9787111213826', 'Effective Java', 'Joshua Bloch', '机械工业出版社', 89.00, 18),
('9787115293800', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 139.00, 10),
('9787115547644', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', 99.00, 25),
('9787302423287', '数据库系统概论', '王珊', '高等教育出版社', 49.00, 22),
('9787115521644', 'JavaScript高级程序设计', 'Matt Frisbie', '人民邮电出版社', 129.00, 16),
('9787115472588', 'Spring实战', 'Craig Walls', '人民邮电出版社', 79.00, 14);

INSERT INTO purchase_orders (status, operator_id, total_amount, created_at)
VALUES
('paid', 1, 1250.00, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('stocked', 1, 780.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('unpaid', 2, 360.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO purchase_order_items (purchase_order_id, book_id, isbn, title, author, publisher, purchase_price, quantity, subtotal)
VALUES
(1, 1, '9787115428028', 'Java核心技术', 'Cay S. Horstmann', '人民邮电出版社', 62.50, 20, 1250.00),
(2, 5, '9787111213826', 'Effective Java', 'Joshua Bloch', '机械工业出版社', 52.00, 15, 780.00),
(3, 8, '9787302423287', '数据库系统概论', '王珊', '高等教育出版社', 30.00, 12, 360.00);

INSERT INTO sales_orders (book_id, sale_price, quantity, total_amount, operator_id, created_at)
VALUES
(1, 98.00, 2, 196.00, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 59.00, 1, 59.00, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 128.00, 1, 128.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO finance_records (type, amount, related_type, related_id, remark, operator_id, created_at)
VALUES
('expense', 1250.00, 'purchase', 1, '进货付款：Java核心技术', 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('expense', 780.00, 'purchase', 2, '进货付款：Effective Java', 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('income', 196.00, 'sale', 1, '销售：Java核心技术 x2', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('income', 59.00, 'sale', 2, '销售：MySQL必知必会 x1', 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('income', 128.00, 'sale', 3, '销售：算法导论 x1', 1, DATE_SUB(NOW(), INTERVAL 1 DAY));
