package org.example;

import java.nio.ByteBuffer;

public class TempRow extends Row {
    public static final int SIZE = 9 + 10; // movieId(9 bytes) + personId(10 bytes)
    private final byte[] movieId;
    private final byte[] personId;

    public TempRow(byte[] movieId, byte[] personId) {
        this.movieId = utilities_new.truncateOrPadByteArray(movieId.clone(), 9);
        this.personId = utilities_new.truncateOrPadByteArray(personId.clone(), 10);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.put(movieId);
        buffer.put(personId);
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
}
