package FileReaders;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeParsingTest {
    public static void main(String[] args) {
        System.out.println("Testing time parsing with the fixed code...");
        
        // Test the problematic time format "8:34"
        String timeStr = "8:34";
        
        // Create the formatters
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter timeFormatterWithLeadingZero = DateTimeFormatter.ofPattern("HH:mm");
        
        try {
            // Try parsing with the first formatter
            LocalTime time = null;
            try {
                time = LocalTime.parse(timeStr, timeFormatter);
                System.out.println("Successfully parsed with H:mm formatter: " + time);
            } catch (Exception e) {
                try {
                    // Try with leading zero format as fallback
                    time = LocalTime.parse(timeStr, timeFormatterWithLeadingZero);
                    System.out.println("Successfully parsed with HH:mm formatter: " + time);
                } catch (Exception e2) {
                    System.out.println("Failed to parse time: " + timeStr);
                    throw e;
                }
            }
            
            // If we got here, parsing was successful
            System.out.println("Time parsing test passed!");
            
            // Now test the DataLoader
            DataLoader dataLoader = new DataLoader();
            System.out.println("\nTesting DataLoader.loadTimeLogs()...");
            dataLoader.loadTimeLogs();
            System.out.println("DataLoader test passed!");
            
        } catch (Exception e) {
            System.out.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}