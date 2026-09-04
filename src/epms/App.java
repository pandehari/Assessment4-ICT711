package epms;

import epms.data.EmployeeRepository;
import epms.data.FileStorage;
import epms.model.Employee;
import epms.ui.gui.MainWindow;
import epms.ui.text.TextInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Entry point. Lets the user pick the interface mode, loads the roster from the
 * default data file (creating a seeded one on first run), and hands control to
 * the chosen front-end.
 *
 * <pre>
 *   java epms.App             prompt for GUI or Text
 *   java epms.App --gui       start the Swing interface
 *   java epms.App --text      start the text interface
 *   java epms.App --data FILE use FILE instead of data/employees.csv
 * </pre>
 */
public final class App {

    private static final Path DEFAULT_DATA = Path.of("data", "employees.csv");

    public static void main(String[] args) {
        Mode mode = null;
        Path dataFile = DEFAULT_DATA;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--gui": mode = Mode.GUI; break;
                case "--text": case "--tbi": mode = Mode.TEXT; break;
                case "--data":
                    if (i + 1 >= args.length) {
                        System.err.println("--data needs a path argument");
                        return;
                    }
                    dataFile = Path.of(args[++i]);
                    break;
                case "--help": case "-h":
                    printUsage();
                    return;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsage();
                    return;
            }
        }

        EmployeeRepository repo = new EmployeeRepository();
        loadOrSeed(repo, dataFile);

        Scanner console = new Scanner(System.in);
        if (mode == null) {
            mode = askMode(console);
        }

        if (mode == Mode.GUI) {
            MainWindow.launch(repo);
        } else {
            new TextInterface(repo, console, System.out).run();
        }
    }

    private static Mode askMode(Scanner console) {
        System.out.println("Employee Performance Management System");
        System.out.println("Choose an interface:");
        System.out.println("  [G] Graphical (Swing)");
        System.out.println("  [T] Text-based");
        while (true) {
            System.out.print("> ");
            System.out.flush();
            String line = console.hasNextLine() ? console.nextLine().trim().toLowerCase() : "t";
            if (line.startsWith("g")) return Mode.GUI;
            if (line.startsWith("t") || line.isEmpty()) return Mode.TEXT;
            System.out.println("Please enter G or T.");
        }
    }

    private static void loadOrSeed(EmployeeRepository repo, Path dataFile) {
        try {
            if (Files.exists(dataFile)) {
                FileStorage.LoadReport report = repo.loadFrom(dataFile);
                System.out.println("Loaded " + report.getEmployees().size()
                        + " employees from " + dataFile.toAbsolutePath());
                for (String w : report.getWarnings()) {
                    System.out.println("  warning: " + w);
                }
            } else {
                seed(repo);
                repo.saveTo(dataFile);
                System.out.println("Created sample data file at " + dataFile.toAbsolutePath());
            }
        } catch (IOException ex) {
            System.err.println("Could not use data file (" + ex.getMessage()
                    + ") - starting with sample data in memory.");
            if (repo.isEmpty()) {
                seed(repo);
            }
        }
    }

    private static void seed(EmployeeRepository repo) {
        repo.add(new Employee(1, "Alice Thapa", "Engineering", 88.5, 6, 92000));
        repo.add(new Employee(2, "Bikash Rai", "Engineering", 74.0, 3, 68000));
        repo.add(new Employee(3, "Chandra Gurung", "Sales", 91.2, 9, 81000));
        repo.add(new Employee(4, "Deepa Shrestha", "Marketing", 65.7, 2, 57000));
        repo.add(new Employee(5, "Elina Karki", "Support", 58.3, 1, 44000));
        repo.add(new Employee(6, "Farhan Ali", "Sales", 79.9, 5, 73000));
        repo.add(new Employee(7, "Gita Lama", "Engineering", 95.0, 11, 105000));
        repo.add(new Employee(8, "Hari Poudel", "Marketing", 42.0, 1, 39000));
        repo.add(new Employee(9, "Ishaan Malla", "Support", 83.4, 4, 61000));
        repo.add(new Employee(10, "Jyoti Bista", "Sales", 70.1, 7, 76000));
    }

    private static void printUsage() {
        System.out.println("Usage: java epms.App [--gui | --text] [--data FILE]");
    }

    private enum Mode { GUI, TEXT }

    private App() {
    }
}
