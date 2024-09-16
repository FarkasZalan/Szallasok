# Accommodations (2022)

## User Management
Users can register if they don't have an account yet. There are two types of users: renters and sellers. After providing the required information, users can log in (if they have already registered but forgot their password, they can request an email reminder), where they can view and modify their own data.

## Admin
There is an admin user (email: admin | password: admin) who can view and delete all registered users and their data from the system. This includes, for example, deleting a renter's bookings that are scheduled after the deletion date, or removing all accommodations from a host, along with all related data. Affected users receive email notifications about these changes.

Additionally, the admin can view and delete reviews for accommodations and can also modify their own data if desired.



## About the Project

In this Spring application, users can view all accommodations and their associated reviews. If a renter is logged in, they have the option to write a review for an accommodation and book it. When booking, they need to specify the desired check-in and check-out dates and the number of people. If there is insufficient availability for the specified number of guests on the chosen dates, the system will not allow the booking and will suggest alternative dates that are closest to the requested period. It will display a previous date only if the new period does not fall within the current day or a future date, and it will also show available options after the desired interval. Renters can modify or cancel bookings as long as they do so before the booking date.

Whenever an accommodation is booked, modified, or canceled, the affected host will receive an email notification.

Hosts can create new accommodations and upload images for each one if desired. If a host decides to modify or delete an accommodation, all affected renters will receive an email notification about the change.

### Additional Information

The application was developed using the Spring framework in Java, with Maven and Thymeleaf, and utilizes a MySQL database. It can be run in a Docker environment (requiring a MySQL image). After running the command docker-compose up --build, the application will start and be accessible at localhost:8090.

The application implements CRUD operations with appropriate error handling, and SQL queries are used in conjunction with JDBC within the project (e.g., for the booking process).
