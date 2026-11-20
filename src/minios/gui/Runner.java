package minios.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import minios.InputHelper;

// Runs one console module on a scripted input and returns everything the
// module printed. This is how the GUI and the console menu share the exact
// same algorithm code: the GUI builds the same lines a user would type,
// the module answers on a captured stream, and the GUI shows that text.
public final class Runner {

    // A module is any of the console classes with a run(InputHelper) method.
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
            module.run(new InputHelper(new Scanner(script)));
        } catch (InputHelper.InputEndedException e) {
            failure = "The script ended before the module finished. Please report this input.";
        } catch (RuntimeException e) {
            failure = "Unexpected error: " + e;
        } finally {
            System.setOut(original);
            capture.flush();
            capture.close();
        }

        String output = buffer.toString();
        if (failure != null) {
            output = output + "\n[!] " + failure;
        }
        return output;
    }

    private Runner() {
    }
}
