CREATE TABLE users
(
    id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL, --only col for the hashed password
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users
        PRIMARY KEY (id),
    CONSTRAINT unique_users_email
        UNIQUE (email),
    --Verifying all the things with the enum values
    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_gender
        CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT chk_users_kyc_status
        CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);