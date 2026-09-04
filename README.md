# Employee Performance Management System (EPMS)

A Java desktop application for managing employee performance records. It offers
**two interchangeable interfaces** over the same core logic:

* a **Graphical User Interface** (Java Swing), and
* a **Text-Based Interface** (numbered-menu console REPL).

Both interfaces support the full operation set — **add, edit, delete,
search / query, sort** — plus CSV file load/save. All sorting and searching is
done with **hand-written algorithms** (no `Collections.sort`, no
`Collections.binarySearch` in the application path) so their cost can be measured
and discussed (see [`DISCUSSION.md`](DISCUSSION.md)).

Target: **JDK 17+** (developed and tested on OpenJDK 21). No build tool required.

---

## Quick start

```bash
./run.sh            # builds, then asks: [G]UI or [T]ext
./run.sh --gui      # start straight in the Swing interface
./run.sh --text     # start straight in the text interface
./run.sh --text --data path/to/other.csv
```

On first run a sample roster is written to `data/employees.csv`; subsequent runs
load it automatically.

### Build / test manually

```bash
./build.sh          # compile sources    -> out/production
./test.sh           # compile + run the JUnit 5 suite (60 tests)
./bench.sh          # print the algorithm growth-rate table
```

`./test.sh` needs `lib/junit-platform-console-standalone.jar`. It is already
included; if it is ever missing the script prints the one-line `curl` command to
fetch it.

---

## Choosing an interface

`epms.App` decides the mode from (in order): a `--gui` / `--text` flag, otherwise
an interactive prompt. Both modes then run against a single shared
`EmployeeRepository`, so behaviour — validation rules, search results, sort
order, file format — is identical between them.

### GUI (Swing)

Native system look-and-feel. A **menu bar** (File / Edit / Data / Help) mirrors
the toolbar and adds keyboard shortcuts (Ctrl/⌘ + N, E, F, R, O, S, Q; `Delete`
removes the selected row; double-click a row to edit it).

| Control | Action |
|---|---|
| **Add / Edit / Delete** | CRUD on the selected row; Edit/Delete auto-disable when nothing is selected |
| **Quick find** | type-to-filter box above the table — instant substring match on any column; `Esc` or **Show All** clears it |
| **Search / Query** | dialog-driven: find by ID (linear **or** binary — reports the comparison count), query by name substring, by department, or by score range |
| **Sort by** | pick a field + one of the 5 algorithms + asc/desc, then **Sort**. The status bar reports comparisons, moves and elapsed time |
| **Data ▸ Compare sort algorithms…** | runs all five sorts on the current data and tabulates comparisons / moves / time |
| **Load / Save / Save As** | `JFileChooser`, CSV filter, malformed-row warnings surfaced in a dialog |

Feedback: `JOptionPane` dialogs (info / warning / error / confirm), a
colour-coded **Rating** column (green → red across the five bands), formatted
number columns, and a two-part status bar (last action + live row count).
Closing the window prompts to save.

The `JTable` column headers still do their own client-side sort for quick
eyeballing; the **Sort** button is what exercises the project's algorithms and
reorders the underlying data.

### Text interface

```
 1) List / sort employees      4) Delete employee      7) Load from file
 2) Add employee                5) Search / query       0) Exit
 3) Edit employee               6) Save to file
```

Every prompt shows a default in `[brackets]` — press Enter to accept it. Invalid
numbers are re-prompted rather than crashing.

---

## Project layout

```
src/epms/
  App.java                     entry point + interface chooser + sample data
  model/
    Employee.java              record type; setters enforce all field invariants
    EmployeeField.java         sortable/queryable fields + their Comparators
  algorithms/
    SortAlgorithm.java         enum: the 5 sorts + their documented complexity
    Sorter.java                bubble, insertion, selection, quick, merge
    SortMetrics.java           comparison / move / time instrumentation
    SortResult.java            sorted list + metrics
    Searcher.java              linear + binary search
    SearchResult.java          index + comparison count
  data/
    EmployeeRepository.java    the one place add/delete/search/sort live
    FileStorage.java           CSV read/write (RFC-4180-ish quoting)
    DuplicateEmployeeException, EmployeeNotFoundException
  ui/
    text/TextInterface.java    the TBI
    gui/MainWindow.java        the Swing frame: menu bar, toolbar, shared Actions
    gui/EmployeeTableModel.java
    gui/EmployeeFormDialog.java modal add/edit form with validation
    gui/CellRenderers.java     rating-colour + number-format table renderers

test/epms/
  SorterTest.java              correctness + metric assertions for all 5 sorts
  SearcherTest.java            linear vs binary, edge cases
  EmployeeRepositoryTest.java  CRUD, search, sort, query behaviour
  FileStorageTest.java         CSV round-trip, quoting, malformed-row handling
  AlgorithmPerformanceTest.java empirical Big-O / growth-rate checks
```

---

## Data file format

CSV, one header line then one employee per row:

```
id,name,department,performanceScore,yearsOfService,salary
1,Alice Thapa,Engineering,88.5,6,92000
2,"Doe, Jane","R&D ""Labs""",70,3,61000.5
```

Fields containing a comma or quote are wrapped in double quotes with embedded
quotes doubled. On load, malformed rows are **skipped with a warning**, not
fatal; duplicate IDs within a file keep the first occurrence.

---

## Testing approach

* **Unit tests** for every algorithm and repository operation, including
  boundary cases (empty / singleton / already-sorted / reversed / all-duplicate
  inputs; not-found searches; blank and out-of-range field values).
* **Parameterised tests** (`@EnumSource(SortAlgorithm.class)`) run the same
  correctness contract against all five sorts.
* **Oracle testing** — each sort is checked against `Collections.sort` on random
  data.
* **Metric / white-box tests** — assert exact comparison counts where they are
  deterministic (selection sort = n(n−1)/2, bubble sort early-exit = n−1).
* **Performance tests** — `AlgorithmPerformanceTest` doubles the input size and
  asserts the comparison count grows ~4× for O(n²) sorts, ~2× + a bit for
  O(n log n), and +1 per doubling for binary search.
* **File I/O tests** use JUnit's `@TempDir` so nothing touches the real data
  file.

Run `./test.sh` — current status: **60/60 passing**.
