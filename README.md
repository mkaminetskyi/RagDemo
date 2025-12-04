
## DB Structure
- **customers** – клієнти з `name`, `email`, `loyalty_tier`.
- **suppliers** – постачальники з `contact_email` та `reliability_score`.
- **product_categories** – класифікація товарів з `name` та `description`.
- **products** – товари з полями `name`, `description`, `price`, `quantity` + зовнішні ключі:
  - `category_id` → `product_categories`
  - `supplier_id` → `suppliers`
- **purchase_orders** – записи продажів із `status`, кількістю `quantity` та посиланнями `customer_id` → `customers`, `product_id` → `products`.
