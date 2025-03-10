package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import java.util.Map;
import java.util.Set;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.example.BufferManagerImplementation;
import org.example.Page;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class BufferManagerImplementation_ESTest extends BufferManagerImplementation_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(100, "&~");
      bufferManagerImplementation0.createPage();
      Map<Integer, Integer> map0 = bufferManagerImplementation0.getPinCount();
      assertEquals(1, map0.size());
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(855, "");
      bufferManagerImplementation0.markDirty((-141));
      Set<Integer> set0 = bufferManagerImplementation0.getDirtyPages();
      assertTrue(set0.contains((-141)));
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "");
      bufferManagerImplementation0.createPage();
      // Undeclared exception!
      bufferManagerImplementation0.getPage(1);
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "");
      Page page0 = bufferManagerImplementation0.createPage();
      assertEquals(0, page0.getPid());
      
      bufferManagerImplementation0.unpinPage(0);
      bufferManagerImplementation0.markDirty(0);
      Page page1 = bufferManagerImplementation0.getPage(861);
      assertNotNull(page1);
      assertFalse(page1.isFull());
      assertEquals(1, page1.getPid());
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(6, "m");
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.unpinPage(0);
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.markDirty(0);
      bufferManagerImplementation0.getPage(861);
      bufferManagerImplementation0.createPage();
      Page page0 = bufferManagerImplementation0.createPage();
      assertEquals(6, page0.getPid());
      assertFalse(page0.isFull());
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(6, "m");
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.unpinPage(0);
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.getPage(861);
      bufferManagerImplementation0.createPage();
      Page page0 = bufferManagerImplementation0.createPage();
      assertFalse(page0.isFull());
      assertEquals(6, page0.getPid());
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(6, "m");
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.createPage();
      bufferManagerImplementation0.getPage(861);
      bufferManagerImplementation0.createPage();
      // Undeclared exception!
      bufferManagerImplementation0.createPage();
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(100, "&~");
      bufferManagerImplementation0.createPage();
      Page page0 = bufferManagerImplementation0.getPage(0);
      assertNotNull(page0);
      assertFalse(page0.isFull());
      assertEquals(0, page0.getPid());
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = null;
      try {
        bufferManagerImplementation0 = new BufferManagerImplementation(100, (String) null);
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // File path cannot be null
         //
         verifyException("org.example.BufferManagerImplementation", e);
      }
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = null;
      try {
        bufferManagerImplementation0 = new BufferManagerImplementation((-1731), "org.example.Row");
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // Buffer size must be positive
         //
         verifyException("org.example.BufferManagerImplementation", e);
      }
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(6, "m");
      Set<Integer> set0 = bufferManagerImplementation0.getDirtyPages();
      assertEquals(0, set0.size());
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(100, "&~");
      bufferManagerImplementation0.unpinPage(100);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      BufferManagerImplementation bufferManagerImplementation0 = new BufferManagerImplementation(1, "2?+");
      Map<Integer, Integer> map0 = bufferManagerImplementation0.getPinCount();
      assertTrue(map0.isEmpty());
  }
}
