/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//pool/src/test/org/apache/commons/pool/impl/TestGenericKeyedObjectPool.java,v 1.1 2001/04/14 16:42:08 rwaldhoff Exp $
 * $Revision: 1.1 $
 * $Date: 2001/04/14 16:42:08 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.pool.impl;

import junit.framework.*;
import org.apache.commons.pool.*;

/**
 * @author Rodney Waldhoff
 * @version $Id: TestGenericKeyedObjectPool.java,v 1.1 2001/04/14 16:42:08 rwaldhoff Exp $
 */
public class TestGenericKeyedObjectPool extends TestCase {
    public TestGenericKeyedObjectPool(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestGenericKeyedObjectPool.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestGenericKeyedObjectPool.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    private GenericKeyedObjectPool pool = null;

    public void setUp() {
        pool = new GenericKeyedObjectPool(
            new KeyedPoolableObjectFactory()  {
                int counter = 0;
                public Object makeObject(Object key) { return String.valueOf(key) + String.valueOf(counter++); }
                public void destroyObject(Object key, Object obj) { }
                public boolean validateObject(Object key, Object obj) { return true; }
                public void activateObject(Object key, Object obj) { }
                public void passivateObject(Object key, Object obj) { }
            }
            );
    }

    public void testBorrow() {
        Object obj0 = pool.borrowObject("");
        assertEquals("0",obj0);
        Object obj1 = pool.borrowObject("");
        assertEquals("1",obj1);
        Object obj2 = pool.borrowObject("");
        assertEquals("2",obj2);
    }

    public void testBorrowReturn() {
        Object obj0 = pool.borrowObject("");
        assertEquals("0",obj0);
        Object obj1 = pool.borrowObject("");
        assertEquals("1",obj1);
        Object obj2 = pool.borrowObject("");
        assertEquals("2",obj2);
        pool.returnObject("",obj2);
        obj2 = pool.borrowObject("");
        assertEquals("2",obj2);
        pool.returnObject("",obj1);
        obj1 = pool.borrowObject("");
        assertEquals("1",obj1);
        pool.returnObject("",obj0);
        pool.returnObject("",obj2);
        obj2 = pool.borrowObject("");
        assertEquals("2",obj2);
        obj0 = pool.borrowObject("");
        assertEquals("0",obj0);
    }

    public void testNumActiveNumIdle() {
        assertEquals(0,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        Object obj0 = pool.borrowObject("");
        assertEquals(1,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        Object obj1 = pool.borrowObject("");
        assertEquals(2,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        pool.returnObject("",obj1);
        assertEquals(1,pool.numActive(""));
        assertEquals(1,pool.numIdle(""));
        pool.returnObject("",obj0);
        assertEquals(0,pool.numActive(""));
        assertEquals(2,pool.numIdle(""));
    }

    public void testNumActiveNumIdle2() {
        assertEquals(0,pool.numActive());
        assertEquals(0,pool.numIdle());
        assertEquals(0,pool.numActive("A"));
        assertEquals(0,pool.numIdle("A"));
        assertEquals(0,pool.numActive("B"));
        assertEquals(0,pool.numIdle("B"));

        Object objA0 = pool.borrowObject("A");
        Object objB0 = pool.borrowObject("B");

        assertEquals(2,pool.numActive());
        assertEquals(0,pool.numIdle());
        assertEquals(1,pool.numActive("A"));
        assertEquals(0,pool.numIdle("A"));
        assertEquals(1,pool.numActive("B"));
        assertEquals(0,pool.numIdle("B"));

        Object objA1 = pool.borrowObject("A");
        Object objB1 = pool.borrowObject("B");

        assertEquals(4,pool.numActive());
        assertEquals(0,pool.numIdle());
        assertEquals(2,pool.numActive("A"));
        assertEquals(0,pool.numIdle("A"));
        assertEquals(2,pool.numActive("B"));
        assertEquals(0,pool.numIdle("B"));

        pool.returnObject("A",objA0);
        pool.returnObject("B",objB0);

        assertEquals(2,pool.numActive());
        assertEquals(2,pool.numIdle());
        assertEquals(1,pool.numActive("A"));
        assertEquals(1,pool.numIdle("A"));
        assertEquals(1,pool.numActive("B"));
        assertEquals(1,pool.numIdle("B"));

        pool.returnObject("A",objA1);
        pool.returnObject("B",objB1);

        assertEquals(0,pool.numActive());
        assertEquals(4,pool.numIdle());
        assertEquals(0,pool.numActive("A"));
        assertEquals(2,pool.numIdle("A"));
        assertEquals(0,pool.numActive("B"));
        assertEquals(2,pool.numIdle("B"));
    }

    public void testClear() {
        assertEquals(0,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        Object obj0 = pool.borrowObject("");
        Object obj1 = pool.borrowObject("");
        assertEquals(2,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        pool.returnObject("",obj1);
        pool.returnObject("",obj0);
        assertEquals(0,pool.numActive(""));
        assertEquals(2,pool.numIdle(""));
        pool.clear("");
        assertEquals(0,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        Object obj2 = pool.borrowObject("");
        assertEquals("2",obj2);
    }

    public void testMaxIdle() {
        pool.setMaxActive(100);
        pool.setMaxIdle(8);
        Object[] active = new Object[100];
        for(int i=0;i<100;i++) {
            active[i] = pool.borrowObject("");
        }
        assertEquals(100,pool.numActive(""));
        assertEquals(0,pool.numIdle(""));
        for(int i=0;i<100;i++) {
            pool.returnObject("",active[i]);
            assertEquals(99 - i,pool.numActive(""));
            assertEquals((i < 8 ? i+1 : 8),pool.numIdle(""));
        }
    }

    public void testMaxActive() {
        pool.setMaxActive(3);
        pool.setWhenExhaustedAction(pool.WHEN_EXHAUSTED_FAIL);

        pool.borrowObject("");
        pool.borrowObject("");
        pool.borrowObject("");
        try {
            pool.borrowObject("");
            fail("Shouldn't get here.");
        } catch(java.util.NoSuchElementException e) {
            // expected
        }
    }

    public void testEviction() {
        pool.setMaxIdle(500);
        pool.setMaxActive(500);
        pool.setNumTestsPerEvictionRun(100);
        pool.setMinEvictableIdleTimeMillis(500L);
        pool.setTimeBetweenEvictionRunsMillis(500L);

        Object[] active = new Object[500];
        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
        }

        try { Thread.currentThread().sleep(2000L); } catch(Exception e) { }
        assert("Should be less than 500 idle, found " + pool.numIdle(""),pool.numIdle("") < 500);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 400 idle, found " + pool.numIdle(""),pool.numIdle("") < 400);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 300 idle, found " + pool.numIdle(""),pool.numIdle("") < 300);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 200 idle, found " + pool.numIdle(""),pool.numIdle("") < 200);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 100 idle, found " + pool.numIdle(""),pool.numIdle("") < 100);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assertEquals("Should be zero idle, found " + pool.numIdle(""),0,pool.numIdle(""));

        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
        }

        try { Thread.currentThread().sleep(2000L); } catch(Exception e) { }
        assert("Should be less than 500 idle, found " + pool.numIdle(""),pool.numIdle("") < 500);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 400 idle, found " + pool.numIdle(""),pool.numIdle("") < 400);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 300 idle, found " + pool.numIdle(""),pool.numIdle("") < 300);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 200 idle, found " + pool.numIdle(""),pool.numIdle("") < 200);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 100 idle, found " + pool.numIdle(""),pool.numIdle("") < 100);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assertEquals("Should be zero idle, found " + pool.numIdle(""),0,pool.numIdle(""));
    }

    public void testEviction2() {
        pool.setMaxIdle(500);
        pool.setMaxActive(500);
        pool.setNumTestsPerEvictionRun(100);
        pool.setMinEvictableIdleTimeMillis(500L);
        pool.setTimeBetweenEvictionRunsMillis(500L);

        Object[] active = new Object[500];
        Object[] active2 = new Object[500];
        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
            active2[i] = pool.borrowObject("2");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
            pool.returnObject("2",active2[i]);
        }

        try { Thread.currentThread().sleep(2000L); } catch(Exception e) { }
        assert("Should be less than 1000 idle, found " + pool.numIdle(),pool.numIdle() < 1000);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 900 idle, found " + pool.numIdle(),pool.numIdle() < 900);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 800 idle, found " + pool.numIdle(),pool.numIdle() < 800);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 700 idle, found " + pool.numIdle(),pool.numIdle() < 700);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 600 idle, found " + pool.numIdle(),pool.numIdle() < 600);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 500 idle, found " + pool.numIdle(),pool.numIdle() < 500);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 400 idle, found " + pool.numIdle(),pool.numIdle() < 400);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 300 idle, found " + pool.numIdle(),pool.numIdle() < 300);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 200 idle, found " + pool.numIdle(),pool.numIdle() < 200);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assert("Should be less than 100 idle, found " + pool.numIdle(),pool.numIdle() < 100);
        try { Thread.currentThread().sleep(600L); } catch(Exception e) { }
        assertEquals("Should be zero idle, found " + pool.numIdle(),0,pool.numIdle());
    }

    public void testThreaded1() {
        pool.setMaxActive(15);
        pool.setMaxIdle(15);
        pool.setMaxWait(1000L);
        TestThread[] threads = new TestThread[20];
        for(int i=0;i<20;i++) {
            threads[i] = new TestThread(pool,100,50);
            Thread t = new Thread(threads[i]);
            t.start();
        }
        for(int i=0;i<20;i++) {
            while(!(threads[i]).complete()) {
                try {
                    Thread.currentThread().sleep(500L);
                } catch(Exception e) {
                    // ignored
                }
            }
            if(threads[i].failed()) {
                fail();
            }
        }
    }

    class TestThread implements Runnable {
        java.util.Random _random = new java.util.Random();
        KeyedObjectPool _pool = null;
        boolean _complete = false;
        boolean _failed = false;
        int _iter = 100;
        int _delay = 50;

        public TestThread(KeyedObjectPool pool) {
            _pool = pool;
        }

        public TestThread(KeyedObjectPool pool, int iter) {
            _pool = pool;
            _iter = iter;
        }

        public TestThread(KeyedObjectPool pool, int iter, int delay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
        }

        public boolean complete() {
            return _complete;
        }

        public boolean failed() {
            return _failed;
        }

        public void run() {
            for(int i=0;i<_iter;i++) {
                try {
                    Thread.currentThread().sleep((long)_random.nextInt(_delay));
                } catch(Exception e) {
                    // ignored
                }
                Object obj = null;
                try {
                    obj = _pool.borrowObject("");
                } catch(java.util.NoSuchElementException e) {
                    _failed = true;
                    _complete = true;
                    break;
                }

                try {
                    Thread.currentThread().sleep((long)_random.nextInt(_delay));
                } catch(Exception e) {
                    // ignored
                }
                _pool.returnObject("",obj);
            }
            _complete = true;
        }
    }
}


