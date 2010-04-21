/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ant.dotnet.compile;

import org.apache.ant.dotnet.NetCommand;
import org.apache.tools.ant.BuildException;


/**
 * This task compiles F# source into executables or modules.
 * The task requires fsc.exe on the execute path, unless it or an equivalent
 * program is specified in the <tt>executable</tt> parameter
 *
 * <p>
 * All parameters are optional: &lt;fsc/&gt; should suffice to produce a debug
 * build of all *.fs files.
 *
 * <p>

 * The task is a directory based task, so attributes like
 * <tt>includes=&quot;**\/*.fs&quot;</tt> and
 * <tt>excludes=&quot;broken.fs&quot;</tt> can be used to control
 * the files pulled in. By default,
 * all *.fs files from the project folder down are included in the command.
 * When this happens the destFile -if not specified-
 * is taken as the first file in the list, which may be somewhat hard to control.
   Specifying the output file with <tt>destfile</tt> is prudent.
 </p>
 <p>
 * Also, dependency checking only works if destfile is set.
 *
 * <p>For historical reasons the pattern
 * <code>**</code><code>/*.fs</code> is preset as includes list and
 * you can not override it with an explicit includes attribute.  Use
 * nested <code>&lt;src&gt;</code> elements instead of the basedir
 * attribute if you need more control.</p>
 *
 * As with &lt;csc&gt; nested <tt>src</tt> filesets of source,
 * reference filesets, definitions and resources can be provided.
 *
 * <p>
 * Example
 * </p>
 * <pre>&lt;fsc
 *   optimize=&quot;true&quot;
 *   debug=&quot;false&quot;
 *   warnLevel=&quot;4&quot;
 *   targetType=&quot;exe&quot;
 *   definitions=&quot;RELEASE&quot;
 *   excludes=&quot;src/unicode_class.fs&quot;
 *   destFile=&quot;NetApp.exe&quot;
 *   tailcalls=&quot;true&quot;
 *   references="System.Xml,System.Web.Xml"
 *   &gt;
 *          &lt;reference file="${testCSC.dll}" /&gt;
 *          &lt;define name="RELEASE" /&gt;
 *          &lt;define name="DEBUG" if="debug.property"/&gt;
 *          &lt;define name="def3" unless="def2.property"/&gt;
 *   &lt;/fsc&gt;
 </pre>
 * @ant.task    name="fsc" category="dotnet"
 */

public class FSharp extends DotnetCompile {

    /**
     * Compiler option to enable tailcalls.
     */
    private boolean tailcalls = true;

    /**
     * Compiler option to enable cross-module optimizations.
     */
    private boolean crossoptimize = false;

    /**
     * Compiler option to statically link the F# library and all
     * referenced DLLs into the assembly.
     */
    private boolean standalone = false;

    public FSharp() {
        clear();
    }

    /**
     *  reset all contents.
     */
    public void clear() {
        super.clear();
        tailcalls = true;
        crossoptimize = false;
        standalone = false;
        setExecutable("fsc");
    }

    /**
     * Whether to enable tailcalls.
     */
    public void setTailcalls(boolean b) {
        tailcalls = b;
    }

    /**
     * Whether to enable tailcalls.
     * @return    true if flag is turned on
     */
    public boolean getTailcalls() {
        return tailcalls;
    }

    /**
     * Form the option string for tailcalls.
     * @return The parameter string.
     */
    public String getTailcallsParameter() {
        return "/tailcalls" + (tailcalls ? "+" : "-");
    }

    /**
     * Whether to enable cross-module optimizations.
     */
    public void setCrossoptimize(boolean b) {
        crossoptimize = b;
    }

    /**
     * Whether to enable cross-module optimizations.
     * @return    true if flag is turned on
     */
    public boolean getCrossoptimize() {
        return crossoptimize;
    }

    /**
     * Form the option string for cross-module optimizations.
     * @return The parameter string.
     */
    public String getCrossoptimizeParameter() {
        return "/crossoptimize" + (crossoptimize ? "+" : "-");
    }

    /**
     * Whether to create a standalone assembly.
     */
    public void setStandalone(boolean b) {
        standalone = b;
    }

    /**
     * Whether to create a standalone assembly.
     * @return    true if flag is turned on
     */
    public boolean getStandalone() {
        return standalone;
    }

    /**
     * Form the option string for standalone.
     * @return The parameter string.
     */
    public String getStandaloneParameter() {
        return standalone ? "/standalone" : null;
    }

    /**
     * implement FSC commands
     * @param command
     */
    protected void addCompilerSpecificOptions(NetCommand command) {
        command.addArgument(getTailcallsParameter());
        command.addArgument(getCrossoptimizeParameter());
        String s = getStandaloneParameter();
        if (s != null) {
            command.addArgument(s);
        }
    }

    /**
     * Get the delimiter that the compiler uses between references.
     */
    public String getReferenceDelimiter() {
        return ";";
    }

    /**
     * Get the extension of filenames to compile.
     * @return The string extension of files to compile.
     */
    public String getFileExtension() {
        return "fs";
    }

    protected void createResourceParameter(NetCommand command,
                                           DotnetResource resource) {
        resource.getParameters(getProject(), command, false);
    }

}
