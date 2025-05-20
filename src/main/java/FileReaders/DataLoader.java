package FileReaders;

import Core.Employee;
import Core.TimeLog;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    // Resource file names (no leading slash for class loader resources)
    private static final String EMPLOYEE_FILE_PATH = "employee-data.tsv";
    private static final String TIME_LOG_FILE_PATH = "attendance-record.csv";
    private static final String SSS_CONTRIBUTION_FILE_PATH = "sss-contribution-table.tsv";
    private static final String WITHHOLDING_TAX_FILE_PATH = "WithholdingTax.csv";

    // File paths for actual file writing (using Maven structure)
    private static final String EMPLOYEE_OUTPUT_PATH = "src/main/resources/employee-data.tsv";
    private static final String TIME_LOG_OUTPUT_PATH = "src/main/resources/attendance-record.csv";

    // Load employees from the resource file
    public List<Employee> loadEmployees() {
        List<Employee> employees = new ArrayList<>();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(EMPLOYEE_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + EMPLOYEE_FILE_PATH);
                return employees; // Return empty list
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                // Skip header line
                reader.readLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    
                    if (fields.length < 17) {
                        System.err.println("Invalid employee data format (not enough fields): " + line);
                        continue;
                    }

                    try {
                        String employeeNumber = fields[0];
                        String lastName = fields[1];
                        String firstName = fields[2];
                        // Skip fields 3-5 (birthdate, address, phone)
                        String sssNumber = fields[6];
                        String philhealthNumber = fields[7];
                        String pagibigNumber = fields[9]; // Correct index for Pag-ibig
                        String tinNumber = fields[8]; // Correct index for TIN
                        // fields[10] is employment status
                        String position = fields[11];
                        // Skip immediate supervisor field[12]
                        double basicSalary = parseMoneyValue(fields[13]);
                        double riceSubsidy = parseMoneyValue(fields[14]);
                        double phoneAllowance = parseMoneyValue(fields[15]);
                        double clothingAllowance = parseMoneyValue(fields[16]);

                        Employee employee = new Employee(
                                employeeNumber, lastName, firstName, basicSalary,
                                sssNumber, philhealthNumber, pagibigNumber,
                                tinNumber, position, riceSubsidy, phoneAllowance, clothingAllowance
                        );

                        employees.add(employee);
                    } catch (Exception e) {
                        System.err.println("Error parsing employee data: " + e.getMessage());
                        System.err.println("Problematic line: " + line);
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("Successfully loaded " + employees.size() + " employees");

        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
            e.printStackTrace();
        }

        return employees;
    }

    // Load time logs from the resource file
    public List<TimeLog> loadTimeLogs() {
        List<TimeLog> timeLogs = new ArrayList<>();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(TIME_LOG_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + TIME_LOG_FILE_PATH);
                return timeLogs; // Return empty list
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                // Skip header line
                reader.readLine();

                String line;
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
                
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");  // Changed to comma separator for CSV format
                    
                    if (fields.length < 6) {
                        System.err.println("Invalid time log format (not enough fields): " + line);
                        continue;
                    }

                    try {
                        String employeeNumber = fields[0];
                        LocalDate date = LocalDate.parse(fields[3], dateFormatter);

                        // Parse time in and time out
                        LocalTime timeIn = null;
                        if (!fields[4].isEmpty()) {
                            timeIn = LocalTime.parse(fields[4], timeFormatter);
                        }

                        LocalTime timeOut = null;
                        if (!fields[5].isEmpty()) {
                            timeOut = LocalTime.parse(fields[5], timeFormatter);
                        }

                        // Create TimeLog with the correct parameter types
                        TimeLog timeLog = new TimeLog(employeeNumber, date, timeIn, timeOut);
                        timeLogs.add(timeLog);
                    } catch (Exception e) {
                        System.err.println("Error parsing time log: " + line + " - " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Successfully loaded " + timeLogs.size() + " time logs");

        } catch (IOException e) {
            System.err.println("Error loading time log data: " + e.getMessage());
            e.printStackTrace();
        }

        return timeLogs;
    }

    /**
     * Saves a new employee record to the employee data file
     * @param employee The employee to save
     * @return true if successful, false otherwise
     */
    public boolean saveEmployee(Employee employee) {
        try {
            // Make sure directory exists
            File outputDir = new File(EMPLOYEE_OUTPUT_PATH).getParentFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // First, read the entire file to get the header and existing data
            List<String> lines = new ArrayList<>();
            String header = "";

            // Check if file exists
            File employeeFile = new File(EMPLOYEE_OUTPUT_PATH);
            if (employeeFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(employeeFile))) {
                    header = reader.readLine(); // Save the header
                    if (header != null) {
                        lines.add(header);
                    } else {
                        // File exists but is empty, create a header
                        header = "Employee #\tLast Name\tFirst Name\tBirthday\tAddress\tPhone Number\tSSS #\tPhilhealth #\tTIN #\tPag-ibig #\tStatus\tPosition\tImmediate Supervisor\tBasic Salary\tRice Subsidy\tPhone Allowance\tClothing Allowance\tGross Semi-monthly Rate\tHourly Rate";
                        lines.add(header);
                    }

                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            } else {
                // File doesn't exist, create header
                header = "Employee #\tLast Name\tFirst Name\tBirthday\tAddress\tPhone Number\tSSS #\tPhilhealth #\tTIN #\tPag-ibig #\tStatus\tPosition\tImmediate Supervisor\tBasic Salary\tRice Subsidy\tPhone Allowance\tClothing Allowance\tGross Semi-monthly Rate\tHourly Rate";
                lines.add(header);
            }

            // Format the new employee data
            String newEmployeeData = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%.2f",
                    employee.getEmployeeNumber(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    "", // Birthday placeholder
                    "", // Address placeholder
                    "", // Phone placeholder
                    employee.getSssNumber(),
                    employee.getPhilhealthNumber(),
                    employee.getTinNumber(),
                    employee.getPagibigNumber(),
                    "Regular", // Status placeholder
                    employee.getPosition(),
                    "", // Supervisor placeholder
                    employee.getBasicSalary(),
                    employee.getRiceSubsidy(),
                    employee.getPhoneAllowance(),
                    employee.getClothingAllowance(),
                    employee.getBasicSalary() / 2, // Semi-monthly rate
                    employee.getBasicSalary() / 168 // Hourly rate (assuming 168 work hours per month)
            );

            lines.add(newEmployeeData);

            // Write all lines back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Employee record saved successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error saving employee data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a new time log record to the attendance file
     * @param timeLog The time log to save
     * @param employeeLastName The employee's last name
     * @param employeeFirstName The employee's first name
     * @return true if successful, false otherwise
     */
    public boolean saveTimeLog(TimeLog timeLog, String employeeLastName, String employeeFirstName) {
        try {
            // Make sure directory exists
            File outputDir = new File(TIME_LOG_OUTPUT_PATH).getParentFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // First, read the entire file to get the header and existing data
            List<String> lines = new ArrayList<>();
            String header = "";

            // Check if file exists
            File timeLogFile = new File(TIME_LOG_OUTPUT_PATH);
            if (timeLogFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(timeLogFile))) {
                    header = reader.readLine(); // Save the header
                    if (header != null) {
                        lines.add(header);
                    } else {
                        // File exists but is empty, create a header
                        header = "Employee #,Last Name,First Name,Date,Log In,Log Out";
                        lines.add(header);
                    }

                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            } else {
                // File doesn't exist, create header
                header = "Employee #,Last Name,First Name,Date,Log In,Log Out";
                lines.add(header);
            }

            // Format date and times
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

            LocalDate date = timeLog.getDate();
            LocalTime timeIn = timeLog.getTimeIn();
            LocalTime timeOut = timeLog.getTimeOut();

            String formattedDate = date != null ? date.format(dateFormatter) : "";
            String formattedTimeIn = timeIn != null ? timeIn.format(timeFormatter) : "";
            String formattedTimeOut = timeOut != null ? timeOut.format(timeFormatter) : "";

            // Format the new time log data
            String newTimeLogData = String.format("%s,%s,%s,%s,%s,%s",
                    timeLog.getEmployeeNumber(),
                    employeeLastName,
                    employeeFirstName,
                    formattedDate,
                    formattedTimeIn,
                    formattedTimeOut
            );

            lines.add(newTimeLogData);

            // Write all lines back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(TIME_LOG_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Time log record saved successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error saving time log data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing employee record in the file
     * @param updatedEmployee The updated employee information
     * @return true if successful, false otherwise
     */
    public boolean updateEmployee(Employee updatedEmployee) {
        try {
            // Check if file exists
            File employeeFile = new File(EMPLOYEE_OUTPUT_PATH);
            if (!employeeFile.exists()) {
                System.err.println("Employee file does not exist. Cannot update.");
                return false;
            }
            
            // Read the entire file
            List<String> lines = new ArrayList<>();
            String header = "";
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_OUTPUT_PATH))) {
                header = reader.readLine(); // Save the header
                lines.add(header);

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    if (fields.length == 0) continue;
                    
                    String employeeNumber = fields[0];

                    if (employeeNumber.equals(updatedEmployee.getEmployeeNumber())) {
                        // Replace this line with updated employee data
                        String updatedData = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%.2f",
                                updatedEmployee.getEmployeeNumber(),
                                updatedEmployee.getLastName(),
                                updatedEmployee.getFirstName(),
                                fields.length > 3 ? fields[3] : "", // Preserve existing birthday
                                fields.length > 4 ? fields[4] : "", // Preserve existing address
                                fields.length > 5 ? fields[5] : "", // Preserve existing phone
                                updatedEmployee.getSssNumber(),
                                updatedEmployee.getPhilhealthNumber(),
                                updatedEmployee.getTinNumber(),
                                updatedEmployee.getPagibigNumber(),
                                fields.length > 10 ? fields[10] : "Regular", // Preserve existing status
                                updatedEmployee.getPosition(),
                                fields.length > 12 ? fields[12] : "", // Preserve existing supervisor
                                updatedEmployee.getBasicSalary(),
                                updatedEmployee.getRiceSubsidy(),
                                updatedEmployee.getPhoneAllowance(),
                                updatedEmployee.getClothingAllowance(),
                                updatedEmployee.getBasicSalary() / 2, // Semi-monthly rate
                                updatedEmployee.getBasicSalary() / 168 // Hourly rate
                        );
                        lines.add(updatedData);
                        found = true;
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (!found) {
                System.err.println("Employee not found. Cannot update.");
                return false;
            }

            // Write all lines back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Employee record updated successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error updating employee data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads the SSS contribution table from the resource file
     * @return A list of SSS contribution ranges and amounts
     */
    public List<SSSContribution> loadSSSContributions() {
        List<SSSContribution> contributions = new ArrayList<>();
        
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(SSS_CONTRIBUTION_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + SSS_CONTRIBUTION_FILE_PATH);
                return contributions;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                // Skip header line
                reader.readLine();
                
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] fields = line.split("\t");
                        if (fields.length >= 4) {
                            double minRange = Double.parseDouble(fields[0].trim());
                            double maxRange = Double.parseDouble(fields[2].trim());
                            double contributionAmount = Double.parseDouble(fields[3].trim());
                            
                            SSSContribution contribution = new SSSContribution(minRange, maxRange, contributionAmount);
                            contributions.add(contribution);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing SSS contribution data: " + line);
                    }
                }
            }
            
            System.out.println("Successfully loaded " + contributions.size() + " SSS contribution ranges");
            
        } catch (IOException e) {
            System.err.println("Error loading SSS contribution data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return contributions;
    }
    
    /**
     * Loads the withholding tax table from the resource file
     * @return A list of tax brackets
     */
    public List<TaxBracket> loadWithholdingTaxTable() {
        List<TaxBracket> taxBrackets = new ArrayList<>();
        
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(WITHHOLDING_TAX_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + WITHHOLDING_TAX_FILE_PATH);
                return taxBrackets;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                // Skip header line
                reader.readLine();
                
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] fields = line.split(",");
                        if (fields.length >= 4) {
                            double upperLimit = Double.parseDouble(fields[0].trim());
                            double lowerLimit = Double.parseDouble(fields[1].trim());
                            double taxRate = Double.parseDouble(fields[2].trim());
                            double baseAmount = Double.parseDouble(fields[3].trim());
                            
                            TaxBracket bracket = new TaxBracket(upperLimit, lowerLimit, taxRate, baseAmount);
                            taxBrackets.add(bracket);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing tax bracket data: " + line);
                    }
                }
            }
            
            System.out.println("Successfully loaded " + taxBrackets.size() + " tax brackets");
            
        } catch (IOException e) {
            System.err.println("Error loading withholding tax data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return taxBrackets;
    }
    
    // Helper class for SSS contribution ranges
    public static class SSSContribution {
        private double minSalary;
        private double maxSalary;
        private double contributionAmount;
        
        public SSSContribution(double minSalary, double maxSalary, double contributionAmount) {
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
            this.contributionAmount = contributionAmount;
        }
        
        public double getMinSalary() { return minSalary; }
        public double getMaxSalary() { return maxSalary; }
        public double getContributionAmount() { return contributionAmount; }
        
        public boolean inRange(double salary) {
            return salary >= minSalary && salary <= maxSalary;
        }
    }
    
    // Helper class for tax brackets
    public static class TaxBracket {
        private double upperLimit;
        private double lowerLimit;
        private double taxRate;
        private double baseAmount;
        
        public TaxBracket(double upperLimit, double lowerLimit, double taxRate, double baseAmount) {
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
            this.taxRate = taxRate;
            this.baseAmount = baseAmount;
        }
        
        public double getUpperLimit() { return upperLimit; }
        public double getLowerLimit() { return lowerLimit; }
        public double getTaxRate() { return taxRate; }
        public double getBaseAmount() { return baseAmount; }
        
        public boolean inBracket(double income) {
            return income > lowerLimit && income <= upperLimit;
        }
        
        public double calculateTax(double income) {
            return baseAmount + (income - lowerLimit) * taxRate;
        }
    }
    
    // Helper method to parse money values that might contain commas
    private double parseMoneyValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        // Remove commas and other non-numeric characters except for decimal point
        String cleanValue = value.replace(",", "").trim();
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing money value: " + value);
            return 0.0;
        }
    }
}