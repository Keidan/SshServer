package fr.ralala.sshd.net;


import fr.ralala.sshd.net.ptm.ShellConfiguration;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Define a server entry
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServerEntry {
  private boolean mStarted = false;
  private String mName = null;
  private int mPort = 0;
  private int mId;
  private static int mStaticId = 1;
  private SshServer mSshServer = null;
  private String mUsername = null;
  private String mPassword = null;
  private ShellConfiguration mShellConfiguration;


  public SshServerEntry(final String name, final int port, final String username, final String password, ShellConfiguration shellConfiguration) {
    setName(name);
    setPort(port);
    setUsername(username);
    setPassword(password);
    mShellConfiguration = shellConfiguration;
    mId = mStaticId;
    mStaticId++;
  }

  /**
   * Returns the shell configuration.
   * @return ShellConfiguration.
   */
  public ShellConfiguration getShellConfiguration() {
    return mShellConfiguration;
  }

  /**
   * Returns the instance of the SSH server.
   * @return SshServer
   */
  public SshServer getSshServer() {
    if (mSshServer == null)
      mSshServer = new SshServer(mShellConfiguration);
    return mSshServer;
  }

  /**
   * Tests whether the connection should be anonymous or authenticated.
   * @return boolean
   */
  public boolean isAuthAnonymous() {
    return mUsername == null || mUsername.isEmpty();
  }

  /**
   * Returns the server ID.
   * @return int
   */
  public int getId() {
    return mId;
  }

  /**
   * Sets the user name to use.
   * @param username The new user name.
   */
  private void setUsername(final String username) {
    if (username == null || username.equals("null")) mUsername = null;
    else mUsername = username;
  }

  /**
   * Sets the user password to use.
   * @param password The new password.
   */
  private void setPassword(final String password) {
    if (password == null || password.equals("null")) mPassword = null;
    else mPassword = password;
  }

  /**
   * Sets the entry name.
   * @param name The new name.
   */
  public void setName(final String name) {
    mName = name;
  }

  /**
   * Sets the port used by the server.
   * @param port The new port.
   */
  private void setPort(final int port) {
    mPort = port;
  }

  /**
   * Changes the server state.
   * @param started The new state.
   */
  public void setStarted(final boolean started) {
    mStarted = started;
  }

  /**
   * Returns the entry name.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Returns the user name to use.
   * @return String
   */
  public String getUsername() {
    return mUsername;
  }

  /**
   * Returns the user password to use.
   * @return String
   */
  public String getPassword() {
    return mPassword;
  }

  /**
   * Returns the port used by the server.
   * @return int
   */
  public int getPort() {
    return mPort;
  }

  /**
   * Tests whether the server is started or not.
   * @return boolean.
   */
  public boolean isStarted() {
    return mStarted;
  }
}
