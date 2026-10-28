# Mini OS Simulator — README

A **console-based Mini Operating System Simulator** written in **plain Java**, built to run in
**Eclipse IDE**. It is aimed at a **second-year computer-science level**: detailed enough to show
the core OS concepts correctly, but written with ordinary loops, arrays and simple classes — no
frameworks, no advanced Java tricks, and **standard Java libraries only**.

> ⚠️ **Important:** this is an **educational demonstration of selected OS concepts**.
> It is **NOT a real operating system**. It touches no hardware, no real memory and no real
> processes — it only simulates the arithmetic and bookkeeping a real OS would perform.

---

## 1. Features

| # | Feature | Algorithms / content |
|---|---------|----------------------|
| 1 | **CPU scheduling** | FCFS, SJF (non-preemptive), Round Robin — waiting time, turnaround time, averages, comparison view |
| 2 | **Banker's algorithm** | Need matrix, safety check, safe sequence (or unsafe report) |
| 3 | **Paging** | Logical address → page number + offset → frame → physical address, page-fault & invalid-address reporting |
| 4 | **Segmentation** | (segment, offset) → base + offset, limit checks, clear segmentation-fault messages |
| 5 | **Fragmentation** | Internal (fixed partitions) and external (holes) demonstrations with memory maps |
| 6 | **Page replacement** | FIFO, LRU, Optimal — frame state after **every** reference, fault totals, comparison |
| 7 | **Disk scheduling** | FCFS, SSTF, SCAN, C-SCAN — service order and total head movement |
| 8 | **Multiprogramming** | Jobs sharing memory (first-fit) and one CPU (Round Robin) with I/O waits; job states: JOB QUEUE → READY → RUNNING → WAITING → TERMINATED |

Every feature has its own sub-menu, its own class, student-friendly comments, and a ready-made
sample input in `samples/`.

---

## 2. Requirements

* **JDK 8 or newer** (any modern JDK works — the code uses only basic language features).
* **Eclipse IDE for Java Developers** (any recent version; the project ships `.project` and
  `.classpath` so Eclipse can import it directly).
* No external libraries, no build tools — just `javac` + `java`.

---

## 3. Creating and running the project in Eclipse

### 3.1 Import the project (fastest way)

1. Start Eclipse and pick any workspace.
2. **File → Import… → General → Existing Projects into Workspace → Next.**
3. Press **Browse…** and select this folder (`Mini_OS-Project`) as the *Root Directory*.
4. The project `Mini_OS-Project` appears in *Projects* — keep it ticked and press **Finish.**
5. In the *Package Explorer*, expand `src` → `minios`.
6. Open **`Main.java`** and press **Ctrl+F11** (Run) — or the green ▶ button.
7. The program starts in the **Console** view at the bottom. Type numbers as prompted and press
   Enter, exactly as shown in the sample sessions below.

### 3.2 Create the project by hand (if you prefer starting empty)

1. **File → New → Java Project**, name it `Mini_OS-Project`, press **Finish**
   (accept defaults; a newer Eclipse may ask to create `module-info.java` — choose **No**).
2. Right-click the project → **New → Source Folder**, name it `src`
   (skip if Eclipse already made one).
3. Right-click `src` → **New → Package**, name it `minios`.
4. Create each class below inside that package: right-click `minios` → **New → Class**,
   type the *Name* exactly (e.g. `Main`) and press **Finish**.
5. Copy the content of each file from this repository into the matching class.
6. Run `Main.java` with **Ctrl+F11**.

### 3.3 Running from the command line (alternative)

Open a terminal **in the project folder**:

```bash
# Linux / macOS
javac -d bin src/minios/*.java
java -cp bin minios.Main
```

```bat
:: Windows (cmd)
javac -d bin src\minios\*.java
java -cp bin minios.Main
```

`bin/` is the compiled output folder (already listed in `.gitignore`, so it is never committed).

You can also replay a saved session (see §6):

```bash
java -cp bin minios.Main < samples/cpu_fcfs.txt
```

---

## 4. Project structure

```
Mini_OS-Project/
├── .project                  Eclipse project description (import straight into Eclipse)
├── .classpath                Eclipse classpath: sources in src/, classes in bin/
├── .gitignore                ignores bin/ and *.class
├── README.md                 this file
├── samples/                  ready-made inputs, one per feature
│   ├── cpu_fcfs.txt          cpu_sjf.txt  cpu_rr.txt  cpu_compare.txt
│   ├── cpu_invalid_input.txt demonstrates graceful handling of bad input
│   ├── banker_safe.txt       banker_unsafe.txt
│   ├── paging.txt            segmentation.txt
│   ├── fragment_internal.txt fragment_external.txt
│   ├── page_replacement.txt  disk.txt     multiprogramming.txt
│   └── expected/             exact expected output of every sample (for diff/testing)
└── src/
    └── minios/               ← every class is one file, named after the class
        ├── Main.java                      entry point + main menu
        ├── InputHelper.java              safe keyboard input (never crashes on typos)
        ├── CPUScheduler.java             FCFS / SJF / Round Robin + comparison table
        ├── BankersAlgorithm.java         need matrix + safety algorithm + safe sequence
        ├── PagingSimulator.java          page-table address translation
        ├── SegmentationSimulator.java    segment-table address translation
        ├── FragmentationDemo.java        internal + external fragmentation demos
        ├── PageReplacementSimulator.java FIFO / LRU / Optimal with per-reference tables
        ├── DiskScheduler.java            FCFS / SSTF / SCAN / C-SCAN
        └── MultiprogrammingSimulator.java jobs sharing memory and the CPU
```

### Class-by-class description (filename → responsibility)

| Filename | Class | What it does |
|----------|-------|--------------|
| `Main.java` | `Main` | Prints the banner and the main menu, reads your choice (0–8), and starts the feature class you picked. Contains `main(String[] args)`. |
| `InputHelper.java` | `InputHelper` | Wraps `Scanner`. `readInt`, `readIntInRange`, `readIntArray`, `readLine` and `readYesNo` **ask again** instead of crashing when you type something invalid. Every other class gets its input through this one helper. |
| `CPUScheduler.java` | `CPUScheduler` | Inner class `Process` (one process: id + burst) and inner class `Result` (waiting/turnaround arrays + execution order). Methods `fcfs`, `sjf`, `roundRobin` compute the results; `printResult` / `printComparison` format the tables. |
| `BankersAlgorithm.java` | `BankersAlgorithm` | Reads allocation / maximum / available, validates them, computes `Need = Max − Allocation`, then runs the classic safety algorithm, logging every step (`P2: need … <= work …`) and printing a safe sequence or the set of processes that could get stuck. |
| `PagingSimulator.java` | `PagingSimulator` | Builds a page table (frame per page, `-1` = on disk), then translates addresses: `page = addr / pageSize`, `offset = addr % pageSize`, `physical = frame * pageSize + offset`. Reports page faults and out-of-range addresses. |
| `SegmentationSimulator.java` | `SegmentationSimulator` | Builds a segment table (base + limit per segment) and translates `(segment, offset)` to `base + offset`, checking segment existence, limit overflow (segmentation fault) and RAM bounds. |
| `FragmentationDemo.java` | `FragmentationDemo` | *Internal:* assigns processes to fixed partitions first-fit and totals the wasted bytes inside partitions. *External:* allocates/frees variable-size blocks, prints memory maps with holes, then tests a new request against total free vs. largest hole. |
| `PageReplacementSimulator.java` | `PageReplacementSimulator` | Runs FIFO, LRU and Optimal on the same reference string. After **each** reference it prints the three frame slots and `FAULT`/`HIT`, then totals and compares the faults. |
| `DiskScheduler.java` | `DiskScheduler` | FCFS (arrival order), SSTF (nearest request), SCAN (elevator, with the trip to the disk edge) and C-SCAN (wrap-around). Prints the full service path and total cylinders travelled. |
| `MultiprogrammingSimulator.java` | `MultiprogrammingSimulator` | Tick-by-tick simulation: first-fit loader fills memory with jobs, one CPU runs the ready jobs Round-Robin, each job pauses once for a simulated I/O. Prints every job's state each tick plus a final summary. |

### How the program is organized

```
Main  (menu loop)
 ├── InputHelper            ← every class asks for input through this
 ├── CPUScheduler.run(in)           ┐
 ├── BankersAlgorithm.run(in)       │
 ├── PagingSimulator.run(in)        │ each feature class:
 ├── SegmentationSimulator.run(in)  │   1. shows its own sub-menu (1..n, 0 = back)
 ├── FragmentationDemo.run(in)      │   2. reads its data with validation
 ├── PageReplacementSimulator.run(in)│  3. runs the algorithm(s)
 ├── DiskScheduler.run(in)          │   4. prints a table / trace
 └── MultiprogrammingSimulator.run(in)┘
```

Design rules used throughout (deliberately simple, second-year friendly):

* **One class per feature** — you can study or modify one feature without reading the others.
* **Plain data + methods** — arrays, `ArrayList`, `ArrayDeque`, `String.format`; no threads,
  no generics beyond `ArrayList<...>`, no streams, no design-pattern machinery.
* **Comments explain the *why*** — each algorithm comment restates the textbook definition it
  implements, so the code doubles as study notes.
* **Never crash on input** — all input goes through `InputHelper`, which re-asks on bad data.

---

## 5. Using the simulator

On start you always see the main menu:

```
=====================================================
   MINI OS SIMULATOR  --  educational demonstration
   (This is NOT a real operating system.)
=====================================================
---------------- MAIN MENU --------------------
  1) CPU scheduling   (FCFS, SJF, Round Robin)
  2) Banker's algorithm (deadlock avoidance)
  3) Paging           (logical -> physical)
  4) Segmentation     (logical -> physical)
  5) Fragmentation    (internal & external)
  6) Page replacement  (FIFO, LRU, Optimal)
  7) Disk scheduling  (FCFS, SSTF, SCAN, C-SCAN)
  8) Multiprogramming (jobs sharing CPU+memory)
  0) Exit
----------------------------------------------
Your choice:
```

Type a number and press Enter. Each feature then shows **its own sub-menu**; choosing `0`
there returns to the main menu, and `0` at the main menu exits. Invalid input is always
re-asked (see §11).

The sample sessions in §6–§13 are real captured runs. For brevity, the repeated main-menu
reprint after leaving a feature is replaced by a single `...` line.

---

## 6. Feature 1 — CPU scheduling (`src/minios/CPUScheduler.java`)

**What it demonstrates**

* Non-preemptive **FCFS**: processes run in arrival order.
* Non-preemptive **SJF**: the shortest burst runs next (provably minimal average waiting time
  when everyone arrives at time 0).
* Preemptive **Round Robin**: each process gets a time *quantum*, then goes to the back of the
  ready queue.
* The two key formulas, used everywhere:
  * `turnaround = completion − arrival` (arrival is 0 for all processes here)
  * `waiting = turnaround − burst`

**Assumptions**

* All processes arrive at time 0 (no arrival times are entered).
* FCFS runs in the order the processes were typed in; SJF breaks equal-burst ties by P-number.
* Round Robin's *execution order* lists one entry per time slice, so a process can appear
  several times.

**Sample input** (`samples/cpu_fcfs.txt`, `cpu_sjf.txt`, `cpu_rr.txt`, `cpu_compare.txt` —
values below are what you type when prompted):

```
4            (4 processes)
5 3 8 6      (their burst times, one per prompt)
4            (RR quantum, for options 3 and 4 only)
```

**Sample output — FCFS** (complete session, start to exit; typed characters shown in bold
position, i.e. right after each prompt):

```
=====================================================
   MINI OS SIMULATOR  --  educational demonstration
   (This is NOT a real operating system.)
=====================================================
---------------- MAIN MENU --------------------
  1) CPU scheduling   (FCFS, SJF, Round Robin)
  2) Banker's algorithm (deadlock avoidance)
  3) Paging           (logical -> physical)
  4) Segmentation     (logical -> physical)
  5) Fragmentation    (internal & external)
  6) Page replacement  (FIFO, LRU, Optimal)
  7) Disk scheduling  (FCFS, SSTF, SCAN, C-SCAN)
  8) Multiprogramming (jobs sharing CPU+memory)
  0) Exit
----------------------------------------------
Your choice: 1

CPU SCHEDULING
Assumption: every process arrives at time 0.

  1) FCFS        (First-Come, First-Served)
  2) SJF         (Shortest Job First, non-preemptive)
  3) Round Robin (preemptive, fixed time quantum)
  4) Compare all three on the same processes
  0) Back to main menu
  Choose an option: 1

How many processes? 4
  CPU burst time for P1: 5
  CPU burst time for P2: 3
  CPU burst time for P3: 8
  CPU burst time for P4: 6

Algorithm: FCFS (First-Come, First-Served)
Execution order: P1 -> P2 -> P3 -> P4

Process   Burst  Waiting  Turnaround
-------- ------ -------- -----------
P1            5        0           5
P2            3        5           8
P3            8        8          16
P4            6       16          22
Average waiting time   : 7.25
Average turnaround time: 12.75

  Choose an option: 0
... (back at the main menu; 0 exits, printing a goodbye message)
```

**Sample output — SJF** (same bursts, option `2`):

```
Algorithm: SJF (Shortest Job First, non-preemptive)
Execution order: P2 -> P1 -> P4 -> P3

Process   Burst  Waiting  Turnaround
-------- ------ -------- -----------
P1            5        3           8
P2            3        0           3
P3            8       14          22
P4            6        8          14
Average waiting time   : 6.25
Average turnaround time: 11.75
```

**Sample output — Round Robin** (option `3`, quantum `4`):

```
Algorithm: Round Robin (quantum = 4)
Execution order: P1 -> P2 -> P3 -> P4 -> P1 -> P3 -> P4

Process   Burst  Waiting  Turnaround
-------- ------ -------- -----------
P1            5       11          16
P2            3        4           7
P3            8       12          20
P4            6       16          22
Average waiting time   : 10.75
Average turnaround time: 16.25
Note: in Round Robin the execution list shows one entry
      per time slice, so a process can appear several times.
```

**Sample output — compare all three** (option `4`, quantum `4`):

```
COMPARISON OF THE THREE ALGORITHMS
(W = waiting time, T = turnaround time)

Process  Burst | FCFS    | SJF     | RR
               | W    T  | W    T  | W    T
-------- ----- | ------- | ------- | -------
P1           5 |   0   5 |   3   8 |  11  16
P2           3 |   5   8 |   0   3 |   4   7
P3           8 |   8  16 |  14  22 |  12  20
P4           6 |  16  22 |   8  14 |  16  22
-------- ----- | ------- | ------- | -------
Averages (waiting / turnaround):
  FCFS : 7.25 / 12.75
  SJF  : 6.25 / 11.75
  RR   : 10.75 / 16.25
```

**How to test the main cases**

1. FCFS on bursts `5 3 8 6` → waiting `0,5,8,16`, averages **7.25 / 12.75** (check by hand:
   each process waits for all previous bursts).
2. SJF on the same data → order `P2 P1 P4 P3`, average waiting **6.25 ≤ 7.25** — SJF must
   never be worse than FCFS for identical arrivals; if you ever see it higher, there is a bug.
3. Round Robin with quantum `4` → execution list `P1 P2 P3 P4 P1 P3 P4`; P1 waits 11
   (it runs at 0–4, then finishes at 16).
4. Option 4 must repeat exactly the three individual results in one table.
5. Replay the scripted run:
   `java -cp bin minios.Main < samples/cpu_rr.txt` and compare with
   `samples/expected/cpu_rr.txt`.

---

## 7. Feature 2 — Banker's algorithm (`src/minios/BankersAlgorithm.java`)

**What it demonstrates**

* How a resource-need matrix is derived: `Need[i] = Max[i] − Allocation[i]`.
* The **safety algorithm**: with free resources as `Work`, a process may finish only if
  `Need[i] ≤ Work`; when it finishes it returns `Allocation[i]` to `Work`.
* If every process can finish → **SAFE state** and a **safe sequence** is printed; otherwise
  **UNSAFE** and the processes that cannot be guaranteed to finish are listed.

**Assumptions**

* Processes are labelled `P1…Pn`, resources `R1…Rm`; all numbers are non-negative.
* `Max ≥ Allocation` for every cell (validated — a violation is reported and all data is
  re-asked).
* A finished process releases *all* of its resources (the classic textbook model).

**Sample input** (`samples/banker_safe.txt` — the well-known textbook instance):

```
5            (processes)
3            (resource types)
Allocation (one row per line):
0 1 0
2 0 0
3 0 2
2 1 1
0 0 2
Max (one row per line):
7 5 3
3 2 2
9 0 2
2 2 2
4 3 3
Available:
3 3 2
```

**Sample output (safe):**

```
BANKER'S ALGORITHM (deadlock avoidance)

  1) Enter data and check for a safe state
  0) Back to main menu
  Choose an option: 1

Number of processes: 5
Number of resource types: 3
Allocation matrix - one row per process, 3 number(s) per row (space separated).
  Allocation for P1 (3 number(s)): 0 1 0
  Allocation for P2 (3 number(s)): 2 0 0
  Allocation for P3 (3 number(s)): 3 0 2
  Allocation for P4 (3 number(s)): 2 1 1
  Allocation for P5 (3 number(s)): 0 0 2
Maximum-need matrix - same format.
  Max for P1 (3 number(s)): 7 5 3
  Max for P2 (3 number(s)): 3 2 2
  Max for P3 (3 number(s)): 9 0 2
  Max for P4 (3 number(s)): 2 2 2
  Max for P5 (3 number(s)): 4 3 3
Available resources (3 number(s)): 3 3 2
Need matrix (Max - Allocation):
           R1    R2    R3
   P1       7     4     3
   P2       1     2     2
   P3       6     0     0
   P4       0     1     1
   P5       4     3     1

Safety check:
  Work (available) starts at [3,3,2]
  P2: need [1,2,2] <= work [3,3,2]  -> runs, returns [2,0,0], work = [5,3,2]
  P4: need [0,1,1] <= work [5,3,2]  -> runs, returns [2,1,1], work = [7,4,3]
  P1: need [7,4,3] <= work [7,4,3]  -> runs, returns [0,1,0], work = [7,5,3]
  P3: need [6,0,0] <= work [7,5,3]  -> runs, returns [3,0,2], work = [10,5,5]
  P5: need [4,3,1] <= work [10,5,5]  -> runs, returns [0,0,2], work = [10,5,7]

RESULT: SAFE STATE - the OS can satisfy every process in some order.
Safe sequence: P2 -> P4 -> P1 -> P3 -> P5

  Choose an option: 0
... (back at the main menu)
```

**Sample output (unsafe)** — same data but `Available = 1 1 2`
(`samples/banker_unsafe.txt`):

```
Safety check:
  Work (available) starts at [1,1,2]
  P4: need [0,1,1] <= work [1,1,2]  -> runs, returns [2,1,1], work = [3,2,3]
  P2: need [1,2,2] <= work [3,2,3]  -> runs, returns [2,0,0], work = [5,2,3]

RESULT: UNSAFE STATE - no safe sequence exists;
        a deadlock could occur if requests are granted.
Processes that could not be guaranteed to finish: P1, P3, P5
```

**How to test the main cases**

1. Safe case → sequence `P2 -> P4 -> P1 -> P3 -> P5` and work grows to `[10,5,7]`
   (= total resources 10/5/7 — a nice self-check: the last work value must equal
   `Available + sum(Allocation)`).
2. Unsafe case → exactly two processes run, then no remaining `Need ≤ Work`; the report names
   `P1, P3, P5`.
3. Validation: enter a Max value smaller than Allocation (e.g. Max row `0 0 0` for P1) —
   the program must explain `max must be >= allocation` and re-ask everything.
4. Replay: `java -cp bin minios.Main < samples/banker_unsafe.txt` vs
   `samples/expected/banker_unsafe.txt`.

---

## 8. Feature 3 — Paging (`src/minios/PagingSimulator.java`)

**What it demonstrates**

* Splitting a logical address: `page = addr / pageSize`, `offset = addr % pageSize`.
* The page table maps *pages* to *frames*; both have the same size.
* Physical address: `physical = frame * pageSize + offset`.
* Two failure modes: **page fault** (page not in RAM, frame `-1`) and **invalid address**
  (outside the logical address space).

**Assumptions**

* The page table is entered by hand (in a real OS the OS builds it when loading a program).
* Frames and pages are the same size — this is exactly what makes paging simple.
* Page numbers start at 0.

**Sample input** (`samples/paging.txt`):

```
1024         (page size in bytes)
8            (8 pages -> logical addresses 0..8191)
frames: 2 3 -1 5 0 -1 7 1     (page 2 and page 5 are "on disk")
then queries: 2500, 1500, 8000, 9000, -1 (stop)
```

**Sample output:**

```
PAGING - logical to physical address translation

  1) Build a page table and translate addresses
  0) Back to main menu
  Choose an option: 1

Page size (bytes, e.g. 1024): 1024
Number of pages in the logical address space: 8
Logical address space = 8 pages x 1024 bytes = 8192 bytes (addresses 0..8191)
Enter the frame number for each page.
Use -1 for a page that is NOT in memory (it is on disk).
  Frame for page 0 (-1 .. 100000): 2
  Frame for page 1 (-1 .. 100000): 3
  Frame for page 2 (-1 .. 100000): -1
  Frame for page 3 (-1 .. 100000): 5
  Frame for page 4 (-1 .. 100000): 0
  Frame for page 5 (-1 .. 100000): -1
  Frame for page 6 (-1 .. 100000): 7
  Frame for page 7 (-1 .. 100000): 1

Page table in use:
  Page | Frame
  -----|------
     0 |     2
     1 |     3
     2 | on disk
     3 |     5
     4 |     0
     5 | on disk
     6 |     7
     7 |     1

Logical address to translate (-1 to stop): 2500
  2500 -> page 2, offset 452 -> PAGE FAULT: page 2 is not in memory; the OS would load it from disk first.
Logical address to translate (-1 to stop): 1500
  1500 -> page 1, offset 476 -> frame 3 -> physical address 3548   (3 x 1024 + 476)
Logical address to translate (-1 to stop): 8000
  8000 -> page 7, offset 832 -> frame 1 -> physical address 1856   (1 x 1024 + 832)
Logical address to translate (-1 to stop): 9000
  9000 -> INVALID ADDRESS: outside the logical address space (valid addresses are 0..8191).
Logical address to translate (-1 to stop): -1
```

**How to test the main cases**

1. `1500 = 1 x 1024 + 476` → page 1 → frame 3 → physical `3 x 1024 + 476 = 3548` (compute by
   hand to confirm).
2. `2500` lands on page 2, whose frame is `-1` → must report a **page fault**, not a number.
3. `9000 ≥ 8192` → must be reported as an **invalid address**.
4. Enter a negative address other than `-1` (e.g. `-5`) → must be rejected as negative.
5. Replay: `java -cp bin minios.Main < samples/paging.txt` vs
   `samples/expected/paging.txt`.

---

## 9. Feature 4 — Segmentation (`src/minios/SegmentationSimulator.java`)

**What it demonstrates**

* Memory as named chunks (code/data/stack/heap) via a **segment table**: `base` + `limit`.
* Translation: `physical = base + offset`, valid only when `offset < limit`.
* Clear failure messages: unknown segment number, offset past the limit
  (**segmentation fault**), and physical address beyond RAM.

**Assumptions**

* You supply the logical address as two numbers: segment number, then offset.
* Segments are already loaded without overlapping (the OS's job) — the RAM-size check still
  catches addresses that fall outside physical memory.

**Sample input** (`samples/segmentation.txt`):

```
4            (4 segments)
segment 0: base 0,    limit 600
segment 1: base 1000, limit 400
segment 2: base 1800, limit 500
segment 3: base 3000, limit 700
RAM size:  4096
queries (segment, offset): (0,250) (1,400) (2,450) (5,10) (1,399), then -1
```

**Sample output:**

```
SEGMENTATION - logical to physical address translation

  1) Build a segment table and translate addresses
  0) Back to main menu
  Choose an option: 1

How many segments? 4
  Base address of segment 0: 0
  Limit (size) of segment 0: 600
  Base address of segment 1: 1000
  Limit (size) of segment 1: 400
  Base address of segment 2: 1800
  Limit (size) of segment 2: 500
  Base address of segment 3: 3000
  Limit (size) of segment 3: 700
Physical memory (RAM) size in bytes: 4096

Segment table in use:
  Segment |   Base | Limit | Ends at
  --------|--------|-------|--------
        0 |      0 |   600 |     599
        1 |   1000 |   400 |    1399
        2 |   1800 |   500 |    2299
        3 |   3000 |   700 |    3699

Segment number (-1 to stop): 0
  Offset inside that segment: 250
  0:250 -> base 0 + offset 250 = physical address 250
Segment number (-1 to stop): 1
  Offset inside that segment: 400
  1:400 -> SEGMENTATION FAULT: offset 400 is past the end of segment 1 (limit = 400).
Segment number (-1 to stop): 2
  Offset inside that segment: 450
  2:450 -> base 1800 + offset 450 = physical address 2250
Segment number (-1 to stop): 5
  Offset inside that segment: 10
  5:10 -> INVALID ADDRESS: segment 5 does not exist (valid segments are 0..3).
Segment number (-1 to stop): 1
  Offset inside that segment: 399
  1:399 -> base 1000 + offset 399 = physical address 1399
Segment number (-1 to stop): -1
```

**How to test the main cases**

1. Valid: `(2,450)` → `1800 + 450 = 2250`; `(1,399)` → `1399` (last legal byte of that segment).
2. Limit check: `(1,400)` → **segmentation fault** — offsets `0..399` only (`limit = 400`).
3. Unknown segment: `(5,10)` → invalid address listing the valid range `0..3`.
4. RAM check: ask for e.g. segment 3, offset 699 → `3699 < 4096` OK; a made-up segment whose
   `base+offset ≥ 4096` must fault (set RAM smaller, e.g. `3000`, to see this branch).
5. Replay: `java -cp bin minios.Main < samples/segmentation.txt` vs
   `samples/expected/segmentation.txt`.

---

## 10. Feature 5 — Fragmentation (`src/minios/FragmentationDemo.java`)

**What it demonstrates**

* **Internal fragmentation** — bytes wasted *inside* an allocated block (classic case:
  fixed-size partitions).
* **External fragmentation** — plenty of free bytes in total, but split into holes that are
  each too small for the next request.

**Assumptions**

* Internal demo: fixed partitions, one process each, first-fit (first free partition big enough).
* External demo: one contiguous memory block, variable-size first-fit allocation, jobs are
  freed by name; no compaction, no paging.

### 10.1 Internal fragmentation — sample input (`samples/fragment_internal.txt`)

```
4 partitions: 100 150 250 400 (KB)
4 processes :  80 140 200 300 (KB)
```

### 10.1 Sample output

```
INTERNAL FRAGMENTATION: space wasted INSIDE a partition.
Number of fixed partitions: 4
  Size of partition 1 (KB): 100
  Size of partition 2 (KB): 150
  Size of partition 3 (KB): 250
  Size of partition 4 (KB): 400
Number of processes: 4
  Size of P1 (KB): 80
  Size of P2 (KB): 140
  Size of P3 (KB): 200
  Size of P4 (KB): 300

Allocation result (first-fit):
  Partition | Size | Process | Wasted (internal frag)
  ----------|------|---------|----------------------
  partition 1 |  100 | P1      |   20 KB
  partition 2 |  150 | P2      |   10 KB
  partition 3 |  250 | P3      |   50 KB
  partition 4 |  400 | P4      |  100 KB

Total internal fragmentation (wasted inside partitions): 180 KB
```

### 10.2 External fragmentation — sample input (`samples/fragment_external.txt`)

```
memory 1000 KB
allocate A 400, B 100, C 300
free B
request 250 KB
```

### 10.2 Sample output

```
EXTERNAL FRAGMENTATION: free space exists, but it is split into holes.
Total memory (KB): 1000
How many jobs to allocate first? 3
  Name of job 1 (e.g. A): A
  Size of A (KB): 400
  A allocated.
  Memory map: [A 0-400] [HOLE 400-1000]
  Name of job 2 (e.g. A): B
  Size of B (KB): 100
  B allocated.
  Memory map: [A 0-400] [B 400-500] [HOLE 500-1000]
  Name of job 3 (e.g. A): C
  Size of C (KB): 300
  C allocated.
  Memory map: [A 0-400] [B 400-500] [C 500-800] [HOLE 800-1000]
How many jobs to free? 1
  Name of the job to free: B
  B freed.
  Memory map: [A 0-400] [HOLE 400-500] [C 500-800] [HOLE 800-1000]
New request size (KB): 250

Request of 250 KB:
  Total free memory : 300 KB
  Largest single hole: 200 KB
  -> EXTERNAL FRAGMENTATION! There is enough free memory in total
     (300 KB >= 250 KB) but no single hole is large
     enough (largest hole = 200 KB). The job must wait, or the
     OS has to compact memory / use paging.
```

**How to test the main cases**

1. Internal: each row is `partitionSize − processSize`; `20+10+50+100 = 180 KB` total.
   Make one process larger than every free partition (e.g. a 5th process of 500 KB) — it must
   report *"did not fit in any free partition (waits)"*.
2. External: the point of the demo is `total free (300) ≥ request (250)` **but**
   `largest hole (200) < 250` → the message must contain the words **EXTERNAL
   FRAGMENTATION**.
3. A request of `150` after the same setup must instead say it **FITS** (200 KB hole), and a
   request of `400` must say **NOT ENOUGH MEMORY** (300 total free).
4. Freeing a job that does not exist must print a friendly `[!]` message, not crash.

---

## 11. Feature 6 — Page replacement (`src/minios/PageReplacementSimulator.java`)

**What it demonstrates**

* What the OS does when a needed page is not in RAM: evict a page that is.
* **FIFO** — evict the page that entered memory first (circular pointer).
* **LRU** — evict the page that was used least recently (a "last used" clock per frame).
* **Optimal** — evict the page whose next use is furthest in the future (the theoretical best;
  a real OS cannot know the future, so it is used as a benchmark).
* The state of **all frames after every reference**, marked `FAULT` or `HIT`.

**Assumptions**

* Frames start empty; `-` marks an empty slot.
* FIFO's pointer advances only when a fault occurs.
* Optimal ties (two pages never used again) pick the lower-numbered frame slot.

**Sample input** (`samples/page_replacement.txt`):

```
13                    (number of references)
7 0 1 2 0 3 0 4 2 3 0 3 2   (the reference string)
3                     (frames in RAM)
```

**Sample output:**

```
PAGE REPLACEMENT - FIFO, LRU and Optimal

  1) Run all three algorithms on a reference string
  0) Back to main menu
  Choose an option: 1

How many page references? 13
Reference string (13 page number(s), space separated): 7 0 1 2 0 3 0 4 2 3 0 3 2
Number of frames in RAM: 3

FIFO  (frames = 3)
  Ref | frame frame frame | Result
  --- | ------ ------ ------ |--------
    7 |     7     -     - | FAULT
    0 |     7     0     - | FAULT
    1 |     7     0     1 | FAULT
    2 |     2     0     1 | FAULT
    0 |     2     0     1 | HIT
    3 |     2     3     1 | FAULT
    0 |     2     3     0 | FAULT
    4 |     4     3     0 | FAULT
    2 |     4     2     0 | FAULT
    3 |     4     2     3 | FAULT
    0 |     0     2     3 | FAULT
    3 |     0     2     3 | HIT
    2 |     0     2     3 | HIT
  Total page faults: 10

LRU  (frames = 3)
  Ref | frame frame frame | Result
  --- | ------ ------ ------ |--------
    7 |     7     -     - | FAULT
    0 |     7     0     - | FAULT
    1 |     7     0     1 | FAULT
    2 |     2     0     1 | FAULT
    0 |     2     0     1 | HIT
    3 |     2     0     3 | FAULT
    0 |     2     0     3 | HIT
    4 |     4     0     3 | FAULT
    2 |     4     0     2 | FAULT
    3 |     4     3     2 | FAULT
    0 |     0     3     2 | FAULT
    3 |     0     3     2 | HIT
    2 |     0     3     2 | HIT
  Total page faults: 9

Optimal  (frames = 3)
  Ref | frame frame frame | Result
  --- | ------ ------ ------ |--------
    7 |     7     -     - | FAULT
    0 |     7     0     - | FAULT
    1 |     7     0     1 | FAULT
    2 |     2     0     1 | FAULT
    0 |     2     0     1 | HIT
    3 |     2     0     3 | FAULT
    0 |     2     0     3 | HIT
    4 |     2     4     3 | FAULT
    2 |     2     4     3 | HIT
    3 |     2     4     3 | HIT
    0 |     2     0     3 | FAULT
    3 |     2     0     3 | HIT
    2 |     2     0     3 | HIT
  Total page faults: 7

COMPARISON
  Algorithm   Page faults
  ---------   -----------
  FIFO               10
  LRU                 9
  Optimal             7

  Fun fact: Optimal is THE best possible result - no other
  algorithm can produce fewer faults on this reference string.
```

> The lines above are reproduced exactly as printed; the actual output pads each row with
> trailing spaces (`FAULT ` / `HIT   `) so the last column lines up.

**How to test the main cases**

1. All three start with 4 faults for the first 4 distinct pages `7 0 1 2` — the interesting
   part starts at reference `3`.
2. **Optimal ≤ LRU ≤ FIFO is not guaranteed pairwise, but Optimal must be ≤ both** — here
   `7 ≤ 9` and `7 ≤ 10` ✓. Try a few strings; if Optimal ever has *more* faults than another
   algorithm, there is a bug.
3. Belady-style check: same string with **4 frames** — FIFO can only stay equal or improve.
4. Frames = 1: every reference except the first repeats of the same page are faults; e.g. the
   string `1 2 1 2` with 1 frame → 4 faults (thrashing).
5. Replay: `java -cp bin minios.Main < samples/page_replacement.txt` vs
   `samples/expected/page_replacement.txt`.

---

## 12. Feature 7 — Disk scheduling (`src/minios/DiskScheduler.java`)

**What it demonstrates**

* **FCFS** — requests served in arrival order (fair, but the head jumps around).
* **SSTF** — always the nearest unserved request (short total travel, possible starvation).
* **SCAN** — the "elevator": keep going in one direction, touch the disk edge, reverse.
* **C-SCAN** — same, but at the edge the head *wraps* to the other end and continues in the
  original direction (more even wait times).
* For each: the exact **service order** and the **total head movement** in cylinders.

**Assumptions** (stated here because textbooks differ — check against your course notes!)

* Cylinders are numbered `0 … max` (the sample disk is `0..199`).
* SSTF ties (equal distance) go to the lower cylinder number.
* SCAN travels to the disk edge only when requests remain on the other side (in that special
  case it behaves like LOOK).
* For C-SCAN the **return jump counts as head movement** — the output says so explicitly;
  some books exclude it.
* A request equal to the current head position costs 0 cylinders.

**Sample input** (`samples/disk.txt` — classic textbook queue):

```
199                    (highest cylinder: disk is 0..199)
53                     (initial head position)
8                      (number of requests)
98 183 37 122 14 124 65 67
1                      (SCAN/C-SCAN start toward higher cylinders)
```

**Sample output** (option `5` — compare all four):

```
DISK SCHEDULING

  1) FCFS
  2) SSTF   (Shortest Seek Time First)
  3) SCAN   (elevator)
  4) C-SCAN (circular elevator)
  5) Compare all four on the same requests
  0) Back to main menu
  Choose an option: 5

Highest cylinder number (disk = 0..max): 199
Initial head position (0..199): 53
Number of pending requests: 8
Request queue (8 cylinder number(s), space separated): 98 183 37 122 14 124 65 67
Initial direction for SCAN / C-SCAN (1 = toward higher cylinders, 2 = toward lower): 1
FCFS (First-Come, First-Served)
  Order of service : 53 -> 98 -> 183 -> 37 -> 122 -> 14 -> 124 -> 65 -> 67
  Total head movement: 640 cylinders

SSTF (Shortest Seek Time First)
  Order of service : 53 -> 65 -> 67 -> 37 -> 14 -> 98 -> 122 -> 124 -> 183
  Total head movement: 236 cylinders

SCAN (elevator)
  Order of service : 53 -> 65 -> 67 -> 98 -> 122 -> 124 -> 183 -> 199 (end of disk) -> 37 -> 14
  Total head movement: 331 cylinders

C-SCAN (circular elevator)
  Order of service : 53 -> 65 -> 67 -> 98 -> 122 -> 124 -> 183 -> 199 (end of disk) -> 0 (wrap) -> 14 -> 37
  Total head movement: 382 cylinders

SUMMARY (total head movement)
  FCFS  : 640 cylinders
  SSTF  : 236 cylinders
  SCAN  : 331 cylinders
  C-SCAN: 382 cylinders  (includes the return jump - some books exclude it)
  Smallest movement in this run: 236 cylinders.
```

**How to test the main cases**

1. **FCFS = 640** — verify by hand: `45+85+146+85+108+110+59+2`.
2. **SSTF = 236** — path `53→65→67→37→14→98→122→124→183`, sum of absolute differences.
3. **SCAN = 331** — `(199−53) up + (199−14) back = 146 + 185`.
4. **C-SCAN = 382** — `146 (to 199) + 199 (wrap to 0) + 14 + 23`.
5. Run option `3` with direction `2` (toward lower) — order must start `53 → 37 → 14`,
   end at `0`, then sweep up through `65 … 183`.
6. A request outside `0..max` (e.g. `250`) → the data is rejected with an explanation and
   re-entered; a negative queue line is rejected by the same validation.
7. Replay: `java -cp bin minios.Main < samples/disk.txt` vs `samples/expected/disk.txt`.

---

## 13. Feature 8 — Multiprogramming (`src/minios/MultiprogrammingSimulator.java`)

**What it demonstrates**

* Several jobs **sharing one memory and one CPU**, with the classic job states:

  ```
  JOB QUEUE --(loaded, memory found)--> READY --(CPU)--> RUNNING
     ^                                   ^               |
     |                                   +---(quantum)---+
     |                                   |               |
     +--(terminated, memory freed)--- TERMINATED   WAITING (I/O)
  ```

* The **loader**: jobs wait in the JOB QUEUE until a first-fit hole large enough exists.
* The **dispatcher**: one CPU, Round Robin with a time quantum — everyone loaded makes progress.
* **I/O waits**: each job leaves the CPU once for a simulated I/O and returns to READY.
* A full **tick-by-tick trace**: who uses the CPU, the READY queue *in actual order*, who is
  WAITING (with ticks left), who still waits for memory, and the memory map — plus events
  (LOADED / quantum expired / starts I/O / FINISHED).

**Simplifications** (stated honestly — a teaching model, not a real OS):

* Jobs are loaded **contiguously, first-fit** — no paging/segmentation inside this demo.
* All jobs arrive at time 0, in the order entered; there is one CPU and one ideal I/O device.
* Each job performs **one** I/O of 3 ticks at the halfway point of its burst
  (`ceil(burst/2)`); the device never breaks and never queues jobs (each job does I/O once).
* Within a tick, this fixed order is used: (1) load jobs, (2) assign CPU,
  (3) progress I/O, (4) run one CPU tick.
* A "tick" is an abstract time unit, not a real millisecond. A safety limit of 10 000 ticks
  stops the simulation if something ever goes wrong.

**Sample input** (`samples/multiprogramming.txt`):

```
100 KB memory
4 jobs: J1 40KB/burst 7, J2 30KB/burst 4, J3 25KB/burst 5, J4 20KB/burst 3
time quantum: 2
```

**Sample output:**

```
MULTIPROGRAMMING - jobs sharing memory and the CPU

  1) Start a new multiprogramming simulation
  0) Back to main menu
  Choose an option: 1

Main memory size (KB): 100
How many jobs? 4
  Name of job 1 (e.g. J1): J1
  Memory needed by J1 (KB): 40
  CPU burst of J1 (ticks): 7
  Name of job 2 (e.g. J1): J2
  Memory needed by J2 (KB): 30
  CPU burst of J2 (ticks): 4
  Name of job 3 (e.g. J1): J3
  Memory needed by J3 (KB): 25
  CPU burst of J3 (ticks): 5
  Name of job 4 (e.g. J1): J4
  Memory needed by J4 (KB): 20
  CPU burst of J4 (ticks): 3
Time quantum for the CPU (ticks): 2

Simulation rules:
  - first-fit contiguous loading, memory = 100 KB
  - Round Robin on the CPU, quantum = 2 tick(s)
  - each job does ONE I/O of 3 ticks at the halfway point of its burst

Tick | State after the tick:  CPU | READY (queue order) | WAITING | JOB QUEUE | memory
-----+--------------------------------------------------------------+-----------------------
0    | CPU: J1 (6 left) | READY: J2, J3 | WAITING: - | QUEUE: J4 | MEM: J1, J2, J3 | J1 LOADED (40 KB at 0); J2 LOADED (30 KB at 40); J3 LOADED (25 KB at 70); J1 -> RUNNING
1    | CPU: J1 (5 left) | READY: J2, J3, J1 | WAITING: - | QUEUE: J4 | MEM: J1, J2, J3 | J1 quantum expired -> READY
2    | CPU: J2 (3 left) | READY: J3, J1 | WAITING: - | QUEUE: J4 | MEM: J1, J2, J3 | J2 -> RUNNING
3    | CPU: J2 (2 left) | READY: J3, J1 | WAITING: J2 (3) | QUEUE: J4 | MEM: J1, J2, J3 | J2 starts I/O (3 ticks)
4    | CPU: J3 (4 left) | READY: J1 | WAITING: J2 (2) | QUEUE: J4 | MEM: J1, J2, J3 | J3 -> RUNNING
5    | CPU: J3 (3 left) | READY: J1, J3 | WAITING: J2 (1) | QUEUE: J4 | MEM: J1, J2, J3 | J3 quantum expired -> READY
6    | CPU: J1 (4 left) | READY: J3, J2 | WAITING: - | QUEUE: J4 | MEM: J1, J2, J3 | J1 -> RUNNING; J2 I/O finished -> READY
7    | CPU: J1 (3 left) | READY: J3, J2 | WAITING: J1 (3) | QUEUE: J4 | MEM: J1, J2, J3 | J1 starts I/O (3 ticks)
8    | CPU: J3 (2 left) | READY: J2 | WAITING: J1 (2), J3 (3) | QUEUE: J4 | MEM: J1, J2, J3 | J3 -> RUNNING; J3 starts I/O (3 ticks)
9    | CPU: J2 (1 left) | READY: - | WAITING: J1 (1), J3 (2) | QUEUE: J4 | MEM: J1, J2, J3 | J2 -> RUNNING
10   | CPU: J2 (0 left) | READY: J1 | WAITING: J3 (1) | QUEUE: J4 | MEM: J1, J3 | J1 I/O finished -> READY; J2 FINISHED (memory freed)
11   | CPU: J1 (2 left) | READY: J4, J3 | WAITING: - | QUEUE: - | MEM: J1, J4, J3 | J4 LOADED (20 KB at 40); J1 -> RUNNING; J3 I/O finished -> READY
12   | CPU: J1 (1 left) | READY: J4, J3, J1 | WAITING: - | QUEUE: - | MEM: J1, J4, J3 | J1 quantum expired -> READY
13   | CPU: J4 (2 left) | READY: J3, J1 | WAITING: - | QUEUE: - | MEM: J1, J4, J3 | J4 -> RUNNING
14   | CPU: J4 (1 left) | READY: J3, J1 | WAITING: J4 (3) | QUEUE: - | MEM: J1, J4, J3 | J4 starts I/O (3 ticks)
15   | CPU: J3 (1 left) | READY: J1 | WAITING: J4 (2) | QUEUE: - | MEM: J1, J4, J3 | J3 -> RUNNING
16   | CPU: J3 (0 left) | READY: J1 | WAITING: J4 (1) | QUEUE: - | MEM: J1, J4 | J3 FINISHED (memory freed)
17   | CPU: J1 (0 left) | READY: J4 | WAITING: - | QUEUE: - | MEM: J4 | J1 -> RUNNING; J4 I/O finished -> READY; J1 FINISHED (memory freed)
18   | CPU: J4 (0 left) | READY: - | WAITING: - | QUEUE: - | MEM: - | J4 -> RUNNING; J4 FINISHED (memory freed)

All jobs finished at tick 19.

Job     Mem(KB)  Burst Loaded at Finished at  Turnaround
------ -------- ------ --------- ---------- -----------
J1           40      7         0         18          18
J2           30      4         0         11          11
J3           25      5         0         17          17
J4           20      3        11         19          19

Note: 'Loaded at' is how long the job waited in the job queue
for memory; turnaround = finish time (all jobs arrived at 0).
```

**How to read the trace (and test the main cases)**

1. **Tick 0:** `J1+J2+J3 = 95 KB` fit in 100 KB, but `J4` needs 20 KB and only 5 KB is left →
   J4 stays in the JOB QUEUE — memory is the bottleneck, exactly the point of multiprogramming.
2. **Tick 1:** J1 used both ticks of its quantum → it goes to the **back** of the ready queue
   (`READY: J2, J3, J1` — queue order, not alphabetical).
3. **Ticks 3, 7, 8, 14:** jobs leave the CPU for I/O (the number in parentheses is ticks of
   I/O remaining) while other jobs keep the CPU busy — the CPU column never shows *idle* when
   someone is runnable.
4. **Tick 10:** J2 finishes → its 30 KB are freed; **tick 11** is the first moment J4 fits
   (`MEM: J1, J4, J3` — J4 reused J2's space at address 40, first-fit).
5. **Tick 16/17:** J3's and J1's memory are freed as they terminate; the last job (J4) ends at
   tick 19 and every memory block is free again (`MEM: -`).
6. Cross-check the summary: J1 receives 7 CPU ticks (t0, t1, t6, t7, t11, t12, t17) = its
   burst; J4 waited 11 ticks for memory (`Loaded at = 11`).
7. Edge test: memory `80 KB` with the same jobs → J3 (25 KB) no longer fits initially
   (`40+30 = 70`, only 10 KB free) — watch J3 load later, when J1 or J2 finishes.
8. Replay: `java -cp bin minios.Main < samples/multiprogramming.txt` vs
   `samples/expected/multiprogramming.txt`.

---

## 14. Graceful handling of invalid input

`InputHelper` re-asks instead of crashing (`Scanner.nextInt()` would throw an exception on
`abc`). Try the scripted session `samples/cpu_invalid_input.txt`:

```
Your choice: oops
  [!] "oops" is not a whole number. Please try again.
Your choice: 1
...
  Choose an option: three
  [!] "three" is not a whole number. Please try again.
  Choose an option: 1
How many processes? many
  [!] "many" is not a whole number. Please try again.
How many processes? 4
  CPU burst time for P1: 5
  CPU burst time for P2: -3
  [!] Please enter a whole number between 1 and 1000.
  CPU burst time for P2: 3
  ... (the run then completes normally)
```

What is protected:

| Situation | Behaviour |
|-----------|-----------|
| Non-numeric text (`abc`, `three`) | `[!] … is not a whole number` and the same prompt repeats |
| Number outside the allowed range (0 processes, burst `-3`, quantum `0`) | `[!]` message stating the valid range, prompt repeats |
| Wrong number of values on a matrix/reference line | `[!] Expected exactly N number(s)…` and the line is re-read |
| Banker data that contradicts itself (`Max < Allocation`, negatives) | explains the exact problem, re-asks the whole data set |
| Disk request outside `0..max` | rejected with the valid range, data re-entered |
| End of input (piped file finishes) | prints `No more input received. Goodbye!` and exits cleanly |
| Paging/segmentation query `-1` | leaves the query loop politely |

---

## 15. Testing guide

### 15.1 Scripted regression test (fast, exact)

Every sample input has a byte-for-byte expected output in `samples/expected/`. From the
project folder:

```bash
javac -d bin src/minios/*.java          # or let Eclipse build into bin/

for f in samples/*.txt; do
  java -cp bin minios.Main < "$f" | diff - "samples/expected/$(basename "$f")" \
    && echo "PASS: $f" || echo "FAIL: $f"
done
```

All 14 samples must print `PASS`. (On Windows, run
`java -cp bin minios.Main < samples\cpu_fcfs.txt` and compare manually, or use `fc`.)

### 15.2 Manual checklist — the main case for every feature

| # | Feature | Do this | Must see |
|---|---------|---------|----------|
| 1 | CPU FCFS | bursts `5 3 8 6` | waiting `0,5,8,16`, avg **7.25 / 12.75** |
| 2 | CPU SJF | same bursts | order `P2 P1 P4 P3`, avg waiting **6.25** (≤ FCFS) |
| 3 | CPU Round Robin | same bursts, quantum `4` | order `P1 P2 P3 P4 P1 P3 P4`, avg **10.75 / 16.25** |
| 4 | CPU compare | option `4`, quantum `4` | one table with the three exact result sets |
| 5 | Banker (safe) | `samples/banker_safe` data | safe sequence `P2 → P4 → P1 → P3 → P5`, final work `[10,5,7]` |
| 6 | Banker (unsafe) | `Available = 1 1 2` | UNSAFE, stuck processes `P1, P3, P5` |
| 7 | Paging | `1500` in the sample table | `page 1, offset 476 → frame 3 → 3548` |
| 8 | Paging fault | `2500` (frame `-1`) | `PAGE FAULT` message, no physical address |
| 9 | Paging invalid | `9000` (space is 8192) | `INVALID ADDRESS` message |
| 10 | Segmentation | `(1,400)` vs `(1,399)` | fault at 400, physical `1399` at 399 |
| 11 | Segmentation unknown | `(5,10)` | invalid segment, lists `0..3` |
| 12 | Internal frag. | partitions `100 150 250 400`, jobs `80 140 200 300` | total wasted **180 KB** |
| 13 | External frag. | A400 B100 C300, free B, request 250 | `EXTERNAL FRAGMENTATION` (total 300, largest hole 200) |
| 14 | Page replacement | string `7 0 1 2 0 3 0 4 2 3 0 3 2`, 3 frames | faults **FIFO 10, LRU 9, Optimal 7**; 13 rows per table |
| 15 | Disk FCFS | sample queue | movement **640** |
| 16 | Disk SSTF | sample queue | movement **236**, order `53 65 67 37 14 98 122 124 183` |
| 17 | Disk SCAN | direction 1 | order ends `… 183 → 199 (end) → 37 → 14`, **331** |
| 18 | Disk C-SCAN | direction 1 | wrap shown, **382** (incl. jump) |
| 19 | Multiprogramming | sample jobs, quantum 2 | J4 waits until tick 11, all done at tick 19, table shows load/finish times |
| 20 | Invalid input | type `oops` at any number prompt | `[!] "oops" is not a whole number…`, program continues |

### 15.3 Edge cases worth trying by hand

* **CPU:** 1 process (waiting time must be `0`); all equal bursts (SJF order = FCFS order);
  quantum `1` (purest RR, execution list length = total burst).
* **Banker:** `Available = 0 0 0` with needs of 0 for some process — a process that needs
  nothing must still be able to run; all-zero matrices → trivially SAFE.
* **Paging:** address `0` (page 0, offset 0); page size `1`.
* **Segmentation:** offset `0` (first byte); segment count `1`.
* **Fragmentation:** request exactly equal to the largest hole → must FIT (≥ comparison).
* **Page replacement:** 1 frame (every repeat of a *different* page faults);
  frames ≥ distinct pages → faults = number of distinct pages.
* **Disk:** all requests on one side (SCAN must not travel to the empty edge);
  head exactly equal to one of the requests (that step costs 0).
* **Multiprogramming:** a job bigger than total memory (must be rejected by input
  validation); quantum `1`; memory so small only one job fits at a time.

### 15.4 Things to look for in the code while studying

* `CPUScheduler.fcfs/sjf/roundRobin` — three tiny loops implementing the three policies;
  compare how each one computes `waiting[i]`.
* `BankersAlgorithm.checkSafe` — the safety loop in its textbook form (find → finish →
  release → repeat).
* `PageReplacementSimulator` — all three algorithms share the same table format, so you can
  diff their behaviour reference by reference.
* `DiskScheduler.buildScanLike` — SCAN and C-SCAN share one engine; the only difference is
  what happens at the disk edge.
* `MultiprogrammingSimulator.runOnce` — the four-step tick (load → dispatch → I/O → run)
  matches the dispatcher/loop structure described in OS textbooks.

---

## 16. Consolidated assumptions and simplifications

| Area | Assumption / simplification |
|------|-----------------------------|
| General | Console-only; single user; no real hardware, memory, processes or files are touched |
| General | Whole numbers everywhere (time units, KB, bytes, cylinders) |
| CPU scheduling | All processes arrive at time 0; SJF is non-preemptive; RR queue starts in input order |
| Banker | `Max ≥ Allocation`, non-negative data, resources fully released on finish; 1-based labels P1…/R1… |
| Paging | Hand-built page table; page size = frame size; frame `-1` means "on disk" |
| Segmentation | Address given as (segment, offset); no segment overlap; segments already loaded by "the OS" |
| Fragmentation | Internal: fixed partitions, first-fit, one job per partition. External: contiguous first-fit, no compaction |
| Page replacement | Frames start empty; FIFO pointer moves only on faults; Optimal peeks at the future (unrealistic by design — it is the benchmark) |
| Disk | SSTF tie → lower cylinder; SCAN touches the edge only when the other side has requests; **C-SCAN counts the return jump** (some books do not) |
| Multiprogramming | Contiguous first-fit loading; all jobs arrive at 0; one CPU + one ideal I/O device; each job does one 3-tick I/O at `ceil(burst/2)`; fixed within-tick order load → dispatch → I/O → run; 10 000-tick safety limit |
| Input | End-of-piped-input exits politely instead of hanging |

---

## 17. Limitations (please keep this in perspective)

* This simulator **demonstrates selected OS concepts for study purposes only**. It is not an
  operating system, does not schedule real processes, and cannot be used as one.
* Not modelled: interrupts and context-switch cost, multi-level feedback queues, virtual
  memory with page tables on disk, multi-CPU scheduling, deadlock *recovery*, disk cache/seek
  physics, process creation syscalls, file systems, protection, users, …
* Algorithms follow the common textbook formulations; where books disagree (C-SCAN jump,
  SCAN vs LOOK, arrival times), the choice is **documented in the class comment and in §16**
  so you can match your course convention.
* Results are deterministic — the same input always produces the same output (this is what
  makes `samples/expected/` usable for testing).

**Enjoy exploring — and remember: it is an educational demonstration, not a real OS.**
