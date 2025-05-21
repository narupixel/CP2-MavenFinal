package GUI;

import Core.Employee;
import Core.PayrollSystem;
import DataManager.EmployeeDataManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class EmployeeManagementPanel extends JPanel {
    private PayrollSystem payrollSystem;
    private EmployeeDataManager employeeDataManager;
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    // Form fields for employee data
    private JTextField txtEmployeeNumber;
    private JTextField txtLastName;
    private JTextField txtFirstName;
    private JTextField txtBasicSalary;
    private JTextField txtSssNumber;
    private JTextField txtPhilhealthNumber;
    private JTextField txtPagibigNumber;
    private JTextField txtTinNumber;
    private JTextField txtPosition;
    private JTextField txtRiceSubsidy;
    private JTextField txtPhoneAllowance;
    private JTextField txtClothingAllowance;

    // Buttons
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public EmployeeManagementPanel() {
        employeeDataManager = new EmployeeDataManager();
        checkResourceFiles(); // Verify files exist

        setLayout(new BorderLayout());

        // Create the table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Create the form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.EAST);

        // Initialize buttons as disabled
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        // Print working directory to help with debugging
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
    }

    // Check if resource files exist in the correct location
    private void checkResourceFiles() {
        // Check two possible locations - the src/main/resources for Maven and the compiled resources in classpath
        File employeeFile = new File("src/main/resources/employee-data.tsv");
        File timeLogFile = new File("src/main/resources/attendance-record.csv");

        if (!employeeFile.exists()) {
            System.out.println("Employee data file not found at: " + employeeFile.getAbsolutePath());

            // Check if resources are available in classpath
            if (getClass().getClassLoader().getResource("employee-data.tsv") != null) {
                System.out.println("Employee data file found in classpath resources");
            } else {
                System.err.println("WARNING: Employee data file not found in classpath resources either");
            }
        } else {
            System.out.println("Employee data file found at: " + employeeFile.getAbsolutePath());
        }

        if (!timeLogFile.exists()) {
            System.out.println("Time log file not found at: " + timeLogFile.getAbsolutePath());

            // Check if resources are available in classpath
            if (getClass().getClassLoader().getResource("attendance-record.csv") != null) {
                System.out.println("Time log file found in classpath resources");
            } else {
                System.err.println("WARNING: Time log file not found in classpath resources either");
            }
        } else {
            System.out.println("Time log file found at: " + timeLogFile.getAbsolutePath());
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        String[] columns = {"Employee Number", "Last Name", "First Name", "Position"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        // Create table
        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add selection listener
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
                    displayEmployeeData(employeeNumber);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().toLowerCase().trim();
            if (searchTerm.isEmpty()) {
                loadEmployeeData(); // Reset to show all employees
            } else {
                filterEmployeeTable(searchTerm);
            }
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        return panel;
    }

    // Filter employee table based on search term
    private void filterEmployeeTable(String searchTerm) {
        tableModel.setRowCount(0);

        for (Employee emp : payrollSystem.getAllEmployees()) {
            // Search in multiple fields
            if (emp.getEmployeeNumber().toLowerCase().contains(searchTerm) ||
                emp.getLastName().toLowerCase().contains(searchTerm) ||
                emp.getFirstName().toLowerCase().contains(searchTerm) ||
                emp.getPosition().toLowerCase().contains(searchTerm)) {

                Object[] row = {
                    emp.getEmployeeNumber(),
                    emp.getLastName(),
                    emp.getFirstName(),
                    emp.getPosition()
                };
                tableModel.addRow(row);
            }
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form fields with labels
        txtEmployeeNumber = createLabeledTextField(panel, "Employee Number:");
        txtLastName = createLabeledTextField(panel, "Last Name:");
        txtFirstName = createLabeledTextField(panel, "First Name:");
        txtBasicSalary = createLabeledTextField(panel, "Basic Salary:");
        txtSssNumber = createLabeledTextField(panel, "SSS Number:");
        txtPhilhealthNumber = createLabeledTextField(panel, "Philhealth Number:");
        txtPagibigNumber = createLabeledTextField(panel, "Pagibig Number:");
        txtTinNumber = createLabeledTextField(panel, "TIN Number:");
        txtPosition = createLabeledTextField(panel, "Position:");
        txtRiceSubsidy = createLabeledTextField(panel, "Rice Subsidy:");
        txtPhoneAllowance = createLabeledTextField(panel, "Phone Allowance:");
        txtClothingAllowance = createLabeledTextField(panel, "Clothing Allowance:");

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        panel.add(buttonPanel);

        // Add button actions
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    private JTextField createLabeledTextField(JPanel panel, String labelText) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(new JLabel(labelText), BorderLayout.NORTH);

        JTextField textField = new JTextField(20);
        fieldPanel.add(textField, BorderLayout.CENTER);

        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(5));

        return textField;
    }

    public void setPayrollSystem(PayrollSystem payrollSystem) {
        this.payrollSystem = payrollSystem;
        loadEmployeeData();
    }

    private void loadEmployeeData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add employee data to table
        for (Employee emp : payrollSystem.getAllEmployees()) {
            Object[] row = {
                    emp.getEmployeeNumber(),
                    emp.getLastName(),
                    emp.getFirstName(),
                    emp.getPosition()
            };
            tableModel.addRow(row);
        }
    }

    private void displayEmployeeData(String employeeNumber) {
        // Find the employee in the payroll system
        Employee emp = null;
        for (Employee e : payrollSystem.getAllEmployees()) {
            if (e.getEmployeeNumber().equals(employeeNumber)) {
                emp = e;
                break;
            }
        }

        if (emp != null) {
            txtEmployeeNumber.setText(emp.getEmployeeNumber());
            txtLastName.setText(emp.getLastName());
            txtFirstName.setText(emp.getFirstName());
            txtBasicSalary.setText(String.valueOf(emp.getBasicSalary()));
            txtSssNumber.setText(emp.getSssNumber());
            txtPhilhealthNumber.setText(emp.getPhilhealthNumber());
            txtPagibigNumber.setText(emp.getPagIbigNumber());
            txtTinNumber.setText(emp.getTinNumber());
            txtPosition.setText(emp.getPosition());
            txtRiceSubsidy.setText(String.valueOf(emp.getRiceSubsidy()));
            txtPhoneAllowance.setText(String.valueOf(emp.getPhoneAllowance()));
            txtClothingAllowance.setText(String.valueOf(emp.getClothingAllowance()));

            // Make employee number field non-editable (primary key)
            txtEmployeeNumber.setEditable(false);
        }
    }

    private void updateEmployee() {
        try {
            // Get the selected employee number
            String employeeNumber = txtEmployeeNumber.getText();

            // Find existing employee to get any values we don't have in the form
            Employee existingEmployee = null;
            for (Employee e : payrollSystem.getAllEmployees()) {
                if (e.getEmployeeNumber().equals(employeeNumber)) {
                    existingEmployee = e;
                    break;
                }
            }

            if (existingEmployee == null) {
                JOptionPane.showMessageDialog(this,
                        "Could not find existing employee record.",
                        "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Create updated employee object with all required parameters
        Employee updatedEmployee = new Employee(
                employeeNumber,                              // Employee Number
                txtLastName.getText(),                       // Last Name
                txtFirstName.getText(),                      // First Name
                existingEmployee.getBirthDate(),             // Birth Date
                existingEmployee.getAddress(),               // Address
                existingEmployee.getPhoneNumber(),           // Phone Number
                txtSssNumber.getText(),                      // SSS Number
                txtPhilhealthNumber.getText(),               // Philhealth Number
                txtTinNumber.getText(),                      // TIN Number
                txtPagibigNumber.getText(),                  // Pagibig Number
                existingEmployee.getStatus(),                // Employment Status
                txtPosition.getText(),                       // Position
                existingEmployee.getImmediateSupervisor(),   // Immediate Supervisor
                Double.parseDouble(txtBasicSalary.getText()), // Basic Salary
                Double.parseDouble(txtRiceSubsidy.getText()), // Rice Subsidy
                Double.parseDouble(txtPhoneAllowance.getText()), // Phone Allowance
                Double.parseDouble(txtClothingAllowance.getText()), // Clothing Allowance
                existingEmployee.getGrossSemiMonthlyRate(),  // Gross Semi-Monthly Rate
                existingEmployee.getHourlyRate()             // Hourly Rate
        );

        // Update in memory first
        payrollSystem.updateEmployee(employeeNumber, updatedEmployee);

        // Then update in file system using EmployeeDataManager
        boolean success = employeeDataManager.updateEmployee(updatedEmployee);

        if (success) {
            // Refresh the employee list in PayrollSystem
            payrollSystem.setEmployees(employeeDataManager.getRefreshedEmployees());

            // Refresh table
            loadEmployeeData();

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Employee record updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update employee record.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this,
                "Please enter valid numeric values for salary and allowances.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }
}

    private void deleteEmployee() {
        String employeeNumber = txtEmployeeNumber.getText();

        if (employeeNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No employee selected.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm deletion
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this employee record? This will also delete all associated time logs.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Delete from memory first
                payrollSystem.deleteEmployee(employeeNumber);

                // Then delete from file system
                boolean success = employeeDataManager.deleteEmployee(employeeNumber);

                if (success) {
                    // Refresh the employee and time log lists in PayrollSystem
                    payrollSystem.setEmployees(employeeDataManager.getRefreshedEmployees());
                    payrollSystem.setTimeLogs(employeeDataManager.getRefreshedTimeLogs());

                    // Refresh table
                    loadEmployeeData();

                    // Show success message
                    JOptionPane.showMessageDialog(this,
                            "Employee record deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Clear form
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete employee record from files.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                // Catch any unexpected exceptions
                JOptionPane.showMessageDialog(this,
                        "Error deleting employee: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        // Clear all text fields
        txtEmployeeNumber.setText("");
        txtLastName.setText("");
        txtFirstName.setText("");
        txtBasicSalary.setText("");
        txtSssNumber.setText("");
        txtPhilhealthNumber.setText("");
        txtPagibigNumber.setText("");
        txtTinNumber.setText("");
        txtPosition.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");

        // Make employee number editable again
        txtEmployeeNumber.setEditable(true);

        // Disable update and delete buttons
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        // Clear table selection
        employeeTable.clearSelection();
    }
}
