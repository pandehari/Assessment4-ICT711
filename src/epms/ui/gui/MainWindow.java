package epms.ui.gui;

import epms.algorithms.SortAlgorithm;
import epms.algorithms.SortResult;
import epms.data.EmployeeRepository;
import epms.data.FileStorage;
import epms.model.Employee;
import epms.model.EmployeeField;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Swing front-end. Presents the roster in a {@link JTable} and drives every
 * operation (add / edit / delete / search / sort / load / save) through the
 * shared {@link EmployeeRepository}, so its behaviour matches the text interface.
 *
 * <p>Actions are defined once as {@link Action} objects and reused by both the
 * menu bar and the toolbar. User feedback is delivered through {@link JOptionPane}
 * dialogs, the colour-coded rating column, and the two-part status bar.
 */
public class MainWindow extends JFrame {

    private final EmployeeRepository repo;
    private final EmployeeTableModel tableModel = new EmployeeTableModel();
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<EmployeeTableModel> rowSorter = new TableRowSorter<>(tableModel);

    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel countLabel = new JLabel();
    private final JTextField quickFilter = new JTextField(16);

    private final JComboBox<EmployeeField> sortFieldBox = new JComboBox<>(EmployeeField.values());
    private final JComboBox<SortAlgorithm> sortAlgoBox = new JComboBox<>(SortAlgorithm.values());
    private final JCheckBox descendingCheck = new JCheckBox("Descending");

    /** True while a search/query subset is shown instead of the whole roster. */
    private boolean subsetShown = false;

    /** True when the roster has unsaved changes since the last load/save. */
    private boolean dirty = false;

    private final Action addAction = action("Add", KeyEvent.VK_A, menuKey(KeyEvent.VK_N),
            "Add a new employee", e -> onAdd());
    private final Action editAction = action("Edit", KeyEvent.VK_E, menuKey(KeyEvent.VK_E),
            "Edit the selected employee", e -> onEdit());
    private final Action deleteAction = action("Delete", KeyEvent.VK_D,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
            "Delete the selected employee", e -> onDelete());
    private final Action searchAction = action("Search / Query", KeyEvent.VK_S,
            menuKey(KeyEvent.VK_F), "Run an ID search or a field query", e -> onSearch());
    private final Action showAllAction = action("Show All", KeyEvent.VK_H,
            menuKey(KeyEvent.VK_R), "Clear the current filter and show every employee",
            e -> onShowAll());
    private final Action sortAction = action("Sort", KeyEvent.VK_R, null,
            "Sort the roster with the chosen field and algorithm", e -> onSort());
    private final Action loadAction = action("Load…", KeyEvent.VK_L,
            menuKey(KeyEvent.VK_O), "Load a roster from a CSV file", e -> onLoad());
    private final Action saveAction = action("Save", KeyEvent.VK_S,
            menuKey(KeyEvent.VK_S), "Save to the current CSV file", e -> onSave());
    private final Action saveAsAction = action("Save As…", KeyEvent.VK_A, null,
            "Save to a chosen CSV file", e -> onSaveAs());
    private final Action exitAction = action("Exit", KeyEvent.VK_X,
            menuKey(KeyEvent.VK_Q), "Close the application", e -> confirmExit());
    private final Action aboutAction = action("About", KeyEvent.VK_B, null,
            "About this application", e -> showAbout());

    public MainWindow(EmployeeRepository repo) {
        super("Employee Performance Management System");
        this.repo = repo;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureTable();

        setPreferredSize(new Dimension(940, 560));
        pack();
        setLocationRelativeTo(null);

        sortAlgoBox.setSelectedItem(SortAlgorithm.QUICK);
        refreshTable();
        updateTitle();
    }

    // ------------------------------------------------------------------
    // Unsaved-changes tracking
    // ------------------------------------------------------------------
    private void markDirty() {
        dirty = true;
        updateTitle();
    }

    private void markClean() {
        dirty = false;
        updateTitle();
    }

    /** Shows the data file name in the title bar, with a "*" while unsaved. */
    private void updateTitle() {
        String file = repo.getBackingFile()
                .map(p -> p.getFileName().toString())
                .orElse("(no file)");
        setTitle("Employee Performance Management System — " + file + (dirty ? " *" : ""));
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.add(new JMenuItem(loadAction));
        file.add(new JMenuItem(saveAction));
        file.add(new JMenuItem(saveAsAction));
        file.addSeparator();
        file.add(new JMenuItem(exitAction));

        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(new JMenuItem(addAction));
        edit.add(new JMenuItem(editAction));
        edit.add(new JMenuItem(deleteAction));

        JMenu data = new JMenu("Data");
        data.setMnemonic(KeyEvent.VK_D);
        data.add(new JMenuItem(searchAction));
        data.add(new JMenuItem(showAllAction));
        data.addSeparator();
        data.add(compareAlgorithmsItem());

        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        help.add(new JMenuItem(aboutAction));

        bar.add(file);
        bar.add(edit);
        bar.add(data);
        bar.add(help);
        return bar;
    }

    private JMenuItem compareAlgorithmsItem() {
        JMenuItem item = new JMenuItem("Compare sort algorithms…");
        item.setToolTipText("Run all five sorts on the current data and tabulate their cost");
        item.addActionListener(e -> onCompareAlgorithms());
        return item;
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        bar.add(new JButton(addAction));
        bar.add(new JButton(editAction));
        bar.add(new JButton(deleteAction));
        bar.addSeparator();
        bar.add(new JButton(searchAction));
        bar.addSeparator();

        bar.add(new JLabel(" Sort by "));
        bar.add(sortFieldBox);
        bar.add(sortAlgoBox);
        bar.add(descendingCheck);
        bar.add(new JButton(sortAction));

        bar.add(Box.createHorizontalGlue());
        bar.add(new JButton(loadAction));
        bar.add(new JButton(saveAction));

        sortFieldBox.setMaximumSize(sortFieldBox.getPreferredSize());
        sortAlgoBox.setMaximumSize(sortAlgoBox.getPreferredSize());
        return bar;
    }

    private JPanel buildCenter() {
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filterBar.add(new JLabel("Quick find:"));
        filterBar.add(quickFilter);
        filterBar.add(new JButton(showAllAction));
        quickFilter.setToolTipText("Type to filter the table by any column; Esc clears");
        quickFilter.getDocument().addDocumentListener((SimpleDocumentListener) e -> applyQuickFilter());
        quickFilter.registerKeyboardAction(e -> quickFilter.setText(""),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filterBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        return panel;
    }

    private void configureTable() {
        table.setRowSorter(rowSorter);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getSelectionModel().addListSelectionListener(e -> updateActionState());

        TableColumnModel columns = table.getColumnModel();
        int[] widths = {50, 190, 140, 70, 150, 60, 120};
        for (int i = 0; i < widths.length; i++) {
            columns.getColumn(i).setPreferredWidth(widths[i]);
        }
        columns.getColumn(3).setCellRenderer(CellRenderers.number("%.1f"));
        columns.getColumn(4).setCellRenderer(CellRenderers.rating());
        columns.getColumn(6).setCellRenderer(CellRenderers.number("%,.2f"));

        // Double-click a row to edit it; Delete key removes it.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onEdit();
                }
            }
        });
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
        table.getActionMap().put("deleteRow", deleteAction);
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------
    private void onAdd() {
        EmployeeFormDialog.showAdd(this, repo.nextId()).ifPresent(emp -> {
            try {
                repo.add(emp);
                markDirty();
                refreshTable();
                selectEmployee(emp);
                setStatus("Added employee " + emp.getId() + " (" + emp.getName() + ")");
            } catch (RuntimeException ex) {
                error(ex.getMessage());
            }
        });
    }

    private void onEdit() {
        Employee selected = selectedEmployee();
        if (selected == null) {
            info("Select a row to edit first.");
            return;
        }
        EmployeeFormDialog.showEdit(this, selected).ifPresent(updated -> {
            try {
                repo.update(updated);
                markDirty();
                refreshTable();
                selectEmployee(updated);
                setStatus("Updated employee " + updated.getId());
            } catch (RuntimeException ex) {
                error(ex.getMessage());
            }
        });
    }

    private void onDelete() {
        Employee selected = selectedEmployee();
        if (selected == null) {
            info("Select a row to delete first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete employee " + selected.getId() + " (" + selected.getName() + ")?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            repo.deleteById(selected.getId());
            markDirty();
            refreshTable();
            setStatus("Deleted employee " + selected.getId());
        }
    }

    private void onSearch() {
        String[] options = {
                "Find by ID (linear)", "Find by ID (binary)",
                "Query by name", "Query by department", "Query by score range"
        };
        int choice = JOptionPane.showOptionDialog(this, "What would you like to search?",
                "Search / Query", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        switch (choice) {
            case 0: findById(false); break;
            case 1: findById(true); break;
            case 2: queryByName(); break;
            case 3: queryByDepartment(); break;
            case 4: queryByScoreRange(); break;
            default: /* dismissed */
        }
    }

    private void findById(boolean binary) {
        String raw = JOptionPane.showInputDialog(this, "Employee ID:");
        if (raw == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            error("\"" + raw + "\" is not a whole number.");
            return;
        }
        EmployeeRepository.SearchOutcome outcome =
                binary ? repo.binarySearchById(id) : repo.searchById(id);
        if (outcome.isFound()) {
            quickFilter.setText("");
            if (subsetShown) {
                refreshTable();
            }
            selectEmployee(outcome.getMatch().get());
            setStatus(String.format("%s search found employee %d in %d comparison(s)",
                    outcome.getMethod(), id, outcome.getComparisons()));
        } else {
            info(String.format("No employee with ID %d.%n(%s search, %d comparison(s))",
                    id, outcome.getMethod(), outcome.getComparisons()));
            setStatus(outcome.getMethod() + " search: ID " + id + " not found");
        }
    }

    private void queryByName() {
        String fragment = JOptionPane.showInputDialog(this, "Name contains:");
        if (fragment == null) {
            return;
        }
        showResults(repo.queryByName(fragment), "name contains \"" + fragment + "\"");
    }

    private void queryByDepartment() {
        String dept = JOptionPane.showInputDialog(this, "Department:");
        if (dept == null) {
            return;
        }
        showResults(repo.queryByDepartment(dept), "department = \"" + dept + "\"");
    }

    private void queryByScoreRange() {
        try {
            String min = JOptionPane.showInputDialog(this, "Minimum score:", "0");
            if (min == null) return;
            String max = JOptionPane.showInputDialog(this, "Maximum score:", "100");
            if (max == null) return;
            List<Employee> hits = repo.queryByScoreRange(
                    Double.parseDouble(min.trim()), Double.parseDouble(max.trim()));
            showResults(hits, "score in [" + min.trim() + ", " + max.trim() + "]");
        } catch (NumberFormatException ex) {
            error("Please enter numeric scores.");
        }
    }

    private void showResults(List<Employee> hits, String description) {
        if (hits.isEmpty()) {
            info("No employees match " + description + ".");
            setStatus("Query (" + description + "): 0 matches");
            return;
        }
        quickFilter.setText("");
        tableModel.setEmployees(hits);
        subsetShown = true;
        clearInteractiveSort();
        updateActionState();
        setStatus(hits.size() + " employee(s) match " + description
                + " — use Show All to restore the full list");
    }

    private void onShowAll() {
        quickFilter.setText("");
        refreshTable();
        setStatus(repo.size() + " employee(s) on file");
    }

    private void onSort() {
        if (repo.isEmpty()) {
            info("There are no employees to sort.");
            return;
        }
        EmployeeField field = (EmployeeField) sortFieldBox.getSelectedItem();
        SortAlgorithm algo = (SortAlgorithm) sortAlgoBox.getSelectedItem();
        boolean ascending = !descendingCheck.isSelected();

        SortResult<Employee> result = repo.sortBy(field, ascending, algo);
        markDirty(); // the stored order changed and will be written on save
        refreshTable();
        setStatus(String.format("%s by %s (%s) — %,d comparisons, %,d moves, %.2f ms; average %s",
                algo.getDisplayName(), field.getDisplayName(), ascending ? "asc" : "desc",
                result.getMetrics().getComparisons(), result.getMetrics().getMoves(),
                result.getMetrics().getElapsedMillis(), algo.getAverageComplexity()));
    }

    private void onCompareAlgorithms() {
        if (repo.isEmpty()) {
            info("There are no employees to sort.");
            return;
        }
        EmployeeField field = (EmployeeField) sortFieldBox.getSelectedItem();
        List<Employee> snapshot = repo.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Sorting %d employees by %s%n%n", snapshot.size(),
                field.getDisplayName()));
        sb.append(String.format("%-14s %13s %11s %10s%n", "Algorithm", "Comparisons", "Moves", "Time"));
        sb.append("-".repeat(52)).append(System.lineSeparator());
        for (SortAlgorithm algo : SortAlgorithm.values()) {
            SortResult<Employee> r = epms.algorithms.Sorter.sort(
                    snapshot, field.ascending(), algo);
            sb.append(String.format("%-14s %13d %11d %8.2f ms%n", algo.getDisplayName(),
                    r.getMetrics().getComparisons(), r.getMetrics().getMoves(),
                    r.getMetrics().getElapsedMillis()));
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, area, "Sort algorithm comparison",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoad() {
        JFileChooser chooser = fileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        try {
            FileStorage.LoadReport report = repo.loadFrom(path);
            markClean();
            refreshTable();
            String msg = "Loaded " + report.getEmployees().size() + " employees from "
                    + path.getFileName();
            if (report.hasWarnings()) {
                JOptionPane.showMessageDialog(this,
                        report.getWarnings().size() + " row(s) were skipped:\n\n"
                                + String.join("\n", report.getWarnings()),
                        "Load completed with warnings", JOptionPane.WARNING_MESSAGE);
            }
            setStatus(msg);
        } catch (IOException ex) {
            error("Could not read " + path + ":\n" + ex.getMessage());
        }
    }

    private void onSave() {
        if (repo.getBackingFile().isEmpty()) {
            onSaveAs();
            return;
        }
        try {
            repo.save();
            Path path = repo.getBackingFile().get();
            markClean();
            setStatus("Saved " + repo.size() + " employees to " + path.getFileName());
            info("Saved " + repo.size() + " employees to:\n" + path.toAbsolutePath());
        } catch (IOException ex) {
            error("Save failed: " + ex.getMessage());
        }
    }

    private void onSaveAs() {
        JFileChooser chooser = fileChooser();
        repo.getBackingFile().ifPresent(p -> chooser.setSelectedFile(p.toFile()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        try {
            repo.saveTo(file.toPath());
            markClean();
            setStatus("Saved " + repo.size() + " employees to " + file.getName());
            info("Saved " + repo.size() + " employees to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            error("Could not write " + file + ":\n" + ex.getMessage());
        }
    }

    private void confirmExit() {
        if (!dirty) { // nothing unsaved - just leave
            dispose();
            System.exit(0);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Save before exiting?", "Exit",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (repo.getBackingFile().isPresent()) {
                    repo.save();
                } else {
                    onSaveAs();
                }
            } catch (IOException ex) {
                error("Save failed: " + ex.getMessage());
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Employee Performance Management System\n\n"
                        + "A Java Swing + text-based application for managing employee\n"
                        + "performance records, with hand-written sorting and searching\n"
                        + "algorithms.\n\n"
                        + "ICT711 Individual Project.",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private void applyQuickFilter() {
        String text = quickFilter.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
        updateActionState();
    }

    private JFileChooser fileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        repo.getBackingFile().ifPresent(p ->
                chooser.setCurrentDirectory(p.toAbsolutePath().getParent().toFile()));
        return chooser;
    }

    private Employee selectedEmployee() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getEmployeeAt(table.convertRowIndexToModel(viewRow));
    }

    private void selectEmployee(Employee target) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getEmployeeAt(i).getId() == target.getId()) {
                int viewRow = table.convertRowIndexToView(i);
                if (viewRow >= 0) { // -1 when the row is hidden by the active filter
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private void refreshTable() {
        tableModel.setEmployees(repo.findAll());
        subsetShown = false;
        clearInteractiveSort();
        updateActionState();
    }

    /** Drop the JTable header sort so our algorithm's ordering is what shows. */
    private void clearInteractiveSort() {
        rowSorter.setSortKeys(null);
    }

    private void updateActionState() {
        boolean hasSelection = table.getSelectedRow() >= 0;
        editAction.setEnabled(hasSelection);
        deleteAction.setEnabled(hasSelection);
        sortAction.setEnabled(!repo.isEmpty());
        showAllAction.setEnabled(subsetShown || rowSorter.getRowFilter() != null);

        int shown = table.getRowCount();
        int total = repo.size();
        if (subsetShown) {
            countLabel.setText("Query result: " + shown + " shown");
        } else if (rowSorter.getRowFilter() != null) {
            countLabel.setText("Filtered: " + shown + " of " + total);
        } else {
            countLabel.setText(total + " employee(s)");
        }
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private Action action(String name, int mnemonic, KeyStroke accelerator,
                          String tooltip, java.awt.event.ActionListener handler) {
        AbstractAction a = new AbstractAction(name) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handler.actionPerformed(e);
            }
        };
        a.putValue(Action.MNEMONIC_KEY, mnemonic);
        a.putValue(Action.SHORT_DESCRIPTION, tooltip);
        if (accelerator != null) {
            a.putValue(Action.ACCELERATOR_KEY, accelerator);
        }
        return a;
    }

    private static KeyStroke menuKey(int keyCode) {
        return KeyStroke.getKeyStroke(keyCode,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
    }

    /** Lets a lambda serve as a {@link DocumentListener} for all three events. */
    @FunctionalInterface
    private interface SimpleDocumentListener extends DocumentListener {
        void onChange(DocumentEvent e);

        @Override default void insertUpdate(DocumentEvent e) { onChange(e); }
        @Override default void removeUpdate(DocumentEvent e) { onChange(e); }
        @Override default void changedUpdate(DocumentEvent e) { onChange(e); }
    }

    /** Builds the window on the EDT and shows it. */
    public static void launch(EmployeeRepository repo) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the cross-platform look and feel
            }
            new MainWindow(repo).setVisible(true);
        });
    }
}
