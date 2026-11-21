# Putting the new code into Eclipse

This is only about one thing: replacing the code inside the project you
already have in Eclipse, so it matches the newest version on GitHub. You
do **not** delete the project and you do **not** import it again.

Takes about two minutes. Follow the numbers in order.

---

## Step 1: Find the folder your Eclipse project lives in

Eclipse does not keep a copy inside itself; it points at a normal folder
on your disk. You need that folder's address.

1. In Package Explorer (the tree on the left), right-click
   **Mini_OS-Project**.
2. Choose **Properties**.
3. Click **Resource** in the list on the left.
4. Look at the line **Location**. It looks like
   `C:\Users\you\eclipse-workspace\Mini_OS-Project`.
5. Press **Cancel**. Do not change anything in that window.

Write that path down, or keep the window open. This is the folder you
will paste into.

Handy alternative: right-click the project > **Show In** > **System
Explorer**. Windows opens the same folder for you.

If you are not sure you are in the right place: the correct folder
contains `src`, `samples`, `.project` and `MiniOS-Simulator.jar`.

## Step 2: Download the new version

1. Open <https://github.com/jroldan9723-gif/Mini_OS-Project>
2. Green **Code** button > **Download ZIP**.
3. Find the download, right-click it > **Extract All...** > **Extract**.
4. Open the folder you just extracted.

The extracted folder contains one folder inside it, usually called
`Mini_OS-Project-main`. **Open that one.** It is the folder with
`.project` and `src` in it.

## Step 3: Copy everything from it

1. In the extracted folder, press **Ctrl+A** to select everything.
2. **Ctrl+C** to copy.
3. Go to the folder from Step 1 and open it.
4. **Ctrl+V** to paste.
5. Windows asks what to do with the files that already exist. Choose
   **Replace the files in the destination**.
6. If it says a folder cannot be replaced, tick **Do this for all
   current items** and press **Yes**.

That is the whole update. Nothing in Eclipse has changed yet, because
Eclipse is still showing its old memory of those files.

> If you edited any of the source files yourself and want to keep your
> changes, copy those files out first. This paste overwrites them.

## Step 4: Tell Eclipse to look again

Eclipse does not watch the folder for changes from outside.

1. Click the **Mini_OS-Project** folder in Package Explorer.
2. Press **F5** on the keyboard. (Same as right-click > **Refresh**.)

The new files appear in the tree, including a `screenshots` folder.

## Step 5: Rebuild

1. **Project** menu > **Clean...**
2. Tick **Mini_OS-Project**.
3. Press **Clean**.

Eclipse throws away the old compiled classes in `bin` and compiles the
new sources. The **Problems** view at the bottom should be empty when it
finishes.

## Step 6: Check the new code is really in there

1. Expand `src > minios > gui` and open `GuiMain.java`.
2. Near the top you should see a comment starting
   `// The window that opens when the program starts.`
3. Right-click the file > **Run As** > **Java Application**.
4. In the window, look at tab 1: the **Time quantum** row is greyed out
   when the algorithm is SRTF. That grey row only exists in the new
   version.
5. Press **Load example**, then **Run**. Results appear in the lower pane.

If you see the grey row and the results, the update worked.

## Step 7: The steps to run it every time

Once the project is in Eclipse, you run things from Package Explorer.
There are four programs worth knowing, and they all start the same way:
**right-click the `.java` file > Run As > Java Application**.

### 7.1 The GUI (this is the one you demonstrate)

1. Expand `src > minios > gui`.
2. Right-click **GuiMain.java** > **Run As** > **Java Application**.
3. Pick a tab along the top.
4. Press **Load example** to fill the form, or type your own numbers.
5. Press **Run**. The report appears in the Results pane.

The first launch may ask which main class to use; pick `GuiMain` and
press **OK**. Eclipse remembers it after that.

### 7.2 The console version (same calculations, text menu)

1. Right-click **src/minios/Main.java** > **Run As** > **Java
   Application**.
2. **Click inside the Console view first.** Typing only reaches the
   program while the Console has focus; without it, nothing happens and
   it looks frozen.
3. Type `1`, press Enter, and follow the prompts.

To replay a whole sample file instead of typing it:

1. **Run > Run Configurations... > Java Application > Main**
2. Open the **Common** tab.
3. Under **Standard Input and Output**, tick **Input File**.
4. **File System...** and pick e.g. `samples/cpu_srtf.txt`.
5. **Apply**, then **Run**.

Untick **Input File** afterwards, or every run replays that same file.

### 7.3 The regression test (all 36 samples)

1. Right-click **src/minios/SampleTestRunner.java** > **Run As** >
   **Java Application**.
2. The Console ends with `Result: 36 passed, 0 failed, 36 total.`

It needs the project folder as the working directory, which Eclipse uses
by default. If you see *"Run this from the project folder"*, go to **Run
Configurations > SampleTestRunner > Arguments** and set **Working
directory > Other > Workspace > Mini_OS-Project**.

### 7.4 The GUI input test (all 8 tabs, no window)

1. Right-click **src/minios/gui/GuiInputSelfTest.java** > **Run As** >
   **Java Application**.
2. Expect 11 `PASS` lines and `ALL GUI CHECKS PASSED`.

No window should open. If one does, something is wrong.

### 7.5 The JAR, from a terminal

Open a terminal in the project folder (Step 1) and run:

```
java -jar MiniOS-Simulator.jar
```

The JAR is a copy of the code from when it was exported; editing a
`.java` file does not change it. To make a new one: **Project > Clean...**,
then **File > Export... > Java > Runnable JAR file > Next**, launch
configuration `GuiMain - Mini_OS-Project`, destination
`MiniOS-Simulator.jar` in the project folder, **Finish**.

---

## What actually changed, if you are curious

| Change | Where you notice it |
|--------|---------------------|
| Rows that the chosen option does not use are greyed out | all tabs, e.g. the quantum row on tab 1 |
| Results pane no longer prints instructions like "Enter the frame number..." | tabs 3 and 8 |
| Shorter, plainer comments in every class | any `.java` file |
| Four new boundary samples (36 in total now) | `samples/` |
| Screenshots of all 8 tabs | `screenshots/` |
| The JAR was rebuilt from this code | `MiniOS-Simulator.jar` |

---

## If you do not have the project in Eclipse at all

This is the only case where you import instead of paste.

1. Extract the ZIP as in Step 2 and open the folder that contains
   `.project`.
2. In Eclipse: **File > Import... > General > Existing Projects into
   Workspace > Next**.
3. **Select root directory** > **Browse** > pick that folder.
4. `Mini_OS-Project` appears ticked > **Finish**.
5. If there is a red cross on the project, right-click it >
   **Properties > Java Build Path > Libraries**, select **JRE System
   Library**, press **Edit...**, choose **Alternate JRE**, pick your JDK,
   **Finish > Apply and Close**, then **Project > Clean...**.

---

## When something goes wrong

| What you see | What it means | Fix |
|--------------|---------------|-----|
| The old behaviour is still there after Step 6 | Eclipse never saw the new files | repeat Step 4 (F5) and Step 5 (Clean) |
| *"already exists in workspace"* while importing | two projects with the same name | you do not need to import; just use Steps 1-6 on the existing one |
| Red crosses after Clean | the JRE library was never set for this project | the last step in the section above |
| `UnsupportedClassVersionError`, *class file version 65.0* | the project is compiled for a newer Java than the one running it | set the Java Build Path to your JDK (same section), then Clean |
| Rows are not greyed out | you are running an old build or the old JAR | **Project > Clean...**, then run `GuiMain` again |
| `java -jar MiniOS-Simulator.jar` shows the old screen | the JAR is fine; it was rebuilt already | run it from the folder from Step 1, not from an older copy |
| Console does not accept typing | the Console view is not focused | click inside the Console view first |

---

## Doing it again next time

Same six steps every time:

1. find the project folder (Step 1),
2. download and extract the ZIP (Step 2),
3. paste and replace (Step 3),
4. F5,
5. Clean,
6. run `GuiMain` and look (Step 7.1).

If you use git, Steps 2 and 3 are replaced by one command inside the
project folder:

```
git pull
```

Then do Steps 4-6 as usual.
