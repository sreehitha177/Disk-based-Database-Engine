package org.example.Executor;

import org.example.BufferManagement.BufferManager;
import org.example.Rows.*;

import javax.sound.midi.Soundbank;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

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
//            System.out.println("Inner join key: " + key + ", Matching rows: " + matchingRows.size());

        }
    }

    public int countRows() {
        // Save current state
        leftChild.close();
        rightChild.close();
        open();  // Restart fresh for counting

        int count = 0;
        Row row;
        while ((row = next()) != null) {
            count++;
        }

        close(); // Clean up after counting
        return count;
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
//            pageCount++;
        }
    }


    private String extractJoinKey(Row row) {
        if (row instanceof DataRow) {
//            System.out.println("Entering datarow");
            return new String(((DataRow) row).getMovieId()).trim();
        } else if (row instanceof TempRow) {
//            System.out.println("Entering TempRow");
            return new String(((TempRow) row).getMovieId()).trim();  // used in join1
        } else if (row instanceof PeopleRow) {
            return new String(((PeopleRow) row).getPersonId()).trim();  // used in join2 right side
        } else if (row instanceof JoinedRow) {
            Row inner = ((JoinedRow) row).getInner();
            if (inner instanceof TempRow) {
//                System.out.println("Entering TempRow");

                return new String(((TempRow) inner).getPersonId()).trim();  // for join2
            } else {
                return extractJoinKey(inner);
            }
        }
        return "";
    }


    private Row joinRows(Row outer, Row inner) {
        return new JoinedRow(outer, inner);
    }
}


