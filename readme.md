# 📊 CovidStat – Real-Time COVID Statistics & Trend Analysis Service

CovidStat is a scalable Spring Boot application that aggregates COVID-19 statistics from external APIs, applies trend evaluation logic, and serves optimized responses using a pluggable caching layer.

The system is designed with clean architecture principles, focusing on separation of concerns, extensibility, and production-grade reliability.

---

## 🚀 Features

- Fetches real-time COVID statistics from external data providers
- Intelligent cache-first data retrieval strategy
- Trend analysis engine for daily case movement and alerts
- Configurable data source providers
- Centralized exception handling
- Docker-ready deployment

---

## 🧱 Architecture Overview

CovidStat follows a layered, clean architecture approach:

Client -> Controller Layer -> Orchestrator (Use Case Coordination) -> Cache Layer | Remote Data Sources | Domain Logic |


### Key Principles

- Interfaces drive all core components
- Dependency Inversion for easy testing and extension
- Strategy Pattern for cache management and trend evaluation
- Infrastructure isolated from business logic

---


---

## 🔧 Tech Stack

- Java 17
- Spring Boot
- Redis (optional caching layer)
- Gradle
- Docker & Docker Compose
- OPEN API
- Jenkins

---

## ⚙️ Setup & Run

### Prerequisites

- Java 17+
- Gradle
- Docker

---

### ▶ Run Locally

```bash
./gradlew bootRun

```

### 🐳 Run with Docker
```
docker-compose up --build
```

### 🧠 Design Patterns Used

```Strategy Pattern – cache handling & trend evaluation

Dependency Injection – loose coupling

Layered Architecture – clean separation

Centralized Exception Handling

Strategy Design Pattern --> To extend contract for Cache, DataSource, ExceptionHandler, RemoteConnection & Data trend evaluation

Proxy Design Pattern --> To connect Orchestrator with the data source

Singleton Design Pattern --> To manage single instances of MappingUtility, URLTemplateRegistry
```

### Testing
```bash
./gradlew test
```

```Includes:

Unit tests for orchestrator flows

Mocked remote API calls

Cache behavior tests
```

### Diagrams
```
/docs
 ├── CaseStudy.drawio.png
 ├── CaseStudy_SD_OK.drawio.png
 ├── CaseStudy_Exception_1Miss.drawio.png
```