//package org.example;
//
//import org.junit.Test;
//import static org.junit.Assert.*;
//import static org.evosuite.runtime.EvoAssertions.*;
//import org.evosuite.runtime.EvoRunner;
//import org.evosuite.runtime.EvoRunnerParameters;
//import org.example.BufferManagerImplementation;
//import org.example.Page;
//import org.junit.runner.RunWith;
//
//@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true)
//public class BufferManager_ESTest extends BufferManager_ESTest_scaffolding {
//
//  @Test(timeout = 4000)
//  public void test0()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "k?");
//      bufferManagerImplementation0.unpinPage(1331);
//  }
//
//  @Test(timeout = 4000)
//  public void test1()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(2851, "File path cannot be null");
//      bufferManagerImplementation0.markDirty(0);
//  }
//
//  @Test(timeout = 4000)
//  public void test2()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(2570, "q");
//      Page page0 = bufferManagerImplementation0.getPage(2570);
//      assertEquals(0, page0.getPid());
//  }
//
//  @Test(timeout = 4000)
//  public void test3()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(2851, "File path cannot be null");
//      Page page0 = bufferManagerImplementation0.createPage();
//      Page page1 = bufferManagerImplementation0.getPage(1883);
//      assertNotSame(page1, page0);
//  }
//
//  @Test(timeout = 4000)
//  public void test4()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(271, "org.example.BufferManager");
//      Page page0 = bufferManagerImplementation0.createPage();
//      assertEquals(0, page0.getPid());
//  }
//
//  @Test(timeout = 4000)
//  public void test5()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(2851, "File path cannot be null");
//      bufferManagerImplementation0.createPage();
//      Page page0 = bufferManagerImplementation0.createPage();
//      assertEquals(1, page0.getPid());
//  }
//
//  @Test(timeout = 4000)
//  public void test6()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "qs-!P% 7zHx3[6v6XZ0");
//      bufferManagerImplementation0.createPage();
//      // Undeclared exception!
//      bufferManagerImplementation0.getPage((-703));
//  }
//
//  @Test(timeout = 4000)
//  public void test7()  throws Throwable  {
//      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "k?");
//      bufferManagerImplementation0.getPage(1);
//      // Undeclared exception!
//      bufferManagerImplementation0.createPage();
//  }
//}
