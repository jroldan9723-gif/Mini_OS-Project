# Mini_OS-Project

A finals project based on creating a mini operating system. This is a console program written
in Java that demonstrates how the most common OS algorithms work. You choose a topic from the
menu, enter the data it asks for, and the program shows the result.

Topics covered:

- CPU scheduling: FCFS, SJF, Round Robin (waiting time, turnaround time, averages)
- Banker's algorithm (safe / unsafe state and safe sequence)
- Paging (logical to physical address translation)
- Segmentation (base and limit translation, invalid addresses)
- Fragmentation (internal and external, with examples)
- Page replacement: FIFO, LRU, Optimal (page faults after every reference)
- Disk scheduling: FCFS, SSTF, SCAN, C-SCAN (head movement)
- Multiprogramming: several jobs sharing memory and one CPU

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
| CPUScheduler.java | CPUScheduler | FCFS, SJF and Round Robin, with waiting/turnaround tables and a compare option |
| BankersAlgorithm.java | BankersAlgorithm | Builds the need matrix and runs the safety algorithm |
| PagingSimulator.java | PagingSimulator | Page table setup and address translation |
| SegmentationSimulator.java | SegmentationSimulator | Segment table setup and address translation |
| FragmentationDemo.java | FragmentationDemo | Internal fragmentation with fixed partitions, external fragmentation with holes |
| PageReplacementSimulator.java | PageReplacementSimulator | FIFO, LRU and Optimal on the same reference string |
| DiskScheduler.java | DiskScheduler | FCFS, SSTF, SCAN and C-SCAN with total head movement |
| MultiprogrammingSimulator.java | MultiprogrammingSimulator | Jobs loaded first-fit into memory, Round Robin on the CPU, simulated I/O |

## How the program is organized

`Main` prints the menu and calls the class for the topic you chose. Every feature class has a
`run(InputHelper)` method that shows its own sub-menu (0 goes back to the main menu), reads
its data, runs the algorithm and prints the result. All input goes through `InputHelper`, so
a wrong value never crashes the program. Each algorithm is kept in its own method (for
example `CPUScheduler.fcfs(...)`, `DiskScheduler.sstf(...)`) so you can read them one by one.

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

- All processes arrive at time 0, so FCFS runs in the order entered and SJF just sorts by
  burst time.
- turnaround = completion - arrival, waiting = turnaround - burst.
- The Round Robin execution list shows one entry per time slice, so a process can appear more
  than once.

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

To test:

- FCFS waiting times should be 0, 5, 8, 16 (each process waits for all earlier bursts).
- SJF average waiting (6.25) must be lower than or equal to FCFS (7.25).
- With one process the waiting time is always 0.
- Round Robin with quantum 1 gives the longest possible execution list.

### Banker's algorithm (BankersAlgorithm.java)

How it works: Need = Max - Allocation. The safety algorithm pretends to run each process
whose need fits in the available resources; a finished process returns everything it held. If
all processes can finish, the system is safe and the order used is the safe sequence.

Assumptions: processes are P1..Pn, resources R1..Rm, all values are non-negative, Max is never
smaller than Allocation (this is checked), and a process releases all its resources when it
finishes.

Sample input (samples/banker_safe.txt):

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

Output (safe):

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

To test:

- The final work vector of the safe case [10,5,7] must equal Available + all allocations
  (3+7, 3+1+1, 2+2+2+2+... check the columns add up to 10, 5, 7).
- Try entering a Max row smaller than the Allocation row; the program should refuse it and
  ask for the data again.
- With all-zero matrices the result is safe (every need is 0).

### Paging (PagingSimulator.java)

How it works: page = address / pageSize, offset = address % pageSize, physical = frame *
pageSize + offset. Frame -1 means the page is not in memory (it would have to be loaded from
disk first).

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

How it works: the user gives a segment number and an offset. The offset must be smaller than
the segment limit; then physical = base + offset. A wrong segment number, an offset past the
limit, or a physical address outside RAM is reported as an invalid address / segmentation
fault.

Assumptions: the address is entered as two numbers (segment, offset), and the segments do not
overlap in memory.

Sample input (samples/segmentation.txt):

```
4            (number of segments)
segment 0: base 0,    limit 600
segment 1: base 1000, limit 400
segment 2: base 1800, limit 500
segment 3: base 3000, limit 700
RAM size: 4096
queries: (0,250), (1,400), (2,450), (5,10), (1,399), then -1
```

Output:

```
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

To test:

- (1,399) works and (1,400) faults, because the limit is 400 (valid offsets are 0..399).
- (5,10) must report that segment 5 does not exist.
- Set the RAM size to 3000 and translate segment 3 offset 699: the physical address 3699 is
  larger than RAM and must be reported as a fault.

### Fragmentation (FragmentationDemo.java)

**Internal fragmentation** is the space wasted inside an allocation. The demo places
processes into fixed-size partitions using first-fit (first free partition that is big enough)
and adds up the leftovers.

Sample input (samples/fragment_internal.txt):

```
4 partitions: 100 150 250 400 (KB)
4 processes:   80 140 200 300 (KB)
```

Output:

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

**External fragmentation** means there is enough free memory overall, but it is split into
holes that are each too small. The demo allocates and frees jobs in one block of memory,
prints the memory map after each step, and then checks a new request against the holes.

Sample input (samples/fragment_external.txt): memory 1000 KB, allocate A 400, B 100, C 300,
free B, then request 250 KB.

Output:

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

To test:

- Internal: 20 + 10 + 50 + 100 = 180 KB.
- External: a request of 150 fits in the 200 KB hole, a request of 250 does not (external
  fragmentation), and a request of 400 is simply more than the 300 KB free in total.
- Freeing a job that is not allocated prints an error message instead of crashing.

### Page replacement (PageReplacementSimulator.java)

FIFO evicts the oldest page, LRU evicts the page that was used least recently, and Optimal
evicts the page that will not be needed for the longest time (it looks ahead in the reference
string, which a real OS cannot do - it is used as the best possible result).

Sample input (samples/page_replacement.txt):

```
13                        (number of references)
7 0 1 2 0 3 0 4 2 3 0 3 2 (reference string)
3                         (frames)
```

Output:

```
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
```

To test:

- All three algorithms fault on the first 4 distinct pages (7 0 1 2).
- Optimal must have fewer or equal faults compared to the others (here 7 vs 9 and 10).
- With 1 frame, the string 1 2 1 2 gives 4 faults (every reference faults).
- If frames are at least as many as distinct pages, faults = number of distinct pages.

### Disk scheduling (DiskScheduler.java)

FCFS serves the queue in arrival order, SSTF always jumps to the nearest unserved request,
SCAN moves in one direction to the end of the disk and then reverses, C-SCAN wraps around to
the other end and keeps going the same way.

Assumptions: cylinders are 0..max, SSTF ties go to the lower cylinder, SCAN only travels to
the disk edge when there are still requests on the other side, and the C-SCAN return jump is
counted as movement (some books do not count it - the output says so).

Sample input (samples/disk.txt):

```
199                     (highest cylinder, so the disk is 0..199)
53                      (initial head position)
8                       (number of requests)
98 183 37 122 14 124 65 67
1                       (start direction for SCAN/C-SCAN: 1 = toward higher cylinders)
```

Output (option 5 runs all four):

```
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

To test:

- FCFS 640 = 45+85+146+85+108+110+59+2 (the absolute differences along the queue).
- SSTF 236, SCAN 331 = (199-53) + (199-14), C-SCAN 382 = 146 + 199 + 14 + 23.
- With direction 2 (toward lower), SCAN must start 53 -> 37 -> 14, touch 0 and come back.
- A request outside 0..199 is rejected and the data is asked for again.

### Multiprogramming (MultiprogrammingSimulator.java)

Several jobs share one memory and one CPU. Jobs wait in a job queue until memory fits them
(first-fit, contiguous), loaded jobs wait in the ready queue, the CPU runs them Round Robin
with a quantum, and each job leaves the CPU once for a short simulated I/O. The program prints
every job's state after each tick.

Simplifications:

- All jobs arrive at time 0 and are loaded contiguously (no paging here).
- One CPU, one simple I/O device; each job does one I/O of 3 ticks at the halfway point of
  its burst.
- In one tick the program: (1) loads jobs, (2) assigns the CPU, (3) progresses I/O,
  (4) runs one CPU tick.

Sample input (samples/multiprogramming.txt):

```
100 KB of memory
J1: 40 KB, burst 7
J2: 30 KB, burst 4
J3: 25 KB, burst 5
J4: 20 KB, burst 3
quantum: 2
```

Output:

```
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

To test:

- J1 + J2 + J3 = 95 KB, so J4 (20 KB) cannot be loaded at the start. It loads at tick 11,
  right after J2 finishes and frees its 30 KB (see MEM: J1, J4, J3).
- The ready column shows queue order: when J1's quantum expires at tick 1 it goes behind
  J2 and J3.
- Every job ends TERMINATED and the last tick shows MEM: - (all memory free).
- Try only 80 KB of memory: J3 no longer fits at the start and has to wait for a free slot.
- A job bigger than the whole memory is rejected while entering the data.

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
sizes, disk requests outside the disk, and Banker data where Max is smaller than Allocation.

## Testing

Every sample input has the exact output it should print in samples/expected, so after changing
the code you can compare:

```
java -cp bin minios.Main < samples/cpu_fcfs.txt | diff - samples/expected/cpu_fcfs.txt
```

No differences means the run is correct. Doing that for all files in samples/ is a quick
regression test.

The main cases to check (they are also the ones used in the sample outputs above):

- CPU scheduling: FCFS averages 7.25 / 12.75, SJF waiting 6.25, Round Robin (quantum 4)
  10.75 / 16.25 for the 5 3 8 6 example.
- Banker: the safe sample must give the sequence P2 -> P4 -> P1 -> P3 -> P5, and Available
  1 1 2 must give UNSAFE with P1, P3, P5 left over.
- Paging: 1500 translates to physical 3548, 2500 is a page fault, 9000 is invalid.
- Segmentation: (1,399) works, (1,400) faults, (5,10) does not exist.
- Fragmentation: 180 KB total internal fragmentation; the external example reports external
  fragmentation (300 free vs largest hole 200 vs request 250).
- Page replacement: FIFO 10, LRU 9, Optimal 7 for the sample string with 3 frames.
- Disk: 640 / 236 / 331 / 382 cylinders for FCFS / SSTF / SCAN / C-SCAN.
- Multiprogramming: J4 loads only at tick 11, all jobs finished at tick 19.
- Invalid input: typing letters or negative numbers prints a [!] message and asks again.

## Assumptions in one place

- CPU scheduling: all processes arrive at time 0; SJF is non-preemptive; RR queue starts in
  input order.
- Banker: non-negative values, Max >= Allocation, processes release everything when they
  finish; P1..Pn and R1..Rm labels.
- Paging: page size = frame size; frame -1 means the page is on disk.
- Segmentation: address given as (segment, offset); segments are already non-overlapping.
- Fragmentation: first-fit everywhere; fixed partitions for internal, one contiguous block
  (no compaction) for external.
- Page replacement: frames start empty; Optimal looks into the future on purpose.
- Disk: SSTF ties go to the lower cylinder; the C-SCAN jump is counted as movement.
- Multiprogramming: jobs arrive at 0, loaded contiguously, one CPU, one I/O per job of 3
  ticks at half its burst, abstract ticks.
- Wrong input never crashes the program; running out of piped input ends with a goodbye
  message.

## Note

This is a class project that demonstrates selected OS concepts only. It is not a real
operating system: no real processes, memory or disks are involved, and where textbooks
disagree on a convention (for example counting the C-SCAN jump), the choice is written down
in the code comments above.
