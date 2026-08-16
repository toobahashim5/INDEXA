# 🔎 INDEXA

<p align="center">
  <strong>A Java-Based Mini Search Engine</strong><br>
  <sub>Practical Data Structures & Algorithms • JavaFX • SQLite</sub>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-6D4AFF?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/JavaFX-Desktop-8B5CF6?style=for-the-badge">
  <img src="https://img.shields.io/badge/SQLite-Database-7C3AED?style=for-the-badge&logo=sqlite&logoColor=white">
  <img src="https://img.shields.io/badge/Maven-Build-9333EA?style=for-the-badge&logo=apachemaven&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit%205-Testing-A855F7?style=for-the-badge&logo=junit5&logoColor=white">
</p>

---

## ✦ About

**INDEXA** is a Java desktop mini search engine built as a university **Data Structures & Algorithms project**.

It indexes and searches a local library of `.txt` documents and ranks results using **hand-built data structures and algorithms** — without SQL `LIKE` queries or external search libraries.

The core search engine is implemented entirely in **Java**, with the searchable index maintained in memory.

---

## ✨ Features

* 🔍 Keyword, multi-keyword (OR), and exact-phrase search
* ⚡ Trie-powered live autocomplete
* 📊 Relevance-based result ranking
* ⏱️ Real search-time measurement using `System.nanoTime()`
* ↕️ Sort by Relevance, Title, or Date
* 📄 Runtime `.txt` document import
* 👤 Optional login and registration
* 🕘 Search history with Search Again, Delete, and Clear
* 🔖 Bookmarks with database-level duplicate prevention
* 📈 User dashboard with live statistics
* 🌙 Dark JavaFX interface with purple accents
* 🚫 No HTML, CSS, JavaScript, cloud database, or external search engine

---

## 🧠 DSA Implementation

| Data Structure     | Implementation                             | Purpose                          |
| ------------------ | ------------------------------------------ | -------------------------------- |
| **HashMap**        | `InvertedIndex.java`                       | Keyword → document IDs           |
| **HashSet**        | `InvertedIndex.java`, `TextProcessor.java` | Deduplication & stop-word lookup |
| **Trie**           | `Trie.java`, `TrieNode.java`               | Prefix-based autocomplete        |
| **Max Heap**       | `MaxHeap.java`                             | Relevance-based result ordering  |
| **Inverted Index** | `InvertedIndex.java`                       | Core document search             |
| **ArrayList**      | Throughout                                 | Token and result storage         |

### Algorithms

| Algorithm      | File                 | Complexity               |
| -------------- | -------------------- | ------------------------ |
| Linear Search  | `LinearSearch.java`  | O(n)                     |
| Binary Search  | `BinarySearch.java`  | O(log n)                 |
| Bubble Sort    | `BubbleSort.java`    | O(n²)                    |
| Selection Sort | `SelectionSort.java` | O(n²)                    |
| Insertion Sort | `InsertionSort.java` | O(n²) worst / ~O(n) best |

---

## 🏗️ Architecture

```text
com.indexa
├── Main.java
├── controller/     → JavaFX screens
├── model/          → Data classes
├── dsa/            → Hand-built data structures & algorithms
├── service/        → Search & business logic
├── dao/            → SQLite persistence
├── database/       → Connection & schema
└── util/           → File handling & password hashing
```

### Design Principle

The **search engine and persistence layer are intentionally separated**.

```text
Search Engine
     │
     ├── dsa/
     └── service/
          │
          ▼
      In-Memory Index
          │
          ├── Trie
          └── Inverted Index

Persistence
     │
     └── dao/
          │
          ▼
        SQLite
```

SQLite stores metadata such as documents, users, history, and bookmarks.

The **Trie and Inverted Index are rebuilt in memory on every application launch** from the `.txt` document collection.

---

## 🔎 How Search Works

```text
User enters a query
        ↓
TextProcessor
        ↓
Lowercase → Remove punctuation
→ Tokenize → Remove stop words
        ↓
SearchEngine
        ↓
Inverted Index lookup
        ↓
RankingService
        ↓
Calculate relevance score
        ↓
Max Heap
        ↓
Highest-relevance results
        ↓
JavaFX result cards
```

### Ranking

| Condition                     | Score |
| ----------------------------- | ----: |
| Keyword in title              |   +10 |
| Each body occurrence          |    +2 |
| Exact phrase match            |   +15 |
| Each distinct keyword matched |    +5 |

The displayed relevance percentage is calculated from the actual ranking process — it is **not random or hardcoded**.

---

## 📚 Indexing Process

1. Read `.txt` files from `sample-documents/`
2. Extract the document title
3. Process text using `TextProcessor`
4. Insert keywords into the **Trie**
5. Insert keywords into the **Inverted Index**
6. Store document metadata and content in SQLite

Re-indexing clears the in-memory search structures and `DOCUMENTS` table first, preventing duplicate document entries.

---

## 🗄️ Database

| Table            | Purpose                               |
| ---------------- | ------------------------------------- |
| `USERS`          | User accounts and authentication data |
| `DOCUMENTS`      | Document metadata and content         |
| `SEARCH_HISTORY` | Logged-in user searches               |
| `BOOKMARKS`      | Saved documents                       |

Passwords are stored as **SHA-256 hashes with per-user salts**, never as plain text.

SQLite is used only for persistence — the actual search index remains in memory.

---

## 🛠️ Technology Stack

| Layer     | Technology                 |
| --------- | -------------------------- |
| Language  | Java 17+                   |
| UI        | JavaFX                     |
| Database  | SQLite                     |
| DB Access | JDBC + `PreparedStatement` |
| Build     | Apache Maven               |
| Testing   | JUnit 5                    |

---

## ⚡ Complexity Reference

| Operation                | Complexity               |
| ------------------------ | ------------------------ |
| HashMap / HashSet lookup | Average O(1)             |
| Trie insert/search       | O(L)                     |
| Linear Search            | O(n)                     |
| Binary Search            | O(log n)                 |
| Max Heap insert/extract  | O(log n)                 |
| Bubble Sort              | O(n²)                    |
| Selection Sort           | O(n²)                    |
| Insertion Sort           | O(n²) worst / ~O(n) best |

`L` = length of the word.

---

## 🚀 Getting Started

### Requirements

* **JDK 17 or newer**
* **Apache Maven**
* No IDE, Docker, or database server required

JavaFX and the SQLite driver are downloaded automatically by Maven.

### Clone

```bash
git clone <your-repo-url>
cd INDEXA
```

### Run

```bash
mvn clean javafx:run
```

On the first run, Maven downloads the required dependencies.

### Run Tests

```bash
mvn test
```

The test suite covers:

* Text processing
* Trie
* Inverted Index
* Max Heap
* Linear & Binary Search
* Bubble, Selection & Insertion Sort
* Search Engine
* Ranking Service
* Core database operations

---

## 🔮 Future Improvements

* PDF and DOCX document support
* Drag-and-drop document upload
* Dedicated document management interface
* Editable and removable documents

---

<p align="center">
  Built as a step-by-step <strong>Data Structures & Algorithms portfolio project</strong>.
</p>

<p align="center">
  <sub>INDEXA • Java • JavaFX • SQLite • DSA</sub>
</p>
