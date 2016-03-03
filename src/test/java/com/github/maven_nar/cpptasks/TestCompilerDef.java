/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.maven_nar.cpptasks;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.github.maven_nar.cpptasks.compiler.CommandLineCompilerConfiguration;
import com.github.maven_nar.cpptasks.compiler.Compiler;
import com.github.maven_nar.cpptasks.compiler.LinkType;
import com.github.maven_nar.cpptasks.gcc.GccCCompiler;
import com.github.maven_nar.cpptasks.msvc.MsvcCCompiler;
import com.github.maven_nar.cpptasks.types.CompilerArgument;
import com.github.maven_nar.cpptasks.types.ConditionalPath;
import com.github.maven_nar.cpptasks.types.DefineArgument;
import com.github.maven_nar.cpptasks.types.DefineSet;
import com.github.maven_nar.cpptasks.types.IncludePath;
import com.github.maven_nar.cpptasks.types.SystemIncludePath;
import com.github.maven_nar.cpptasks.types.UndefineArgument;

/**
 * Tests for CompilerDef.
 */
public final class TestCompilerDef extends TestProcessorDef {
  /**
   * Sets the name attribute.
   *
   * @param compiler
   *          compiler under test
   * @param name
   *          compiler name
   */
  private static void setCompilerName(final CompilerDef compiler, final String name) {
    final CompilerEnum compilerName = new CompilerEnum();
    compilerName.setValue(name);
    compiler.setName(compilerName);
  }

  /**
   * Constructor.
   *
   * @param name
   *          test name
   */
  public TestCompilerDef(final String name) {
    super(name);
  }

  /**
   * Creates a new processor.
   *
   * @return new processor
   */
  @Override
  protected ProcessorDef create() {
    return new CompilerDef();
  }

  /**
   * Gets the command line arguments that precede filenames.
   *
   * This method filters out <i>-m</i> options because they are
   * platform-specific options of the GNU compiler suite.
   *
   * @param processor
   *          processor under test
   * @return command line arguments
   */
  @Override
  protected String[] getPreArguments(final ProcessorDef processor) {
    final String[] result = ((CommandLineCompilerConfiguration) getConfiguration(processor)).getPreArguments();

    // filter out -m (i.e. platform-specific) options
    int j = -1;
    for (int i = 0; i < result.length; i++) {
      if (result[i].startsWith("-m")) {
        continue;
      }
      if (i != ++j) {
        result[j] = result[i];
      }
    }
    if (++j == result.length) {
      return result;
    }
    final String[] filtered = new String[j];
    System.arraycopy(result, 0, filtered, 0, j);
    return filtered;
  }

  /**
   * Tests that the classname attribute in the base compiler is effective.
   */
  public void testExtendsClassname() {
    final CompilerDef baseCompiler = new CompilerDef();
    baseCompiler.setClassname("com.github.maven_nar.cpptasks.msvc.MsvcCCompiler");
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    extendedCompiler.setExceptions(true);
    final String[] preArgs = getPreArguments(extendedCompiler);
    assertEquals("/EHsc", preArgs[2]);
  }

  /**
   * Tests that compilerarg's contained in the base compiler definition are
   * effective.
   */
  public void testExtendsCompilerArgs() {
    final CompilerDef baseLinker = new CompilerDef();
    final CompilerArgument linkerArg = new CompilerArgument();
    linkerArg.setValue("/base");
    baseLinker.addConfiguredCompilerArg(linkerArg);
    final CompilerDef extendedLinker = (CompilerDef) createExtendedProcessorDef(baseLinker);
    final String[] preArgs = getPreArguments(extendedLinker);
    // FREEHEP, passes extra option
    assertEquals(3, preArgs.length);
    assertEquals("/base", preArgs[0]);
  }

  /**
   * Tests that defineset's contained in the base compiler definition are
   * effective.
   */
  public void testExtendsDefineSet() {
    final CompilerDef baseCompiler = new CompilerDef();
    final DefineSet defSet = new DefineSet();
    final DefineArgument define = new DefineArgument();
    define.setName("foo");
    define.setValue("bar");
    defSet.addDefine(define);
    baseCompiler.addConfiguredDefineset(defSet);
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    final String[] preArgs = getPreArguments(extendedCompiler);
    // BEGINFREEHEP, passes extra option
    assertEquals(3, preArgs.length);
    assertEquals("-Dfoo=bar", preArgs[2]);
    // ENDFREEHEP
  }

  /**
   * Tests that the extend attribute of the base compiler definition is
   * effective.
   */
  public void testExtendsExceptions() {
    final CompilerDef baseCompiler = new CompilerDef();
    baseCompiler.setExceptions(true);
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    setCompilerName(extendedCompiler, "msvc");
    final String[] preArgs = getPreArguments(extendedCompiler);
    assertEquals("/EHsc", preArgs[2]);
  }

  /**
   * Tests if a fileset enclosed in the base compiler definition is effective.
   *
   * @throws IOException
   *           if unable to create or delete a temporary file
   */
  public void testExtendsFileSet() throws IOException {
    super.testExtendsFileSet(File.createTempFile("cpptaskstest", ".cpp"));
  }

  /**
   * Tests that includepath's contained in the base compiler definition are
   * effective.
   */
  public void testExtendsIncludePath() {
    final CompilerDef baseCompiler = new CompilerDef();
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    final IncludePath path = baseCompiler.createIncludePath();
    path.setPath("/tmp");
    final String[] preArgs = getPreArguments(extendedCompiler);
    // BEGINFREEHEP, passes extra option
    assertEquals(3, preArgs.length);
    assertEquals("-I", preArgs[2].substring(0, 2));
    // ENDFREEHEP
  }

  /**
   * Tests that the multithread attribute of the base compiler definition is
   * effective.
   */
  public void testExtendsMultithreaded() {
    final CompilerDef baseCompiler = new CompilerDef();
    baseCompiler.setMultithreaded(false);
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    setCompilerName(extendedCompiler, "msvc");
    final CCTask cctask = new CCTask();
    final LinkType linkType = new LinkType();
    final File objDir = new File("dummy");
    cctask.setObjdir(objDir);
    linkType.setStaticRuntime(true);
    final CommandLineCompilerConfiguration config = (CommandLineCompilerConfiguration) extendedCompiler
        .createConfiguration(cctask, linkType, null, null, null);
    final String[] preArgs = config.getPreArguments();
    assertEquals("/ML", preArgs[3]);
  }

  /**
   * Tests that the name attribute in the base compiler is effective.
   */
  public void testExtendsName() {
    final CompilerDef baseCompiler = new CompilerDef();
    setCompilerName(baseCompiler, "msvc");
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    extendedCompiler.setExceptions(true);
    final String[] preArgs = getPreArguments(extendedCompiler);
    assertEquals("/EHsc", preArgs[2]);
  }

  /**
   * Tests if the rebuild attribute of the base compiler definition is
   * effective.
   *
   */
  public void testExtendsRebuild() {
    testExtendsRebuild(new CompilerDef());
  }

  /**
   * Tests that sysincludepath's contained in the base compiler definition are
   * effective.
   */
  public void testExtendsSysIncludePath() {
    final CompilerDef baseCompiler = new CompilerDef();
    final CompilerDef extendedCompiler = (CompilerDef) createExtendedProcessorDef(baseCompiler);
    final SystemIncludePath path = baseCompiler.createSysIncludePath();
    path.setPath("/tmp");
    final String[] preArgs = getPreArguments(extendedCompiler);
    System.out.println("Class: " + baseCompiler + " and: " + extendedCompiler);
    // BEGINFREEHEP, passes extra option
    assertEquals(3, preArgs.length);
    assertEquals("-isystem", preArgs[2].substring(0, 8));
    // ENDFREEHEP
  }

  /**
   * This method tests CompilerDef.getActiveDefines.
   *
   * A CompilerDef is created similar to what would be created for
   *
   * <cc><defineset><define name="DEBUG" if="debug"/> <define name="NDEBUG"
   * unless="debug"/> </defineset> </cc>
   *
   * Then getActiveDefines is called for a project without and with the
   * "debug" property defined. Return value from getActiveDefines should
   * contain one member
   */
  public void testGetActiveDefines() {
    final Project project = new org.apache.tools.ant.Project();
    final CompilerDef def = new CompilerDef();
    def.setProject(project);
    final DefineSet defset = new DefineSet();
    final DefineArgument arg1 = new DefineArgument();
    arg1.setName("DEBUG");
    arg1.setIf("debug");
    defset.addDefine(arg1);
    final DefineArgument arg2 = new DefineArgument();
    arg2.setName("NDEBUG");
    arg2.setUnless("debug");
    defset.addDefine(arg2);
    def.addConfiguredDefineset(defset);
    //
    // Evaluate without "debug" set
    //
    UndefineArgument[] activeArgs = def.getActiveDefines();
    assertEquals(1, activeArgs.length);
    assertEquals("NDEBUG", activeArgs[0].getName());
    //
    // Set the "debug" property
    //
    project.setProperty("debug", "");
    activeArgs = def.getActiveDefines();
    assertEquals(1, activeArgs.length);
    assertEquals("DEBUG", activeArgs[0].getName());
  }

  /**
   * This method tests CompilerDef.getActiveIncludePath.
   *
   * A CompilerDef is created similar to what would be created for
   *
   * <cc><includepath location=".." if="debug"/> </cc>
   *
   * and is evaluate for a project without and without "debug" set
   */
  public void testGetActiveIncludePaths() {
    final Project project = new org.apache.tools.ant.Project();
    final CompilerDef def = new CompilerDef();
    def.setProject(project);
    final ConditionalPath path = def.createIncludePath();
    path.setLocation(new File(".."));
    path.setIf("debug");
    //
    // Evaluate without "debug" set
    //
    String[] includePaths = def.getActiveIncludePaths();
    assertEquals(0, includePaths.length);
    //
    // Set the "debug" property
    //
    project.setProperty("debug", "");
    includePaths = def.getActiveIncludePaths();
    assertEquals(1, includePaths.length);
  }

  /**
   * Tests that setting classname to the Gcc compiler is effective.
   */
  public void testGetGcc() {
    final CompilerDef compilerDef = (CompilerDef) create();
    compilerDef.setClassname("com.github.maven_nar.cpptasks.gcc.GccCCompiler");
    final Compiler comp = (Compiler) compilerDef.getProcessor();
    assertNotNull(comp);
    assertSame(GccCCompiler.getInstance(), comp);
  }

  /**
   * Tests that setting classname to the MSVC compiler is effective.
   */
  public void testGetMSVC() {
    final CompilerDef compilerDef = (CompilerDef) create();
    compilerDef.setClassname("com.github.maven_nar.cpptasks.msvc.MsvcCCompiler");
    final Compiler comp = (Compiler) compilerDef.getProcessor();
    assertNotNull(comp);
    assertSame(MsvcCCompiler.getInstance(), comp);
  }

  /**
   * Tests that setting classname to an bogus class name results in a
   * BuildException.
   */
  public void testUnknownClass() {
    final CompilerDef compilerDef = (CompilerDef) create();
    try {
      compilerDef.setClassname("com.github.maven_nar.cpptasks.bogus.BogusCompiler");
    } catch (final BuildException ex) {
      return;
    }
    fail("Exception not thrown");
  }

  /**
   * Test that setting classname to a class that doesn't support Compiler
   * throws a BuildException.
   *
   */
  public void testWrongType() {
    final CompilerDef compilerDef = (CompilerDef) create();
    try {
      compilerDef.setClassname("com.github.maven_nar.cpptasks.msvc.MsvcLinker");
    } catch (final BuildException ex) {
      return;
    }
    fail("Exception not thrown");
  }
}
