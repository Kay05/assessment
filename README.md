# Chess Club Administration System

A comprehensive web application for managing chess club members, tracking rankings, and recording match results. Built with Spring Boot, Thymeleaf, Tailwind CSS, and MySQL.

## 🏆 Features

- **Member Management**: Full CRUD operations for chess club members
- **Dynamic Ranking System**: Automatically calculates and updates member rankings based on match results
- **Match Recording**: Record matches with automatic rank adjustments
- **Leaderboard**: Real-time leaderboard displaying current rankings
- **Statistics Tracking**: Individual member statistics and match history
- **Responsive Design**: Modern, professional UI with dark mode support
- **Search Functionality**: Search members by name or surname

## 🚀 Technology Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA, Spring MVC
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Additional**: Lombok, Bean Validation

## 📋 Prerequisites

- Docker and Docker Compose
- Java 17 or higher (for development)
- Maven 3.6 or higher (for development)
- A modern web browser

## 🛠️ Setup Instructions

### Quick Start with Docker Compose (Recommended)

#### 1. Clone the Repository

```bash
git clone https://github.com/Kay05/assessment.git
cd netstock-assessment
```

#### 2. Start the Application

Start both the MySQL database and the application using Docker Compose:

```bash
docker compose up -d
```

This will:
- Start a MySQL 8.0 container on port 3306
- Create the `chess_club_db` database automatically
- Start the Spring Boot application on port 8080

The application will be available at `http://localhost:8080`

#### 3. Stop the Application

To stop all services:

```bash
docker compose down
```

To stop and remove all data (including database):

```bash
docker compose down -v
```

### Manual Setup (Alternative)

#### 1. Database Setup

If not using Docker Compose, create a MySQL database manually:

```sql
CREATE DATABASE chess_club_db;
```

**Default Configuration:**
- Host: `mysql:3306` (when using Docker Compose)
- Host: `localhost:3306` (when running locally)
- Database: `chess_club_db`
- Username: `root`
- Password: `rootpassword`

**Custom Configuration:**
Update the database configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### 2. Build the Application

```bash
mvn clean compile
```

#### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

#### 4. Alternative: Run with JAR

```bash
mvn clean package
java -jar target/netstock-assessment-1.0.0.jar
```

## 🎮 Usage Guide

### Adding Members

1. Navigate to **Members** > **Add New Member**
2. Fill in the required information:
   - Name and Surname
   - Email address (must be unique)
   - Birthday
3. New members are automatically assigned the lowest rank

### Recording Matches

1. Navigate to **Matches** > **Record New Match**
2. Select two different players
3. Choose the match result:
   - **Player 1 Wins**
   - **Player 2 Wins**
   - **Draw**
4. Optionally add notes and custom date/time
5. Rankings are automatically updated based on the result

### Ranking Rules

The application implements the following ranking rules:

1. **Higher-ranked player wins**: No rank changes occur

2. **Draw**: 
   - Lower-ranked player gains one position
   - The player at that position moves down to fill the gap
   - **Exception**: If players are adjacent (e.g., ranks 3 and 4), no changes occur

3. **Lower-ranked player wins**:
   - **Adjacent ranks** (e.g., ranks 3 vs 4): Players simply swap positions
   - **Non-adjacent ranks**: 
     - Higher-ranked player moves down one position
     - Lower-ranked player moves up by half the difference between their original ranks
     - All affected players between them are adjusted accordingly
     - Example: Ranks 10 vs 16, lower player wins → Player at rank 10 moves to 11, Player at rank 16 moves to 13

Note: The system maintains ranking integrity by ensuring all ranks remain sequential (1, 2, 3, ...) with no gaps or duplicates.

### Viewing Statistics

- **Individual Statistics**: Click on any member to view their profile, match history, and statistics
- **Leaderboard**: View the complete ranking of all members
- **Home Dashboard**: Quick overview with top 10 rankings and recent matches

## 📁 Project Structure

```
netstock-assessment/
├── src/main/java/com/netstock/chessclub/
│   ├── entity/          # JPA entities (Member, Match)
│   ├── repository/      # Data access layer
│   ├── service/         # Business logic layer
│   ├── controller/      # Web controllers
│   ├── dto/             # Data transfer objects
│   ├── exception/       # Custom exceptions
│   └── ChessClubApplication.java
├── src/main/resources/
│   ├── templates/       # Thymeleaf templates
│   ├── static/          # Static resources (CSS, JS)
│   └── application.properties
└── src/test/java/       # Test classes
```

## 🎨 UI Features

### Dark Mode Support
- Toggle between light and dark themes
- Preference is saved in browser localStorage
- Responsive design for all screen sizes

### Professional Styling
- Clean, modern interface using Tailwind CSS
- Intuitive navigation with breadcrumbs
- Visual feedback for all user actions
- Form validation with helpful error messages

## 🔧 Configuration Options

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/chess_club_db?createDatabaseIfNotExist=true
spring.jpa.hibernate.ddl-auto=update

# Logging Levels
logging.level.com.netstock.chessclub=DEBUG
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chess_club_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export SERVER_PORT=8080
```

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

Run a single test class

```bash
mvn test -Dtest=MatchTest
```

Run with full class name

```bash
mvn test -Dtest=com.netstock.chessclub.entity.MatchTest
```

Run multiple specific test classes

```bash
mvn test -Dtest=MatchTest,MemberTest
```

Run all tests matching a pattern

```bash
mvn test -Dtest=*RepositoryTest
```

Run a specific test method

```bash
mvn test -Dtest=MatchTest#testMatchBuilder
```

The test suite includes:
- Unit tests for services and repositories
- Integration tests for controllers
- Data JPA tests with H2 in-memory database
- Total of 175+ test cases covering all major functionality

## 📊 Database Schema

### Members Table
- `id` (Primary Key)
- `name`, `surname`, `email` (Required)
- `birthday` (Required)
- `games_played` (Auto-incremented)
- `current_rank` (Unique, auto-assigned)
- `created_at`, `updated_at` (Timestamps)

### Matches Table
- `id` (Primary Key)
- `player1_id`, `player2_id` (Foreign Keys)
- `result` (PLAYER1_WIN, PLAYER2_WIN, DRAW)
- `player1_rank_before`, `player1_rank_after`
- `player2_rank_before`, `player2_rank_after`
- `match_date`, `notes`

## 👨‍💻 Development

### Code Style
- Uses Lombok to reduce boilerplate code
- Follows Spring Boot best practices
- Comprehensive commenting for business logic
- Clean architecture with separation of concerns

### Key Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic encapsulation
- **DTO Pattern**: Data transfer between layers
- **MVC Pattern**: Web layer organization

*Chess Club Administration System v1.0.0*