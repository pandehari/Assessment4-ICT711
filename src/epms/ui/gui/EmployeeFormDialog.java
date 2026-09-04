package epms.ui.gui;

import epms.model.Employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Optional;

/**
 * Modal add/edit form. Returns the entered {@link Employee} via
 * {@link #showDialog}, or {@link Optional#empty()} if the user cancels.
 * All field validation is performed here before the dialog closes.
 */
public class EmployeeFormDialog extends JDialog {

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField departmentField = new JTextField();
    private final JTextField scoreField = new JTextField();
    private final JTextField yearsField = new JTextField();
    private final JTextField salaryField = new JTextField();

    private Employee result;

    private EmployeeFormDialog(Window owner, String title, Employee existing, boolean lockId) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Employee ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Department:"));
        form.add(departmentField);
        form.add(new JLabel("Performance score (0-100):"));
        form.add(scoreField);
        form.add(new JLabel("Years of service:"));
        form.add(yearsField);
        form.add(new JLabel("Salary:"));
        form.add(salaryField);

        if (existing != null) {
            idField.setText(String.valueOf(existing.getId()));
            nameField.setText(existing.getName());
            departmentField.setText(existing.getDepartment());
            scoreField.setText(String.valueOf(existing.getPerformanceScore()));
            yearsField.setText(String.valueOf(existing.getYearsOfService()));
            salaryField.setText(String.valueOf(existing.getSalary()));
        }
        idField.setEnabled(!lockId);

        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        JPanel buttons = new JPanel();
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        try {
            int id = parseInt(idField, "Employee ID");
            double score = parseDouble(scoreField, "Performance score");
            int years = parseInt(yearsField, "Years of service");
            double salary = parseDouble(salaryField, "Salary");
            // Employee's own setters enforce ranges / non-blank text.
            result = new Employee(id, nameField.getText(), departmentField.getText(),
                    score, years, salary);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int parseInt(JTextField field, String label) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number");
        }
    }

    private double parseDouble(JTextField field, String label) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    public static Optional<Employee> showAdd(Component parent, int suggestedId) {
        Employee seed = new Employee(suggestedId, "New", "General", 0, 0, 0);
        EmployeeFormDialog d = new EmployeeFormDialog(windowFor(parent), "Add Employee", seed, false);
        d.nameField.setText("");
        d.departmentField.setText("");
        d.setVisible(true);
        return Optional.ofNullable(d.result);
    }

    public static Optional<Employee> showEdit(Component parent, Employee existing) {
        EmployeeFormDialog d = new EmployeeFormDialog(windowFor(parent), "Edit Employee", existing, true);
        d.setVisible(true);
        return Optional.ofNullable(d.result);
    }

    private static Window windowFor(Component c) {
        if (c == null || c instanceof Window) {
            return (Window) c;
        }
        return javax.swing.SwingUtilities.getWindowAncestor(c);
    }
}
