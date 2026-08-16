# 🔎 INDEXA

<p align="center">
  <img src="src/main/resources/icon.png" width="90" alt="INDEXA Logo">
</p>

<h1 align="center">INDEXA</h1>

<p align="center">
  <strong>A Java-Based Mini Search Engine</strong><br>
  <sub>Built with Data Structures & Algorithms • JavaFX • SQLite</sub>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-6D4AFF?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/JavaFX-UI-8B5CF6?style=for-the-badge">
  <img src="https://img.shields.io/badge/SQLite-Database-7C3AED?style=for-the-badge&logo=sqlite&logoColor=white">
  <img src="https://img.shields.io/badge/Maven-Build-9333EA?style=for-the-badge&logo=apachemaven&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit%205-Testing-A855F7?style=for-the-badge&logo=junit5&logoColor=white">
</p>

---

## ✦ About

**INDEXA** is a desktop mini search engine developed as a **Data Structures & Algorithms project**.

It indexes and searches local `.txt` documents using **hand-built data structures and algorithms** instead of SQL `LIKE` queries or external search libraries.

The search index runs entirely in memory using a **Trie, Inverted Index, and Max Heap**, while SQLite handles application data and user-related features.

---

## ✨ Features

* 🔍 Keyword, multi-keyword (OR), and exact-phrase search
* ⚡ Trie-powered live autocomplete
* 📊 Relevance-based result ranking
* ⏱️ Real search-time measurement
* ↕️ Sort by relevance, title, or date
* 📄 Add `.txt` documents at runtime
* 👤 Login and registration
* 🕘 Search history
* 🔖 Bookmarks with duplicate prevention
* 📈 User dashboard with statistics
* 🌙 Dark interface with elegant purple accents

---

## 🧠 DSA at the Core

| Structure / Algorithm | Purpose                    | Complexity       |
| --------------------- | -------------------------- | ---------------- |
| **HashMap**           | Keyword → document IDs     | Avg. O(1)        |
| **HashSet**           | Deduplication & stop words | Avg. O(1)        |
| **Trie**              | Autocomplete               | O(L)             |
| **Inverted Index**    | Document searching         | Avg. O(1) lookup |
| **Max Heap**          | Result ranking             | O(log n)         |
| **Linear Search**     | Sequential searching       | O(n)             |
| **Binary Search**     | Sorted searching           | O(log n)         |
| **Bubble Sort**       | Title sorting              | O(n²)            |
| **Selection Sort**    | Date sorting               | O(n²)            |
| **Insertion Sort**    | ID sorting                 | O(n²) worst      |

`L` = length of the word.

All core DSA implementations are **built from scratch in Java**.

---

## 🏗️ Architecture

```text
com.indexa
├── controller/    → JavaFX screens
├── model/         → Data classes
├── dsa/           → Data structures & algorithms
├── service/       → Search & business logic
├── dao/           → SQLite persistence
├── database/      → Database setup
└── util/           → Utility classes
```

### Search Flow

```text
User Query
    ↓
TextProcessor
    ↓
Inverted Index
    ↓
RankingService
    ↓
Max Heap
    ↓
Ranked Results
    ↓
JavaFX UI
```

The **Trie and Inverted Index are rebuilt in memory** when the application starts. SQLite stores documents, users, search history, and bookmarks.

---

## 🛠️ Tech Stack

| Technology   | Usage                         |
| ------------ | ----------------------------- |
| **Java 17+** | Core application              |
| **JavaFX**   | Desktop UI                    |
| **SQLite**   | Local database                |
| **JDBC**     | Database access               |
| **Maven**    | Build & dependency management |
| **JUnit 5**  | Unit testing                  |

No web technologies or external search engine are required.

---

## 🚀 Run INDEXA

### Requirements

* JDK 17 or newer
* Apache Maven
* Git

### 1. Clone the repository

```bash
git clone https://github.com/toobahashim5/INDEXA.git
```

### 2. Enter the project folder

```bash
cd INDEXA
```

### 3. Run the application

```bash
mvn clean javafx:run
```

Maven automatically downloads JavaFX and SQLite dependencies on the first run.

### 🧪 Run Tests

```bash
mvn test
```

The test suite covers the core DSA implementations, search engine, ranking system, text processing, and database operations.

---

## 📁 Project Structure

```text
INDEXA/
├── README.md
├── pom.xml
├── sample-documents/
└── src/
    ├── main/
    │   ├── java/com/indexa/
    │   └── resources/
    └── test/
```

---

## 🔮 Future Improvements

* PDF and DOCX support
* Drag-and-drop document upload
* Document management interface

---

<p align="center">
  <strong>INDEXA</strong><br>
  <sub>Java • JavaFX • SQLite • Data Structures & Algorithms</sub>
</p>

<p align="center">
  Built as a practical DSA portfolio project.
  <br><br>
  <strong>Author</strong><br>
  <a href="https://github.com/toobahashim5">Tooba Hashim</a>
</p>
