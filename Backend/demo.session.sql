CREATE TABLE if NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(60) NOT NULL,
    price VARCHAR(16) NOT NULL
);

INSERT INTO books(title, author, price) VALUES
    ('jack rabbit', 'The rabbit hole', '21.50'),
    ('susan black', 'Where is the spot', '24.70'),
    ('michael powers', 'A look inside the sun', '22.90');

SELECT * FROM books