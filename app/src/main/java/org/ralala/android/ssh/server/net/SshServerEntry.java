package org.ralala.android.ssh.server.net;


import android.util.Log;

import org.ralala.android.ssh.server.net.SshServer;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Define a server entry
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SshServerEntry {
  private boolean    started     = false;
  private String     name        = null;
  private int        port        = 0;
  private int        id          = 1;
  private static int sid         = 1;
  private SshServer  srv        = null;
  private String     username   = null;
  private String     password   = null;


  public SshServerEntry(final String name, final int port, final String username, final String password) {
    setName(name);
    setPort(port);
    setUsername(username);
    setPassword(password);
    id = sid;
    sid++;
  }

  public SshServer getSshServer() {
    if(srv == null)
      srv = new SshServer();
    return srv;
  }

  public boolean isAuthAnonymous() {
    return username == null;
  }

  public int getId() {
    return id;
  }

  public void setUsername(final String username) {
    if(username == null || username.equals("null")) this.username = null;
    else this.username = username;
  }

  public void setPassword(final String password) {
    if(password == null || password.equals("null")) this.password = null;
    else this.password = password;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public void setStarted(final boolean started) {
    this.started = started;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public int getPort() {
    return port;
  }

  public boolean isStarted() {
    return started;
  }
}
