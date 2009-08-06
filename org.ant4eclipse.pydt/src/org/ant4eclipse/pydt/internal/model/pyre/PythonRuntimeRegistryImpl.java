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
package org.ant4eclipse.pydt.internal.model.pyre;

import org.ant4eclipse.core.Assert;
import org.ant4eclipse.core.ant.ExtendedBuildException;
import org.ant4eclipse.core.logging.A4ELogging;
import org.ant4eclipse.core.util.Utilities;

import org.ant4eclipse.pydt.model.pyre.PythonRuntime;
import org.ant4eclipse.pydt.model.pyre.PythonRuntimeRegistry;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

/**
 * Implementation of a registry for {@link PythonRuntime} instances.
 * 
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@Kasisoft.net)
 */
public class PythonRuntimeRegistryImpl implements PythonRuntimeRegistry {

  private static final String[]      EXENAMES                 = new String[] { "python", "pythonw", "ipy", "ipyw",
      "jython"                                               };

  private static final String[]      EXESUFFICES              = new String[] { "", ".exe", ".bat" };

  private static final String        MSG_NODEFAULTRUNTIME     = "A default python runtime could not be determined !";

  private static final String        MSG_INVALIDDEFAULTID     = "A python runtime with the id '%s' needs to be registered first !";

  private static final String        MSG_CANONICALFILE        = "Failed to get a canonical filesystem location from the path '%s' !";

  private static final String        MSG_DUPLICATERUNTIME     = "An attempt has been made to register a python runtime using the id '%s' with different locations: '%s' <-> '%s' !";

  private static final String        MSG_REPEATEDREGISTRATION = "A python runtime with the id '%s' and the location '%s' has been registered multiple times !";

  private static final String        MSG_REGISTEREDRUNTIME    = "Registered runtime with id '%s' for the location '%s'.";

  private static final String        MSG_MISSINGPYTHONLISTER  = "The python script 'lister.py' is not available on the classpath (org/ant4eclipse/pydt) !";

  private Map<String, PythonRuntime> _runtimes                = new Hashtable<String, PythonRuntime>();

  private String                     _defaultid               = null;

  private File                       _pythonlister            = null;

  public PythonRuntimeRegistryImpl() {
    URL url = getClass().getResource("/org/ant4eclipse/pydt/lister.py");
    if (url == null) {
      throw new BuildException(MSG_MISSINGPYTHONLISTER);
    }
    try {
      _pythonlister = File.createTempFile("lister", ".py");
    } catch (IOException ex) {
      throw new BuildException(ex);
    }
    Utilities.copy(url, _pythonlister);
  }

  /**
   * Tries to determine the location of a python interpreter.
   * 
   * @param location
   *          The location of a python installation. Not <code>null</code>.
   * 
   * @return The filesystem location of an interpreter or <code>null</code> if none could be found.
   */
  private File lookupInterpreter(final File location) {
    for (String exename : EXENAMES) {
      for (String suffix : EXESUFFICES) {
        final File candidate = new File(location, exename + suffix);
        if (candidate.isFile()) {
          return candidate;
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasRuntime(String id) {
    Assert.nonEmpty(id);
    return _runtimes.containsKey(id);
  }

  /**
   * {@inheritDoc}
   */
  public void registerRuntime(String id, File location) {

    Assert.nonEmpty(id);
    Assert.notNull(location);

    try {
      location = location.getCanonicalFile();
    } catch (IOException ex) {
      throw new ExtendedBuildException(MSG_CANONICALFILE, ex, location);
    }

    final PythonRuntime existing = getRuntime(id);
    if (existing != null) {

      // check the current setting

      if (!location.equals(existing.getLocation())) {
        // same id for different locations is not allowed
        throw new ExtendedBuildException(MSG_DUPLICATERUNTIME, id, existing.getLocation(), location);
      } else {
        // same record, so skip this registration while creating a message only
        A4ELogging.debug(MSG_REPEATEDREGISTRATION, id, location);
        return;
      }

    }

    // register the new runtime but we need to identify the corresponding libraries
    final File interpreter = lookupInterpreter(location);
    if (interpreter == null) {
      throw new RuntimeException();
    }

    // StringBuffer output = new StringBuffer();
    // int result = Utilities.execute(interpreter, output, _pythonlister.getAbsolutePath());
    //
    // System.err.println(output);

    final PythonRuntime newruntime = new PythonRuntimeImpl(id, location);
    A4ELogging.debug(MSG_REGISTEREDRUNTIME, id, location);
    _runtimes.put(id, newruntime);

  }

  /**
   * {@inheritDoc}
   */
  public void setDefaultRuntime(String id) {
    Assert.nonEmpty(id);
    if (!hasRuntime(id)) {
      throw new ExtendedBuildException(MSG_INVALIDDEFAULTID, id);
    }
    _defaultid = id;
  }

  /**
   * {@inheritDoc}
   */
  public PythonRuntime getRuntime() {
    if (_defaultid != null) {
      return _runtimes.get(_defaultid);
    } else if (_runtimes.size() == 1) {
      return _runtimes.values().iterator().next();
    }
    throw new BuildException(MSG_NODEFAULTRUNTIME);
  }

  /**
   * {@inheritDoc}
   */
  public PythonRuntime getRuntime(String id) {
    Assert.nonEmpty(id);
    return _runtimes.get(id);
  }

  // public static final void main(String[] args) throws Exception {
  // Properties props = new Properties();
  // File f = new File("Q:/workspace-a4e/sample/dummy/org/ant4eclipse/ant4eclipse-configuration.properties");
  // props.load(new FileInputStream(f));
  // System.err.println(props);
  // Ant4EclipseConfigurator.configureAnt4Eclipse(props);
  // // Ant4EclipseConfigurationImpl config = new Ant4EclipseConfigurationImpl(props);
  // // ServiceRegistry.configure(new PropertiesBasedServiceRegistryConfiguration(config));
  // PythonRuntimeRegistryImpl impl = new PythonRuntimeRegistryImpl();
  // // impl.registerRuntime("def", new File("K:/programme/python/2.6"));
  // // impl.registerRuntime("def", new File("K:/programme/python/3.1"));
  // impl.registerRuntime("def", new File("K:/programme/ironpython/2.0"));
  // // impl.registerRuntime("def", new File("K:/programme/jython/2.5"));
  // }

} /* ENDCLASS */
