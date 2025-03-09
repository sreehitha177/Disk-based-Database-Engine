package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.example.Row;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class Row_ESTest extends Row_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      byte[] byteArray0 = new byte[1];
      Row row0 = new Row(byteArray0, byteArray0);
      byte[] byteArray1 = row0.getBytes();
      assertEquals(2, byteArray1.length);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      Row row0 = new Row((byte[]) null, (byte[]) null);
      // Undeclared exception!
      try { 
        row0.getBytes();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.example.Row", e);
      }
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      byte[] byteArray0 = new byte[0];
      Row row0 = new Row(byteArray0, byteArray0);
      byte[] byteArray1 = row0.getBytes();
      row0.title = byteArray0;
      row0.title = byteArray1;
      byte[] byteArray2 = row0.getBytes();
      assertEquals(0, byteArray2.length);
      assertNotSame(byteArray2, byteArray0);
  }
}
