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

package org.apache.ant.dotnet.wix;

import org.apache.ant.dotnet.DotNetExecTask;
import org.apache.ant.dotnet.build.AbstractBuildTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.DirSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Task to run the tallow on packaging content to create a wxs file
 *
 * @see http://sf.net/projects/wix
 */
public class TallowTask extends Task {

    /**
     * The vm attribute - if given.
     */
    private String vm;

    /**
     * The source dirs.
     */
    private ArrayList sources = new ArrayList();

    /**
     * A single source file.
     */
    private File source;

    /**
     * The target file.
     */
    private File target;

    /**
     * Where is WiX installed?
     */
    private File wixHome = null;
    /**
     * addtional command line arguments for tallow.
     */
    private Commandline cmdl = new Commandline();

    public TallowTask() {
        super();
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
     * The main source file.
     *
     * <p><code>tallow</code> may include more directories than this one,
     * the main source is the one passed on the command line.</p>
     *
     * @param File object of the main source file.
     */
    public void setSource(File f) {
        source = f;
    }

    /**
     * The main target file.
     *
     * @param File object of the main target file.
     */
    public void setTarget(File f) {
        target = f;
    }

    /**
     * A set of source files.
     */
    public void addSources(DirSet ds) {
        sources.add(ds);
    }
    /**
     * Sets the installation directory of WiX.
     *
     * <p>If omitted, Ant will assume that WiX's executables can be
     * found on the PATH.</p>
     */
    public void setWixHome(File f) {
        wixHome = f;
    }
    /**
     * Adds a command-line argument for tallow.exe.
     *
     * @return new command line argument created.
     */
    public Commandline.Argument createArg() {
        return cmdl.createArgument();
    }
    public void execute() {

        Collection grabbedDirs = grabSources();

        if (target == null) {
            throw new BuildException("You must specify the target if you want"
                                     + " to run tallow.");
        }
        runTallow(grabbedDirs);
    }

    private Collection grabSources() {

        Set r = new HashSet();

        if (source != null) {

            if (!source.exists()) {
                throw new BuildException("Source " + source
                                         + " doesn't exist.");
            } else if (!source.isDirectory()) {

                throw new BuildException("Source " + source
                                         + " is not directory.");
            }

            r.add(source);

        } else if (sources.size() == 0) {
            throw new BuildException("You must specify at least one source"
                                     + " file.");
        } else {
            Iterator iter = sources.iterator();
            while (iter.hasNext()) {
                DirSet ds = (DirSet) iter.next();
                DirectoryScanner scanner = ds.getDirectoryScanner(getProject());
                String[] f = scanner.getIncludedDirectories();
                File base = ds.getDir(getProject());
                for (int i = 0; i < f.length; i++) {
                    r.add(new File(base, f[i]));
                }
            }
            if (r.isEmpty()) {
                throw new BuildException("No sources found");
            }
        }

        return r;
    }
    /**
     * Run tallow passing all files of the collection on the command line.
     */
    private void runTallow(Collection s) {
        run(wixExecutable("tallow.exe"), s, target, cmdl);
    }
    /**
     * returns an absolute path for the given executable if wixHome
     * has been specified, the given name otherwise.
     */
    private String wixExecutable(String name) {
        return wixHome == null ? name
            : new File(wixHome, name).getAbsolutePath();
    }

    /**
     * Runs the specified command passing all files of the collection
     * on the command line - potentially adding an /out parameter.
     */
    private void run(String executable, Collection s, File target,
                     Commandline cmdl) {
        DotNetExecTask exec = 
            DotNetExecTask.getTask(this, vm, executable, null);
        exec.setFailonerror(true);
        exec.setTaskType("wix");

        exec.createArg().setValue("/nologo");

        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            File f = (File) iter.next();
            exec.createArg().setValue("-d");
            exec.createArg().setValue(f.getAbsolutePath());
        }
        String[] extraArgs = cmdl.getArguments();
        for (int i = 0; i < extraArgs.length; i++) {
            exec.createArg().setValue(extraArgs[i]);
        }
        exec.setOutput (target);

        exec.execute();
    }
}
