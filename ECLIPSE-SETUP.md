# Putting the project into Eclipse, step by step

This guide starts from a fresh download and ends with the GUI running, the
tests passing, and a JAR you can double-click-free run from a terminal. It
assumes Windows; the only difference on macOS or Linux is the folder paths
in steps 2 and 5.

What you are importing:

| Item | Count | Where |
|------|-------|-------|
| Console classes | 11 | `src/minios/` |
| GUI classes | 13 | `src/minios/gui/` |
| Sample tests | 32 inputs + 32 expected outputs | `samples/`, `samples/expected/` |
| Runnable JAR | 1 | `MiniOS-Simulator.jar` |
| External libraries | none | only the JDK is needed |

There are four things you can run, and it helps to know which is which
before you start:

| Class | What it is |
|-------|------------|
| `minios.gui.GuiMain` | the GUI application (the one you demonstrate) |
| `minios.Main` | the console menu version, same engine |
| `minios.SampleTestRunner` | runs all 32 samples and reports pass or fail |
| `minios.gui.GuiInputSelfTest` | checks the GUI input of all 8 tabs |

---

## Step 1: Delete the old copy first

The project name inside `.project` is `Mini_OS-Project`. Eclipse refuses to
import a second project with the same name, so clear the old one out.

1. In Eclipse, right-click `Mini_OS-Project` in Package Explorer.
2. Choose **Delete**.
3. Tick **Delete project contents on disk (cannot be undone)** so the old
   `bin/` folder disappears too. Leaving it behind is what causes stale
   classes later.
4. Also delete any old project folder lying around in Downloads, the
   Desktop, or your Eclipse workspace folder.

## Step 2: Get the new code

**Option A, download a ZIP (no tools needed)**

1. Open <https://github.com/jroldan9723-gif/Mini_OS-Project>
2. Press the green **Code** button, then **Download ZIP**.
3. Right-click the downloaded `Mini_OS-Project-main.zip` and choose
   **Extract All**.
4. Extract it somewhere without spaces in the path, for example
   `C:\Users\<your name>\Documents\Mini_OS-Project`.

**Option B, git clone (only if git is installed)**

```
git clone https://github.com/jroldan9723-gif/Mini_OS-Project.git
```

Either way, step 3 is the one people skip and then wonder why the import
finds nothing.

## Step 3: Check the folder you are about to import

Open the extracted folder and look inside. The project root is the folder
that directly contains all of these:

```
.project
.classpath
src
samples
README.md
MiniOS-Simulator.jar
```

A ZIP download usually creates one extra wrapper folder
(`Mini_OS-Project-main`). If the folder you open contains only a single
folder, go one level deeper; that inner folder is the project root.

This is exactly the folder Eclipse wants as the "root directory" in the
next step, and the wrong level produces the message
**"No projects are found to import"**.

## Step 4: Import into Eclipse

1. **File > Import...**
2. **General > Existing Projects into Workspace**, then **Next**.
3. **Select root directory**, press **Browse**, and choose the project root
   from step 3.
4. `Mini_OS-Project` appears ticked in the Projects box. Press **Finish**.
5. If the project is listed but greyed out with *"already exists in
   workspace"*, you skipped step 1.

Package Explorer should now show `src` with the two packages `minios` and
`minios.gui`, plus `samples` and `MiniOS-Simulator.jar`.

## Step 5: Point the project at one JDK (detailed)

This is the step that prevents the `UnsupportedClassVersionError` you hit
before. Read the next paragraph once; it explains why the step has the
shape it has.

### 5.0 Why this step exists

Three different Java versions can be in play at the same time:

| Place | What it decides |
|-------|-----------------|
| The JDK that runs Eclipse itself | which language the code editor and compiler support |
| The JRE System Library on the project's build path | which class library your code is written against |
| The Compiler compliance level | the version stamped into the `.class` files |

The version stamp is a number inside every compiled file. A runtime
refuses to load a class stamped newer than itself, which is exactly the
message you saw:

```
java.lang.UnsupportedClassVersionError: minios/Main
has been compiled by a more recent version of the Java Runtime
(class file version 65.0), this version of the Java Runtime
only recognises class file versions up to 61.0
```

Read it as *"the class was built by Java 21 (65) but is being run by
Java 17 (61)"*. Here is the number table so you can decode any such
message:

| Java version | Class file version |
|--------------|--------------------|
| 8 | 52 |
| 11 | 55 |
| 17 | 61 |
| 21 | 65 |
| 25 | 69 |

This project uses no API newer than Java 8, so any of these works. Pick
one version and use it in all three places.

### 5.1 Find out what you have installed

1. Open a terminal (Windows: press Start, type `cmd`, press Enter).
2. Type:

```
java -version
```

3. Write down the number in the first line, for example
   `openjdk version "17.0.11"`. That is the Java your terminal will use
   to run `java -jar`, so the project must not be compiled to anything
   higher than that.
4. Now type:

```
where java
```

   (macOS or Linux: `which java`). This prints the full path of the
   runtime, for example
   `C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe`.
   The folder two levels up from `bin` is the JDK home you will need in
   step 5.3.

If `java -version` says *command not found*, install a JDK first
(Adoptium Temurin 17 is a safe choice), then reopen the terminal so the
new `PATH` is picked up.

### 5.2 Check which Java Eclipse itself is running on

1. In Eclipse: **Help > About Eclipse IDE**.
2. Press the **Installation Details** button.
3. Open the **Configuration** tab.
4. In the long property list, find `java.version` and `java.home`.
   That is the JVM Eclipse is running on.

If this number is older than the JDK you found in 5.1, that is fine as
long as Eclipse's JVM is at least as new as the compliance level you
choose. A 2022 or newer Eclipse needs Java 11 or 17; it will not start
on Java 8. In practice: pick the JDK from 5.1, and pick a compliance
level that is no higher than either one.

### 5.3 Register the JDK in Eclipse

1. **Window > Preferences** (macOS: **Eclipse > Settings**).
2. Open **Java > Installed JREs**.
3. Look for an entry matching the JDK home from 5.1.
   * If it is there, tick its checkbox. Exactly one entry should be
     ticked, because the ticked one becomes the workspace default.
   * If it is missing, continue with the next step.
4. Press **Add...**, choose **Standard VM**, press **Next**.
5. Press **Directory...** and select the JDK home folder, for example
   `C:\Program Files\Eclipse Adoptium\jdk-17`. Select the folder that
   directly contains `bin` and `lib`, **not** the `bin` folder itself.
6. The **JRE name** box fills in by itself. Press **Finish**.
7. Tick the new entry, press **Apply and Close**.

Common mistake: pointing at `jdk-17\bin` instead of `jdk-17`. Eclipse
then reports *"The JRE home entry is not a valid JRE home"*, because it
cannot find `lib` in that folder.

### 5.4 Point the project's build path at that JDK

The repository's `.classpath` contains a bare
`org.eclipse.jdt.launching.JRE_CONTAINER` entry, which means *"use
whatever JRE this workspace considers current"*. You are about to make
that explicit for the project.

1. Right-click the project in Package Explorer, choose **Properties**.
2. Open **Java Build Path**, then the **Libraries** tab.
3. Find the entry named **JRE System Library**. The text after it tells
   you what it uses, for example `[jdk-17]`, `[JavaSE-17]`, or
   `[JRE] (unbound)`.
4. Select it and press **Edit...**.
5. Choose **Alternate JRE** and pick the JDK from step 5.3.
   If you prefer a version-independent setting that survives moving the
   project to another PC, choose **Execution environment** and pick the
   matching entry, for example `JavaSE-17`.
6. Press **Finish**. A red cross or the word *unbound* should disappear
   from the entry.
7. Tick the box **"Use compliance from execution environment on the Java
   Build Path"** if it is visible and not already ticked. With this box
   ticked, Eclipse takes the compiler level from the entry you just
   fixed, so the two can no longer disagree.

Leave this dialog open; the next step is in the same window.

### 5.5 Set the compiler compliance level

1. Still in **Properties**, open **Java Compiler** in the left-hand list.
2. Look at **Compiler compliance level**. Two cases:

   **Case A, the checkbox is ticked and greyed out.** The box
   *"Use compliance from execution environment on the Java Build Path"*
   is in charge, and it already follows step 5.4. Check that the level
   shown matches your JDK (17 for a jdk-17) and skip to 5.7.

   **Case B, the checkbox is clear or editable.** Then:
   1. Tick **Enable project specific settings**. Eclipse creates a
      `.settings/org.eclipse.jdt.core.prefs` file in the project so the
      choice travels with it.
   2. Untick **"Use compliance from execution environment on the Java
      Build Path"** if it is ticked and blocking the fields.
   3. Set **Compiler compliance level** to your JDK's number, for
      example **17**.
   4. Let Eclipse adjust **Source compatibility** and
      **Target compatibility** automatically; they follow the compliance
      level. If they do not, set both to the same number.

3. Ignore the *"Configure Project Specific Settings"* yellow warning
   about deprecated options unless it names a version you chose
   deliberately (Java 8 shows this warning on newer Eclipse builds; if
   you see it, use 11 or 17 instead).
4. Press **Apply and Close**.

### 5.6 If the fields will not accept your number

Some Eclipse builds cap the compliance level at the JDK that is running
Eclipse, not at the JDK you added.

* Choosing a level higher than Eclipse's own JVM is not supported. Lower
  it, or start Eclipse with a newer JDK: edit `eclipse.ini` in the
  Eclipse folder and put

  ```
  -vm
  C:\Program Files\Eclipse Adoptium\jdk-17\bin\javaw.exe
  ```

  on two separate lines, **above** the `-vmargs` line. Restart Eclipse.
* Choosing 1.4 or 1.5 on a modern Eclipse also fails, because those
  releases are no longer supported. Use 1.8 as the floor.

### 5.7 Force a rebuild

Settings only affect code compiled afterwards, and `bin/` still holds the
old classes.

1. **Project > Clean...**
2. Tick `Mini_OS-Project`, press **Clean**.
3. Wait for the progress bar to finish, then look at the **Problems**
   view: it should be empty.

If it still shows *"The project was not built since its build path is
incomplete"*, the JRE entry is still unbound; go back to 5.4.

### 5.8 Verify the setting really took effect

Do not assume; check the stamp Eclipse wrote.

1. Open the **Console** view and start a terminal there, or use the
   Windows terminal in the project folder.
2. Run (this tool ships with every JDK, in its `bin` folder):

```
javap -verbose -cp bin minios.Main | findstr "major version"
```

On macOS or Linux:

```
javap -verbose -cp bin minios.Main | grep "major version"
```

3. The answer must match your chosen level from the table in 5.0:
   `major version: 52` for Java 8, `55` for 11, `61` for 17, `65` for 21.
4. Now confirm the runtime that will launch the app is not older than
   that: `java -version` from 5.1.

If the stamp is higher than the runtime, Eclipse compiled with a newer
JDK than the one on your `PATH`. Fix it by pointing 5.4 at the *older*
JDK, or by putting the newer JDK first on `PATH`.

### 5.9 What "correct" looks like when you are done

| Check | Expected |
|-------|----------|
| Preferences > Java > Installed JREs | exactly one entry ticked, matching your JDK home |
| Java Build Path > Libraries | `JRE System Library [jdk-17]` or `[JavaSE-17]`, no *unbound* |
| Java Compiler | compliance 17 (or your number), ticked project settings |
| Problems view after Clean | no red crosses and no yellow warnings from `src` |
| `javap` stamp | matches the table in 5.0 |
| `java -version` in the terminal | same number or newer |

From that point, running `GuiMain` inside Eclipse and running
`java -jar MiniOS-Simulator.jar` from a terminal use compatible
bytecode, and the class version error cannot come back after a Clean.

## Step 6: Clean build

1. **Project > Clean...**, tick `Mini_OS-Project`, press **Clean**.
2. Wait for the progress bar in the bottom right to finish.
3. Look at the **Problems** view. There should be no red crosses.

If the project has red crosses, the build path is still wrong: right-click
the project, **Build Path > Configure Build Path...**, and repeat step 5.

The sources are plain ASCII, so you do not need to touch the text file
encoding. If you ever see odd characters in comments, set
**Properties > Resource > Text file encoding > UTF-8** as a precaution.

## Step 7: Run the GUI

1. Expand `src > minios > gui`.
2. Right-click `GuiMain.java` and choose **Run As > Java Application**.
3. A window titled *Mini OS Simulator - educational demonstration (not a
   real OS)* opens with eight tabs.
4. On any tab press **Load example**, then **Run**. The report appears in
   the lower pane.

The first launch may show a list of main classes; pick `GuiMain` and press
**OK**. Eclipse remembers the choice afterwards.

## Step 8: Run the console version

1. Right-click `src/minios/Main.java`, choose **Run As > Java
   Application**.
2. The Console view shows the main menu. **Click inside the Console view
   first**, then type `1` and press **Enter**. Typing only reaches the
   program while the Console view has focus.

To play back a whole sample file instead of typing:

1. **Run > Run Configurations...**
2. Select **Java Application > Main**.
3. Open the **Common** tab on the right.
4. Under **Standard Input and Output** tick **Input File**, choose
   **File System**, and browse to for example
   `samples\cpu_srtf.txt`.
5. **Apply**, then **Run**.

The full session runs by itself and the transcript scrolls past. Untick
**Input File** again for normal interactive use, otherwise every launch
will replay the same file.

If your Eclipse version has no **Input File** box, use a terminal from the
project folder instead:

```
java -cp bin minios.Main < samples\cpu_srtf.txt
```

## Step 9: Run the 32-sample regression test

1. Right-click `src/minios/SampleTestRunner.java`, choose **Run As > Java
   Application**.
2. The Console ends with:

```
Result: 32 passed, 0 failed, 32 total.
```

What it does: for every file in `samples/`, it starts a fresh Java process,
feeds the file as standard input to `minios.Main`, and compares the output
with the matching file in `samples/expected/`. A `FAIL` line names the
sample that changed, which is handy after you edit an algorithm.

Two things to know:

* It needs the working directory to be the project folder. Eclipse uses
  that by default. If you see *"Run this from the project folder"*, open
  **Run Configurations > SampleTestRunner > Arguments** tab and set
  **Working directory > Other > Workspace...** to `Mini_OS-Project`.
* Exit code 1 means at least one sample failed; that is a real difference,
  not a setup problem.

## Step 10: Run the GUI input self test

1. Right-click `src/minios/gui/GuiInputSelfTest.java`, choose **Run As >
   Java Application**.
2. Expect 11 `PASS` lines followed by `ALL GUI CHECKS PASSED`.

It builds each tab's input the same way the Run button does and feeds it
to the real module, without opening a window. If a window or a warning
dialog appears during this test, something is wrong.

## Step 11: Run the JAR (the version you hand in)

Open a terminal in the project folder and run:

```
java -jar MiniOS-Simulator.jar
```

The JAR is a snapshot of the code at export time. Editing a `.java` file
in Eclipse does **not** change the JAR, so repeat step 12 whenever you
change something you want in the submitted JAR.

## Step 12: Export a fresh JAR after your changes

1. **Project > Clean...** so `bin/` matches the current sources.
2. **File > Export... > Java > Runnable JAR file > Next**.
3. **Launch configuration:** `GuiMain - Mini_OS-Project`.
4. **Export destination:** `MiniOS-Simulator.jar` inside the project folder
   (overwrite the old one).
5. **Finish**, and confirm the overwrite warnings.
6. Test it from a terminal: `java -jar MiniOS-Simulator.jar`.

If the export refuses to overwrite because the JAR is in use, close any
running copy of the application first.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `No projects are found to import` | wrong folder chosen in step 4 | go one level deeper, pick the folder that contains `.project` |
| Project greyed out, *already exists in workspace* | old copy not deleted | do step 1, then import again |
| `UnsupportedClassVersionError`, *class file version 65.0* | compiled with a newer JDK than the JRE running it | step 5.0 to decode the numbers, 5.4 to repoint, 5.8 to confirm |
| Red crosses right after import | JRE library unbound | step 5.3 then 5.4, then **Project > Clean** |
| `Selection does not contain a main type` | file not on the build path, or `bin/` out of date | **Project > Clean**, then run again; if it persists, redo step 5.4 |
| GUI opens but a tab looks empty | old JAR or stale classes | rerun from `GuiMain.java`, and Clean the project |
| Console does not accept typing | Console view has no focus | click inside the Console view, then type |
| `Run this from the project folder` | wrong working directory | step 9, **Arguments** tab |
| `Result: 31 passed, 1 failed` | you changed an algorithm or a message | compare the named sample against `samples/expected/`, then regenerate that expected file if the change is intended |
| JAR runs old behaviour | JAR not re-exported | step 12 |
| `Error: Unable to access jarfile` | wrong folder, or a typo in the name | run from the folder that contains the JAR, or use the full path |

## Files that must stay together

`SampleTestRunner` looks for `samples/` relative to the working directory,
and `GuiInputSelfTest` does not depend on any file at all. Keep this layout
if you move the project:

```
Mini_OS-Project/
  src/                  source code
  samples/              32 inputs
  samples/expected/     32 expected outputs
  MiniOS-Simulator.jar  runnable GUI
```

Copying only `src/` somewhere else still lets the GUI run, but the
regression test will report that it cannot find the samples.

## Final checklist before you present

1. Project imported, **Problems** view empty.
2. `GuiMain` runs and all eight tabs respond to Load example then Run.
3. `SampleTestRunner` prints `32 passed, 0 failed`.
4. `GuiInputSelfTest` prints `ALL GUI CHECKS PASSED`.
5. `java -jar MiniOS-Simulator.jar` opens the same GUI.
6. `samples/` and `samples/expected/` are both still present.
