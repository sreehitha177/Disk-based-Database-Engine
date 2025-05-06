package org.example.Rows;

public class Rid {
    private final int pid;
    private final int sid;

    public Rid(int pid, int sid) {
        this.pid = pid;
        this.sid = sid;
    }

    public int getPid() {
        return pid;
    }

    public int getSid() {
        return sid;
    }
}