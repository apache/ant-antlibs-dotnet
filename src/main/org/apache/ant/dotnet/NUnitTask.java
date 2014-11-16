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

package org.apache.ant.dotnet;

import org.apache.ant.dotnet.util.CollectionUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.RedirectorElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Task to run the NUnit Console test runner.
 *
 * @see http://www.nunit.org/
 */
public class NUnitTask extends Task {

    /**
     * The vm attribute - if given.
     */
    private String vm;

    /**
     * Test assemblies.
     */
    private ArrayList testAssemblies = new ArrayList();

    /**
     * The /config argument.
     */
    private File configFile;

    /**
     * The /output argument.
     */
    private File out;

    /**
     * The /err argument.
     */
    private File err;

    /**
     * The /xml argument.
     */
    private File xmlOut;

    /**
     * The /transform argument.
     */
    private File transform;

    /**
     * The /thread argument.
     */
    private boolean thread = false;

    /**
     * The /fixture argument.
     */
    private String fixture;

    /**
     * Categories to include.
     */
    private ArrayList includes = new ArrayList();

    /**
     * Categories to exclude.
     */
    private ArrayList excludes = new ArrayList();

    /**
     * The /noshadow argument.
     */
    private boolean noshadow = false;

    /**
     * The /labels argument.
     */
    private boolean labels = false;

    /**
     * Redirects everything that NUnit wants to send to the console.
     */
    private RedirectorElement redirectorElement;

    /**
     * Whether a failure should stop the build.
     */
    private boolean failOnError = false;

    /**
     * Name of property to set if a test fails.
     *
     * @since 1.0 Beta 2
     */
    private String errorProperty;

    /**
     *  executable
     */
    private String executable;

    /**
     * Support for nested environment variables.
     */
    private Environment env = new Environment();

    public NUnitTask() {
        super();
        executable = "nunit-console.exe";
    }

    /**
     * Set the name of the executable for the virtual machine.
     *
     * @param value the name of the executable for the virtual machine
     */
    public void setVm(String value) {
        this.vm = value;
    }

    /**
     * Sets the name of the config file.
     */
    public void setConfig(File c) {
        configFile = c;
    }

    /**
     * The /output argument.
     */
    public void setOut(File out) {
        this.out = out;
    }

    /**
     * The /err argument.
     */
    public void setError(File err) {
        this.err = err;
    }

    /**
     * The /xml argument.
     */
    public void setXmlOut(File out) {
        this.xmlOut = out;
    }

    /**
     * The /transform argument.
     */
    public void setTransform(File transform) {
        this.transform = transform;
    }

    /**
     * The /thread argument.
     */
    public void setThread(boolean thread) {
        this.thread = thread;
    }

    /**
     * The /fixture argument.
     */
    public void setFixture(String fixture) {
        this.fixture = fixture;
    }

    /**
     * The /noshadow argument.
     */
    public void setNoshadow(boolean noshadow) {
        this.noshadow = noshadow;
    }

    /**
     * The /labels argument.
     */
    public void setLabels(boolean labels) {
        this.labels = labels;
    }

    /**
     * Whether a failure should stop the build.
     */
    public void setFailOnError(boolean b) {
        failOnError = b;
    }

    /**
     * Name of property to set if a test fails.
     *
     * @since 1.0 Beta 2
     */
    public void setErrorProperty(String name) {
        errorProperty = name;
    }

    /**
     * set the name of the program, overriding the defaults.
     *
     * <p>Can be used to set the full path to a program, or to switch
     * to an alternate implementation of the command - e.g. using
     * "nunit-console-x86.exe" to run the 32bit version on a 64bit
     * system.</p>
     *
     * @param executable
     * @since .NET Antlib 1.2
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Adds a test assembly by name.
     */
    public void addTestAssembly(NamedElement a) {
        testAssemblies.add(a);
    }

    /**
     * Adds a category to the include list.
     */
    public void addInclude(NamedElement a) {
        includes.add(a);
    }

    /**
     * Adds a category to the exclude list.
     */
    public void addExclude(NamedElement a) {
        excludes.add(a);
    }

    /**
     * Add an environment variable to the launched process.
     *
     * @param var new environment variable
     */
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }

    /**
     * Add a <code>RedirectorElement</code> to this task.
     *
     * <p>This does not use the <code>out</code> and
     * <code>error</code> attributes, it only captures NUnits output
     * that has not been redirected by those attributes.</p>
     */
    public void addConfiguredRedirector(RedirectorElement redirectorElement) {
        if (this.redirectorElement != null) {
            throw new BuildException("cannot have > 1 nested <redirector>s");
        } else {
            this.redirectorElement = redirectorElement;
        }
    }

    public void execute() {
        if (testAssemblies.size() == 0) {
            throw new BuildException("You must specify at least one test "
                                     + "assembly.");
        }
        
        DotNetExecTask exec = DotNetExecTask.getTask(this, vm, 
                                                     executable,
                                                     env);
        Iterator iter = testAssemblies.iterator();
        while (iter.hasNext()) {
            NamedElement a = (NamedElement) iter.next();
            exec.createArg().setValue(a.getName());
        }
        if (configFile != null) {
            exec.createArg().setValue("/config=" 
                                      + configFile.getAbsolutePath());
        }
        exec.createArg().setValue("/nologo");

        if (out != null) {
            exec.createArg().setValue("/output=" + out.getAbsolutePath());
        }
        if (err != null) {
            exec.createArg().setValue("/err=" + err.getAbsolutePath());
        }
        if (xmlOut != null) {
            exec.createArg().setValue("/xml=" + xmlOut.getAbsolutePath());
        }
        if (transform != null) {
            exec.createArg().setValue("/transform=" 
                                      + transform.getAbsolutePath());
        }

        if (thread) {
            exec.createArg().setValue("/thread");
        }
        if (noshadow) {
            exec.createArg().setValue("/noshadow");
        }
        if (labels) {
            exec.createArg().setValue("/labels");
        }
        if (fixture != null) {
            exec.createArg().setValue("/fixture=" + fixture);
        }
        
        if (includes.size() > 0) {
            StringBuffer sb = new StringBuffer("/include=");
            sb.append(CollectionUtils.flattenToString(includes));
            exec.createArg().setValue(sb.toString());
        }
        if (excludes.size() > 0) {
            StringBuffer sb = new StringBuffer("/exclude=");
            sb.append(CollectionUtils.flattenToString(excludes));
            exec.createArg().setValue(sb.toString());
        }

        if (redirectorElement != null) {
            exec.addConfiguredRedirector(redirectorElement);
        }
        exec.setFailonerror(failOnError);
        exec.internalSetErrorProperty(errorProperty);

        exec.execute();
    }

    public static class NamedElement {
        private String name;
        public String getName() {return name;}
        public void setName(String s) {name = s;}
        public String toString() {return getName();}
    }
}
