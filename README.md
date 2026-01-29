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

## 📏 Meter Reading Module

The Meter Reading module records customer energy consumption over a period and converts raw readings into billable energy usage. These readings are the foundation for the **Invoice Management** module.

### Meter Reading Input

| Input Field | Description |
| :--- | :--- |
| **Account Number** | Unique customer identifier |
| **Meter Type** | GAS or ELECTRICITY |
| **Opening Read** | Meter reading at the start of the period |
| **Closing Read** | Meter reading at the end of the period |
| **Opening Date** | Start date of billing period |
| **Closing Date** | End date of billing period |

#### Additional Inputs by Meter Type

**Gas Meter**
- **Units**: Calculated automatically (Closing − Opening)
- **Imperial to m³ Factor**: Fixed (2.83)
- **Correction Factor**: System-defined default (~1.02264)
- **Calorific Value**: System-defined default (~39.0 MJ/m³)

**Electricity Meter**
- **Day Opening/Closing Read**: Day meter values
- **Night Opening/Closing Read**: Night meter values

---

### Meter Reading Output

#### Gas Meter Output
| Output Field | Description |
| :--- | :--- |
| **Units** | Closing Read − Opening Read |
| **m³** | Units × 2.83 |
| **kWh** | (m³ × Correction Factor × Calorific Value) ÷ 3.6 |

**Example (Gas)**:
- Opening: 10091.5, Closing: 10127.6 → Units: 36.1
- m³ = 36.1 × 2.83 = 102.16
- kWh = (102.16 × 1.02264 × 39.0) ÷ 3.6 = 1143.43

#### Electricity Meter Output
| Output Field | Description |
| :--- | :--- |
| **Day/Night Units** | Respective Closing − Opening |
| **Total Units** | Day Units + Night Units |
| **kWh** | Same as total units |

---

### Validation Rules
- **Logical Check**: Closing read cannot be less than Opening read.
- **Completeness**: All required fields must be present.
- **Format**: Non-numeric input is rejected.
- **Temporality**: Invalid date ranges or future dates are flagged.

---

## 📊 Billing Features

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
