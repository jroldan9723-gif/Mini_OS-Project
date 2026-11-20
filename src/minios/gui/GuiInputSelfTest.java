package minios.gui;

import minios.BankersAlgorithm;
import minios.CPUScheduler;
import minios.DiskScheduler;
import minios.FragmentationDemo;
import minios.MultiprogrammingSimulator;
import minios.PageReplacementSimulator;
import minios.PagingSimulator;
import minios.SegmentationSimulator;

// Fills every tab with its example data, builds the input, runs the real
// module and looks for an expected line. No window is opened, so it is quick
// to check before a demo.
public final class GuiInputSelfTest {

    public static void main(String[] args) {
        int failures = 0;
        failures += check("CPU scheduling", new CpuPanel(), CPUScheduler::run,
                "Average waiting time");
        failures += check("Paging", new PagingPanel(), PagingSimulator::run,
                "Page table in use");
        failures += check("Segmentation", new SegmentationPanel(), SegmentationSimulator::run,
                "Segment table in use");
        failures += check("Fragmentation", new FragmentationPanel(), FragmentationDemo::run,
                "Memory map:");
        failures += check("Page replacement", new PageReplacePanel(),
                PageReplacementSimulator::run, "Fewest page faults");
        failures += check("Disk scheduling", new DiskPanel(), DiskScheduler::run,
                "Total head movement");
        failures += check("Multiprogramming", new MultiPanel(), MultiprogrammingSimulator::run,
                "Average turnaround time");
        failures += bankerChecks();

        System.out.println(failures == 0
                ? "ALL GUI CHECKS PASSED"
                : failures + " GUI CHECK(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static int check(String name, ModulePanel panel, Runner.Module module, String marker) {
        try {
            panel.loadExample();
            String script = panel.buildScript();
            String output = Runner.run(module, script);
            if (output.contains(marker)) {
                System.out.println("PASS  " + name);
                return 0;
            }
            System.out.println("FAIL  " + name + " (marker not found: " + marker + ")");
            System.out.println(output);
            return 1;
        } catch (RuntimeException e) {
            System.out.println("FAIL  " + name + " (" + e + ")");
            return 1;
        }
    }

    private static int bankerChecks() {
        int failures = 0;
        BankerPanel panel = new BankerPanel();

        panel.loadExample();
        panel.mode.setSelectedIndex(1);
        failures += bankerStep(panel, "Banker request (first run)", "Request from P2");

        panel.mode.setSelectedIndex(0);
        failures += bankerStep(panel, "Banker safety check", "RESULT: SAFE STATE");

        panel.mode.setSelectedIndex(1);
        failures += bankerStep(panel, "Banker request (data loaded)", "the request is GRANTED");

        panel.available.setText("2 2 1");
        failures += bankerStep(panel, "Banker request (data changed)", "Request from P2");

        return failures;
    }

    private static int bankerStep(BankerPanel panel, String name, String marker) {
        try {
            String script = panel.buildScript();
            String output = Runner.run(BankersAlgorithm::run, script);
            if (output.contains(marker)) {
                System.out.println("PASS  " + name);
                return 0;
            }
            System.out.println("FAIL  " + name + " (marker not found: " + marker + ")");
            System.out.println(output);
            return 1;
        } catch (RuntimeException e) {
            System.out.println("FAIL  " + name + " (" + e + ")");
            return 1;
        }
    }

    private GuiInputSelfTest() {
    }
}
