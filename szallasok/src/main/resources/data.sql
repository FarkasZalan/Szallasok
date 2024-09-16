-- Default User
INSERT  into user (email,user_type, name, address, phone, password) VALUES ('admin','Admin', 'Admin', 'SZTE Irinyi épület', '+36 30 000 0000','admin')
ON DUPLICATE KEY UPDATE email = VALUES (email),user_type = VALUES (user_type), name = VALUES(name), address = VALUES(address), phone = VALUES(phone), password = VALUES(password);
INSERT  into user (email,user_type, name, address, phone, password) VALUES ('farkaszalan2001@gmail.com','Bérlő', 'Farkas Zalán', 'Szeged', '+36 30 000 0000','123')
ON DUPLICATE KEY UPDATE email = VALUES (email),user_type = VALUES (user_type), name = VALUES(name), address = VALUES(address), phone = VALUES(phone), password = VALUES(password);

INSERT  into accommodation (accommodationID,name, rental_user, location, description, price_per_day_per_person, max_capacity,image) VALUES (1,'Balatoni szállás','farkaszalan2001@gmail.com', 'Siófok, egyutca 1', 'Szép kilátás a balatonra', 1200, 12, '')
ON DUPLICATE KEY UPDATE accommodationID = VALUES (accommodationID), name = VALUES (name),rental_user = VALUES (rental_user), location = VALUES(location), description = VALUES(description), price_per_day_per_person = VALUES(price_per_day_per_person), max_capacity = VALUES(max_capacity), image = VALUES(image);