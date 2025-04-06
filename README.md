STEPS to RUN:
1. The build tool we used is MAVEN, so it is recommended to use maven or a tool like IntelliJ which runs files with build tools integrated in the run settings.
2. The pom.xml file has plugins for java jdk 8, please edit that according to the version on your system.
3. Change the file path inside “utilities_new.java” for the title.basics.tsv file, based on where it is stored in your pc or laptop.
4. Open CMD as admin (for windows, MAC users use corresponding commands in terminal or IntelliJ)
5. Execute these commands in order:
           To compile and run the main program:
             - cd “root directory of project”
             - mvn clean compile
             - mvn exec:java -Dexec.mainClass="org.example.utilities_new"
           To run tests:
             - mvn test-compile
             - mvn exec:java -Dexec.mainClass="org.example.TestCases" 
             - mvn exec:java -Dexec.mainClass="org.example.TestCasesPerformance"
