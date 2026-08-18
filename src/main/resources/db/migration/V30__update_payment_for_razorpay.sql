ALTER TABLE payments
    ADD COLUMN razorpay_order_id VARCHAR(255);

ALTER TABLE payments
    ADD COLUMN razorpay_payment_id VARCHAR(255);

ALTER TABLE payments
    ADD CONSTRAINT uk_payment_razorpay_order
        UNIQUE (razorpay_order_id);

ALTER TABLE payments
    ADD CONSTRAINT uk_payment_razorpay_payment
        UNIQUE (razorpay_payment_id);