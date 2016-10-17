package org.ralala.android.ssh.server.net;

import android.util.Log;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.net.InetSocketAddress;
import java.security.Security;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Representation of a SSH server
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SshServer {
  private org.apache.sshd.SshServer sshd  = null;
  private SshServerEntry            sse   = null;

  public SshServer() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public void setSshServerEntry(final SshServerEntry sse) {
    this.sse = sse;
  }

  public void start(final String host) throws Exception {
    if ( sshd != null ) return;
    /* create the server instance and set the default port */
    sshd = org.apache.sshd.SshServer.setUpDefaultServer();
    sshd.getProperties().put(org.apache.sshd.SshServer.IDLE_TIMEOUT, "10000");
    sshd.getProperties().put(org.apache.sshd.SshServer.AUTH_TIMEOUT, "10000");
    sshd.getProperties().put("welcome-banner", "Welcome to android SshServer\n");
    sshd.setHost(host);
    sshd.setReuseAddress(true);
    sshd.setPort(sse.getPort());
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    /* fix shell */
    sshd.setShellFactory(new ProcessShellFactory(new String[]{"/system/bin/sh", "-i", "-l"},
      EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr)));


    List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
    if(sse.isAuthAnonymous())
      /* Allow anonymous connections */
      userAuthFactories.add(new UserAuthNone.Factory());
    else {
      userAuthFactories.add(new UserAuthPassword.Factory());
      sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String username, String password, ServerSession session) {
          return sse.getUsername().equals(username) && sse.getPassword().equals(password);
        }
      });
    }
    sshd.setUserAuthFactories(userAuthFactories);
    /* Enable SFTP commands */
    sshd.setCommandFactory(new ScpCommandFactory());
    List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
    namedFactoryList.add(new SftpSubsystem.Factory());
    sshd.setSubsystemFactories(namedFactoryList);
    sshd.setForwardingFilter(new ForwardingFilter() {
      @Override
      public boolean canForwardAgent(ServerSession serverSession) {
        return true;
      }

      @Override
      public boolean canForwardX11(ServerSession serverSession) {
        return true;
      }

      @Override
      public boolean canListen(InetSocketAddress inetSocketAddress, ServerSession serverSession) {
        return true;
      }

      @Override
      public boolean canConnect(InetSocketAddress inetSocketAddress, ServerSession serverSession) {
        return true;
      }
    });
    /* start the server */
    sshd.start();
  }

  public void stop() {
    if(sshd != null) {
      try{
        sshd.stop(true);
      }catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
      sshd = null;
    }
  }

}
