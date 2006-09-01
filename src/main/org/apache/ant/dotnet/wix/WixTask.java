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
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Task to run the WiX utility to create MSI files from an XML description.
 *
 * @see http://sf.net/projects/wix
 */
public class WixTask extends Task {

    /**
     * The vm attribute - if given.
     */
    private String vm;

    /**
     * The source files.
     */
    private ArrayList sources = new ArrayList();

    /**
     * Additional source files (include files in the case of candle,
     * or media/files/whatever in the case of light).
     */
    private ArrayList moreSources = new ArrayList();

    /**
     * A single source file.
     */
    private File source;

    /**
     * The target file.
     */
    private File target;

    /**
     * What to do.
     */
    private Mode mode;

    /**
     * Where is WiX installed?
     */
    private File wixHome = null;

    /**
     * Where to place the generated .wixobj files.
     */
    private File wixobjDestDir = null;

    /**
     * list of parameters for the preprocessor.
     */
    private ArrayList parameters = new ArrayList();

    public WixTask() {
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
     * <p><code>candle</code> may include more files than this one,
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
    public void addSources(FileSet fs) {
        sources.add(fs);
    }

    /**
     * A set of additional source files (include files in the case of
     * candle, or media/files/whatever in the case of light).
     *
     * <p>Unlike the files specified as sources, these will not be
     * passed on the command line, they only help Ant to determine
     * whether the target is out-of-date.</p>
     */
    public void addMoreSources(FileSet fs) {
        moreSources.add(fs);
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
     * Whether to run candle, light or both.
     */
    public void setMode(Mode m) {
        mode = m;
    }

    /**
     * Sets the destination directory for wixobj files generated by candle.
     *
     * <p>Let's candle decide and assumes they'll be created in the
     * current working directory.</p>
     */
    public void setWixobjDestDir(File f) {
        wixobjDestDir = f;
    }

    /**
     * A parameter to pass to candle.exe.
     */
    public final void addCandleParameter(AbstractBuildTask.Property t) {
        parameters.add(t);
    }

    public void execute() {
        if (source == null && sources.size() == 0) {
            throw new BuildException("You must specify at least one source"
                                     + " file.");
        }

        if (source != null && !source.exists()) {
            throw new BuildException("Source file " + source
                                     + " doesn't exist.");
        }

        String m = Mode.BOTH;
        if (mode != null) {
            m = mode.getValue();
        }

        if (target == null && !m.equals(Mode.CANDLE)) {
            throw new BuildException("You must specify the target if you want"
                                     + " to run light.");
        }

        Collection lightSources = null;
        if (!m.equals(Mode.LIGHT)) {
            lightSources = doCandle();
        } else {
            lightSources = new HashSet();
            if (source != null) {
                lightSources.add(source);
            }
            if (sources.size() > 0) {
                lightSources.addAll(grabFiles(sources));
            }
        }

        if (!m.equals(Mode.CANDLE)) {
            Collection moreLightSources = Collections.EMPTY_SET;
            if (moreSources.size() > 0) {
                moreLightSources = grabFiles(moreSources);
            }
            doLight(lightSources, moreLightSources);
        }
    }

    /**
     * Invoke candle on all sources that are newer than their targets.
     *
     * @return a set of File objects pointing to the generated files.
     */
    private Collection doCandle() {
        Set s = new HashSet();
        if (source != null) {
            s.add(source);
        }
        if (sources != null) {
            s.addAll(grabFiles(sources));
        }
        Set ms = new HashSet();
        if (moreSources != null) {
            ms.addAll(grabFiles(moreSources));
        }

        Set toProcess = new HashSet();
        Set generatedTargets = new HashSet();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            File thisSource = (File) iter.next();
            File t = getTarget(thisSource);
            generatedTargets.add(t);
            if (isOutOfDate(t, thisSource, ms)) {
                toProcess.add(thisSource);
            }
        }
        if (toProcess.size() != 0) {
            runCandle(toProcess);
            return generatedTargets;
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Invoke light on all sources that are newer than their targets.
     */
    private void doLight(Collection lightSources,
                         Collection moreLightSources) {
        Set tmp = new HashSet(lightSources);
        tmp.addAll(moreLightSources);
        if (isOutOfDate(target, tmp)) {
            runLight(lightSources);
        }
    }

    /**
     * Run candle passing all files of the collection on the command line.
     */
    private void runCandle(Collection s) {
        run(wixExecutable("candle.exe"), s, null, wixobjDestDir, parameters);
    }

    /**
     * Run light passing all files of the collection on the command line.
     */
    private void runLight(Collection s) {
        run(wixExecutable("light.exe"), s, target, null, Collections.EMPTY_LIST);
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
                     File runInDir, Collection params) {
        DotNetExecTask exec = DotNetExecTask.getTask(this, vm, 
                                                     executable, null);
        if (runInDir != null) {
            exec.setDir(runInDir);
        }

        exec.setFailonerror(true);
        exec.setTaskType("wix");

        exec.createArg().setValue("/nologo");

        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            File f = (File) iter.next();
            exec.createArg().setValue(f.getAbsolutePath());
        }
        if (target != null) {
            exec.createArg().setValue("/out");
            exec.createArg().setValue(target.getAbsolutePath());
        }
        
        iter = params.iterator();
        while (iter.hasNext()) {
            AbstractBuildTask.Property p =
                (AbstractBuildTask.Property) iter.next();
            exec.createArg().setValue("-d" + p.getName() + "=" + p.getValue());
        }

        exec.execute();
    }

    /**
     * Is t older than s or any of the files in list?
     */
    private boolean isOutOfDate(File t, File s, Collection l) {
        return t.lastModified() < s.lastModified() || isOutOfDate(t, l);
    }

    /**
     * Is t older than any of the files in list?
     */
    private boolean isOutOfDate(File t, Collection l) {
        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            File f = (File) iter.next();
            if (t.lastModified() < f.lastModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Turn the fileset collection into a list of Files.
     */
    private Collection grabFiles(Collection s) {
        Set r = new HashSet();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            FileSet fs = (FileSet) iter.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] f = ds.getIncludedFiles();
            File base = fs.getDir(getProject());
            for (int i = 0; i < f.length; i++) {
                r.add(new File(base, f[i]));
            }
        }
        return r;
    }

    /**
     * Generates the name of a candle target from the source file.
     *
     * <p>Simply chops of the extension, adds .wixobj and calculates
     * the absolute path based on wixobjDestDir.</p>
     */
    private File getTarget(File s) {
        String name = s.getName();
        int dot = name.lastIndexOf(".");
        if (dot > -1) {
            name = name.substring(0, dot) + ".wixobj";
        } else {
            name = name + ".wixobj";
        }

        return wixobjDestDir == null
            ? new File(name) : new File(wixobjDestDir, name);
    }

    public static class Mode extends EnumeratedAttribute {
        private final static String CANDLE = "candle";
        private final static String LIGHT = "light";
        private final static String BOTH = "both";

        public Mode() {
            super();
        }

        public String[] getValues() {
            return new String[] {CANDLE, LIGHT, BOTH,};
        }
    }
}