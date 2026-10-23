<div align="center">

# 📝 CT Bot

**Automated answer checking for Belarusian centralized-testing practice exams**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Build](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white)
![Tests](https://img.shields.io/badge/tests-JUnit%205-25A162?logo=junit5&logoColor=white)
![Status](https://img.shields.io/badge/status-work%20in%20progress-yellow)
![License](https://img.shields.io/badge/license-source--available-blue)

</div>

A Java toolkit, with a Telegram bot on top, that checks students' answers on practice runs of the
Belarusian centralized exams (ЦТ / РТ), applies the official partial-credit rules, and converts
primary scores into official test scores.

It is built for a real private tutor with about 20 students, who currently checks these tests by hand.

---

## Why this project

Practice tests follow the format of the national exams, and so does their grading. A single task can be:

- a multi-select question where one mistake still earns partial credit,
- an ordering task,
- a letter-to-option matching task (`А1Б1В2Г3`),
- a numeric task that must be rounded to an exact digit,
- or a free-text answer with synonyms.

Getting partial credit wrong silently changes a student's score, so the core logic is isolated from I/O
and covered by unit tests.

## Status

| Area | State |
|------|-------|
| Answer-key and student-answer spreadsheet parsing | ✅ Done |
| Checking engine with five question types and partial credit | ✅ Done |
| Primary → official test score conversion (data-driven tables) | ✅ Done |
| Student registry (JSON file storage) | ✅ Done |
| Console tool: check one student against one variant | ✅ Done |
| Telegram bot skeleton (`/start`, `/help`, command routing) | ✅ Done |
| Bot commands: add students, upload variants, receive answer files | 🚧 In progress |
| Tutor summary and packaging as a one-click Windows app | 📋 Planned |

## How it works

```
 Tutor's answer key (.xlsx)                     Student's answers (.xlsx)
          │                                               │
          ▼                                               ▼
   ExcelTableReader                                ExcelTableReader
          │                                               │
          ▼                                               ▼
  VariantTableParser ──► VariantRow          StudentAnswerTableParser
          │                                               │
          ▼                                               │
    VariantFactory ──► Variant                            │
          │                                               │
          └──────────────► AnswerCheckingService ◄────────┘
                                   │
                                   ▼
                          List<QuestionResult>
                                   │
                  ┌────────────────┴────────────────┐
                  ▼                                 ▼
        ScoreConversionService               ReportFormatter
        (ScoreScale from JSON)             (Telegram message text)
```

The parser layer knows about spreadsheets and nothing else. The factory knows about question types and
nothing about spreadsheets. The checking service works only with domain objects.

## Question types

| Type (table label) | Answer format | Rule |
|--------------------|---------------|------|
| `выбор` (multiple choice) | `135` | One mistake (extra or missing option) is partial credit, two or more is zero |
| `последовательность` (sequence) | `1352` | Positional comparison; partial-credit rules are still being validated against the official criteria |
| `соответствие` (matching) | `А1Б1В2Г3` | One wrong letter is partial credit, two or more is zero; digits may repeat across letters |
| `число` (numeric) | `3.1` | Exact match after rounding, comma or dot accepted, no partial credit |
| `текст` (open text) | `term\|synonym` | Any listed synonym matches, optional case and whitespace insensitivity, no partial credit |

### Answer-key spreadsheet

One row per task. The `Тип` column is a dropdown, so the question type is never ambiguous
(`123` can be a choice, a sequence, or a number).

| № | Тип | Правильный ответ | Макс. балл | Частично | Доп. параметры |
|---|-----|------------------|-----------|----------|----------------|
| 1 | выбор | 135 | 2 | 1 | |
| 3 | соответствие | А1Б1В2Г3 | 2 | 1 | |
| 5 | текст | митохондрия\|mitochondrion | 1 | | регистр=нет;пробелы=игнор |

## Tech stack

Java 21 · Maven · Apache POI · Jackson · JUnit 5 ·
[TelegramBots](https://github.com/rubenlagus/TelegramBots) (long polling)

## Getting started

**Prerequisites:** JDK 21+ and Maven.

```bash
mvn clean package
```

### Check one student from the command line

```bash
java -jar target/ct-bot-1.0-SNAPSHOT.jar \
  examples/variant_key_filled_example.xlsx \
  examples/student_answers_filled_example.xlsx \
  "Test Student"
```

Adjust the jar name to your artifact. Expected output:

```
Вариант: variant_key_filled_example.xlsx
Ученик: Test Student

№1   частично   1 / 2
№2   верно      2 / 2
№3   частично   1 / 2
№4   верно      1 / 1
№5   верно      1 / 1

Первичный балл: 6 из 8
```


### Run the Telegram bot

1. Create a bot with [@BotFather](https://t.me/BotFather) and copy its token.
2. Provide the token through the `CT_BOT_TOKEN` environment variable, or put `bot.token=<token>` in
   `config/bot.properties` (this file is git-ignored).
3. Start `com.kansas.ctbot.BotApp` and send `/start` to your bot.

## Roadmap

- [x] Spreadsheet parsing and validation
- [x] Five question types with partial credit
- [x] Score conversion for biology 2026
- [x] Student registry
- [ ] Student onboarding through personal invite links
- [ ] `/newvariant`, `/setactive`, and answer-file upload handling
- [ ] Per-variant results and a tutor summary
- [ ] Unified, user-friendly error messages
- [ ] One-click Windows packaging with `jpackage`

## License

This project is **source-available, not open source**. You are welcome to read the code and to build and
run it locally to evaluate it, for example as part of a hiring process. Any other use, including
running it for real users, copying, modifying, or redistributing it, requires written permission.
See [LICENSE](LICENSE) for the full terms.

## Author

Kanstantsin Sasnouski · [GitHub](https://github.com/k0stus)