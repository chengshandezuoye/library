# 图书销售管理系统

本项目是“图书销售管理系统的设计与实现”中期实验项目，采用 `Java Swing + MySQL + JDBC + Maven` 实现桌面端管理程序。

## 功能模块

- 用户登录：所有功能需登录后使用；系统会记住上一次成功登录的用户名，但不会记住密码。
- 用户管理：超级管理员可创建普通管理员、删除普通管理员、重置普通管理员密码；普通管理员可维护本人资料并修改自己的密码。
- 图书库存：支持新增、查询、修改图书信息和库存数量；ISBN 创建后不可修改，新增和修改前会弹出确认提示。
- 图书进货：支持新书/已有图书进货、付款、退货、入库；进货管理界面按“载入已有图书、图书信息、进货信息、进货单操作”分区展示。
- 图书销售：按零售价格销售图书，自动扣减库存。
- 财务账单：进货付款生成支出记录，图书销售生成收入记录，可按日期和类型查询。

## 环境要求

- JDK 17 或以上
- Maven 3.8 或以上
- MySQL 8.x

## 数据库初始化

1. 修改 `src/main/resources/db.properties` 中的 MySQL 账号密码。
2. 在 MySQL 中执行：

```sql
SOURCE sql/schema.sql;
```

如果使用命令行，也可以执行：

```bash
mysql -u root -p < sql/schema.sql
```

如需追加更多演示数据，可在初始化后执行：

```sql
SOURCE sql/seed_extra.sql;
```

默认数据库：`bookstore_db`

默认账号：

- 超级管理员：`admin / admin123`
- 普通管理员：`manager / 123456`

## 启动项目

在项目根目录执行：

```bash
mvn clean compile
mvn exec:java
```

## 现场演示建议

1. 使用 `admin / admin123` 登录系统。
2. 在“用户管理”中创建一个普通管理员账号，演示两次输入密码确认；也可在表格中右键普通管理员，演示重置密码或删除普通管理员。
3. 使用普通管理员登录，在“用户管理”中修改本人资料或修改自己的密码。
4. 在“图书库存”中查询、新增或修改一本图书，说明 ISBN 创建后不可修改。
5. 在“进货管理”中创建进货单，演示未付款、付款、退货和入库流程。
6. 在“图书销售”中选择图书并销售，观察库存减少。
7. 在“财务账单”中查询进货支出和销售收入记录。

## 目录结构

```text
.
├── pom.xml
├── README.md
├── sql
│   ├── schema.sql
│   └── seed_extra.sql
└── src
    └── main
        ├── java
        │   └── com/bookstore
        │       ├── App.java
        │       ├── dao
        │       ├── model
        │       ├── ui
        │       └── util
        └── resources
            └── db.properties
```

## 说明

实验要求建议使用 MD5 加密密码，本项目按要求实现。密码哈希逻辑位于 `src/main/java/com/bookstore/util/PasswordUtil.java`，通过 `MessageDigest.getInstance("MD5")` 生成 MD5；登录和创建用户时在 `UserDao` 中调用 `PasswordUtil.md5(password)`，数据库中保存的是 `password_hash`，不是明文密码。

实际生产系统不建议使用 MD5 存储密码，应使用 bcrypt、argon2 等专用密码哈希算法。
