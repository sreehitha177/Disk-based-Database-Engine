package org.example.DataTrimmer;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;

//Function to trim the original dataset for testing purpose
public class Trimmer {
    public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {

        String inputFile = "/Users/sreehithanarayana/Downloads/title.principals.tsv";    // Input original TSV file path
        String outputFile = "test100000.workedon.tsv";  // Output TSV file path

        int maxLines = 100000;
        int count = 0;


        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            //reader.readLine();
            while ((line = reader.readLine()) != null && count<=100000) {
                writer.write(line);
                writer.newLine();
                count++;
            }

            System.out.println("Copied " + count + " lines from " + inputFile + " to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
