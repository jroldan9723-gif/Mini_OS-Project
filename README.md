# Mini_OS-Project

A finals project based on creating a mini operating system. This is a console program written
in Java that demonstrates how the main OS algorithms work. You choose a topic from the menu,
enter the data it asks for, and the program shows the result.

Topics covered:

- CPU scheduling: FCFS, SJF, Round Robin, and SRTF (preemptive, with arrival times,
  Gantt chart and response time)
- Banker's algorithm: safe / unsafe state, safe sequence, and the Resource Request
  Algorithm (grant or deny a runtime request)
- Paging (logical to physical address translation, overflow-safe calculations)
- Segmentation (base and limit translation, overlap and RAM validation)
- Fragmentation (internal and external, with First-Fit, Best-Fit and Worst-Fit and a
  comparison mode)
- Page replacement: FIFO, LRU, Optimal, and LFU (page faults, hits and rates)
- Disk scheduling: FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK (head movement)
- Multiprogramming: jobs with arrival times sharing memory and one CPU, one I/O device,
  memory layout with holes, and a full statistics report

Everything is done with standard Java (Scanner, ArrayList, String.format...), no external
libraries are needed.

Note: this project only simulates a few OS concepts for the class. It does not run on real
hardware and it is not a real operating system.

## Running the project in Eclipse

1. File > Import > General > Existing Projects into Workspace > Next.
2. Browse to this folder (Mini_OS-Project) and press Finish.
3. Open `src/minios/Main.java` and press Ctrl+F11 to run.
4. Use the Console view at the bottom and type your choices as prompted.

If you want to create the project yourself instead of importing:

1. File > New > Java Project, name it Mini_OS-Project (say no if Eclipse asks to create
   module-info.java).
2. Create a source folder named `src` and a package named `minios` inside it.
3. Create each class listed in the project structure below and paste in the code.

From the command line, inside the project folder:

```
javac -d bin src/minios/*.java
java -cp bin minios.Main
```

(Windows: `javac -d bin src\minios\*.java`)

The `bin` folder is where compiled classes go; it is listed in .gitignore so it is not
committed.

## Project structure

```
Mini_OS-Project/
├── .project / .classpath    Eclipse settings (src is the source folder, bin the output)
├── README.md
├── samples/                 input files you can replay, one per feature
│   └── expected/            the output each sample is supposed to print
└── src/minios/
    ├── Main.java
    ├── InputHelper.java
    ├── CPUScheduler.java
    ├── BankersAlgorithm.java
    ├── PagingSimulator.java
    ├── SegmentationSimulator.java
    ├── FragmentationDemo.java
    ├── PageReplacementSimulator.java
    ├── DiskScheduler.java
    └── MultiprogrammingSimulator.java
```

Each class and what it does:

| File | Class | Purpose |
|------|-------|---------|
| Main.java | Main | Entry point: shows the main menu and starts the feature you pick |
| InputHelper.java | InputHelper | Reads input safely; if you type something invalid it asks again instead of crashing |
| CPUScheduler.java | CPUScheduler | FCFS, SJF and Round Robin (arrival time 0), plus SRTF with arrival times, a Gantt chart and response time |
| BankersAlgorithm.java | BankersAlgorithm | Safety algorithm with safe sequence, plus the Resource Request Algorithm (validate, tentatively allocate, re-check safety, grant or restore) |
| PagingSimulator.java | PagingSimulator | Page table setup and address translation; the logical size is computed as a long so it cannot overflow |
| SegmentationSimulator.java | SegmentationSimulator | Segment table with overlap and RAM validation on entry, then address translation |
| FragmentationDemo.java | FragmentationDemo | Internal (fixed partitions) and external (holes) fragmentation with First-Fit, Best-Fit, Worst-Fit and a compare mode |
| PageReplacementSimulator.java | PageReplacementSimulator | FIFO, LRU, Optimal and LFU on the same reference string, with fault/hit rates |
| DiskScheduler.java | DiskScheduler | FCFS, SSTF, SCAN, C-SCAN, LOOK and C-LOOK with total head movement |
| MultiprogrammingSimulator.java | MultiprogrammingSimulator | Jobs with arrival times loaded first-fit into memory, Round Robin on the CPU, one shared I/O device, memory layout display and a statistics report |

## How the program is organized

`Main` prints the menu and calls the class for the topic you chose. Every feature class has a
`run(InputHelper)` method that shows its own sub-menu (0 goes back to the main menu), reads
its data, runs the algorithm and prints the result. All input goes through `InputHelper`, so
a wrong value never crashes the program. Each algorithm is kept in its own method (for
example `CPUScheduler.srtf(...)`, `DiskScheduler.clook(...)`) so you can read them one by
one.

The menu looks like this:

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

## Sample inputs and outputs

The outputs below are copied from running the program. To try them yourself, either type the
input when prompted or replay a saved file:

```
java -cp bin minios.Main < samples/cpu_fcfs.txt
```

The part where the main menu prints again after leaving a feature is cut out of the listings
below to keep them short.

### CPU scheduling (CPUScheduler.java)

Assumptions:

- Options 1-4 (FCFS, SJF, Round Robin, compare) assume every process arrives at time 0, so
  FCFS runs in the order entered and SJF just sorts by burst time.
- turnaround = completion - arrival, waiting = turnaround - burst.
- SRTF (option 5) asks for an arrival time per process and preempts whenever a newly arrived
  process has a shorter remaining burst than what is left of the current one. Ties (equal
  remaining time) go to the earlier arrival, then to the lower P-number, so the result is
  always deterministic.
- The Round Robin execution list shows one entry per time slice, so a process can appear
  more than once.

Sample input (samples/cpu_fcfs.txt, cpu_sjf.txt, cpu_rr.txt, cpu_compare.txt):

```
4           (number of processes)
5           (burst of P1)
3           (burst of P2)
8           (burst of P3)
6           (burst of P4)
4           (time quantum, only for options 3 and 4)
```

FCFS (option 1):

```
CPU SCHEDULING
Options 1-4 assume every process arrives at time 0.
SRTF (option 5) asks for an arrival time per process.

  1) FCFS        (First-Come, First-Served)
  2) SJF         (Shortest Job First, non-preemptive)
  3) Round Robin (preemptive, fixed time quantum)
  4) Compare all three on the same processes
  5) SRTF        (Shortest Remaining Time First, preemptive)
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
... (back at the main menu)
```

SJF (option 2), same bursts:

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

Round Robin (option 3), quantum 4:

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
```

Compare (option 4), quantum 4:

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

SRTF (option 5), sample input (samples/cpu_srtf.txt):

```
3            (number of processes)
P1: arrival 0, burst 5
P2: arrival 2, burst 3
P3: arrival 5, burst 2
```

SRTF output:

```
  Choose an option: 5

How many processes? 3
  Arrival time of P1: 0
  CPU burst time for P1: 5
  Arrival time of P2: 2
  CPU burst time for P2: 3
  Arrival time of P3: 5
  CPU burst time for P3: 2

Algorithm: SRTF (Shortest Remaining Time First, preemptive)
Execution order: P1 -> P3 -> P2

Gantt chart:
  0-5 P1 | 5-7 P3 | 7-10 P2

Process   Arrival  Burst  Completion  Turnaround  Waiting  Response
-------- -------- ------ ----------- ----------- -------- ---------
P1              0      5           5           5        0         0
P2              2      3          10           8        5         5
P3              5      2           7           2        0         0
Average turnaround time: 5.00
Average waiting time   : 1.67
Average response time  : 1.67
```

How the SRTF example works: P1 runs from 0. At time 2 P2 arrives with 3 ticks left, the same
as P1's remaining 3 - the tie goes to P1 (earlier arrival), so P1 keeps the CPU. P1 finishes
at 5. P3 arrives at 5 with 2 ticks, which is shorter than P2's 3, so P3 runs 5-7 and P2 runs
7-10. The Gantt chart shows every segment with its start and end time; a segment marked
"(preempted)" means the process still had work left and runs again later (this example has
no preemption because the only new arrival never interrupts - try bursts where a short job
arrives mid-run to see "(preempted)" appear).

The formulas used in the table:

- Turnaround = Completion - Arrival
- Waiting = Turnaround - Burst
- Response = first start time - Arrival (how long the job waited for its FIRST CPU slice)

To test:

- FCFS waiting times should be 0, 5, 8, 16 (each process waits for all earlier bursts).
- SJF average waiting (6.25) must be lower than or equal to FCFS (7.25).
- SRTF: check the Gantt segments add up (0-5, 5-7, 7-10 covers0..10) and that
  turnaround = waiting + burst for every row (e.g. P2: 5 + 3 = 8).
- SRTF with all arrival times 0 behaves like non-preemptive SJF (no preemption can happen).
- SRTF with a first arrival of 4 shows idle time: the Gantt chart starts with a segment
  "0-4 idle".
- With one process the waiting time is always 0.
- Round Robin with quantum 1 gives the longest possible execution list.

### Banker's algorithm (BankersAlgorithm.java)

How it works: Need = Max - Allocation. The safety algorithm pretends to run each process
whose need fits in the available resources; a finished process returns everything it held. If
all processes can finish, the system is safe and the order used is the safe sequence.

Option 2 adds the **Resource Request Algorithm**: a process asks for more resources while the
system is running. The program checks that the request is not negative, does not exceed the
process's remaining Need and does not exceed the Available resources, then it tentatively
allocates the request and runs the SAME safety algorithm on the new state. If the result is
safe the request is granted (Available, Allocation and Need are updated); if it is unsafe the
tentative allocation is undone and the request is denied, so an invalid or unsafe request
never changes the system state.

Assumptions: processes are P1..Pn, resources R1..Rm, all values are non-negative, Max is
never smaller than Allocation (this is checked), and a process releases all its resources
when it finishes. Option 2 uses the data from option 1 if you already entered it (it asks
for the data itself otherwise).

Sample input for the safety check (samples/banker_safe.txt):

```
5            (number of processes)
3            (number of resource types)
Allocation, one row per line:
0 1 0
2 0 0
3 0 2
2 1 1
0 0 2
Maximum need, one row per line:
7 5 3
3 2 2
9 0 2
2 2 2
4 3 3
Available:
3 3 2
```

Output (option 1, safe):

```
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
```

Same data with Available = 1 1 2 (samples/banker_unsafe.txt) gets stuck after two processes:

```
Safety check:
  Work (available) starts at [1,1,2]
  P4: need [0,1,1] <= work [1,1,2]  -> runs, returns [2,1,1], work = [3,2,3]
  P2: need [1,2,2] <= work [3,2,3]  -> runs, returns [2,0,0], work = [5,2,3]

RESULT: UNSAFE STATE - no safe sequence exists;
        a deadlock could occur if requests are granted.
Processes that could not be guaranteed to finish: P1, P3, P5
```

Resource request example (samples/banker_request.txt): same data as the safe case, then two
requests. The first is granted, the second is denied:

```
Process making the request (1..5): 2
Request for P2 (3 number(s)): 1 1 1

Request from P2: [1,1,1]
Remaining need of P2: [1,2,2]
Available right now        : [3,3,2]
Request is valid and the resources are available.

Safety check after granting the request:
  P2: need [0,1,1] <= work [2,2,1]  -> runs, returns [3,1,1], work = [5,3,2]
  P4: need [0,1,1] <= work [5,3,2]  -> runs, returns [2,1,1], work = [7,4,3]
  P1: need [7,4,3] <= work [7,4,3]  -> runs, returns [0,1,0], work = [7,5,3]
  P3: need [6,0,0] <= work [7,5,3]  -> runs, returns [3,0,2], work = [10,5,5]
  P5: need [4,3,1] <= work [10,5,5]  -> runs, returns [0,0,2], work = [10,5,7]
Result: SAFE - the request is GRANTED.

Available now: [2,2,1]
Updated allocation:
           R1    R2    R3
   P1       0     1     0
   P2       3     1     1
   P3       3     0     2
   P4       2     1     1
   P5       0     0     2

Updated need (Max - Allocation):
           R1    R2    R3
   P1       7     4     3
   P2       0     1     1
   P3       6     0     0
   P4       0     1     1
   P5       4     3     1
```

The second request (P1 asks for 0 2 1) passes the value checks but would leave the system
unsafe, so it is denied and everything is restored:

```
Process making the request (1..5): 1
Request for P1 (3 number(s)): 0 2 1

Request from P1: [0,2,1]
Remaining need of P1: [7,4,3]
Available right now        : [2,2,1]
Request is valid and the resources are available.

Safety check after granting the request:
  (no remaining process fits in the tentative work - nothing can run)
Result: UNSAFE - the request is DENIED and the
        original Available / Allocation values are restored.
Available still: [2,2,1]
```

To test:

- The final work vector of the safe case [10,5,7] must equal Available + all allocations
  (the three columns add up to 10, 5 and 7).
- Try entering a Max row smaller than the Allocation row; the program should refuse it and
  ask for the data again.
- A request larger than the process's Need is rejected with "exceeds the remaining need" and
  the state does not change; a request larger than Available is rejected with "NOT
  available". In both cases a following option-1 run must show the original numbers.
- After the granted request in samples/banker_request.txt, Available went from [3,3,2] to
  [2,2,1] (3-1, 3-1, 2-1) and P2's allocation went from [2,0,0] to [3,1,1].

### Paging (PagingSimulator.java)

How it works: page = address / pageSize, offset = address % pageSize, physical = frame *
pageSize + offset. Frame -1 means the page is not in memory (it would have to be loaded from
disk first). The logical address space is computed as a long
(`(long) pageSize * numPages`) so the multiplication can never overflow, and the physical
address is a long as well because frame * pageSize can exceed the int range.

Assumptions: page size and page count are entered by hand, pages and frames have the same
size, page numbers start at 0.

Sample input (samples/paging.txt):

```
1024         (page size in bytes)
8            (8 pages, so addresses 0..8191)
frame for each page: 2 3 -1 5 0 -1 7 1
addresses to translate: 2500, 1500, 8000, 9000, then -1 to stop
```

Output:

```
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

To test:

- 1500 = 1 x 1024 + 476, frame 3, physical 3548 (check with a calculator).
- 2500 lands on page 2 which has frame -1, so it must report a page fault.
- 9000 is outside the address space and must be reported as invalid.
- Address 0 should give page 0, offset 0, physical 0.

### Segmentation (SegmentationSimulator.java)

How it works: the RAM size is entered first, then every segment is checked while it is
entered: base >= 0, limit > 0, the whole range [base .. base+limit-1] must fit inside RAM,
and it must not overlap a segment that is already in the table. If a segment is invalid the
program prints why and asks for that segment again. After the table is complete, a query
(segment, offset) is translated with physical = base + offset as long as offset < limit; a
wrong segment number, an offset past the limit, or a physical address outside RAM is
reported as an invalid address / segmentation fault. The end address and the physical
address are computed as long so base + limit / base + offset cannot overflow.

Sample input (samples/segmentation.txt):

```
4            (number of segments)
4096         (RAM size - entered BEFORE the segments so it can validate them)
segment 0: base 0,    limit 600
segment 1: base 1000, limit 400
segment 2: base 1800, limit 500
segment 3: base 3000, limit 700
queries: (0,250), (1,400), (2,450), (5,10), (1,399), then -1
```

Output:

```
How many segments? 4
Physical memory (RAM) size in bytes: 4096
  Base address of segment 0: 0
  Limit (size) of segment 0: 600
  Base address of segment 1: 1000
  Limit (size) of segment 1: 400
  Base address of segment 2: 1800
  Limit (size) of segment 2: 500
  Base address of segment 3: 3000
  Limit (size) of segment 3: 700

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

Overlap validation demo (samples/segmentation_overlap.txt): the second segment first tries
to start at 300, which overlaps segment 0, so it is rejected and re-asked:

```
How many segments? 2
Physical memory (RAM) size in bytes: 4096
  Base address of segment 0: 0
  Limit (size) of segment 0: 600
  Base address of segment 1: 300
  Limit (size) of segment 1: 400
  [!] Segment 1 [300..699] overlaps segment 0 [0..599]. Enter this segment again.
  Base address of segment 1: 1000
  Limit (size) of segment 1: 400
```

To test:

- (1,399) works and (1,400) faults, because the limit is 400 (valid offsets are 0..399).
- (5,10) must report that segment 5 does not exist.
- Try a segment whose end address is >= RAM (e.g. RAM 2000, segment base 1500 limit 600):
  it must be rejected with "outside RAM" and asked again.
- Try overlapping segments (as in the overlap demo) and a negative base: both must be
  rejected without changing the table.

### Fragmentation (FragmentationDemo.java)

**Internal fragmentation** is the space wasted inside an allocation. The demo places
processes into fixed-size partitions and adds up the leftovers.

**External fragmentation** means there is enough free memory overall, but it is split into
holes that are each too small. The demo allocates and frees jobs in one block of memory,
prints the memory map after each step, and then checks a new request against the holes.

Both demos first ask which allocation strategy to use:

```
Allocation strategy:
  1) First-Fit  (first partition/hole that fits)
  2) Best-Fit   (smallest one that fits)
  3) Worst-Fit  (largest one that fits)
  4) Compare all three on the same data
  Choose a strategy:
```

- First-Fit takes the first candidate large enough (fast, simple).
- Best-Fit takes the smallest candidate that fits (leaves big holes free for big jobs).
- Worst-Fit takes the largest candidate (keeps the remaining hole as large as possible).
- Equal-size candidates always go to the lower partition number / lower address, so results
  are deterministic.

Sample input (samples/fragment_internal.txt, strategy 1 = first-fit):

```
4 partitions: 100 150 250 400 (KB)
4 processes:   80 140 200 300 (KB)
```

Internal output (first-fit):

```
INTERNAL FRAGMENTATION: space wasted INSIDE a partition.
Allocation strategy:
  1) First-Fit  (first partition/hole that fits)
  2) Best-Fit   (smallest one that fits)
  3) Worst-Fit  (largest one that fits)
  4) Compare all three on the same data
  Choose a strategy: 1
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

Compare mode (samples/fragment_compare.txt, strategy 4): same partitions and processes run
through all three strategies, each with its own copy of the layout:

```
COMPARISON - same partitions and processes for every strategy

Allocation result (first-fit):
  Partition | Size | Process | Wasted (internal frag)
  ----------|------|---------|----------------------
  partition 1 |  100 | P1      |   20 KB
  partition 2 |  150 | P2      |   10 KB
  partition 3 |  250 | P3      |   50 KB

Total internal fragmentation (wasted inside partitions): 80 KB

Allocation result (best-fit):
  Partition | Size | Process | Wasted (internal frag)
  ----------|------|---------|----------------------
  partition 1 |  100 | P1      |   20 KB
  partition 2 |  150 | P2      |   10 KB
  partition 3 |  250 | P3      |   50 KB

Total internal fragmentation (wasted inside partitions): 80 KB

Allocation result (worst-fit):
  Partition | Size | Process | Wasted (internal frag)
  ----------|------|---------|----------------------
  partition 3 |  250 | P1      |  170 KB
  partition 2 |  150 | P2      |   10 KB
  (any)     |    - | P3      | did not fit in any free partition (waits)
  partition 1 |  100 | -       | empty (no process)

Total internal fragmentation (wasted inside partitions): 180 KB
Summary (total internal fragmentation):
  first-fit: 80 KB
  best-fit: 80 KB
  worst-fit: 180 KB
```

Worst-Fit wastes the most here: it gives P1 the biggest partition (250), so P3 (200 KB)
fits nowhere.

External fragmentation sample input (samples/fragment_external.txt, strategy 1):
memory 1000 KB, allocate A 400, B 100, C 300, free B, then request 250 KB.

Output:

```
EXTERNAL FRAGMENTATION: free space exists, but it is split into holes.
Allocation strategy:
  1) First-Fit  (first partition/hole that fits)
  2) Best-Fit   (smallest one that fits)
  3) Worst-Fit  (largest one that fits)
  4) Compare all three on the same data
  Choose a strategy: 1
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

To test:

- Internal: 20 + 10 + 50 + 100 = 180 KB for first-fit; make one process larger than every
  free partition - it must report "did not fit in any free partition (waits)".
- Compare mode: worst-fit must pick the LARGEST partition each time (check the table), and
  all three runs must see identical input data.
- External: a request of 150 fits in the 200 KB hole, a request of 250 does not (external
  fragmentation), and a request of 400 is simply more than the 300 KB free in total.
- Best-Fit with jobs 400 then 100 into 1000 KB: B must go into the SMALLEST hole that fits
  (watch the memory map addresses), Worst-Fit must pick the largest.
- Freeing a job that is not allocated prints an error message instead of crashing.

### Page replacement (PageReplacementSimulator.java)

FIFO evicts the oldest page, LRU evicts the page that was used least recently, Optimal
evicts the page that will not be needed for the longest time (it looks ahead in the
reference string, which a real OS cannot do - it is used as the best possible result), and
LFU evicts the page with the lowest access frequency.

LFU details: every loaded page has a frequency counter that starts at 1 (the access that
caused the load counts) and goes up by 1 on every hit. When all frames are full the page
with the lowest frequency is replaced; if two pages are equally frequent, the least recently
used of them is replaced, so the result is always deterministic. The LFU table shows the
frequency of each page in memory and which page was replaced on every fault.

Sample input (samples/page_replacement.txt):

```
13                        (number of references)
7 0 1 2 0 3 0 4 2 3 0 3 2 (reference string)
3                         (frames)
```

Output (LFU table and the comparison; FIFO/LRU/Optimal print the same kind of table with
the same totals):

```
LFU  (frames = 3)
  Ref | frame frame frame |freqfreqfreq | replaced | Result
  --- | ------ ------ ------ |------------ | -------- |--------
    7 |     7     -     - |   1   -   - |        - | FAULT
    0 |     7     0     - |   1   1   - |        - | FAULT
    1 |     7     0     1 |   1   1   1 |        - | FAULT
    2 |     2     0     1 |   1   1   1 |        7 | FAULT
    0 |     2     0     1 |   1   2   1 |        - | HIT
    3 |     2     0     3 |   1   2   1 |        1 | FAULT
    0 |     2     0     3 |   1   3   1 |        - | HIT
    4 |     4     0     3 |   1   3   1 |        2 | FAULT
    2 |     4     0     2 |   1   3   1 |        3 | FAULT
    3 |     3     0     2 |   1   3   1 |        4 | FAULT
    0 |     3     0     2 |   1   4   1 |        - | HIT
    3 |     3     0     2 |   2   4   1 |        - | HIT
    2 |     3     0     2 |   2   4   2 |        - | HIT
  Total page faults: 8
  Total page hits  : 5
  Page fault rate  : 61.54%
  Page hit rate    : 38.46%

COMPARISON
  Algorithm   Faults   Hits   Fault rate   Hit rate
  ---------   ------   ----   ----------   ---------
  FIFO          10      3       76.92%      23.08%
  LRU            9      4       69.23%      30.77%
  Optimal        7      6       53.85%      46.15%
  LFU            8      5       61.54%      38.46%
  Fewest page faults: Optimal (7).
```

The rates use the formulas: fault rate = faults / references x 100, hit rate = hits /
references x 100 (here 8/13 = 61.54%).

How LFU works on this string (frames = 3): after 7 0 1 all three frames have frequency 1, so
the faulting page 2 replaces 7 (the least recently used among the frequency-1 pages). The
next hit on 0 raises its frequency to 2, which protects it: when 3 arrives, the frequency-1
pages 2 and 1 compete and 1 (older) is evicted. Page 0 keeps being hit, so its frequency
grows to 4 and it is never replaced again.

To test:

- All four algorithms fault on the first 4 distinct pages (7 0 1 2).
- Optimal must have fewer or equal faults compared to the others (here 7 vs 9, 10 and 8).
- Faults + hits must equal the number of references for every algorithm
  (e.g. LFU: 8 + 5 = 13), and the two rates must add up to 100%.
- With 1 frame, the string 1 2 1 2 gives 4 faults (every reference faults).
- If frames are at least as many as distinct pages, faults = number of distinct pages.
- LFU tie-break: with all frequencies equal the victim is the least recently used page - use
  the string 1 2 3 1 2 3 with 3 frames and watch which page the next fault removes.

### Disk scheduling (DiskScheduler.java)

FCFS serves the queue in arrival order, SSTF always jumps to the nearest unserved request,
SCAN moves in one direction, ALWAYS travels on to the disk boundary (even when the other
side is empty), then reverses and services the rest. C-SCAN does the same but jumps from
one boundary to the other and keeps going in the original direction (it never reverses).
LOOK is SCAN without the trips to the boundary: it reverses at the last pending request of
the current direction. C-LOOK jumps from the last request of the first leg directly to the
farthest pending request on the other side and continues in the original direction - it
never travels to the physical boundaries.

Assumptions: cylinders are 0..max, SSTF ties go to the lower cylinder, a request equal to
the current head position costs 0 (it is shown as "50 (at head)"), and the circular jumps
of C-SCAN (boundary to boundary) and C-LOOK (request to request) are counted as head
movement - the summary says so because some books leave them out. When nothing is left to
service, SCAN/C-SCAN stop at the boundary and LOOK/C-LOOK stop at the last request.

Sample input (samples/disk.txt):

```
199                     (highest cylinder, so the disk is 0..199)
53                      (initial head position)
8                       (number of requests)
98 183 37 122 14 124 65 67
1                       (start direction: 1 = toward higher cylinders)
```

The sub-menu:

```
DISK SCHEDULING

  1) FCFS
  2) SSTF   (Shortest Seek Time First)
  3) SCAN   (elevator)
  4) C-SCAN (circular elevator)
  5) LOOK   (SCAN without the trip to the boundary)
  6) C-LOOK (circular LOOK)
  7) Compare all six on the same requests
  0) Back to main menu
  Choose an option: 7
```

Output (compare all six):

```
Highest cylinder number (disk = 0..max): 199
Initial head position (0..199): 53
Number of pending requests: 8
Request queue (8 cylinder number(s), space separated): 98 183 37 122 14 124 65 67
Initial direction for SCAN / C-SCAN / LOOK / C-LOOK (1 = toward higher cylinders, 2 = toward lower): 1
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

LOOK
  Order of service : 53 -> 65 -> 67 -> 98 -> 122 -> 124 -> 183 -> 37 -> 14
  Total head movement: 299 cylinders

C-LOOK
  Order of service : 53 -> 65 -> 67 -> 98 -> 122 -> 124 -> 183 -> 14 (jump) -> 37
  Total head movement: 322 cylinders

SUMMARY (total head movement)
  FCFS  : 640 cylinders
  SSTF  : 236 cylinders
  SCAN  : 331 cylinders
  C-SCAN: 382 cylinders  (includes the return jump - some books exclude it)
  LOOK  : 299 cylinders
  C-LOOK: 322 cylinders  (includes the jump - some books exclude it)
  Smallest movement in this run: 236 cylinders (SSTF (Shortest Seek Time First)).
```

To test:

- FCFS 640 = 45+85+146+85+108+110+59+2 (the absolute differences along the queue).
- SSTF 236, SCAN 331 = (199-53) + (199-14), C-SCAN 382 = 146 + 199 + 14 + 23.
- LOOK 299 = 130 (up to 183) + 169 (183 down to 14) - no trip to 199 or 0.
- C-LOOK 322 = 130 + 169 (the jump 183 -> 14) + 23 (14 -> 37).
- With direction 2 (toward lower), SCAN must start 53 -> 37 -> 14, touch 0 and come back.
- All requests on ONE side (e.g. disk 0..100, head 10, requests 30 50 70, direction 1):
  SCAN/C-SCAN must end at "100 (end of disk)" (movement 90), while LOOK/C-LOOK stop at 70
  (movement 60) - this is the edge-versus-last-request difference.
- A request equal to the head (e.g. head 50 and50 in the queue) appears once as
  "50 (at head)" and adds 0 to the movement.
- A request outside 0..199 is rejected and the data is asked for again.

### Multiprogramming (MultiprogrammingSimulator.java)

Several jobs share one memory, one CPU and one I/O device:

- Every job has an **arrival time**. Before that tick it does not exist for the OS (shown in
  the "not arrived" column); at its arrival time it enters the JOB QUEUE.
- Jobs leave the JOB QUEUE when memory fits them (first-fit, contiguous).
- Loaded jobs wait in the READY queue; the CPU runs them Round Robin with a quantum.
- There is exactly ONE I/O device: a job that reaches the halfway point of its burst leaves
  the CPU and either takes the free device or lines up in the I/O queue. Only one job is on
  the device at a time; the "I/O device" column always shows who is using it and for how
  many ticks.
- Whenever the layout changes, a "memory:" line prints every occupied block and hole with
  addresses and sizes, the total free memory and the largest hole - and warns when external
  fragmentation applies (a job waiting for memory would fit in the total free space but not
  in any single hole).
- The final report shows arrival, memory, burst, load time, completion, turnaround and
  waiting per job, plus total simulation time, averages, CPU utilization and how many jobs
  finished.

Definitions used in the report: turnaround = finished at - arrival time; waiting counts the
ticks where the job used neither the CPU nor the I/O device (job queue, ready queue or I/O
queue). CPU utilization = busy ticks / total ticks.

Sample input (samples/multiprogramming.txt): 100 KB, four jobs with different arrival
times, quantum 2:

```
J1: 40 KB, burst 7, arrives 0
J2: 30 KB, burst 4, arrives 2
J3: 25 KB, burst 5, arrives 0
J4: 20 KB, burst 3, arrives 5
quantum: 2
```

Output (complete run; the indented "memory:" lines appear whenever the layout changes):

```
Simulation rules:
  - first-fit contiguous loading, memory = 100 KB
  - Round Robin on the CPU, quantum = 2 tick(s)
  - each job does ONE I/O of 3 ticks at the halfway point of its burst, one job at a time on the device
  - a job only exists for the OS once its arrival time is reached

Tick | CPU | READY (queue order) | WAITING | I/O device | JOB QUEUE | not arrived | memory
-----+-----+---------------------+---------+------------+-----------+-------------+-------
0    | CPU: J1 (6 left) | READY: J3 | WAITING: - | I/O device: free | QUEUE: - | not arrived: J2@2, J4@5 | MEM: J1, J3 | J1 arrived -> JOB QUEUE; J3 arrived -> JOB QUEUE; J1 LOADED (40 KB at 0); J3 LOADED (25 KB at 40); J1 -> RUNNING
    memory: [J1 0-40, 40 KB] [J3 40-65, 25 KB] [free 65-100] | free total 35 KB, largest hole 35 KB
1    | CPU: J1 (5 left) | READY: J3, J1 | WAITING: - | I/O device: free | QUEUE: - | not arrived: J2@2, J4@5 | MEM: J1, J3 | J1 quantum expired -> READY
2    | CPU: J3 (4 left) | READY: J1, J2 | WAITING: - | I/O device: free | QUEUE: - | not arrived: J4@5 | MEM: J1, J3, J2 | J2 arrived -> JOB QUEUE; J2 LOADED (30 KB at 65); J3 -> RUNNING
    memory: [J1 0-40, 40 KB] [J3 40-65, 25 KB] [J2 65-95, 30 KB] [free 95-100] | free total 5 KB, largest hole 5 KB
3    | CPU: J3 (3 left) | READY: J1, J2, J3 | WAITING: - | I/O device: free | QUEUE: - | not arrived: J4@5 | MEM: J1, J3, J2 | J3 quantum expired -> READY
4    | CPU: J1 (4 left) | READY: J2, J3 | WAITING: - | I/O device: free | QUEUE: - | not arrived: J4@5 | MEM: J1, J3, J2 | J1 -> RUNNING
5    | CPU: J1 (3 left) | READY: J2, J3 | WAITING: J1 (I/O, 3 left) | I/O device: J1 (3 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J4 arrived -> JOB QUEUE; J1 starts I/O (3 ticks) - device in use
6    | CPU: J2 (3 left) | READY: J3 | WAITING: J1 (I/O, 2 left) | I/O device: J1 (2 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J2 -> RUNNING
7    | CPU: J2 (2 left) | READY: J3 | WAITING: J1 (I/O, 1 left), J2 (I/O queue) | I/O device: J1 (1 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J2 waits for the I/O device (in use by J1)
8    | CPU: J3 (2 left) | READY: J1 | WAITING: J2 (I/O, 3 left), J3 (I/O queue) | I/O device: J2 (3 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J3 -> RUNNING; J1 I/O finished - device free; J2 starts I/O (3 ticks) - device in use; J3 waits for the I/O device (in use by J2)
9    | CPU: J1 (2 left) | READY: - | WAITING: J2 (I/O, 2 left), J3 (I/O queue) | I/O device: J2 (2 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J1 -> RUNNING
10   | CPU: J1 (1 left) | READY: J1 | WAITING: J2 (I/O, 1 left), J3 (I/O queue) | I/O device: J2 (1 left) | QUEUE: J4 | not arrived: - | MEM: J1, J3, J2 | J1 quantum expired -> READY
11   | CPU: J1 (0 left) | READY: J2 | WAITING: J3 (I/O, 3 left) | I/O device: J3 (3 left) | QUEUE: J4 | not arrived: - | MEM: J3, J2 | J1 -> RUNNING; J2 I/O finished - device free; J3 starts I/O (3 ticks) - device in use; J1 FINISHED (memory freed)
    memory: [free 0-40] [J3 40-65, 25 KB] [J2 65-95, 30 KB] [free 95-100] | free total 45 KB, largest hole 40 KB
12   | CPU: J2 (1 left) | READY: J4 | WAITING: J3 (I/O, 2 left) | I/O device: J3 (2 left) | QUEUE: - | not arrived: - | MEM: J4, J3, J2 | J4 LOADED (20 KB at 0); J2 -> RUNNING
    memory: [J4 0-20, 20 KB] [free 20-40] [J3 40-65, 25 KB] [J2 65-95, 30 KB] [free 95-100] | free total 25 KB, largest hole 20 KB
13   | CPU: J2 (0 left) | READY: J4 | WAITING: J3 (I/O, 1 left) | I/O device: J3 (1 left) | QUEUE: - | not arrived: - | MEM: J4, J3 | J2 FINISHED (memory freed)
    memory: [J4 0-20, 20 KB] [free 20-40] [J3 40-65, 25 KB] [free 65-100] | free total 55 KB, largest hole 35 KB
14   | CPU: J4 (2 left) | READY: J3 | WAITING: - | I/O device: free | QUEUE: - | not arrived: - | MEM: J4, J3 | J4 -> RUNNING; J3 I/O finished - device free
15   | CPU: J4 (1 left) | READY: J3 | WAITING: J4 (I/O, 3 left) | I/O device: J4 (3 left) | QUEUE: - | not arrived: - | MEM: J4, J3 | J4 starts I/O (3 ticks) - device in use
16   | CPU: J3 (1 left) | READY: - | WAITING: J4 (I/O, 2 left) | I/O device: J4 (2 left) | QUEUE: - | not arrived: - | MEM: J4, J3 | J3 -> RUNNING
17   | CPU: J3 (0 left) | READY: - | WAITING: J4 (I/O, 1 left) | I/O device: J4 (1 left) | QUEUE: - | not arrived: - | MEM: J4 | J3 FINISHED (memory freed)
    memory: [J4 0-20, 20 KB] [free 20-100] | free total 80 KB, largest hole 80 KB
18   | CPU: idle | READY: J4 | WAITING: - | I/O device: free | QUEUE: - | not arrived: - | MEM: J4 | J4 I/O finished - device free
19   | CPU: J4 (0 left) | READY: - | WAITING: - | I/O device: free | QUEUE: - | not arrived: - | MEM: - | J4 -> RUNNING; J4 FINISHED (memory freed)
    memory: [free 0-100] | free total 100 KB, largest hole 100 KB

All jobs finished at tick 20.

Job     Arrival  Mem(KB)  Burst  Loaded at  Finished at  Turnaround  Waiting
------ -------- -------- ------ ---------- ------------ ----------- --------
J1            0       40      7          0           12          12        2
J2            2       30      4          2           14          12        5
J3            0       25      5          0           18          18       10
J4            5       20      3         12           20          15        9

Total simulation time  : 20 ticks
Average turnaround time: 14.25 ticks
Average waiting time   : 6.50 ticks
CPU utilization        : 95.00% (19 busy ticks out of 20)
Completed jobs         : 4 / 4

Note: turnaround = finished at - arrival time. Waiting time counts
the ticks spent in the job queue, the ready queue or the I/O queue;
time running and time actually on the I/O device are not waiting.
```

How to read the trace (and test the main cases):

- **Ticks 0-1:** J1 and J3 arrive at 0 and fit (40 + 25 <= 100). J2 and J4 are shown in
  "not arrived" until ticks 2 and 5 - the CPU cannot run them before then.
- **Tick 2:** J2 arrives, fits in the 35 KB hole at address 65, and the memory line shows
  every block with addresses.
- **Tick 7:** J2 reaches its halfway point but J1 is using the I/O device, so J2 goes to the
  I/O queue - the device column always shows exactly one job (or "free").
- **Tick 8:** J1's I/O finishes, J2 takes the device, and J3 also reaches its halfway point
  and queues behind J2 - two jobs waiting, but only one on the device.
- **Tick 11:** J1 finishes and frees 40 KB; **tick 12** is the first moment J4 (20 KB) fits
  - it loads at address 0, inside the freed block.
- **Tick 18:** the CPU is idle for one tick because J4 is finishing its I/O - the trace
  shows "CPU: idle" instead of pretending to run someone.
- Cross-check the numbers: J1's turnaround 12 = waiting 2 + burst 7 + I/O 3. CPU
  utilization 95% = 19 busy ticks (7+4+5+3) out of 20.
- All jobs arriving at time 0 still works (enter 0 for every arrival): the trace then looks
  like the older version with an empty "not arrived" column.
- Edge cases to try: a job bigger than memory is rejected while entering the data; a single
  job arriving at tick 4 shows idle CPU ticks 0-3; memory small enough for only one job at a
  time makes the others wait in the JOB QUEUE with the memory line showing one block.

## Handling wrong input

InputHelper catches bad values and asks again instead of stopping the program. Example
(samples/cpu_invalid_input.txt):

```
Your choice: oops
  [!] "oops" is not a whole number. Please try again.
Your choice: 1
...
How many processes? many
  [!] "many" is not a whole number. Please try again.
How many processes? 4
  CPU burst time for P1: 5
  CPU burst time for P2: -3
  [!] Please enter a whole number between 1 and 1000.
  CPU burst time for P2: 3
```

The same happens for menu choices, matrix rows with the wrong number of values, negative
sizes, arrival times below 0, disk requests outside the disk, Banker data where Max is
smaller than Allocation, and segments that overlap or fall outside RAM.

## Testing

Every sample input has the exact output it should print in samples/expected, so after
changing the code you can compare:

```
java -cp bin minios.Main < samples/cpu_fcfs.txt | diff - samples/expected/cpu_fcfs.txt
```

No differences means the run is correct. Doing that for all files in samples/ is a quick
regression test (there are 18 samples).

The main cases to check (they are also the ones used in the sample outputs above):

- CPU scheduling: FCFS averages 7.25 / 12.75, SJF waiting 6.25, Round Robin (quantum 4)
  10.75 / 16.25 for the 5 3 8 6 example; SRTF gives the Gantt chart
  `0-5 P1 | 5-7 P3 | 7-10 P2` with averages 5.00 / 1.67 / 1.67 (turnaround / waiting /
  response).
- Banker: the safe sample must give the sequence P2 -> P4 -> P1 -> P3 -> P5; Available
  1 1 2 gives UNSAFE with P1, P3, P5 left over; the request sample grants P2's 1 1 1
  (Available becomes 2 2 1) and denies P1's 0 2 1 with the values restored.
- Paging: 1500 translates to physical 3548, 2500 is a page fault, 9000 is invalid.
- Segmentation: (1,399) works, (1,400) faults, (5,10) does not exist, an overlapping
  segment is rejected and re-asked.
- Fragmentation: first-fit totals 180 KB on the 4-partition example; compare mode gives
  first-fit 80, best-fit 80, worst-fit 180 on the 3-partition example; the external example
  reports external fragmentation (300 free vs largest hole 200 vs request 250).
- Page replacement: FIFO 10, LRU 9, Optimal 7, LFU 8 faults; every row's faults + hits add
  up to 13 and the fewest-faults line names Optimal.
- Disk: 640 / 236 / 331 / 382 / 299 / 322 cylinders for FCFS / SSTF / SCAN / C-SCAN /
  LOOK / C-LOOK; one-sided SCAN stops at the boundary while LOOK stops at the last request.
- Multiprogramming: J4 loads only at tick 11, all jobs finished at tick 20, waits are
  2 / 5 / 10 / 9, CPU utilization 95.00%.
- Invalid input: typing letters or negative numbers prints a [!] message and asks again.

## Assumptions in one place

- CPU scheduling: options 1-4 use arrival time 0, SJF is non-preemptive, the RR queue starts
  in input order; SRTF uses arrival times, preempts on shorter remaining time, ties go to
  earlier arrival then lower P-number.
- Banker: non-negative values, Max >= Allocation, processes release everything when they
  finish; P1..Pn and R1..Rm labels; a request is granted only if the tentative allocation
  passes the same safety algorithm, otherwise every value is restored.
- Paging: page size = frame size; frame -1 means the page is on disk; logical and physical
  sizes are computed as long.
- Segmentation: RAM is entered before the segments so each one is checked for range and
  overlap while it is entered; the address is given as (segment, offset).
- Fragmentation: first-fit / best-fit / worst-fit with lowest-number/lowest-address as the
  tie-break; fixed partitions for internal, one contiguous block (no compaction) for
  external; comparison mode replays identical data on separate copies.
- Page replacement: frames start empty; Optimal looks into the future on purpose; LFU
  resets a page's counter to 1 on load and breaks frequency ties with LRU.
- Disk: SSTF ties go to the lower cylinder; SCAN/C-SCAN always touch the disk boundary;
  LOOK/C-LOOK never do; the C-SCAN and C-LOOK jumps are counted in the total movement.
- Multiprogramming: jobs arrive at their entered times, loaded contiguously first-fit, one
  CPU, ONE I/O device shared by a queue, one I/O of 3 ticks per job at half its burst,
  abstract ticks; waiting excludes time running and time on the device; a 10000-tick safety
  limit stops a runaway simulation.
- Wrong input never crashes the program; running out of piped input ends with a goodbye
  message.

## Note

This is a class project that demonstrates selected OS concepts only. It is not a real
operating system: no real processes, memory or disks are involved, and where textbooks
disagree on a convention (for example counting the C-SCAN jump, or whether SCAN travels to
an empty boundary), the choice is written down in the code comments above.
