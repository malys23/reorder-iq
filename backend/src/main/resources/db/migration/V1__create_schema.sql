CREATE TABLE users (
    user_id             INT PRIMARY KEY,
    username            VARCHAR(100) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login          TIMESTAMPTZ,
    user_role           VARCHAR(100) NOT NULL CHECK (user_role IN ('planner', 'approver', 'receiver'))
);

CREATE TABLE suppliers (
    supplier_id         INT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    category            VARCHAR(100) NOT NULL,
    lead_time_cv        NUMERIC(3,2) NOT NULL CHECK (lead_time_cv >= 0),  
    on_time_rate        NUMERIC(3,2) NOT NULL CHECK (on_time_rate<=1 AND on_time_rate>=0)
);

CREATE TABLE items (
    item_id             INT PRIMARY KEY,         
    name                VARCHAR(100) NOT NULL UNIQUE,
    category            VARCHAR(100) NOT NULL,
    supplier_id         INT NOT NULL REFERENCES suppliers(supplier_id),
    unit_cost           NUMERIC(8,2) NOT NULL CHECK (unit_cost > 0),
    lead_time_days      INT NOT NULL CHECK (lead_time_days >0),
    abc_class           VARCHAR(1) NOT NULL CHECK (abc_class IN ('A', 'B', 'C'))
);

CREATE TABLE demand_history(
    item_id             INT NOT NULL REFERENCES items(item_id),
    week_start          DATE NOT NULL,
    quantity            INT NOT NULL,
    is_holdout          BOOLEAN NOT NULL,
    PRIMARY KEY (item_id, week_start)
);

CREATE TABLE stock_levels(
    item_id             INT PRIMARY KEY REFERENCES items(item_id),
    on_hand             INT NOT NULL CHECK (on_hand >= 0),
    on_order            INT NOT NULL CHECK (on_order >=0)
);

CREATE TABLE purchase_requests(
    request_id          SERIAL PRIMARY KEY,
    item_id             INT NOT NULL REFERENCES items(item_id),
    quantity            INT NOT NULL CHECK (quantity > 0),
    needed_by           DATE NOT NULL,
    created_at          DATE NOT NULL,
    requested_by        INT NOT NULL REFERENCES users(user_id),
    status              VARCHAR(100) NOT NULL CHECK (status IN ('draft','submitted', 'approved', 'ordered', 'received')),
    submitted_at        DATE,
    approved_at         DATE,
    approved_by         INT REFERENCES users(user_id),
    ordered_at          DATE,
    received_at         Date,
    received_by         INT REFERENCES users(user_id)
);

CREATE INDEX idx_pr_item_id
    ON purchase_requests(item_id);

CREATE INDEX idx_pr_status
    ON purchase_requests(status);

CREATE TABLE forecasts (
    forecast_id         SERIAL PRIMARY KEY,
    item_id             INT NOT NULL REFERENCES items(item_id),
    week_start          DATE NOT NULL,
    predicted_quantity  NUMERIC(9,2) NOT NULL,
    method              VARCHAR(100) NOT NULL,
    created_at          DATE NOT NULL
);

CREATE INDEX idx_item_forecast
    ON forecasts(item_id);