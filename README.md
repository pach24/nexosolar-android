<h1>
  <img src="https://github.com/user-attachments/assets/ae74958e-7ebf-45d1-9a23-e3143dacd432" alt="NexoSolar" width="30" style="vertical-align:middle;"/> 
  NexoSolar
</h1>

A native Android application focused on displaying and filtering invoice data, created to showcase clean architecture, MVVM-based presentation logic, and good development practices rather than complex business functionality.

> **Last Updated:** March 2026 



**Clean Architecture • MVVM • Offline-First**


![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![SOLID](https://img.shields.io/badge/SOLID-4A90E2?style=for-the-badge)
![MVVM](https://img.shields.io/badge/MVVM-FF6F00?style=for-the-badge&logo=android&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Clean_Architecture-4A90E2?style=for-the-badge)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)
![Room](https://img.shields.io/badge/Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)




---

## Table of Contents

- 📄 [Overview](#overview)
- 🖼️ [Visual Showcase](#visual-showcase)
- 💡 [Features](#features)
- 🏛️ [Architecture](#architecture)
- 🧩 [Modularization Strategy](#modularization-strategy)
- 📲 [Download / Release](#download--release)
- 💾 [Installation](#installation)
- 🚀 [Usage](#usage)
- 🧪 [Testing Strategy](#testing-strategy)
- 📚 [Lessons Learned](#lessons-learned)
- ⚖️ [License](#license)

---
## Overview

Developed during a **Professional Traineeship at Viewnext**, this project demonstrates **enterprise-grade Android development** practices, bridging the gap between academic theory and **industry standards**.

The application showcases **production-ready architecture** through strict adherence to **SOLID principles** and **Clean Architecture (MVVM)**, resulting in:

✅ **High testability** – 100% coverage on business logic layer  
✅ **Low coupling** – Modular design with clear boundaries between layers  
✅ **Easy extensibility** – New features integrate without modifying existing code  
✅ **Maintainability** – Single Responsibility and Dependency Inversion throughout

Key technical highlights include:
- A robust **Offline-First** strategy using **Room** as the **Single Source of Truth (SSOT)**
-  Modern **Reactive UI**: utilizing Kotlin **StateFlow** & **Coroutines** for a fully Unidirectional Data Flow (UDF), strictly adhering to Google's latest architectural recommendations.
- A flexible networking layer supporting both **Real API (Retrofit)** and **Mocking strategies** for isolated development
- Commitment to **Code Quality** with comprehensive **Unit Testing** (JUnit/Mockito)

​

---

## Visual Showcase

<img width="1000" height="967" alt="Image" src="https://github.com/user-attachments/assets/12e0d3f9-a96b-4efa-9e22-1126daffb42b" />

### Live Demo


<table>
  <tr>
    <td align="center" width="25%" valign="top">
      <img src="https://github.com/user-attachments/assets/497b91f8-24c3-4103-94e2-4c200a2b422a" width="100%" alt="Invoice Navigation">
      <br>
      <p><em>Quick overview of the invoice navigation and filtering system.</em></p>
    </td>
    <td align="center" width="25%" valign="top">
      <img src="https://github.com/user-attachments/assets/cd00de11-d74f-4db6-a49b-1a61eec1ab1e" width="100%" alt="Smart Solar Dashboard">
      <br>
      <p><em>Overview of Smart Solar Dashboard flow.</em></p>
    </td>
    <td align="center" width="25%" valign="top">
      <img src="https://github.com/user-attachments/assets/854a1b33-58a7-453b-9605-24acbc3e3147" width="100%" alt="Circular Mock">
      <br>
      <p><em>Custom Annotation Mocking & Decoupled Data Source.</em></p>
    </td>
    <td align="center" width="25%" valign="top">
      <img src="https://github.com/user-attachments/assets/f3dd4e41-7dad-464a-968c-5622167cf313" width="100%" alt="Error Handling">
      <br>
      <p><em>Resilient Offline-First UI & Robust error management.</em></p>
    </td> <!-- Recuerda cerrar este td -->
  </tr>
</table>



<details>
  <summary><strong>📸 View All Required Screens (Click to expand)</strong></summary>
  
  <br>

  
  
  | **Main Dashboard** | **Invoice List** | **Smart Filters** | **Invoice Details** |
  |:---:|:---:|:---:|:---:|
  | <img src="https://github.com/user-attachments/assets/8e03da9b-a1fe-4e1e-bc53-65e68532e992" width="200"> | <img src="https://github.com/user-attachments/assets/6301795f-c0a4-421f-bd83-3a3bfe34a4d9" width="200"> | <img src="https://github.com/user-attachments/assets/74a509cb-e8dc-44f2-84ba-0fbfc6add234" width="200"> | <img width="200"  alt="Image" src="https://github.com/user-attachments/assets/5749e226-d7b7-4a0d-8859-f527d5bbd4fe" />|

  
  | **Smart Solar Main** | **Installation Tab** | **Details Tab** | **Info Popup** |
  |:---:|:---:|:---:|:---:|
  | <img src="https://github.com/user-attachments/assets/69e20763-9377-4666-9f56-8105d37e585f" width="200"> | <img src="https://github.com/user-attachments/assets/6ea24b2b-460e-4508-b2b1-5a7dfbc97a9e" width="200"> | <img src="https://github.com/user-attachments/assets/f87b6b65-ce7e-4ddc-9586-0676b3e38dde" width="200"> | <img src="https://github.com/user-attachments/assets/0ddaec94-cc39-4ff2-9abb-57fdc098dc86" width="200"> |

 
  | **Skeleton Loading** | **Empty State** | **Offline Error** | **Date Picker** |
  |:---:|:---:|:---:|:---:|
  | <img src="https://github.com/user-attachments/assets/6f0705c2-5663-493d-a100-7484931c3569" width="200"> | <img src="https://github.com/user-attachments/assets/0a6f53c7-373d-429a-8651-e8c833ac6d54" width="200"> | <img src="https://github.com/user-attachments/assets/3228e120-0c57-4cc0-855e-840d70783010" width="200"> | <img width="200" src="https://github.com/user-attachments/assets/5d9c5fa1-0647-4773-9f9e-9ec2c0623c24" /> |

</details>



---


## Features

- **Offline-First Architecture:** Uses **Room Database** as the single source of truth.
- **Modern UI & Theming:**
  - **Full Dark Mode Support:** Seamlessly adapts to system preferences using Jetpack Compose Material 3 theming.
  - **Smart Loading:** Skeleton shimmer animation during data fetching.
- **Invoice Management:**
  - Robust Filtering: Status, date range, and amount.
  - Visual status indicators.
- **Installation Management:** 
  -  **Installation Details:** View technical specs of your solar setup.
  -  **Energy Monitoring:** Track self-consumption and energy generation.
- **Dual Data Source:** Toggle between **Real API (Retrofit)** and **Mock Data (Retromock)** instantly via a UI switch.
- **Quality Assurance:** Unit Tests for Domain and ViewModels.


---

## Architecture

The project follows a strict **Clean Architecture** approach combined with **MVVM (Model-View-ViewModel)**. This ensures a unidirectional data flow, adherence to **SOLID principles**, and high testability by decoupling the business logic from the Android framework.

### 📱 Presentation Layer (UI + ViewModel)
- **State Management:** Uses **`StateFlow`** (modern Kotlin Flow API) with **Unidirectional Data Flow (UDF)** pattern, following Google's latest recommendations.
- **Reactive UI:** Strict lifecycle-aware observation using Compose's `collectAsStateWithLifecycle()` to optimize background resource usage and prevent memory leaks.
- **Type-Safe States:** Sealed interfaces (`InvoiceUIState`) ensure compile-time safety and exhaustive `when` statements.

### 🧠 Domain Layer (Business Logic)
- **Pure Kotlin Module:** Completely isolated from the Android SDK.
- **Use Cases:** Encapsulate specific business rules (e.g., `GetInvoicesUseCase`, `FilterInvoicesUseCase`). They orchestrate the flow between the Repository and the Presentation layer.
- **Testability:** Being pure Java, this layer is tested continuously with fast-running JUnit tests.

### 💾 Data Layer (Repository + Sources)
- **Repository Pattern:** Acts as a mediator that abstracts the origin of the data.
- **Single Source of Truth (SSOT):** The app prioritizes **Room Database** for data retrieval, ensuring total offline capabilities.
- **Dual Network Strategy:** 
  - **Retrofit:** For production-grade HTTP requests.
  - **Retromock:** For simulating backend responses during development or demos.

### 🗺️ Architecture Diagram - Invoice Module

<p align="center">
 <img width="4576" height="5108" alt="Image" src="https://github.com/user-attachments/assets/89acd606-50be-455b-9fec-e02eaa874295" />
</p>

> ⚠️ **Scope Note:** This diagram represents the complete Clean Architecture implementation for the **Invoice Module**, which is the core feature requiring business logic, offline persistence, and comprehensive testing. The Smart Solar module uses a simplified implementation with static data visualization, following the principle of avoiding over-engineering for straightforward requirements.


---

##  Modularization Strategy

The application is strictly modularized to enforce separation of concerns, scalability, and faster build times:

- **`app`**: The **Presentation Layer**. Contains Activities, Fragments, ViewModels, and Dependency Injection setup. It depends on all other modules.
- **`domain`**: The **Business Logic Layer**. A pure Java module containing Use Cases, Domain Models, and Repository Interfaces. It has **zero Android dependencies**.
- **`data`**: The **Data Layer**. Responsible for the Repository implementation and local persistence (Room Database). It orchestrates data fetching strategies.
- **`data-retrofit`**: The **Network Layer**. Dedicated module for API communication, containing Retrofit service definitions, DTOs, and Retromock client implementation.
- **`core`**: **Shared Utilities**. Contains common extensions, helper classes, and constants used across the entire application.



---

## Download / Release

You can download the latest APK of **NexoSolar** directly from the releases section:

- **Version:** v2.0.0 – The Compose & Material 3 Era 
- **Download:** [NexoSolar v2.0.0 APK](https://github.com/pach24/nexosolar-android/releases/tag/2.0.0)

> ⚠️ Make sure to allow installation from **unknown sources** on your Android device before installing.

---

## Installation

### Prerequisites

- Android Studio (Giraffe or newer recommended).  
- JDK 8 or higher (managed by Android Studio).  
- Android device or emulator with a recent Android version.  

### Steps

1. **Clone the repository:**

```
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

2. **Open the project in Android Studio:**

- Choose “Open an Existing Project”.  
- Select the project folder.  

3. **Sync Gradle and build:**

- Android Studio will automatically download dependencies.  
- Wait until the sync and initial build finishes.  

4. **Run the app:**

- Select a device or emulator.  
- Click **Run** ▶️ in Android Studio.  



---

## Usage

### Basic Flow

1. **Launch the app** to open the main dashboard (`DashboardScreen` hosted in a Single-Activity).
2. **Select Data Source**:
   - Use the toggle switch to choose between **Retrofit (Real API)** and **Retromock (Mock Data)**.
   - This preference controls the data source for the entire session.
3. **Enter Smart Solar Dashboard**:
   - Tap the enter button to navigate to the `SmartSolarScreen`.
   - Here you can access the new monitoring features:
     - **Installation:** View technical details and status of your solar setup.
     - **Energy:** Monitor real-time self-consumption and energy generation.
4. **View Invoices**:
   - Navigate to the **Invoices** section (`InvoiceListScreen`).
   - The app loads bills through `InvoiceViewModel` → `GetInvoicesUseCase` → `InvoiceRepository` (using Room for offline cache).

### Filtering Invoices

1. In the invoice list (`InvoiceListScreen`), open the filter panel from the top app bar.
2. In the `FilterBottomSheet` (or `FilterScreen`), configure your criteria:
   - **Status:** Select checkboxes (Paid, Pending, Cancelled, Fixed Fee, Payment Plan...).
   - **Date Range:** Use the Compose `DateRangePicker` to set a period.
   - **Amount Range:** Adjust the Compose `RangeSlider` to set minimum and maximum amounts.
3. Tap **Apply**:
   - The UI delegates the action to the `InvoiceViewModel` (Intent/Event), which updates the `FilterState`.
   - The ViewModel's `StateFlow` emits the newly filtered data, triggering an automatic recomposition of the list. No `Bundles` or manual UI updates required.
4. **Reset:** Tap the "Reset" button to clear all filters from the ViewModel state and seamlessly restore the full list.


---

## Testing Strategy

Project with **complete unit test suite** (100% business logic coverage) using **JUnit 5 + Mockito**:

**Implemented Tests:**
| Class | Coverage | Purpose |
|-------|----------|---------|
| `DateValidatorTest` | Ranges, bounds, nulls/edges | Date filtering validation |
| `ErrorClassifierTest` | HTTP codes, throwables (SocketTimeout, UnknownHost) | Resilient error handling |
| `FilterInvoicesUseCaseTest` | Status/date/amount multiples, empty lists | Complex filtering logic |
| `GetInvoicesUseCaseTest` | Success/error/refresh callbacks | Repository + cache interaction |
| `GetInstallationDetailsUseCaseTest` | Repository delegation | SmartSolar module |



---

### Declarative UI: Jetpack Compose Integration

The presentation layer is built **100% with Jetpack Compose**, adopting a Single-Activity architecture and modern state management principles:

- **Unidirectional Data Flow (UDF):** UI components never mutate state directly. They hoist events to the `@HiltViewModel`, which exposes a read-only `StateFlow`.
- **Lifecycle-Aware Collection:** States are safely collected using `collectAsStateWithLifecycle()` to prevent resource leaks when the app is in the background.
- **Sealed Interfaces for UI State:** Exhaustive state mapping guarantees that the UI always renders exactly what the ViewModel dictates, eliminating impossible states (e.g., showing both a list and an error at the same time).

### Code Quality
- **Integration Tests:** Add Espresso UI tests for critical user flows
- **CI/CD Pipeline:** Automated testing and deployment with GitHub Actions
- **Code Coverage:** Expand test coverage to UI and Data layers


---

### Tech Stack Highlights
*   **Concurrency:** Kotlin Coroutines + **StateFlow** (hot stream) + **Flow** (cold stream) with lifecycle-aware collection (`repeatOnLifecycle`).
*   **UI Framework:** Jetpack Compose with **Material 3** design system (Dynamic theming & Dark Mode).  
*   **Dependency Injection:** Hilt (compile-time DI with KSP).
*   **Architecture:** Clean Architecture + MVVM + Repository Pattern.
*   **Network:** Retrofit + OkHttp + Retromock (Custom Interceptor strategy).
*   **Persistence:** Room (Offline-First Single Source of Truth).
*   **Testing:** JUnit 5, Mockito.



---

##  Production-Ready Patterns

This project demonstrates **real-world Android development** practices used in top tech companies:

### Modern State Management
-  **StateFlow over LiveData**: Industry standard since 2021
-  **Sealed Classes for States**: Eliminates impossible states (no "loading + error" simultaneously)
-  **Flow Operators**: Debouncing, error handling, and backpressure management

### Enterprise Architecture
-  **Compile-Time Safety**: Pure Kotlin domain layer (testable without Android)
-  **Scalable DI**: Hilt modules follow single-responsibility principle
-  **Offline-First**: Room as SSOT ensures zero-network-dependency UX

### Code Quality Signals
-  **100% Test Coverage**: Business logic is battle-tested
-  **Immutable Models**: Thread-safe by design
-  **Flow-Based Reactivity**: Replaces callback hell with declarative streams


## Lessons Learned

- **Single Source of Truth:** Implementing **Room** as the cache layer taught me that the UI should never observe the network directly. By observing the database, the app remains responsive and fully functional offline.
- **Modularization Discipline:** keeping the `domain` module as a pure Java library (no Android dependencies) forced me to write cleaner, decoupled code that is strictly focused on business rules.
- **Abstraction Power:** Designing the toggle between **Retrofit and Retromock** demonstrated the value of coding against interfaces. It allows the app to be testable and demos to run without relying on a stable internet connection or backend.
- **Unit Testing:** Writing tests for the `InvoiceViewModel` helped verify that the complex filtering logic (dates, amounts, status) works correctly without needing to run the app on a device constantly.

## License

This project is currently for educational and portfolio purposes.  











