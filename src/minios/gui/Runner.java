package minios.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import minios.InputHelper;

// Runs a module with the console prompts switched off, so the output box
// only gets the tables and the answers. This is also why the GUI and the
// console always agree: it is the same code doing the calculation.
public final class Runner {

    public interface Module {
        void run(InputHelper in);
    }

    public static String run(Module module, String script) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true);
        PrintStream original = System.out;
        String failure = null;

        try {
            System.setOut(capture);
            InputHelper.setQuiet(true);
            module.run(new InputHelper(new Scanner(script)));
        } catch (InputHelper.InputEndedException e) {
            failure = "The input ended before the calculation finished.";
        } catch (RuntimeException e) {
            failure = "Unexpected error: " + e;
        } finally {
            InputHelper.setQuiet(false);
            System.setOut(original);
            capture.flush();
            capture.close();
        }

        String output = buffer.toString().trim();
        if (failure != null) {
            output = output + "\n\n[!] " + failure;
        }
        return output;
    }

    private Runner() {
    }
}
