<div align="center">

# 🏥 CarePoint | Ultimate Hospital Command

**A modern, real-time, multithreaded Hospital & ICU Management System.**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Swing](https://img.shields.io/badge/Java_Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Architecture](https://img.shields.io/badge/Architecture-Modular-success?style=for-the-badge)]()

*A comprehensive desktop solution for managing ICU beds, patient triage, pharmacy inventory, and hospital staff — built from the ground up with clean, modular architecture and background multithreading.*

</div>

---

## 📖 Overview

**CarePoint** is an enterprise-grade desktop application designed to solve the real-world problem of hospital resource management. It provides a single-pane-of-glass view of critical hospital operations, allowing administrators and medical staff to track patient flow, manage ICU bed allocation, and monitor pharmacy inventory in real-time.

Unlike traditional systems that require manual refreshing, CarePoint features a **multithreaded background synchronization engine** that automatically updates the entire UI every 3 seconds. This ensures that data is never stale and operations run smoothly without ever freezing the interface.

---

## ✨ Key Features

### 🩺 Clinical Operations
- **Smart Triage & Bed Allocation:** Automatically assigns freed ICU beds to the highest-priority waiting patient (CRITICAL > HIGH > MEDIUM > LOW) without human intervention.
- **Visual Bed Map:** A dynamic, color-coded ICU grid. Beds glow **Red** for critical patients, **Orange** for high-risk, and **Mint-Green** when available.
- **Patient Directory:** Full CRUD operations for patients, including symptoms, age, gender, and emergency severity.
- **Automated Billing:** Calculates ICU stay duration and generates a bill upon discharge.

### 💊 Administration
- **Doctor Management:** Directory to manage staff specialties and toggle their ON_DUTY/OFF_DUTY status.
- **Pharmacy Inventory:** Real-time medicine stock tracking with automated "LOW STOCK" warnings.
- **Database Manager:** An administrative tool to delete specific patient histories or perform a full "Wipe Database" reset without touching SQL.

### ⚙️ Technical Highlights
- **100% Responsive UI:** Database queries are offloaded to background threads using `ExecutorService`. The UI will **never freeze**.
- **Live Sync Engine:** A `javax.swing.Timer` automatically refreshes all panels every 3 seconds.
- **Live Clock:** A dedicated daemon `Thread` updates the system clock every second.
- **Theme Engine:** A centralized `Theme.java` file allows switching between Dark Mode, Light Mode, and Custom Palettes instantly.

---

## 🏗️ System Architecture & Project Structure

This project follows a strict **Modular Architecture (Separation of Concerns)**. It is not a single massive file, but rather a collection of specialized components that communicate via background threads and JDBC.

### 📊 Architecture Diagram

```mermaid
graph TD
    %% Define Styles
    classDef ui fill:#2D6EF5,stroke:#1A4BB5,stroke-width:2px,color:#fff;
    classDef thread fill:#14B8A6,stroke:#0F766E,stroke-width:2px,color:#fff;
    classDef db fill:#F8FAFC,stroke:#CBD5E1,stroke-width:2px,color:#0F172A;
    classDef logic fill:#1D2739,stroke:#415065,stroke-width:2px,color:#F1F5F9;

    %% Main App Entry
    Main([Main.java <br/> Entry Point]) --> HMS[HospitalManagementSystem <br/> Main JFrame & Navigation]
    class Main,HMS ui;

    %% UI Layer
    HMS --> |Loads Panels| Dashboard[Dashboard Panel]
    HMS --> |Loads Panels| Patients[Patients Panel]
    HMS --> |Loads Panels| ICUBeds[ICU Beds Panel]
    HMS --> |Loads Panels| Pharmacy[Pharmacy Panel]
    HMS --> |Loads Panels| Doctors[Doctors Panel]
    class Dashboard,Patients,ICUBeds,Pharmacy,Doctors logic;

    %% Multithreading Layer
    Dashboard -.-> |Live Clock| ClockThread((Daemon Thread))
    HMS -.-> |3-Second Sync| Timer((javax.swing.Timer))
    Dashboard --> |Async Load| Executor((ExecutorService))
    Patients --> |Async Load| Executor
    ICUBeds --> |Async Load| Executor
    class ClockThread,Timer,Executor thread;

    %% Smart Allocation Logic
    ICUBeds --> |1. Check Free Beds| SmartLogic{Smart Auto-Allocation Logic}
    SmartLogic --> |2. Check Waiting List| PrioritySort[Sort by CRITICAL > HIGH > LOW]
    PrioritySort --> |3. Auto-Assign Bed| Database
    class SmartLogic,PrioritySort logic;

    %% Database Layer
    Executor --> |JDBC Connection| DBManager[DatabaseManager.java]
    Timer -.-> |Triggers Reloads| Executor
    
    DBManager --> |SQL Queries| Database[(MySQL Database <br/> hospital_icu_management)]
    class DBManager,Database db;
