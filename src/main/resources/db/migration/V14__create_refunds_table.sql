CREATE TABLE refunds
(
    id BIGSERIAL PRIMARY KEY,

    payment_id BIGINT NOT NULL UNIQUE,

    refund_amount DECIMAL(10,2) NOT NULL,

    refund_reason VARCHAR(255),

    refund_status VARCHAR(20) NOT NULL,

    refund_transaction_id VARCHAR(100) UNIQUE,

    refunded_at TIMESTAMP,

    CONSTRAINT fk_refund_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments(id)
);