package org.example.Rows;

import org.example.utilities_new;

import java.nio.ByteBuffer;

public class PeopleRow extends Row {
    public static final int SIZE = 10 + 105; // 10 (personId) + 105 (name)
    private final byte[] personId;
    private final byte[] name;

    public PeopleRow(byte[] personId, byte[] name) {
        this.personId = utilities_new.truncateOrPadByteArray(personId, 10);
        this.name = utilities_new.truncateOrPadByteArray(name, 105);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.put(personId);
        buffer.put(name);
        return buffer.array();
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    // Getters
    public byte[] getPersonId() {
        return personId;
    }

    public byte[] getName() {
        return name;
    }
}
