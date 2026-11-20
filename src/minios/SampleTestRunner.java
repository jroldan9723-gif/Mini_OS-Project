package minios;

import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Runs every file in samples/ through the console program and compares the
// output with samples/expected/. One JVM per sample so saved state cannot
// leak from one test into the next.
public class SampleTestRunner {

    public static void main(String[] args) throws Exception {
        Path samplesDir = Paths.get("samples");
        Path expectedDir = Paths.get("samples", "expected");
        if (!Files.isDirectory(samplesDir) || !Files.isDirectory(expectedDir)) {
            System.out.println("Run this from the project folder (the one that contains samples/).");
            System.exit(2);
        }

        List<Path> inputs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(samplesDir, "*.txt")) {
            for (Path path : stream) {
                inputs.add(path);
            }
        }
        Collections.sort(inputs);

        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String javaExe = Paths.get(System.getProperty("java.home"), "bin",
                windows ? "java.exe" : "java").toString();
        String classpath = System.getProperty("java.class.path");

        int pass = 0;
        int fail = 0;
        for (Path input : inputs) {
            String name = input.getFileName().toString();
            Path expected = expectedDir.resolve(name);
            if (!Files.exists(expected)) {
                System.out.println("FAIL  " + name + "  (no expected file)");
                fail++;
                continue;
            }

            Path raw = Files.createTempFile("minios-out", ".txt");
            try {
                ProcessBuilder builder = new ProcessBuilder(javaExe, "-cp", classpath, "minios.Main");
                builder.redirectInput(input.toFile());
                builder.redirectErrorStream(true);
                builder.redirectOutput(raw.toFile());
                Process process = builder.start();
                int exit = process.waitFor();

                String got = normalize(new String(Files.readAllBytes(raw), StandardCharsets.UTF_8));
                String want = normalize(new String(Files.readAllBytes(expected), StandardCharsets.UTF_8));
                if (exit == 0 && got.equals(want)) {
                    System.out.println("PASS  " + name);
                    pass++;
                } else {
                    System.out.println("FAIL  " + name + (exit == 0 ? "" : "  (exit code " + exit + ")"));
                    fail++;
                }
            } finally {
                Files.deleteIfExists(raw);
            }
        }

        System.out.println();
        System.out.println("Result: " + pass + " passed, " + fail + " failed, "
                + inputs.size() + " total.");
        System.exit(fail == 0 ? 0 : 1);
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private SampleTestRunner() {
    }
}
