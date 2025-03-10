package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.example.PageImplementation;
import org.example.Row;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class PageImplementation_ESTest extends PageImplementation_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(0, 0);
      pageImplementation0.getRow(0);
      assertEquals(0, pageImplementation0.getPid());
      assertTrue(pageImplementation0.isFull());
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(1062, 1062);
      byte[] byteArray0 = new byte[4];
      Row row0 = new Row(byteArray0, byteArray0);
      pageImplementation0.insertRow(row0);
      int int0 = pageImplementation0.insertRow(row0);
      assertEquals(1, int0);
      assertEquals(1062, pageImplementation0.getPid());
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(1062, 1062);
      byte[] byteArray0 = new byte[4];
      Row row0 = new Row(byteArray0, byteArray0);
      int int0 = pageImplementation0.insertRow(row0);
      assertEquals(0, int0);
      
      pageImplementation0.getRows();
      assertEquals(1062, pageImplementation0.getPid());
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(1062, 1062);
      byte[] byteArray0 = new byte[4];
      Row row0 = new Row(byteArray0, byteArray0);
      int int0 = pageImplementation0.insertRow(row0);
      assertEquals(0, int0);
      
      Row row1 = pageImplementation0.getRow(0);
      assertNotNull(row1);
      assertEquals(1062, pageImplementation0.getPid());
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(333, 0);
      int int0 = pageImplementation0.getPid();
      assertTrue(pageImplementation0.isFull());
      assertEquals(333, int0);
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation((-1), 0);
      int int0 = pageImplementation0.getPid();
      assertEquals((-1), int0);
      assertTrue(pageImplementation0.isFull());
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(911, 911);
      boolean boolean0 = pageImplementation0.isFull();
      assertEquals(911, pageImplementation0.getPid());
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(0, 0);
      boolean boolean0 = pageImplementation0.isFull();
      assertEquals(0, pageImplementation0.getPid());
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation((-1910), (-1910));
      int int0 = pageImplementation0.insertRow((Row) null);
      assertEquals((-1), int0);
      assertEquals((-1910), pageImplementation0.getPid());
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(0, 1993);
      pageImplementation0.getRow(1993);
      assertEquals(0, pageImplementation0.getPid());
      assertFalse(pageImplementation0.isFull());
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(0, 1993);
      pageImplementation0.getRows();
      assertEquals(0, pageImplementation0.getPid());
      assertFalse(pageImplementation0.isFull());
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation(0, 1993);
      int int0 = pageImplementation0.getPid();
      assertEquals(0, int0);
      assertFalse(pageImplementation0.isFull());
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      PageImplementation pageImplementation0 = new PageImplementation((-1910), (-1910));
      pageImplementation0.getRow((-1910));
      assertTrue(pageImplementation0.isFull());
      assertEquals((-1910), pageImplementation0.getPid());
  }
}
