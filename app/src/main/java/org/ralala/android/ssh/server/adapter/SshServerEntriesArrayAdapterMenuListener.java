package org.ralala.android.ssh.server.adapter;

import org.ralala.android.ssh.server.net.SshServerEntry;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Called when the user click on the entry menu
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface SshServerEntriesArrayAdapterMenuListener {

  boolean onMenuStart(SshServerEntry sse);
  boolean onMenuStop(SshServerEntry sse);
  void onMenuRemove(SshServerEntry sse);

}
