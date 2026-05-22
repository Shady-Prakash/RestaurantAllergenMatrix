# Restaurant Allergy Matrix System

ICT711 Programming and Algorithms — Assessment 4 (Individual Java
Application). A menu allergen manager with **two interchangeable
interfaces** (Swing GUI and a text-based interface) that share one data
layer, hand-written sorting/searching algorithms, CSV persistence and a
JUnit 5 test suite.

## Requirements

- **Java JDK 17 or newer** (developed and tested on Temurin 21).
- **Apache Maven 3.8+**.
- **VS Code** with the *Extension Pack for Java*. The repo ships with
  `.vscode/launch.json` so the Run / Debug buttons work out of the box.

JUnit 5 and every other test dependency are pulled in automatically by
Maven — nothing else to install.

## Project layout

```
restaurant-allergy-matrix/
├── pom.xml                       Maven build file
├── data/
│   └── menu.csv                  seed data (auto-created on first run)
├── src/
│   ├── main/java/
│   │   ├── Main.java             entry point + GUI / TBI mode chooser
│   │   ├── algorithms/           SortAlgorithms, SearchAlgorithms
│   │   ├── cli/
│   │   │   └── TextInterface.java
│   │   ├── data/
│   │   │   └── MenuRepository.java   CRUD + CSV load/save
│   │   ├── model/
│   │   │   ├── Allergen.java
│   │   │   ├── Category.java
│   │   │   └── MenuItem.java
│   │   └── ui/
│   │       ├── MainFrame.java
│   │       ├── AddItemDialog.java
│   │       ├── MenuTableModel.java
│   │       └── Theme.java
│   └── test/java/
│       ├── MenuRepositoryTest.java
│       ├── SearchAlgorithmsTest.java
│       └── SortAlgorithmsTest.java
├── target/                       Maven build output (gitignored)
└── .gitignore
```

## Build

From the project root:

```sh
mvn compile          # compile sources into target/classes
mvn package          # build a runnable jar in target/
```

`mvn package` produces `target/restaurant-allergy-matrix-1.0.0.jar`.

## Run

### From VS Code (recommended)

1. Open the project folder in VS Code.
2. Install the recommended extensions when prompted (the
   *Extension Pack for Java*).
3. Wait for the status bar to show **Java: Ready**.
4. Click the **Run and Debug** icon (▶ in the left sidebar).
5. Pick one of the pre-defined launch configurations and press the
   green ▶:
   - **Run: Mode Chooser** — asks at startup whether to use GUI or text.
   - **Run: GUI** — launches the Swing window directly.
   - **Run: Text Interface** — uses VS Code's integrated terminal for
     interactive input.

You can also open `Main.java` and press **F5**, or click the *Run* code
lens that appears above the `main` method.

### From the command line

```sh
java -jar target/restaurant-allergy-matrix-1.0.0.jar          # asks mode
java -jar target/restaurant-allergy-matrix-1.0.0.jar --gui
java -jar target/restaurant-allergy-matrix-1.0.0.jar --text
```

On first run a sample menu of 15 dishes is created in `data/menu.csv`.

### Operations (identical in both interfaces)

Add, Edit, Delete, List, Sort (by name / price / category / allergen
count, using Quick Sort or Merge Sort), Search (substring), the
allergen-safety filter (*"what is safe for a guest allergic to X?"*),
Save and Reload. Every add / edit / delete is auto-persisted to
`data/menu.csv`.

## Test

### From VS Code (recommended)

1. Click the **Testing** beaker icon in the left sidebar.
2. The 14 JUnit tests appear under
   `MenuRepositoryTest`, `SearchAlgorithmsTest`, `SortAlgorithmsTest`.
3. Press ▶ next to any node:
   - the **root** runs the whole suite,
   - a **class** runs every test for one source file,
   - a **method** runs that single test.
4. Green ticks = pass; red crosses jump to the failing assertion.

Or open any `*Test.java` file and click the **Run Test / Debug Test**
code lens that appears above each `@Test` method.

### From the command line

```sh
mvn test                                          # run all 14 tests
mvn test -Dtest=SortAlgorithmsTest                # one class
mvn test -Dtest=SortAlgorithmsTest#mergeSortIsStableForEqualKeys
```

Expected: `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0`.