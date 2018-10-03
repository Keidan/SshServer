package fr.ralala.sshd.net.ptm;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * A basic shell configuration.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ShellConfiguration {
  private String mHome;
  private String mUser;
  private String mGroup;
  private String mShell;
  private boolean mOverride;

  /**
   *
   * @param home The default home directory.
   * @param user The default user name.
   * @param group The default group name.
   * @param shell The default shell path.
   * @param override If true, the variable will be overloaded, otherwise the configuration value will be ignored.
   */
  public ShellConfiguration(String home, String user, String group, String shell, boolean override) {
    mHome = home;
    mUser = user;
    mGroup = group;
    mShell = shell;
    mOverride = override;
  }


  /**
   * Returns the override state.
   */
  public boolean isOverride() {
    return mOverride;
  }

  /**
   * Returns the default home directory.
   */
  public String getHome() {
    return mHome;
  }

  /**
   * Returns the default user name.
   */
  public String getUser() {
    return mUser;
  }

  /**
   * Returns the default group name.
   */
  public String getGroup() {
    return mGroup;
  }

  /**
   * Returns the default shell path.
   */
  public String getShell() {
    return mShell;
  }
}
