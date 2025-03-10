package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.testdata.EvoSuiteFile;
import org.evosuite.runtime.testdata.FileSystemHandling;
import org.example.BufferManagerImplementation;
import org.example.Utilities;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class Utilities_ESTest extends Utilities_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      Utilities.loadDataset("");
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      FileSystemHandling.shouldAllThrowIOExceptions();
      String[] stringArray0 = new String[1];
      // Undeclared exception!
      try { 
        Utilities.main(stringArray0);
        fail("Expecting exception: RuntimeException");
      
      } catch(RuntimeException e) {
         //
         // Failed to create storage file: C:\\Users\\Priya\\Desktop\\database.bin
         //
         verifyException("org.example.BufferManagerImplementation", e);
      }
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      // Undeclared exception!
      try { 
        Utilities.loadDataset((String) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("java.io.File", e);
      }
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      Utilities.loadDataset("1Wf1flJ|>MOECrmoJ?");
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      EvoSuiteFile evoSuiteFile0 = new EvoSuiteFile("File path cannot be null");
      byte[] byteArray0 = new byte[8];
      byteArray0[1] = (byte)9;
      FileSystemHandling.appendLineToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      byteArray0[0] = (byte)9;
      FileSystemHandling.appendDataToFile(evoSuiteFile0, byteArray0);
      String[] stringArray0 = new String[9];
      FileSystemHandling.appendStringToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      Utilities.main(stringArray0);
      FileSystemHandling.appendLineToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      Utilities.loadDataset("File path cannot be null");
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      EvoSuiteFile evoSuiteFile0 = new EvoSuiteFile("File path cannot be null");
      byte[] byteArray0 = new byte[8];
      byteArray0[1] = (byte)9;
      FileSystemHandling.appendLineToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      byteArray0[0] = (byte)9;
      FileSystemHandling.appendDataToFile(evoSuiteFile0, byteArray0);
      String[] stringArray0 = new String[9];
      Utilities.main(stringArray0);
      Utilities.loadDataset("File path cannot be null");
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      EvoSuiteFile evoSuiteFile0 = new EvoSuiteFile("File path cannot be null");
      byte[] byteArray0 = new byte[8];
      FileSystemHandling.appendLineToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      FileSystemHandling.appendDataToFile(evoSuiteFile0, byteArray0);
      String[] stringArray0 = new String[9];
      Utilities.main(stringArray0);
      Utilities.loadDataset("File path cannot be null");
  }

  @Test(timeout = 4000)
  public void test7()  throws Throwable  {
      EvoSuiteFile evoSuiteFile0 = new EvoSuiteFile("File path cannot be null");
      FileSystemHandling.appendLineToFile(evoSuiteFile0, "1Wf1flJ|>MOECrmoJ?");
      // Undeclared exception!
      try { 
        Utilities.loadDataset("File path cannot be null");
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.example.Utilities", e);
      }
  }

  @Test(timeout = 4000)
  public void test8()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "");
      Utilities.setBufferManager(bufferManagerImplementation0);
  }

  @Test(timeout = 4000)
  public void test9()  throws Throwable  {
      Utilities utilities0 = new Utilities();
  }
}
