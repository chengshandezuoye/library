USE bookstore_db;

INSERT IGNORE INTO users (username, password_hash, real_name, employee_no, gender, age, role)
VALUES
('buyer', 'e10adc3949ba59abbe56e057f20f883e', '采购管理员', 'EMP-002', '男', 26, 'admin'),
('cashier', 'e10adc3949ba59abbe56e057f20f883e', '收银管理员', 'EMP-003', '女', 23, 'admin');

INSERT IGNORE INTO books (isbn, title, author, publisher, retail_price, stock_qty)
VALUES
('9787111612728', '算法导论', 'Thomas H. Cormen', '机械工业出版社', 128.00, 12),
('9787111213826', 'Effective Java', 'Joshua Bloch', '机械工业出版社', 89.00, 18),
('9787115293800', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 139.00, 10),
('9787115547644', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', 99.00, 25),
('9787302423287', '数据库系统概论', '王珊', '高等教育出版社', 49.00, 22),
('9787115521644', 'JavaScript高级程序设计', 'Matt Frisbie', '人民邮电出版社', 129.00, 16),
('9787115472588', 'Spring实战', 'Craig Walls', '人民邮电出版社', 79.00, 14),
('9787115607584', 'Vue.js设计与实现', '霍春阳', '人民邮电出版社', 119.00, 20),
('9787115594839', 'Redis设计与实现', '黄健宏', '机械工业出版社', 89.00, 13),
('9787115504265', '鸟哥的Linux私房菜', '鸟哥', '人民邮电出版社', 118.00, 11);
