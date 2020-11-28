/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.jrouter.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * ClassUtilTest。
 */
public class ClassUtilTest {

    /**
     * Test of getClasses method, of class ClassUtil.
     */
    @Test
    public void testGetClasses() throws Exception {
        assertEquals(0, ClassUtil.getClasses().size());
        assertEquals(0, ClassUtil.getClasses("").size());

        assertEquals(11, ClassUtil.getClasses("net.jrouter.annotation").size());
        assertEquals(11, ClassUtil.getClasses("net.jrouter.annotation", "net.jrouter.annotation").size());

        //包含测试类、内部类，不做个数验证
        assertTrue(!ClassUtil.getClasses("net.jrouter.impl").isEmpty());
        assertTrue(!ClassUtil.getClasses("net.jrouter.util").isEmpty());
    }

    /**
     * Test of loadClass method, of class ClassUtil.
     */
    @Test
    public void testLoadClass() throws Exception {
        ClassUtil.loadClass("net.jrouter.ActionFactory");
        ClassUtil.loadClass("net.jrouter.annotation.Action");
        try {
            ClassUtil.loadClass("net.jrouter.ActionFactory_Null");
            fail("no exception");
        } catch (ClassNotFoundException e) {
            assertNotNull(e);
        }
    }
}
