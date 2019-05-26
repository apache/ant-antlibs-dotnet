/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ant.dotnet.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Helper methods related to collection manipulation.
 *
 * <p>This is a stripped down copy of Ant 1.8.2's version so the .NET
 * Antlib can still be used with Ant 1.7.0.</p>
 *
 * @since .NET Antlib 1.1
 */
public class CollectionUtils {

    /**
     * Creates a comma separated list of all values held in the given
     * collection.
     *
     * @since .NET Antlib 1.1
     */
    public static String flattenToString(Collection c) {
        return flattenToString(c, ",");
    }

    /**
     * Creates a list of all values held in the given collection
     * separated by the given separator.
     *
     * @since .NET Antlib 1.1
     */
    public static String flattenToString(Collection c, String sep) {
        Iterator iter = c.iterator();
        boolean first = true;
        StringBuffer sb = new StringBuffer();
        while (iter.hasNext()) {
            if (!first) {
                sb.append(sep);
            }
            sb.append(String.valueOf(iter.next()));
            first = false;
        }
        return sb.toString();
    }
}
