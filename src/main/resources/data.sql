INSERT INTO product_categories (name, description) VALUES
  ('Electronics', 'Consumer electronics and accessories'),
  ('Office Supplies', 'Equipment and supplies for office productivity'),
  ('Home & Kitchen', 'Essentials for home improvement and kitchen use'),
  ('Industrial', 'Parts and consumables for light manufacturing'),
  ('Health & Wellness', 'Personal care and safety essentials'),
  ('Outdoor & Travel', 'Gear for travel and outdoor activities');

INSERT INTO suppliers (name, contact_email, reliability_score) VALUES
  ('Aurora Supply Co.', 'contact@aurorasupply.example', 9),
  ('Beacon Wholesale', 'sales@beaconwholesale.example', 8),
  ('Northwind Traders', 'support@northwindtraders.example', 10),
  ('Summit Gear Ltd.', 'hello@summitgear.example', 8),
  ('Evergreen Imports', 'orders@evergreenimports.example', 7),
  ('Blue Harbor Logistics', 'service@blueharbor.example', 9),
  ('Metro Components', 'support@metrocomponents.example', 8),
  ('Atlas Stationers', 'care@atlasstationers.example', 7);

INSERT INTO customers (name, email, loyalty_tier) VALUES
  ('Acme Industries', 'ops@acme-industries.example', 'Gold'),
  ('Bright Future Labs', 'contact@brightfuture.example', 'Silver'),
  ('Crestview Retail', 'purchasing@crestview.example', 'Platinum'),
  ('Delta Outfitters', 'buy@deltaoutfitters.example', 'Silver'),
  ('Elysian Design Studio', 'team@elysian.example', 'Gold'),
  ('Frontier Hospitality', 'procurement@frontierhotels.example', 'Bronze'),
  ('Grove Markets', 'inventory@grovemarkets.example', 'Platinum'),
  ('Harbor City Schools', 'admin@harborcityschools.example', 'Silver'),
  ('Inspire Fitness', 'orders@inspirefitness.example', 'Gold'),
  ('Juniper Health', 'supplies@juniperhealth.example', 'Gold');

INSERT INTO products (name, description, price, quantity, category_id, supplier_id)
SELECT '4K Monitor', '27-inch UHD monitor with HDR support', 420, 35,
       (SELECT id FROM product_categories WHERE name = 'Electronics'),
       (SELECT id FROM suppliers WHERE name = 'Aurora Supply Co.')
UNION ALL
SELECT 'Wireless Keyboard', 'Compact Bluetooth keyboard with backlight', 95, 120,
       (SELECT id FROM product_categories WHERE name = 'Electronics'),
       (SELECT id FROM suppliers WHERE name = 'Beacon Wholesale')
UNION ALL
SELECT 'Ergonomic Office Chair', 'Adjustable lumbar support and breathable mesh', 260, 18,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Northwind Traders')
UNION ALL
SELECT 'Stainless Steel Cookware Set', '10-piece set with tri-ply construction', 310, 22,
       (SELECT id FROM product_categories WHERE name = 'Home & Kitchen'),
       (SELECT id FROM suppliers WHERE name = 'Beacon Wholesale')
UNION ALL
SELECT 'Noise-Cancelling Headphones', 'Over-ear design with adaptive noise control', 180, 50,
       (SELECT id FROM product_categories WHERE name = 'Electronics'),
       (SELECT id FROM suppliers WHERE name = 'Aurora Supply Co.')
UNION ALL
SELECT 'Industrial Drill Press', 'Floor-standing drill press with laser guide', 890, 6,
       (SELECT id FROM product_categories WHERE name = 'Industrial'),
       (SELECT id FROM suppliers WHERE name = 'Metro Components')
UNION ALL
SELECT 'Safety Gloves (Pack of 24)', 'Cut-resistant nitrile gloves for light assembly', 48, 200,
       (SELECT id FROM product_categories WHERE name = 'Health & Wellness'),
       (SELECT id FROM suppliers WHERE name = 'Evergreen Imports')
UNION ALL
SELECT 'Portable Projector', '1080p LED projector with wireless casting', 360, 15,
       (SELECT id FROM product_categories WHERE name = 'Electronics'),
       (SELECT id FROM suppliers WHERE name = 'Summit Gear Ltd.')
UNION ALL
SELECT 'High-Density Notebooks', 'A5 dotted notebooks, pack of 12', 38, 160,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Atlas Stationers')
UNION ALL
SELECT 'Standing Desk Converter', 'Adjustable riser for sit-stand setups', 185, 25,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Northwind Traders')
UNION ALL
SELECT 'Commercial Blender', '1500W blender with stainless steel jar', 275, 14,
       (SELECT id FROM product_categories WHERE name = 'Home & Kitchen'),
       (SELECT id FROM suppliers WHERE name = 'Blue Harbor Logistics')
UNION ALL
SELECT 'Travel Backpack 40L', 'Carry-on compliant with laptop sleeve', 115, 60,
       (SELECT id FROM product_categories WHERE name = 'Outdoor & Travel'),
       (SELECT id FROM suppliers WHERE name = 'Summit Gear Ltd.')
UNION ALL
SELECT 'LED Shop Light (Pack of 4)', 'Linkable 4-foot lights for warehouses and garages', 130, 90,
       (SELECT id FROM product_categories WHERE name = 'Industrial'),
       (SELECT id FROM suppliers WHERE name = 'Metro Components')
UNION ALL
SELECT 'First Aid Cabinet', 'Wall-mounted metal cabinet with 4 shelves', 210, 32,
       (SELECT id FROM product_categories WHERE name = 'Health & Wellness'),
       (SELECT id FROM suppliers WHERE name = 'Blue Harbor Logistics')
UNION ALL
SELECT 'Wireless Presentation Remote', 'Laser pointer with dual connectivity', 48, 85,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Aurora Supply Co.')
UNION ALL
SELECT 'Outdoor Portable Power Station', '600Wh lithium battery with AC and USB outputs', 520, 10,
       (SELECT id FROM product_categories WHERE name = 'Outdoor & Travel'),
       (SELECT id FROM suppliers WHERE name = 'Summit Gear Ltd.')
UNION ALL
SELECT 'Smart Air Purifier', 'HEPA H13 filter with Wi-Fi control', 240, 28,
       (SELECT id FROM product_categories WHERE name = 'Home & Kitchen'),
       (SELECT id FROM suppliers WHERE name = 'Evergreen Imports')
UNION ALL
SELECT 'Professional Label Maker', 'Thermal label printer with auto-cutter', 165, 40,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Atlas Stationers')
UNION ALL
SELECT 'Fiber Optic Patch Panel', '24-port rack-mount panel with LC adapters', 310, 12,
       (SELECT id FROM product_categories WHERE name = 'Industrial'),
       (SELECT id FROM suppliers WHERE name = 'Metro Components')
UNION ALL
SELECT 'Desk Lamp with Qi Charging', 'Adjustable LED lamp with wireless charger', 72, 95,
       (SELECT id FROM product_categories WHERE name = 'Office Supplies'),
       (SELECT id FROM suppliers WHERE name = 'Beacon Wholesale');

INSERT INTO purchase_orders (reference, status, customer_id) VALUES
  ('PO-2024-1001', 'Processing', (SELECT id FROM customers WHERE name = 'Acme Industries')),
  ('PO-2024-1002', 'Completed',  (SELECT id FROM customers WHERE name = 'Bright Future Labs')),
  ('PO-2024-1003', 'Awaiting Shipment', (SELECT id FROM customers WHERE name = 'Crestview Retail')),
  ('PO-2024-1004', 'Processing', (SELECT id FROM customers WHERE name = 'Delta Outfitters')),
  ('PO-2024-1005', 'Completed', (SELECT id FROM customers WHERE name = 'Grove Markets')),
  ('PO-2024-1006', 'Pending Approval', (SELECT id FROM customers WHERE name = 'Harbor City Schools')),
  ('PO-2024-1007', 'Awaiting Shipment', (SELECT id FROM customers WHERE name = 'Inspire Fitness')),
  ('PO-2024-1008', 'Completed', (SELECT id FROM customers WHERE name = 'Juniper Health')),
  ('PO-2024-1009', 'Processing', (SELECT id FROM customers WHERE name = 'Elysian Design Studio'));

INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price)
VALUES
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1001'),
   (SELECT id FROM products WHERE name = 'Wireless Keyboard'), 15, 90),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1001'),
   (SELECT id FROM products WHERE name = 'Noise-Cancelling Headphones'), 8, 175),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1002'),
   (SELECT id FROM products WHERE name = '4K Monitor'), 6, 410),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1002'),
   (SELECT id FROM products WHERE name = 'Ergonomic Office Chair'), 4, 255),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1003'),
   (SELECT id FROM products WHERE name = 'Stainless Steel Cookware Set'), 10, 295),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1003'),
   (SELECT id FROM products WHERE name = 'Smart Air Purifier'), 5, 230),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1004'),
   (SELECT id FROM products WHERE name = 'Travel Backpack 40L'), 24, 110),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1004'),
   (SELECT id FROM products WHERE name = 'Outdoor Portable Power Station'), 3, 510),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1005'),
   (SELECT id FROM products WHERE name = 'LED Shop Light (Pack of 4)'), 40, 125),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1005'),
   (SELECT id FROM products WHERE name = 'Industrial Drill Press'), 2, 875),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1006'),
   (SELECT id FROM products WHERE name = 'High-Density Notebooks'), 85, 34),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1006'),
   (SELECT id FROM products WHERE name = 'Desk Lamp with Qi Charging'), 30, 70),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1007'),
   (SELECT id FROM products WHERE name = 'Safety Gloves (Pack of 24)'), 60, 45),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1007'),
   (SELECT id FROM products WHERE name = 'First Aid Cabinet'), 12, 205),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1008'),
   (SELECT id FROM products WHERE name = 'Professional Label Maker'), 18, 160),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1008'),
   (SELECT id FROM products WHERE name = 'Fiber Optic Patch Panel'), 5, 300),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1009'),
   (SELECT id FROM products WHERE name = 'Standing Desk Converter'), 12, 175),
  ((SELECT id FROM purchase_orders WHERE reference = 'PO-2024-1009'),
   (SELECT id FROM products WHERE name = 'Portable Projector'), 7, 345);
