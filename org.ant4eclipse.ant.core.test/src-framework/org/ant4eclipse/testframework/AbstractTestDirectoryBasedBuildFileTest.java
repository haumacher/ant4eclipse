/**********************************************************************
 * Copyright (c) 2005-2009 ant4eclipse project team.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nils Hartmann, Daniel Kasmeroglu, Gerd Wuetherich
 **********************************************************************/
package org.ant4eclipse.testframework;

import org.ant4eclipse.lib.core.service.ServiceRegistry;
import org.ant4eclipse.lib.core.service.ServiceRegistryAccess;
import org.ant4eclipse.lib.core.util.Utilities;
import org.apache.tools.ant.BuildFileTest;

import java.io.File;

/**
 * Baseclass for all buildfile-based tests in the platform layer
 * 
 * @author Nils Hartmann (nils@nilshartmann.net)
 */
public abstract class AbstractTestDirectoryBasedBuildFileTest extends BuildFileTest {

  /** - */
  private TestDirectory _testWorkspace;

  /**
   * Creates the Test Environment before execution of a test case
   */
  @Override
  public void setUp() {
    this._testWorkspace = new TestDirectory();
  }

  /**
   * Disposes the test environment and resets the {@link ServiceRegistry}
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this._testWorkspace.dispose();
    ServiceRegistryAccess.reset();
    this._testWorkspace = null;
  }

  /**
   * Returns the name of the build file for a test case.
   * 
   * <p>
   * The build file must stay in the same folder as the test class
   * 
   * @param unqualifiedName
   *          The name of the build file without folders
   * @return The name of the build file
   */
  protected String getProjectBuildFile(String unqualifiedName) {
    return getClass().getPackage().getName().replace('.', '/') + "/" + unqualifiedName;
  }

  @Override
  protected void runTest() throws Throwable {
    try {
      super.runTest();
    } catch (Throwable t) {
      System.err.println(getName() + " throws exception (" + t + "). Output:");
      t.printStackTrace();
      System.err.println(getError());
      throw t;
    }
  }

  /**
   * Copies the given build.xml-file from the classpath to the testenvironment's root directory and configures the ant
   * project
   * 
   * <p>
   * This methods sets the build project property <tt>workspaceDir</tt> to the workspace directory
   * 
   * @param unqualifiedBuildFileName
   *          the unqualified name of the build file, that must be accessible from classpath
   * @throws Exception
   * @see {@link #configureProject(String)}
   */
  protected void setupBuildFile(String unqualifiedBuildFileName) throws Exception {
    String qualifiedBuildFileName = getProjectBuildFile(unqualifiedBuildFileName);
    StringBuffer buffer = Utilities.readTextContent("/" + qualifiedBuildFileName, Utilities.ENCODING, true);
    String buildFileContent = buffer.toString();
    File buildFile = this._testWorkspace.createFile(unqualifiedBuildFileName, buildFileContent);
    configureProject(buildFile.getAbsolutePath());
    getProject().setProperty("workspaceDir", this._testWorkspace.getRootDir().getAbsolutePath());
  }

  /**
   * Returns a {@link TestDirectory} for this test case.
   * 
   * @return
   */
  protected TestDirectory getTestDirectory() {
    return this._testWorkspace;
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  protected File getTestDirectoryRootDir() {
    return this._testWorkspace.getRootDir();
  }

  /**
   * <p>
   * </p>
   * 
   * @param string
   * @return
   */
  public String normalize(String string) {
    return string.replace('/', File.separatorChar).replace('\\', File.separatorChar);
  }

} /* ENDCLASS */
