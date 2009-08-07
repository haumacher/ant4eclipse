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
package org.ant4eclipse.pydt.internal.tools;

import org.ant4eclipse.core.Assert;
import org.ant4eclipse.core.ant.ExtendedBuildException;
import org.ant4eclipse.core.logging.A4ELogging;
import org.ant4eclipse.core.service.ServiceRegistry;

import org.ant4eclipse.pydt.PydtFailures;
import org.ant4eclipse.pydt.model.RawPathEntry;
import org.ant4eclipse.pydt.model.ReferenceKind;
import org.ant4eclipse.pydt.model.ResolvedContainerEntry;
import org.ant4eclipse.pydt.model.ResolvedLibraryEntry;
import org.ant4eclipse.pydt.model.ResolvedOutputEntry;
import org.ant4eclipse.pydt.model.ResolvedPathEntry;
import org.ant4eclipse.pydt.model.ResolvedProjectEntry;
import org.ant4eclipse.pydt.model.ResolvedRuntimeEntry;
import org.ant4eclipse.pydt.model.ResolvedSourceEntry;
import org.ant4eclipse.pydt.model.pyre.PythonRuntime;
import org.ant4eclipse.pydt.model.pyre.PythonRuntimeRegistry;
import org.ant4eclipse.pydt.tools.PathEntryRegistry;

import java.io.File;

/**
 * General resolved for python.
 * 
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@Kasisoft.net)
 */
public class PydtResolver {

  private PathEntryRegistry     _pathregistry;

  private PythonRuntimeRegistry _runtimeregistry;

  /**
   * Initialises this resolver.
   */
  public PydtResolver() {
    _pathregistry = ServiceRegistry.instance().getService(PathEntryRegistry.class);
    _runtimeregistry = ServiceRegistry.instance().getService(PythonRuntimeRegistry.class);
  }

  /**
   * Resolves the supplied entry to get access to a source folder.
   * 
   * @param entry
   *          The unresolved entry pointing to a source folder. Not <code>null</code>.
   * 
   * @return A resolved entry identifying a source folder. Not <code>null</code>.
   */
  public ResolvedPathEntry resolve(final RawPathEntry entry) {
    Assert.notNull(entry);
    ResolvedPathEntry result = _pathregistry.getResolvedPathEntry(entry);
    if (result == null) {
      result = newResolvedEntry(entry);
      _pathregistry.registerResolvedPathEntry(entry, result);
    }
    return result;
  }

  /**
   * Resolves the supplied entries to get access to the source folders.
   * 
   * @param entries
   *          The unresolved entries pointing to the source folders. Not <code>null</code>.
   * 
   * @return The resolved entries identifying the source folders. Not <code>null</code>.
   */
  public ResolvedPathEntry[] resolve(final RawPathEntry[] entries) {
    Assert.notNull(entries);
    final ResolvedPathEntry[] result = new ResolvedPathEntry[entries.length];
    for (int i = 0; i < entries.length; i++) {
      result[i] = resolve(entries[i]);
    }
    return result;
  }

  private ResolvedPathEntry newResolvedEntry(final RawPathEntry entry) {
    if (entry.getKind() == ReferenceKind.Container) {
      return newResolvedContainerEntry(entry);
    } else if (entry.getKind() == ReferenceKind.Library) {
      return newResolvedLibraryEntry(entry);
    } else if (entry.getKind() == ReferenceKind.Output) {
      return newResolvedOutputEntry(entry);
    } else if (entry.getKind() == ReferenceKind.Project) {
      return newResolvedProjectEntry(entry);
    } else if (entry.getKind() == ReferenceKind.Runtime) {
      return newResolvedRuntimeEntry(entry);
    } else /* if (entry.getKind() == ReferenceKind.Source) */{
      return newResolvedSourceEntry(entry);
    }
  }

  /**
   * Creates a new record representing a path container.
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent a path container. Not <code>null</code>.
   */
  private ResolvedContainerEntry newResolvedContainerEntry(final RawPathEntry entry) {
    return new ResolvedContainerEntry(new File[0]);
  }

  /**
   * Creates a new record representing a library.
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent a library. Not <code>null</code>.
   */
  private ResolvedLibraryEntry newResolvedLibraryEntry(final RawPathEntry entry) {
    return new ResolvedLibraryEntry(new File(entry.getValue()));
  }

  /**
   * Creates a new record representing an output folder.
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent an output folder. Not <code>null</code>.
   */
  private ResolvedOutputEntry newResolvedOutputEntry(final RawPathEntry entry) {
    return new ResolvedOutputEntry(entry.getValue());
  }

  /**
   * Creates a new record representing a project.
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent a python project Not <code>null</code>.
   */
  private ResolvedProjectEntry newResolvedProjectEntry(final RawPathEntry entry) {
    String value = entry.getValue();
    if ((value.charAt(0) != '/') || (value.length() == 1)) {
      /** @todo [02-Aug-2009:KASI] We need to cause an exception here. */
      A4ELogging.warn("The raw projectname '%s' does not start conform to the required format '/' <identifier> !",
          value);
      return null;
    }
    return new ResolvedProjectEntry(value.substring(1));
  }

  /**
   * Creates a new record representing a source folder.
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent a source folder. Not <code>null</code>.
   */
  private ResolvedSourceEntry newResolvedSourceEntry(final RawPathEntry entry) {
    return new ResolvedSourceEntry(entry.getValue());
  }

  /**
   * Creates a new record representing a single runtime..
   * 
   * @param entry
   *          The raw entry. Not <code>null</code>.
   * 
   * @return A newly created record used to represent a single runtime. Not <code>null</code>.
   */
  private ResolvedRuntimeEntry newResolvedRuntimeEntry(final RawPathEntry entry) {
    final String value = entry.getValue();
    PythonRuntime runtime = null;
    if (value.length() == 0) {
      // use the default runtime
      runtime = _runtimeregistry.getRuntime();
    } else {
      // use the selected runtime
      runtime = _runtimeregistry.getRuntime(value);
    }
    if (runtime == null) {
      // now runtime available
      throw new ExtendedBuildException(PydtFailures.UNKNOWN_PYTHON_RUNTIME, value);
    }
    return new ResolvedRuntimeEntry(runtime.getVersion(), runtime.getLibraries());
  }

} /* ENDCLASS */
