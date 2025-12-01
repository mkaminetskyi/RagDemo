INSERT INTO product_categories (name, description) VALUES
  ('Електроніка', 'Смартфони, ноутбуки та аксесуари для щоденної роботи'),
  ('Дім і кухня', 'Техніка та корисні речі для затишного побуту'),
  ('Офіс і навчання', 'Рішення для робочого місця та навчання'),
  ('Авто та інструменти', 'Обладнання для авто та побутового ремонту'),
  ('Туризм і спорт', 'Спорядження для активного відпочинку'),
  ('Краса та здоров''я', 'Догляд за собою та товари для відновлення');

INSERT INTO suppliers (name, contact_email, reliability_score) VALUES
  ('СвітТех Поставка', 'sales@svitteh.ua', 92),
  ('Балтія Логістик', 'orders@balticlogi.eu', 88),
  ('Карпати Трейд', 'hello@karpatytrade.ua', 81),
  ('Дунай Імпорт', 'support@danubeimport.eu', 85),
  ('Полісся Маркет', 'contact@polissya.market', 76);

INSERT INTO products (name, description, price, quantity, category_id, supplier_id) VALUES
  ('Бездротова миша', 'Ергономічна форма, 6 кнопок, сенсор 16000 DPI, час роботи до 70 годин.', 1299, 140,
   (SELECT id FROM product_categories WHERE name = 'Електроніка'),
   (SELECT id FROM suppliers WHERE name = 'СвітТех Поставка')),
  ('Навушники з шумопоглинанням', 'Bluetooth 5.3, режим прозорості, до 35 годин автономності, зарядка USB-C.', 3699, 65,
   (SELECT id FROM product_categories WHERE name = 'Електроніка'),
   (SELECT id FROM suppliers WHERE name = 'Балтія Логістик')),
  ('Мультиварка 5л', '11 програм, антипригарна чаша, відкладений старт і підтримка підігріву.', 2899, 48,
   (SELECT id FROM product_categories WHERE name = 'Дім і кухня'),
   (SELECT id FROM suppliers WHERE name = 'Карпати Трейд')),
  ('Регульований офісний стіл', 'Електропривід, пам''ять висот, система кабель-менеджменту.', 8799, 22,
   (SELECT id FROM product_categories WHERE name = 'Офіс і навчання'),
   (SELECT id FROM suppliers WHERE name = 'Дунай Імпорт')),
  ('Настільна лампа з Qi зарядкою', 'LED, 4 кольорові температури, бездротова зарядка 15 Вт, таймер сну.', 1890, 75,
   (SELECT id FROM product_categories WHERE name = 'Офіс і навчання'),
   (SELECT id FROM suppliers WHERE name = 'СвітТех Поставка')),
  ('Трекінговий рюкзак 45л', 'Захист від дощу, вентильована спинка, кріплення для палиць, відділення для ноутбука.', 3490, 38,
   (SELECT id FROM product_categories WHERE name = 'Туризм і спорт'),
   (SELECT id FROM suppliers WHERE name = 'Полісся Маркет')),
  ('Намет на 3 особи', 'Двошаровий тент 5000 мм, алюмінієві дуги, вентиляційні вікна та снігові спідниці.', 5120, 19,
   (SELECT id FROM product_categories WHERE name = 'Туризм і спорт'),
   (SELECT id FROM suppliers WHERE name = 'Балтія Логістик')),
  ('Масажний пістолет', '6 насадок, 5 швидкостей, акумулятор 2500 мА·год, металевий корпус.', 2790, 54,
   (SELECT id FROM product_categories WHERE name = 'Краса та здоров''я'),
   (SELECT id FROM suppliers WHERE name = 'Карпати Трейд')),
  ('Компресор автомобільний', 'Цифровий манометр, автостоп, ліхтарик, металевий циліндр.', 2150, 102,
   (SELECT id FROM product_categories WHERE name = 'Авто та інструменти'),
   (SELECT id FROM suppliers WHERE name = 'Полісся Маркет')),
  ('Набір гайкових ключів 24 шт.', 'Хром-ванадієва сталь, тріскачка 72 зубці, ударостійкий кейс.', 3990, 34,
   (SELECT id FROM product_categories WHERE name = 'Авто та інструменти'),
   (SELECT id FROM suppliers WHERE name = 'Дунай Імпорт'));

INSERT INTO customers (name, email, loyalty_tier) VALUES
  ('Олена Коваль', 'olena.koval@example.com', 'Gold'),
  ('Ігор Мельник', 'ihor.melnyk@example.com', 'Silver'),
  ('Марія Ткач', 'maria.tkach@example.com', 'Bronze');

INSERT INTO purchase_orders (customer_id, status, reference) VALUES
  ((SELECT id FROM customers WHERE name = 'Олена Коваль'), 'submitted', 'PO-2024-001'),
  ((SELECT id FROM customers WHERE name = 'Ігор Мельник'), 'processing', 'PO-2024-002');

INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price) VALUES
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-001'),
   (SELECT id FROM products WHERE name = 'Бездротова миша'), 3, 1250),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-001'),
   (SELECT id FROM products WHERE name = 'Настільна лампа з Qi зарядкою'), 2, 1850),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-002'),
   (SELECT id FROM products WHERE name = 'Компресор автомобільний'), 4, 2100),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-002'),
   (SELECT id FROM products WHERE name = 'Намет на 3 особи'), 1, 5050);
