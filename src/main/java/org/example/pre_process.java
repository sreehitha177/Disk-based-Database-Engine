package org.example;

public class pre_process {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java pre_process <movies_file> <workedon_file> <people_file>");
            return;
        }

        String moviesFile = args[0];
        String workedOnFile = args[1];
        String peopleFile = args[2];

        // Initialize buffer manager and utilities
        BufferManager bufferManager = new BufferManagerImplementation(2048); // buffer size can be passed if you want
        utilities_new.setBufferManager(bufferManager);

        System.out.println("Loading Movies table...");
        utilities_new.loadDataset(moviesFile);

        System.out.println("Loading WorkedOn table...");
        utilities_new.loadWorkedOnDataset(workedOnFile);

        System.out.println("Loading People table...");
        utilities_new.loadPeopleDataset(peopleFile);

        System.out.println("Preprocessing completed successfully.");
    }
}
