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
  private boolean mOverride;

  /**
   *
   * @param home The default home directory.
   * @param user The default user name.
   * @param group The default group name.
   * @param override If true, the variable will be overloaded, otherwise the configuration value will be ignored.
   */
  public ShellConfiguration(String home, String user, String group, boolean override) {
    mHome = home;
    mUser = user;
    mGroup = group;
    mOverride = override;
  }

  /**
   * Sets the override state.
   * @param override If true, the variable will be overloaded, otherwise the configuration value will be ignored.
   */
  public void setOverride(boolean override) {
    mOverride = override;
  }

  /**
   * Sets the default home directory.
   * @param home The default home directory.
   */
  public void setHome(String home) {
    mHome = home;
  }

  /**
   * Sets the default user name.
   * @param user The default user name.
   */
  public void setUser(String user) {
    mUser = user;
  }

  /**
   * Sets the default group name.
   * @param group The default group name.
   */
  public void setGroup(String group) {
    mGroup = group;
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
}
