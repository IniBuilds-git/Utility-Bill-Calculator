# Utility Bill Management System

A professional utility management application for electricity and gas billing. Designed with **JavaFX** and **Maven**, this system demonstrates advanced **Object-Oriented Programming (OOP)** principles, design patterns, and robust data persistence.

---

## Key Features

- **Customer Management**: Full life-cycle management (Add, View, Edit, Delete).
- **Meter Readings**: Precision tracking for Electricity (Day/Night) and Gas (including m³ to kWh conversion).
- **Automated Invoicing**: Generation of itemized bills with VAT and standing charges.
- **Payment Processing**: Record and track payments against pending invoices.
- **Interactive Dashboard**: Real-time analytics, debt tracking, and system summaries.
- **Tariff System**: Flexible pricing for both electricity and gas utilities.

---

## Technology Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 21 |
| **UI Framework** | JavaFX 21 |
| **Build Tool** | Apache Maven |
| **Persistence** | Binary Serialization (.dat files) |
| **Styling** | Custom CSS (Modern Aesthetic) |
| **Logging** | Custom AppLogger |

---

## Prerequisites

Before you begin, ensure you have the following installed:
- [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Apache Maven 3.8+](https://maven.apache.org/download.cgi)
- An IDE (IntelliJ IDEA, VS Code, or Eclipse)

---

## Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd UtilityBillCalculatorAssignment
   ```

2. **Install Dependencies**
   ```bash
   ./mvnw clean install
   ```

---

## Running the Application

### Option 1: Using Maven (Recommended)
Launch the application directly using the JavaFX Maven plugin:
```bash
./mvnw javafx:run
```

### Option 2: Using the Shadow Launcher
If you are running in an environment where JavaFX modules are not automatically detected:
```bash
./mvnw clean compile
./mvnw exec:java -Dexec.mainClass="com.utilitybill.Launcher"
```

---

## Default Credentials

Upon first launch, the system initializes a default administrator account:

| Field | Value |
| :--- | :--- |
| **Username** | `admin` |
| **Password** | `Admin123` |

---

## Project Structure

```text
src/main/java/com/utilitybill/
├── controller/         # FXML UI Controllers
├── dao/                # Data Access Objects (Binary Persistence)
├── exception/          # Custom Application Exceptions
├── model/              # Domain Objects (Customer, Meter, Invoice, etc.)
├── service/            # Business Logic Layer (Singletons)
├── util/               # Helper classes (Validation, Security, Logging)
└── Main.java           # Entry Point
```

---

## Data Persistence

This application uses a high-performance **Binary Serialization** layer for data storage. All state is persisted in the `data/` directory:

- `customers.dat`: Customer profiles and addresses.
- `tariffs.dat`: Active and historic utility rates.
- `invoices.dat`: Billing records and itemized charges.
- `payments.dat`: Transaction history.
- `users.dat`: System users and encrypted credentials.

---

## Academic: OOP Principles

This project serves as a showcase for core Object-Oriented principles:

- **Inheritance**: Abstract `Tariff` base class with `ElectricityTariff` and `GasTariff` specializations.
- **Polymorphism**: Dynamic calculation of bills based on utility type using method overriding.
- **Encapsulation**: Strict use of private fields, public getters/setters, and service-layer abstraction.
- **Abstraction**: Interface-driven design (e.g., `DataPersistence` interface) to decouple logic from storage.

---

## Testing

The system includes a comprehensive suite of JUnit 5 tests covering business logic and validation.

**Run All Tests:**
```bash
./mvnw test
```

---

## Author
Inioluwa Alake

## Github
https://github.com/IniBuilds-git/Utility-Bill-Calculator