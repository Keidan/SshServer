package fr.ralala.sshd.net;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Network status listener
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public interface NetworkStatusListener {

  /**
   * Handle the network status.
   * @param down True if down.
   */
  void onNetworkStatus(final boolean down);
}
