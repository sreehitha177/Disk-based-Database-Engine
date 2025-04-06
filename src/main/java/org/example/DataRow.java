package org.example;

import java.nio.ByteBuffer;

public class DataRow extends Row {
    public static final int SIZE = 39; // 9 (movieId) + 30 (title)
    private final byte[] movieId; // 9 bytes
    private final byte[] title;   // 30 bytes

    public DataRow(byte[] movieId, byte[] title) {
        this.movieId = utilities_new.truncateOrPadByteArray(movieId, 9);
        this.title = utilities_new.truncateOrPadByteArray(title, 30);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.put(movieId);
        buffer.put(title);
        return buffer.array();
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    // Getters
    public byte[] getMovieId() {
        return movieId;
    }

    public byte[] getTitle() {
        return title;
    }
}