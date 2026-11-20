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

## Step 5: Point the project at one JDK

This is the step that prevents the `UnsupportedClassVersionError` you saw
before. That error appears when the code is compiled by a newer JDK than
the JRE that runs it. The repository's `.classpath` uses the Eclipse
workspace default JRE, so the fix is to make the compiler and the JRE the
same version.

1. **Window > Preferences > Java > Installed JREs**: exactly one JDK should
   be ticked, for example Adoptium **jdk-17**.
   If it is missing, press **Add... > Standard VM > Next**, press
   **Directory...**, and select `C:\Program Files\Eclipse Adoptium\jdk-17`
   (or wherever you installed it), then **Finish** and tick it.
2. Right-click the project, choose **Properties > Java Build Path >
   Libraries**. The entry **JRE System Library** should read
   `JavaSE-17` (or your version). If it does not, select it, press
   **Edit...**, choose the matching **Alternate JRE** or execution
   environment, then **Finish**.
3. In the same Properties window open **Java Compiler**. Tick
   **Enable project specific settings** and set **Compiler compliance
   level** to the same number, for example **17**. If the checkbox
   *"Use compliance from execution environment on the Java Build Path"* is
   ticked and greyed, untick it first.
4. Press **Apply and Close**.

The rule is simple: the compliance level and the JRE must carry the same
number. A mismatch is what produces lines such as

```
java.lang.UnsupportedClassVersionError: minios/Main
has been compiled by a more recent version of the Java Runtime
(class file version 65.0), this version ... recognises 61.0
```

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
| `UnsupportedClassVersionError`, *class file version 65.0* | compiled with a newer JDK than the JRE running it | step 5, then **Project > Clean** |
| Red crosses right after import | JRE library unbound | **Build Path > Configure Build Path**, step 5 |
| `Selection does not contain a main type` | file not on the build path, or `bin/` out of date | **Project > Clean**, then run again |
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
