# 🏥 CarePoint — Ultimate Hospital Command

**A real-time, multithreaded Hospital and ICU Management System built with Java Swing and MySQL.**

CarePoint is a comprehensive desktop application designed to solve the real-world problem of hospital resource management. It provides a single-pane-of-glass view of critical hospital operations, allowing administrators and medical staff to track patient flow, manage ICU bed allocation, monitor pharmacy inventory, and automate patient billing in real-time.

Unlike traditional systems that require manual refreshing, CarePoint features a **multithreaded background synchronization engine** that automatically updates the entire UI every 3 seconds. This ensures that data is never stale and operations run smoothly without ever freezing the interface.

> **Disclaimer:** CarePoint is an educational and administrative software project, not a certified medical device or official hospital dispatch system. Its outputs do not guarantee real-world bed availability or medical decisions. Do not rely on this project for actual clinical triage.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Installation & Configuration](#installation--configuration)
- [Running the Application](#running-the-application)
- [Multithreading & Concurrency](#multithreading--concurrency)
- [The Smart Allocation Algorithm](#the-smart-allocation-algorithm)
- [Data and Persistence](#data-and-persistence)
- [Limitations](#limitations)
- [Future Improvements](#future-improvements)
- [Author](#author)

## Project Overview

During a hospital crisis or peak admission hours, staff need clear, real-time data on bed availability, patient severity, and staff allocation. CarePoint explores how desktop applications and multithreading can assist with these administrative tasks.

The backend is built entirely on **Java (Swing/AWT)** with **MySQL** for relational data storage. The project is highly modularized—separated into UI panels, a centralized theme engine, and background threads to ensure zero UI freezing.

## Features

- **Live Command Dashboard:** Real-time snapshot of total patients, active admissions, high/critical cases, available beds, and the waiting list. Includes a dynamic ICU occupancy progress bar and a live background daemon clock.
- **Smart Triage & Bed Allocation:** Automatically assigns freed ICU beds to the highest-priority waiting patient (CRITICAL > HIGH > MEDIUM > LOW) without human intervention.
- **Visual Bed Map:** A dynamic, color-coded ICU grid. Beds glow **Red** for critical patients, **Orange** for high-risk, and **Mint-Green** when available.
- **Patient Directory:** Full CRUD operations for patients, including symptoms, age, gender, and emergency severity.
- **Automated Billing:** Calculates ICU stay duration and generates a bill upon discharge.
- **Doctor Management:** Directory to manage staff specialties and toggle their ON_DUTY/OFF_DUTY status.
- **Pharmacy Inventory:** Real-time medicine stock tracking with automated "LOW STOCK" warnings.
- **Database Manager:** An administrative tool to delete specific patient histories or perform a full "Wipe Database" reset without touching SQL.
- **Theme Engine:** A centralized `Theme.java` file allows switching between Dark Mode, Light Mode, and Custom Palettes instantly.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 8+ | Core programming language |
| Java Swing/AWT | Desktop GUI framework |
| MySQL Server | Relational database |
| JDBC | Database connectivity |
| `ExecutorService` | Background thread pool for database operations |
| `javax.swing.Timer` | 3-second auto-refresh sync engine |
| MySQL Connector/J | JDBC Driver for MySQL |

## System Architecture

CarePoint follows a strict **Modular Architecture (Separation of Concerns)**. The UI, multithreading layer, and database access are completely decoupled.

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
    DBManager --> |SQL Queries| Database[(MySQL Database <br/> hospital_icu_management)]
    class DBManager,Database db;
