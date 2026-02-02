CREATE TABLE items (
    item_id INT PRIMARY KEY,
    name VARCHAR(255),
    category VARCHAR(50),
    brand VARCHAR(50),
    price DECIMAL(10,2),
    stock_qty INT
);

INSERT INTO items (item_id, name, category, brand, price, stock_qty) VALUES
(1, 'ThinkPad X1 Carbon', 'LAPTOP', 'Lenovo', 1200.00, 10),
(2, 'MacBook Air M2', 'LAPTOP', 'Apple', 999.00, 5),
(3, 'Pixel 7', 'PHONE', 'Google', 499.00, 20),
(4, 'iPhone 15', 'PHONE', 'Apple', 799.00, 0),
(5, 'Dell UltraSharp', 'MONITOR', 'Dell', 350.00, 8),
(6, 'Logitech MX Master', 'ACCESSORY', 'Logitech', 99.00, 50),
(7, 'Sony Alpha a7', 'CAMERA', 'Sony', 1800.00, 2);
