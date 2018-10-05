package fr.ralala.sshd.net.ptm;

import android.os.Environment;

import java.io.File;

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
  public static final String DEFAULT_HOME = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getParentFile().getAbsolutePath();
  public static final String DEFAULT_USER = "shell";
  public static final String DEFAULT_GROUP = "sdcard_rw";
  public static final String DEFAULT_SHELL = "/system/bin/sh";
  public static final boolean DEFAULT_OVERRIDE = true;
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
    mHome = home == null ? "" : home;
    mUser = user == null ? "" : user;
    mGroup = group == null ? "" : group;
    mShell = shell == null ? "" : shell;
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
