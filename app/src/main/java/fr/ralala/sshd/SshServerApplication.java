package fr.ralala.sshd;

import android.app.Application;
import android.content.Context;

import fr.ralala.sshd.net.SshServerEntryFactory;


/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Application context
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServerApplication extends Application {
  private SshServerEntryFactory mSshServerEntryFactory;

  /**
   * Called when the application is created.
   */
  public void onCreate() {
    super.onCreate();
    mSshServerEntryFactory = new SshServerEntryFactory(this);
  }

  /**
   * Returns the factory used for SSH server entries.
   * @return SshServerEntryFactory
   */
  public SshServerEntryFactory getSshServerEntryFactory() {
    return mSshServerEntryFactory;
  }

  /**
   * Returns this instance.
   * @param c The Android context.
   * @return SshServerApplication
   */
  public static SshServerApplication getApp(final Context c) {
    return (SshServerApplication) c.getApplicationContext();
  }

}
