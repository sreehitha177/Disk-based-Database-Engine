package org.example;

import java.util.*;

public class BNLJOperator implements Operator {
    private final Operator leftChild;   // NOT just ScanOperator
    private final Operator rightChild;  // NOT just ScanOperator
    private final BufferManager bufferManager;
    private final int blockPageLimit;
    private List<Row> outerBlock;
    private Map<String, List<Row>> hashTable;
    private Row currentInnerRow;
    private Iterator<Row> matchingOuterRows;
    private boolean endOfLeft;

    public BNLJOperator(Operator leftChild, Operator rightChild, BufferManager bufferManager, int bufferSize) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.bufferManager = bufferManager;
        this.blockPageLimit = (bufferSize - 4) / 2;
        this.outerBlock = new ArrayList<>();
        this.hashTable = new HashMap<>();
        this.endOfLeft = false;
    }

    @Override
    public void open() {
        leftChild.open();
        rightChild.open();
        loadNextBlock();
    }

    @Override
    public Row next() {
        while (true) {
            if (matchingOuterRows != null && matchingOuterRows.hasNext()) {
                Row outerRow = matchingOuterRows.next();
                return joinRows(outerRow, currentInnerRow);
            }

            currentInnerRow = rightChild.next();
            if (currentInnerRow == null) {
                if (endOfLeft) return null;
                loadNextBlock();
                rightChild.close();
                rightChild.open();
                currentInnerRow = rightChild.next();
                if (currentInnerRow == null) return null;
            }

            String key = extractJoinKey(currentInnerRow);
            List<Row> matchingRows = hashTable.getOrDefault(key, new ArrayList<>());
            matchingOuterRows = matchingRows.iterator();
        }
    }

    @Override
    public void close() {
        leftChild.close();
        rightChild.close();
    }

    private void loadNextBlock() {
        outerBlock.clear();
        hashTable.clear();
        int pageCount = 0;
        while (pageCount < blockPageLimit) {
            Row row = leftChild.next();
            if (row == null) {
                endOfLeft = true;
                break;
            }
            outerBlock.add(row);
            String key = extractJoinKey(row);
            hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
    }

    private String extractJoinKey(Row row) {
        if (row instanceof DataRow) {
            return new String(((DataRow) row).getMovieId()).trim();
        } else if (row instanceof TempRow) {
            return new String(((TempRow) row).getMovieId()).trim();
        } else if (row instanceof PeopleRow) {
            return new String(((PeopleRow) row).getPersonId()).trim();
        }
        return "";
    }

    private Row joinRows(Row outer, Row inner) {
        return new JoinedRow(outer, inner);
    }
}
