# Database Design and Query Executor

## Steps to Run the Project

1. **Build Tool**  
   This project uses **Maven**. It is recommended to use Maven directly, or an IDE like **IntelliJ IDEA** that supports Maven integration through its run settings.

2. **JDK Configuration**  
   The `pom.xml` is currently set up for **Java JDK 7**. If you're using a different version, please update the JDK version accordingly in the `pom.xml` file.

3. **Compiling the Project**  
   Use the following command to compile the project:
   ```bash
   javac -d out $(find src/main/java -name "*.java")

4. **Pre-processing the data**  
   Use the following command to pre-process the data:
   ```bash
   java -cp out org.example.pre_process "file_path to title.basics.tsv" "file_path to title.principals.tsv" "file_path to name.basics.tsv"

5. **Query Executor**  
   Use the following command to pre-process the data:
   ```bash
   java -cp out org.example.run_query "start_range" "end_range" buffer_size

6. **Correctness Test**  
   Sqlite command for the query:
   ```sql
   SELECT primaryTitle, primaryName
   FROM movies, people, workedon
   WHERE primaryTitle >= "A" 
   AND primaryTitle <= "Z" 
   AND category = "director" 
   AND movies.tconst = workedon.tconst 
   AND workedon.nconst = people.nconst;
   ```
   
   To copy this output to a text file:
   ```bash
   sqlite3 fintest.db -header -csv "SELECT primaryTitle, primaryName
   FROM movies, people, workedon
   WHERE primaryTitle>='A' AND primaryTitle<='Z' AND category='director' AND movies.tconst=workedon.tconst AND workedon.nconst=people.nconst;" > sql_output.txt
   ```
   Sort this file:
   ```bash
   sort sql_output.txt > sql_output.sorted.txt
   ```




