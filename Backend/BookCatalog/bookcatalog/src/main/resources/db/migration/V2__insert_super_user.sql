-- V2__insert_super_user.sql

-- Insert a SUPER user into the 'users' table
INSERT INTO users (username, email, password, role, cover_image, version)
VALUES (
    'superuser',
    'publixoapagar@gmail.com',
    '$2a$10$Dre7yM16nlHIloWftfw2Kuk90nVsrh4tnqN/1MSrIoqlp3bHdquX6',
    'SUPER',
    NULL,
    0
);