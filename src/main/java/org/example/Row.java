package org.example;

//import java.nio.charset.StandardCharsets;

//Row
public class Row {
    // Define primary data type fields, depending on the schema of the table
    // These fields are for the Movies table described below

    public static final int ROW_SIZE=39;
    public byte[] movieId;
    public byte[] title;

    public Row(byte[] movieId, byte[] title) {
        this.movieId = movieId;
        this.title = title;
    }

    //Converts the row data into a byte array
    public byte[] getBytes() {
        int totalLength = movieId.length + title.length;

        byte[] rowData = new byte[totalLength];

        System.arraycopy(movieId, 0, rowData, 0, movieId.length);
        System.arraycopy(title, 0, rowData, movieId.length, title.length);

        return rowData;
    }
}
