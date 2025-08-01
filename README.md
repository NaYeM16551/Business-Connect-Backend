# Business-connect
## A social media platform designed to enable businessman and business enthusiastic to connect , collaborate and share ideas.


## How to run the project
1. Clone the repository(Before this must install git,jdk17+ and maven)
```bash
git clone https://github.com/NaYeM16551/Business-Connect.git
```
2. Navigate to the project directory(where pom.xml is located)
```bash
cd Business-Connect
```
3. Must create a .evn file in the root directory of the project and add the following lines:
```bash
   MAIL_PASSWORD=your-email-app--password(Not your email password)
   JWT_SECRET_KEY=your-secret-key
```   
4. Build the project using Maven
```bash
mvn clean install
```
5. Run the project
```bash
mvn spring-boot:run
```
6. Access the application
   - Open your web browser and go to `http://localhost:8080` to access the application.
7. Access the API
    - APIs active(login,register,verify-email,update-profile)
    - E.g: Open your web browser and go to `http://localhost:8080/api/v1/auth/login` to access the API.

8. Steps for database connection
   - This project uses postgresql database.
   - Create a database named `business_connect` in your PostgreSQL server.
   - Look at the application.properties file in the src/main/resources directory.
   - Update the database connection details in the application.properties file:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/business_connect
spring.datasource.username=your_database_username 
spring.datasource.password=your_database_password 

#import these from .env file 
```
   - After updating the database connection details, you can run the application again, and it should connect to the PostgreSQL database.
