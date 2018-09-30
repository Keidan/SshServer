package fr.ralala.sshd.ui.adapter;

import fr.ralala.sshd.net.SshServerEntry;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Called when the user click on the entry menu.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public interface SshServerEntriesArrayAdapterMenuListener {

  /**
   * Called when the user click on the start menu.
   * @param sse The associated entry.
   * @return true started.
   */
  boolean onMenuStart(SshServerEntry sse);

  /**
   * Called when the user click on the stop menu.
   * @param sse The associated entry.
   * @return true stopped.
   */
  boolean onMenuStop(SshServerEntry sse);

  /**
   * Called when the user click on the edit menu.
   * @param sse The associated entry.
   */
  void onMenuEdit(SshServerEntry sse);

  /**
   * Called when the user click on the remove menu.
   * @param sse The associated entry.
   */
  void onMenuRemove(SshServerEntry sse);

}
