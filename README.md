# RagDemo: огляд схеми БД та API з function calling

## Структура реляційної бази даних
- **customers** – клієнти з `name`, `email`, `loyalty_tier`.
- **suppliers** – постачальники з `contact_email` та `reliability_score`.
- **product_categories** – класифікація товарів з `name` та `description`.
- **products** – товари з полями `name`, `description`, `price`, `quantity` + зовнішні ключі:
  - `category_id` → `product_categories`
  - `supplier_id` → `suppliers`
- **purchase_orders** – замовлення з `reference`, `status` та посиланням `customer_id` → `customers`.
- **purchase_order_lines** – позиції замовлень з `quantity`, `unit_price` + зв'язки `order_id` → `purchase_orders`, `product_id` → `products`.

Схему застосовують як для табличного RAG (LLM отримує рядки як контекст), так і для текст-to-SQL, де модель генерує запити до цих таблиць.

## API та можливості function calling
Контролер `RagDemoController` експонує декілька демонстраційних сценаріїв:

- `GET /chatWithRag` – звичайний RAG: підтягує схожі документи з vector store і віддає їх у prompt.
- `GET /chatWithRagAndTool` – RAG + function calling. Модель може викликати методи `ProductTools` щоб витягнути структуровані дані з БД.
- `GET /search-product` – семантичний пошук товарів у vector store.
- `GET /ask-product-question` – формує prompt із рядками товарів для Q&A по каталогу.
- `GET /text-to-sql` – генерує SQL для схеми вище, виконує його через JDBC і просить модель підсумувати результат.
- `POST /add-products-to-vector-store`, `/add-document`, `/upload-*` – демонстраційні ендпоїнти для наповнення vector store.

### Інструменти (`ProductTools`) доступні для function calling
- `getProductDetails(name)` – повертає конкретний товар.
- `findClosestProducts(name, topK)` – використовує embeddings для пошуку схожих назв.
- `searchProductsByName(phrase)` – знаходить товари за частковим збігом у БД.
- `listLowStock(threshold)` – повертає товари з запасом нижче або на рівні порогу.
- `productsBySupplier(supplierName)` – відбирає товари певного постачальника.
- `summarizeInventory()` – агрегує кількість товарів і середню ціну по категоріях.

Такі інструменти дозволяють будувати складніші ланцюжки: наприклад, модель може спершу викликати `summarizeInventory`, а потім на основі агрегованих чисел сформувати відповідь користувачу.
