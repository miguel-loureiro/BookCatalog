-- V1__create_user_and_books_tables.sql

-- Drop the 'users' and 'books' tables if they exist
DROP TABLE IF EXISTS user_books CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    cover_image TEXT,
    version BIGINT,
    CONSTRAINT users_unique_email UNIQUE (email)
);

-- Create the 'books' table
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) UNIQUE NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    price VARCHAR(10),
    publish_date DATE,
    cover_image_url TEXT,
    version BIGINT
);

-- Create the many-to-many relationship table between users and books
CREATE TABLE IF NOT EXISTS user_books (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    book_id INTEGER REFERENCES books(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, book_id)
);