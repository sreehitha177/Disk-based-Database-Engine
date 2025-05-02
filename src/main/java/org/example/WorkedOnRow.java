package org.example;

import java.nio.ByteBuffer;

public class WorkedOnRow extends Row {
    public static final int SIZE = 39; // 9 + 10 + 20
    private final byte[] movieId;   // 9 bytes
    private final byte[] personId;  // 10 bytes
    private final byte[] category;  // 20 bytes

    public WorkedOnRow(byte[] movieId, byte[] personId, byte[] category) {
        this.movieId = utilities_new.truncateOrPadByteArray(movieId, 9);
        this.personId = utilities_new.truncateOrPadByteArray(personId, 10);
        this.category = utilities_new.truncateOrPadByteArray(category, 20);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.put(movieId);
        buffer.put(personId);
        buffer.put(category);
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

    public byte[] getPersonId() {
        return personId;
    }

    public byte[] getCategory() {
        return category;
    }
}
