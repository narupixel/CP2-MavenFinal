package DataManager;

import Core.Employee;

public class FileOperationsTest {
    public static void main(String[] args) {
        System.out.println("Starting file operations test");

        EmployeeDataManager manager = new EmployeeDataManager();

        // Load all employees
        var employees = manager.getEmployees();
        System.out.println("Loaded " + employees.size() + " employees");

        if (employees.size() > 0) {
            // Try updating the first employee
            Employee firstEmployee = employees.get(0);
            System.out.println("Testing update on employee: " + firstEmployee.getEmployeeNumber());

            // Create updated employee with a small change - ensuring all parameters match constructor
            Employee updatedEmployee = new Employee(
                    firstEmployee.getEmployeeNumber(),
                    firstEmployee.getLastName(),
                    firstEmployee.getFirstName(),
                    firstEmployee.getBirthDate(),
                    firstEmployee.getAddress(),
                    firstEmployee.getPhoneNumber(),
                    firstEmployee.getSssNumber(),
                    firstEmployee.getPhilhealthNumber(),
                    firstEmployee.getTinNumber(),
                    firstEmployee.getPagIbigNumber(),
                    firstEmployee.getStatus(),
                    firstEmployee.getPosition(),
                    firstEmployee.getImmediateSupervisor(),
                    firstEmployee.getBasicSalary(),
                    firstEmployee.getRiceSubsidy() + 100,  // Adding 100 to rice subsidy
                    firstEmployee.getPhoneAllowance(),
                    firstEmployee.getClothingAllowance(),
                    firstEmployee.getGrossSemiMonthlyRate(),
                    firstEmployee.getHourlyRate()
            );

            // Try to update
            boolean updateResult = manager.updateEmployee(updatedEmployee);
            System.out.println("Update result: " + (updateResult ? "SUCCESS" : "FAILED"));

            // Try deleting (if you want to test)
            // boolean deleteResult = manager.deleteEmployee(firstEmployee.getEmployeeNumber());
            // System.out.println("Delete result: " + (deleteResult ? "SUCCESS" : "FAILED"));
        }

        System.out.println("Test completed");
    }
}