
USE hospital_icu_management;

-- Create ICU beds
CREATE TABLE IF NOT EXISTS icu_beds (
    id INT PRIMARY KEY,
    occupied BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT IGNORE INTO icu_beds (id, occupied)
VALUES
    (1, FALSE), (2, FALSE), (3, FALSE),
    (4, FALSE), (5, FALSE), (6, FALSE),
    (7, FALSE), (8, FALSE), (9, FALSE),
    (10, FALSE);

-- Create patients
CREATE TABLE IF NOT EXISTS patients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    emergency_level VARCHAR(20) NOT NULL,
    symptoms VARCHAR(500),
    bed_id INT UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ADMITTED',
    admitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bed_id) REFERENCES icu_beds(id)
);

-- Add new patient columns only when missing
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patients'
      AND column_name = 'doctor_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE patients ADD COLUMN doctor_id INT DEFAULT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patients'
      AND column_name = 'bill_amount'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE patients ADD COLUMN bill_amount DECIMAL(10,2) DEFAULT 0.00',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patients'
      AND column_name = 'discharged_at'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE patients ADD COLUMN discharged_at TIMESTAMP NULL DEFAULT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create doctors
CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ON_DUTY'
);

-- Create pharmacy
CREATE TABLE IF NOT EXISTS pharmacy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(100) NOT NULL,
    stock INT DEFAULT 0,
    price_per_unit DECIMAL(10,2) DEFAULT 0.00
);

-- Insert sample doctors only if their names do not exist
INSERT INTO doctors (name, specialty, status)
SELECT 'Dr. A. Sharma', 'Cardiologist', 'ON_DUTY'
WHERE NOT EXISTS (
    SELECT 1 FROM doctors WHERE name = 'Dr. A. Sharma'
);

INSERT INTO doctors (name, specialty, status)
SELECT 'Dr. S. Patel', 'Neurologist', 'ON_DUTY'
WHERE NOT EXISTS (
    SELECT 1 FROM doctors WHERE name = 'Dr. S. Patel'
);

INSERT INTO doctors (name, specialty, status)
SELECT 'Dr. R. Gupta', 'Pulmonologist', 'OFF_DUTY'
WHERE NOT EXISTS (
    SELECT 1 FROM doctors WHERE name = 'Dr. R. Gupta'
);

-- Insert sample medicines only if their names do not exist
INSERT INTO pharmacy (medicine_name, stock, price_per_unit)
SELECT 'Paracetamol 500mg', 500, 2.50
WHERE NOT EXISTS (
    SELECT 1 FROM pharmacy WHERE medicine_name = 'Paracetamol 500mg'
);

INSERT INTO pharmacy (medicine_name, stock, price_per_unit)
SELECT 'Amoxicillin 250mg', 120, 15.00
WHERE NOT EXISTS (
    SELECT 1 FROM pharmacy WHERE medicine_name = 'Amoxicillin 250mg'
);

INSERT INTO pharmacy (medicine_name, stock, price_per_unit)
SELECT 'Oxygen Cylinder', 10, 1500.00
WHERE NOT EXISTS (
    SELECT 1 FROM pharmacy WHERE medicine_name = 'Oxygen Cylinder'
);

INSERT INTO pharmacy (medicine_name, stock, price_per_unit)
SELECT 'Saline IV', 200, 45.00
WHERE NOT EXISTS (
    SELECT 1 FROM pharmacy WHERE medicine_name = 'Saline IV'
);
