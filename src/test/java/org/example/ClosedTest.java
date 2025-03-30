package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Test;

// Step 1:
//import src.main.java.BufferManager;
//import src.main.java.BufferManagerImplementation;
//import src.main.java.Page;
//import src.main.java.Row;
//import src.main.java.Utilities;

import org.example.BufferManager;
import org.example.BufferManagerImplementation;
import org.example.Page;
import org.example.Row;
import org.example.Utilities;


public class ClosedTest {


    //     Test1:
//     For a buffer size of 1, Continuosly insert, evict and check
//     Evicted page must be written to disk.
    @Test
    void testCreationAndEviction(){
        // Create a buffer of size 2 pages
        int bufferSize = 10;
        BufferManagerImplementation bf = new BufferManagerImplementation(bufferSize);
        int counter = 0;
        int maxPages = 1000;
        int rows_per_page = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.createPage();
//            System.out.println(bf.lruQueue);
//            System.out.println(bf.getPinCount());
//            System.out.println(bf.getPinCount().get(page.getPid()));
            while(!page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            bf.markDirty(i);
            bf.unpinPage(i);
//            System.out.println(bf.getPinCount().get(page.getPid()));
        }
        int maxRows = counter;
        // System.out.print("max rows" +  maxRows + " " + rows_per_page);
        counter = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.getPage(i);
            System.out.println("Page found for pageId:"+page.getPid());
            for(int localRow = 0; localRow < rows_per_page; localRow ++ ){
                Row row = page.getRow(localRow);
                String movString = "movie" + counter;
                if (movString.length()>9){
                    movString = movString.substring(0, 9);
                }
                String refmovie =  StringUtils.rightPad(movString, 9, '*');
                String reftitle = StringUtils.rightPad("title" + counter, 30, '*');
                assertEquals(new String(row.movieId), refmovie);
                assertEquals(new String(row.title), reftitle);
                counter = counter + 1;
            }
            bf.unpinPage(i);
        }
    }


    // Updates not marked dirty are not persisted.
    @Test
    void testMarkDirty(){
        // Create a buffer of size 2 pages
        int bufferSize = 2;
        BufferManagerImplementation bf = new BufferManagerImplementation(bufferSize);
        int counter = 0;
        int maxPages = 4;
        int rows_per_page = 0;

        for(int i = 0; i < maxPages; i ++){
            Page page = bf.createPage();
            bf.unpinPage(i);
        }
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.getPage(i);
            while(! page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            if(i %2 == 0) bf.markDirty(i);
            bf.unpinPage(i);
        }

//        Page page= bf.getPage(2);
//        System.out.println(page.isFull()+" for page id: 2");
//        Page page1= bf.getPage(3);
//        System.out.println(page1.isFull()+" for page id: 3");

        for(int i = 0; i < maxPages; i ++ ){
            Page pg = bf.getPage(i);
            System.out.println(pg.isFull()+" for page id: "+i);
            if (i % 2 == 0){
                assertTrue(pg.isFull());
            }else{
                assertFalse(pg.isFull());
            }
            bf.unpinPage(i);
        }
    }

    // LRU eviction of pages.
    @Test
    void testLRUEviction(){
        // Create a buffer of size 2 pages
        int bufferSize = 5;
        BufferManagerImplementation bf = new BufferManagerImplementation(bufferSize);
        int counter = 0;
        int maxPages = 1000;
        int rows_per_page = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.createPage();
            while(! page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            bf.unpinPage(i);
            bf.getPage(0);
            bf.unpinPage(0);

        }
        long s1 = System.nanoTime();
        bf.getPage(0);
        long e1 = System.nanoTime();
        long d1 = e1 - s1;

        long s2 = System.nanoTime();
        bf.getPage(maxPages - 1);
        long e2 = System.nanoTime();
        long d2 = e2 - s2;


        long s3 = System.nanoTime();
        bf.getPage(1);
        long e3 = System.nanoTime();
        long d3 = e3 - s3;
        System.out.println("" + d3 + " " + d1 + " " + d2);
        assertTrue(d3 > d1 * 2);
        assertTrue(d3 > d2 * 2);
    }

    // pinned pages are not evicted, creating more than buffer manager size causes exception
    @Test
    void testPinnedEviction(){
        // Create a buffer of size 2 pages
        int bufferSize = 3;
        BufferManagerImplementation bf = new BufferManagerImplementation(bufferSize);
        int counter = 0;
        int maxPages = 10;
        int rows_per_page = 0;
        try{
            for(int i = 0; i < maxPages; i ++){
                Page page = bf.createPage();
                while(! page.isFull()){
                    String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                    String title = StringUtils.rightPad("title" + counter, 50, '*');
                    Row row =new Row(movie.getBytes(), title.getBytes());
                    page.insertRow(row);
                    counter = counter + 1;
                }

            }
            assertTrue(false);
        }catch (Exception e){
            assertTrue(true);
        }
    }

    // test loading of imdb dataset
    @Test
    void testImdbDataset(){
        // Create a buffer of size 2 pages
        int bufferSize1 = 3;
        BufferManagerImplementation bf1 = new BufferManagerImplementation(bufferSize1);

        String filepath = "title.basics.tsv";
        Utilities ut = new Utilities();
//        ut.loadDataset(bf1, filepath);
        ut.loadDataset(filepath);
        Row row1 = bf1.getPage(0).getRow(19);

        int bufferSize2 = 30;
        BufferManagerImplementation bf2 = new BufferManagerImplementation(bufferSize2);
//        ut.loadDataset(bf2, filepath);
        ut.loadDataset(filepath);
        Row row2 = bf1.getPage(0).getRow(19);

        assertEquals(new String(row1.movieId),new String(row2.movieId));
    }


}