package org.ralala.android.ssh.server.net;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Network handler
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface INetworkBroadcaster {

    void networkStatus(final boolean down);
}
