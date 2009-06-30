package org.ant4eclipse.platform.internal.model.resource.role;

import org.ant4eclipse.core.Lifecycle;
import org.ant4eclipse.core.configuration.Ant4EclipseConfiguration;
import org.ant4eclipse.core.logging.A4ELogging;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class NatureNicknameRegistry implements Lifecycle {

  /** The prefix of properties that holds a nature nickname */
  public final static String  NATURE_NICKNAME_PREFIX = "natureNickname";

  /** all known nicknames */
  private Map<String, String> _nicknames;

  /** - */
  private boolean             _initialized           = false;

  /**
   * {@inheritDoc}
   */
  public void initialize() {
    if (this._initialized) {
      return;
    }

    // get all properties that defines a nature nickname
    Iterable<String[]> natureNicknameEntries = Ant4EclipseConfiguration.Helper.getAnt4EclipseConfiguration()
        .getAllProperties(NATURE_NICKNAME_PREFIX);

    final Map<String, String> nicknames = new HashMap<String, String>();
    for (String[] natureNicknameEntry : natureNicknameEntries) {
      String nature = natureNicknameEntry[0];
      String nickname = natureNicknameEntry[1];
      A4ELogging.trace("Register nickname '%s' for nature '%s'", nickname, nature);
      nicknames.put(nickname, nature);
    }

    this._nicknames = nicknames;

    this._initialized = true;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInitialized() {
    return this._initialized;
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {
    // nothing to do...
  }

  /**
   * <p>
   * Returns <code>true</code> if the registry contains a nature for the given nickname.
   * </p>
   * 
   * @param nickname
   * @return
   */
  public boolean hasNatureForNickname(String nickname) {
    return this._nicknames.containsKey(nickname);
  }

  /**
   * <p>
   * Returns the nature for the given nickname or <code>null</code>.
   * </p>
   * 
   * @param nickname
   * @return
   */
  public String getNatureForNickname(String nickname) {
    return this._nicknames.get(nickname);
  }
}