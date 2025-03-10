Steps to Run the Project:
1. Open Command Prompt as Administrator
    Ensure you have the necessary permissions.
2. Navigate to the Project Root Directory
    Use cd to move to your project folder.
3. Compile the Source Files using the below command
    mvn compile
4. Run Tests
    To execute all test files:
      mvn clean test
    To run a specific test case:
      mvn clean test -Dtest=org.example.Row_ESTest.java
5. Understanding Test Files
    Test.java files contain executable test cases.
    Scaffolding files provide essential support for test execution.
