-- Insert data into 'users' table only if it is empty
INSERT INTO users (id, username, email, password, role)
SELECT 1, 'admin_user', 'admin@example.com', 'admin_password', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (id, username, email, password, role)
SELECT 2, 'reader_user1', 'reader1@example.com', 'reader1_password', 'READER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 2);

INSERT INTO users (id, username, email, password, role)
SELECT 3, 'reader_user2', 'reader2@example.com', 'reader2_password', 'READER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 3);

-- Insert data into 'books' table only if it is empty
INSERT INTO books (id, title, author, price, publish_date, cover_image, user_id)
SELECT 1, 'Book One', 'Author One', '10.99', '2023-01-01', 'cover1.jpg', 
(SELECT id FROM users WHERE email = 'admin@example.com')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE id = 1);

INSERT INTO books (id, title, author, price, publish_date, cover_image, user_id)
SELECT 2, 'Book Two', 'Author Two', '12.99', '2022-05-01', 'cover2.jpg', 
(SELECT id FROM users WHERE email = 'reader1@example.com')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE id = 2);

INSERT INTO books (id, title, author, price, publish_date, cover_image, user_id)
SELECT 3, 'Book Three', 'Author Three', '15.99', '2021-03-15', 'cover3.jpg', 
(SELECT id FROM users WHERE email = 'reader2@example.com')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE id = 3);

INSERT INTO books (id, title, author, price, publish_date, cover_image, user_id)
SELECT 4, 'Book Four', 'Author Four', '9.99', '2020-08-20', 'cover4.jpg', 
(SELECT id FROM users WHERE email = 'reader1@example.com')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE id = 4);

INSERT INTO books (id, title, author, price, publish_date, cover_image, user_id)
SELECT 5, 'Book Five', 'Author Five', '11.99', '2019-11-11', 'cover5.jpg', 
(SELECT id FROM users WHERE email = 'reader2@example.com')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE id = 5);