DROP TABLE IF EXISTS users, admin, traveler, provider CASCADE;

DROP TYPE IF EXISTS user_status, gender_type CASCADE;

-- ENUMS
CREATE TYPE user_status AS ENUM ('active', 'banned');
CREATE TYPE gender_type AS ENUM ('male', 'female', 'other');
CREATE TYPE role_type AS ENUM ('TRAVELER', 'PROVIDER', 'ADMIN');

-- TABLES
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    role role_type,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    gender gender_type,
    dob DATE,
    status user_status NOT NULL DEFAULT 'active',
    avatar TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);
CREATE TABLE admin (
    id BIGSERIAL PRIMARY KEY REFERENCES users(id)

);
CREATE TABLE traveler (
    id BIGSERIAL PRIMARY KEY REFERENCES users(id)
);
CREATE TABLE provider (
    id BIGSERIAL PRIMARY KEY REFERENCES users(id),
    phone_number VARCHAR(10) UNIQUE,
    address TEXT,
    business_name VARCHAR(100)
);