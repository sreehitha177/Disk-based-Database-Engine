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

6. **Data Trimmer**  
   Trimmer.java is a function to trim the data for testing purposes. You should run this three times seperately changing the path of input file(original datasets) and path to output file and also the number of lines of data you want in new dataset to generate mini datasets from the original gigantic dataset


7. **Correctness Test**  
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

7. **Data Files**  

   The tables are taken from the IMDB database Links, available [here](https://developer.imdb.com/non-commercial-datasets/).

   Movies is taken from title.basics.tsv file from the IMDB database. Based on that file, build a table Movies(movieId: char(10), title: char(450)), where       each row is a movie
   WorkedOn is taken from the title.principals.tsv file. The movieId attribute is the tconst column, the personId attribute is the nconst column, and the       category attribute is the category column.
   People is taken from name.basics.tsv. The personId attribute is the nconst column, and the name attribute is the primaryName column.

