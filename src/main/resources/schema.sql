CREATE TABLE IF NOT EXISTS company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_company_company_id (company_id)
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    KEY idx_category_category_id (category_id)
);

CREATE TABLE IF NOT EXISTS keyword (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    KEY idx_keyword_keyword (keyword)
);

CREATE TABLE IF NOT EXISTS bank_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_date DATETIME NOT NULL,
    description VARCHAR(500) NOT NULL,
    deposit_amount BIGINT DEFAULT 0,
    withdrawal_amount BIGINT DEFAULT 0,
    balance_after BIGINT NOT NULL,
    branch VARCHAR(100),
    company_id BIGINT NULL,
    category_id BIGINT NULL,
    is_classified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    KEY idx_bank_transaction_company_id (company_id),
    KEY idx_bank_transaction_category_id (category_id),
    KEY idx_bank_transaction_date (transaction_date),
    KEY idx_bank_transaction_classified (is_classified)
);
