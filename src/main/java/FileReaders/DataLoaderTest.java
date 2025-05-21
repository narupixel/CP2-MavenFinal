package FileReaders;

import Core.TimeLog;
import java.util.List;

public class DataLoaderTest {

    public static void main(String[] args) {
        // Create a test to verify the time parsing fix
        testLoadTimeLogs();
    }

    public static void testLoadTimeLogs() {
        System.out.println("Testing DataLoader.loadTimeLogs()...");

        DataLoader dataLoader = new DataLoader();
        List<TimeLog> timeLogs = dataLoader.loadTimeLogs();

        // Check that we loaded some time logs
        if (timeLogs.isEmpty()) {
            System.out.println("ERROR: Time logs should not be empty");
        } else {
            System.out.println("SUCCESS: Loaded " + timeLogs.size() + " time logs");

            // Print the first few time logs to verify parsing
            System.out.println("\nSample time logs:");
            for (int i = 0; i < Math.min(5, timeLogs.size()); i++) {
                TimeLog log = timeLogs.get(i);
                System.out.println("Employee: " + log.getEmployeeNumber() + 
                                  ", Date: " + log.getDate() + 
                                  ", TimeIn: " + log.getTimeIn() + 
                                  ", TimeOut: " + log.getTimeOut());
            }
        }

        System.out.println("\nTest completed. If no exceptions were thrown, the time parsing issue is fixed.");
    }
}
