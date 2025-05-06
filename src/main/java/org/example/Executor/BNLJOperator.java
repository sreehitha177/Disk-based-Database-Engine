package org.example.Executor;

import org.example.BufferManagement.BufferManager;
import org.example.Rows.*;

import javax.sound.midi.Soundbank;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

//Block Nested Loop Join (BNLJ) Operator implementation.
public class BNLJOperator implements Operator {
    private final Operator leftChild;
    private final Operator rightChild;
    private final BufferManager bufferManager;
    private final int blockPageLimit;    //Max number of pages we can use for outer block (excluding right buffer and temp pages)
    private List<Row> outerBlock;
    private Map<String, List<Row>> hashTable;
    private Row currentInnerRow;
    private Iterator<Row> matchingOuterRows;
    private boolean endOfLeft;

    public BNLJOperator(Operator leftChild, Operator rightChild, BufferManager bufferManager, int bufferSize) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.bufferManager = bufferManager;
        this.blockPageLimit = (bufferSize - 4) / 2;   //Reserving 4 pages (usually for input/output/temporary)
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

    //Returns next joined row from the join result
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

            //Finding matching outer rows for this inner row using the join key
            String key = extractJoinKey(currentInnerRow);
            List<Row> matchingRows = hashTable.getOrDefault(key, new ArrayList<>());
            matchingOuterRows = matchingRows.iterator();
//            System.out.println("Inner join key: " + key + ", Matching rows: " + matchingRows.size());

        }
    }

    //To count total number of joined rows
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

    //Loads the next block of rows from the outer input
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

    //Extracts the join key from a row based on its type
    private String extractJoinKey(Row row) {
        if (row instanceof DataRow) {
            return new String(((DataRow) row).getMovieId()).trim();
        } else if (row instanceof TempRow) {
            return new String(((TempRow) row).getMovieId()).trim();  // used in join1
        } else if (row instanceof PeopleRow) {
            return new String(((PeopleRow) row).getPersonId()).trim();  // used in join2 right side
        } else if (row instanceof JoinedRow) {
            Row inner = ((JoinedRow) row).getInner();
            if (inner instanceof TempRow) {

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


