# Resume Matcher Pro 🎯

A desktop application built with **Java Swing + SQLite** that analyzes how well a resume matches a job description using keyword extraction, scoring, and **Gemini AI-powered suggestions**.

---

## ✨ Features

- **Keyword Match Scoring** — Extracts meaningful keywords from both the resume and job description, calculates a percentage match score
- **Tech-Aware Extraction** — Handles technical symbols correctly: `C++`, `C#`, `.NET`, `Node.js`, `TypeScript`, hyphenated terms like `low-latency`
- **Abbreviation Matching** — `JS` matches `JavaScript`, `TS` matches `TypeScript`, `Mongo` matches `MongoDB`, and more
- **Color-Coded Score** — Green (≥75%), Orange (≥40%), Red (<40%)
- **Match History** — All results saved to a local SQLite database with a master-detail history view
- **Full Content View** — Click any history record to see the original resume and job description side by side
- **✨ AI Suggestions** — Powered by Gemini 2.5 Flash; provides a recruiter-style analysis with improvement tips, rendered as formatted HTML

---

## 🗂 Project Structure

```
Resume_Keyword_Match_Checker/
├── src/resumematcher/
│   ├── Main.java               # Entry point
│   ├── ResumeMatcherUI.java    # Main Swing UI (tabs, AI dialog, history)
│   ├── MatchService.java       # Keyword comparison + scoring logic
│   ├── KeywordExtractor.java   # 3-pass extractor with alias expansion
│   ├── StopWords.java          # Filtered words list (generic verbs/adjectives)
│   ├── MatchDAO.java           # SQLite read/write operations
│   ├── MatchResult.java        # Result model (score, matched, missing)
│   ├── DatabaseInitializer.java# Creates DB schema on first run
│   ├── DBConnection.java       # SQLite JDBC connection helper
│   └── GeminiService.java      # Gemini REST API client (zero extra deps)
├── lib/
│   ├── sqlite-jdbc-3.45.3.0.jar
│   ├── slf4j-api-1.7.36.jar
│   └── slf4j-simple-1.7.36.jar
├── bin/                        # Compiled output (git-ignored)
├── .env                        # Your API key (git-ignored)
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 11+** (built and tested on Java 17/21)
- No Maven or Gradle required

### 1. Clone the repo
```bash
git clone https://github.com/Bharat940/Resume_match_checker_with_ai_summarizer.git
cd Resume_Keyword_Match_Checker
```

### 2. Set up your Gemini API Key *(optional, for AI features)*
Create a `.env` file in the project root:
```
GEMINI_API_KEY=your_api_key_here
```
Get a free key at [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey).

### 3. Compile
```bash
javac -cp ".;lib/*" -d bin src/resumematcher/*.java
```

### 4. Run
```bash
java --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" resumematcher.Main
```

---

## 🧠 How It Works

1. **Paste** your resume and job description into the text areas
2. Click **Analyze Match** — keywords are extracted using a 3-pass algorithm:
   - Pass 1: Tech tokens (`C++`, `.NET`, `Node.js`)
   - Pass 2: Hyphenated compounds (`low-latency`, `full-stack`)
   - Pass 3: Regular words (filtered through stop words + conservative stemming)
3. View the **match score** and matched/missing keywords
4. Click **✨ AI Suggestions** for a Gemini-powered recruiter analysis (rendered as formatted HTML)
5. Click **Save Result** to store the result in the local SQLite database
6. Switch to the **Match History** tab to browse past analyses

---

## 📦 Dependencies

| Library | Version | Purpose |
|---|---|---|
| `sqlite-jdbc` | 3.45.3.0 | SQLite database access via JDBC |
| `slf4j-api` | 1.7.36 | Logging API (required by sqlite-jdbc) |
| `slf4j-simple` | 1.7.36 | Logging implementation |

> **No Maven required.** All jars are in `lib/`. The Gemini AI integration uses Java's built-in `java.net.http.HttpClient` — zero extra dependencies.
