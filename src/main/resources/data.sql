-- Crear tabla de hechos
CREATE TABLE IF NOT EXISTS fact_sales (
    Region VARCHAR(50),
    Product VARCHAR(50),
    Sales DECIMAL(10,2),
    Quantity INT,
    "Year" INT
);

-- Insertar datos de ejemplo
INSERT INTO fact_sales (Region, Product, Sales, Quantity, "Year") VALUES
('EMEA', 'ProductA', 1000.00, 50, 2024),
('EMEA', 'ProductB', 1500.00, 75, 2024),
('APAC', 'ProductA', 800.00, 40, 2024),
('APAC', 'ProductB', 1200.00, 60, 2024),
('AMERICAS', 'ProductA', 2000.00, 100, 2024),
('AMERICAS', 'ProductB', 2500.00, 125, 2024);

-- Crear vista del cubo OLAP
CREATE OR REPLACE VIEW vw_olap_cube AS 
SELECT Region, Product, Sales, Quantity, "Year" 
FROM fact_sales;