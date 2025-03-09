package org.example;

import java.nio.charset.StandardCharsets;

public class Row {
    // Define primary data type fields, depending on the schema of the table
    // These fields are for the Movies table described below
    public byte[] movieId;
    public byte[] title;

    public Row(byte[] movieId, byte[] title) {
        this.movieId = movieId;
        this.title = title;
    }

    // Converts the Row to a byte array for serialization or storage
    public byte[] getBytes() {
        // Calculate the total length of the combined byte array
        int totalLength = movieId.length + title.length;

        // Create a new byte array to hold the combined data
        byte[] rowData = new byte[totalLength];

        // Copy the movieId and title into the rowData byte array
        System.arraycopy(movieId, 0, rowData, 0, movieId.length);
        System.arraycopy(title, 0, rowData, movieId.length, title.length);

        return rowData; // Return the byte array containing both movieId and title
    }
}
