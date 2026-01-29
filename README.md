# Utility Bill Management System

A comprehensive desktop application for utility bill management built with JavaFX, demonstrating Object-Oriented Programming principles, design patterns, and file-based data persistence.

## Project Overview

This application is designed for managing utility (electricity and gas) billing operations, including:
- Customer management (CRUD operations)
- Meter reading recording and validation
- Invoice generation with itemized charges
- Payment processing and tracking
- Tariff management with flexible pricing structures
- Dashboard with analytics and reporting

##  Architecture

### Design Patterns Implemented

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Singleton** | Service classes (AuthenticationService, CustomerService, etc.) | Single instance for centralized state management |
| **Factory** | Meter class static factory methods | Creating different meter types (Electricity, Gas, Dual) |
| **Template Method** | Tariff.calculateBill() | Defining billing algorithm skeleton with customizable steps |
| **DAO Pattern** | CustomerDAO, InvoiceDAO, PaymentDAO, etc. | Abstracting data persistence operations |
| **MVC** | FXML Views + Controllers + Models | Separating UI, business logic, and data |

### Project Structure

```
src/main/java/com/utilitybill/
├── Main.java                    # Application entry point
├── controller/                  # FXML Controllers
│   ├── LoginController.java
│   ├── DashboardController.java
│   ├── CustomerController.java
│   └── CustomerDialogController.java
├── model/                       # Domain models
│   ├── User.java
│   ├── Customer.java
│   ├── Address.java
│   ├── Meter.java
│   ├── MeterType.java
│   ├── MeterReading.java
│   ├── Tariff.java (abstract)
│   ├── ElectricityTariff.java
│   ├── GasTariff.java
│   ├── Invoice.java
│   └── Payment.java
├── service/                     # Business logic (Singleton pattern)
│   ├── AuthenticationService.java
│   ├── CustomerService.java
│   ├── BillingService.java
│   ├── PaymentService.java
│   └── TariffService.java
├── dao/                         # Data Access Objects
│   ├── DataPersistence.java (interface)
│   ├── AbstractJsonDAO.java
│   ├── UserDAO.java
│   ├── CustomerDAO.java
│   ├── InvoiceDAO.java
│   ├── PaymentDAO.java
│   ├── TariffDAO.java
│   └── MeterReadingDAO.java
├── util/                        # Utility classes
│   ├── ValidationUtil.java
│   ├── DateUtil.java
│   ├── PasswordUtil.java
│   └── BillCalculator.java
└── exception/                   # Custom exceptions
    ├── UtilityBillException.java
    ├── InvalidCredentialsException.java
    ├── CustomerNotFoundException.java
    ├── InvalidMeterReadingException.java
    ├── DuplicateAccountException.java
    ├── InsufficientPaymentException.java
    ├── DataPersistenceException.java
    └── ValidationException.java

src/main/resources/com/utilitybill/
├── view/                        # FXML views
│   ├── login.fxml
│   ├── dashboard.fxml
│   ├── customer-management.fxml
│   └── customer-dialog.fxml
└── css/
    └── styles.css               # Application styling

src/test/java/com/utilitybill/
└── util/                        # JUnit 5 tests
    ├── ValidationUtilTest.java
    └── BillCalculatorTest.java
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **JavaFX 21** (included via Maven dependencies)

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run with Maven:

```bash
mvn clean javafx:run
```

Or compile and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.utilitybill.Launcher"
```

### Default Login Credentials

```
Username: admin
Password: Admin123
```

## 📦 Data Persistence

The application uses **JSON-based file storage** for all data persistence:

| File | Purpose |
|------|---------|
| `data/users.json` | User accounts and authentication |
| `data/customers.json` | Customer information and meters |
| `data/tariffs.json` | Electricity and gas tariff rates |
| `data/invoices.json` | Generated invoices |
| `data/payments.json` | Payment records |
| `data/meter_readings.json` | Meter reading history |

The data directory is automatically created on first run.

## 🧪 Testing

Run JUnit 5 tests:

```bash
mvn test
```

Tests cover:
- Input validation (email, phone, postcode, password)
- Bill calculations (unit costs, standing charges, VAT)
- Tariff pricing (flat-rate and tiered)

##   OOP Principles Demonstrated

### Encapsulation
- All model classes use private fields with public getters/setters
- Services expose only necessary methods
- Internal state protected from external modification

### Inheritance
- `Tariff` (abstract) → `ElectricityTariff`, `GasTariff`
- `UtilityBillException` → All custom exceptions
- `AbstractJsonDAO` → All DAO implementations

### Polymorphism
- Tariff calculations vary by subclass
- DAO operations work on generic types
- Exception handling with hierarchy

### Abstraction
- `DataPersistence<T, ID>` interface for all DAOs
- Abstract `Tariff` class with abstract methods
- Service interfaces hide implementation details

## UI Features

- Modern, clean design with teal/cyan theme
- Responsive layout with sidebar navigation
- Interactive dashboard with statistics cards
- Data tables with search and filtering
- Modal dialogs for data entry
- Form validation with error messages

## 📝 Javadoc

Generate documentation:

```bash
mvn javadoc:javadoc
```

Documentation will be generated in `target/site/apidocs/`

## 🔐 Security Features

- Password hashing with SHA-256 and salt
- Account lockout after 3 failed attempts
- Role-based access control (Admin, Operator, Viewer)
- Input validation and sanitization

## 📊 Billing Features

- Flat-rate and tiered pricing support
- Automatic VAT calculation (5% reduced rate)
- Standing charge per day
- Consumption tracking
- Invoice generation with line items

## 🛠️ Future Enhancements

- [ ] PDF invoice export
- [ ] Email notifications
- [ ] Smart meter integration
- [ ] Payment gateway integration
- [ ] Advanced analytics and charts
- [ ] Data export (CSV/Excel)

## 📄 License

This project is developed for educational purposes as part of an academic assignment.

## 👨‍💻 Author

Utility Bill Management System - Academic Project
