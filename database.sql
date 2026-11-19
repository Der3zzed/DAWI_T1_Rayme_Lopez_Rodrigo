-- Opcional: Hibernate puede generar la tabla con ddl-auto=update.
-- No elimina datos existentes.
CREATE DATABASE IF NOT EXISTS dawi_t1_rayme_lopez_rodrigo
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE dawi_t1_rayme_lopez_rodrigo;
CREATE TABLE IF NOT EXISTS empleados (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  especialidad VARCHAR(100) NULL,
  PRIMARY KEY (id)
);
