package fr.ralala.sshd.net;

import android.util.Log;

//import org.apache.log4j.BasicConfigurator;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.forward.ForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;


import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.sshd.net.ptm.ShellConfiguration;
import fr.ralala.sshd.net.ptm.ShellPTM;


/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Representation of a SSH server
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServer {
  private org.apache.sshd.server.SshServer mSshServer = null;
  private SshServerEntry mSshServerEntry = null;
  private ShellConfiguration mShellConfiguration;

  public SshServer(ShellConfiguration shellConfiguration) {
    mShellConfiguration = shellConfiguration;
    /* Fix home */
    System.setProperty("user.home", mShellConfiguration.getHome());
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Sets the SSH server entry.
   * @param sse SshServerEntry to set.
   */
  public void setSshServerEntry(final SshServerEntry sse) {
    mSshServerEntry = sse;
  }

  /**
   * Starts the SSH server.
   * @param host The host address to use.
   * @throws Throwable If a network exception occurs.
   */
  public void start(final String host) throws Throwable {
    if (mSshServer != null) return;
    //BasicConfigurator.configure();
    /* create the server instance and set the default port */
    mSshServer = org.apache.sshd.server.SshServer.setUpDefaultServer();
    //mSshServer.getProperties().put(org.apache.sshd.server.SshServer.IDLE_TIMEOUT, "10000");
    mSshServer.getProperties().put(org.apache.sshd.server.SshServer.AUTH_TIMEOUT, "100000");
    mSshServer.getProperties().put("welcome-banner", "Welcome to android SshServer\n");
    mSshServer.setHost(host);
    //mSshServer.setReuseAddress(true);
    mSshServer.setPort(mSshServerEntry.getPort());
    mSshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    /* fix shell */
    if(mShellConfiguration == null)
      mShellConfiguration = new ShellConfiguration(ShellConfiguration.DEFAULT_HOME,
          ShellConfiguration.DEFAULT_USER, ShellConfiguration.DEFAULT_GROUP,
          ShellConfiguration.DEFAULT_SHELL, ShellConfiguration.DEFAULT_OVERRIDE);
    mSshServer.setShellFactory(new ShellPTM(mShellConfiguration,
        "-i", "-l"));

    List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>();
    if (mSshServerEntry.isAuthAnonymous())
      /* Allow anonymous connections */
      userAuthFactories.add(UserAuthNoneFactory.INSTANCE);
    else {
      userAuthFactories.add(UserAuthPasswordFactory.INSTANCE);
      mSshServer.setPasswordAuthenticator((username, password, session) ->
          mSshServerEntry.getUsername().equals(username) && mSshServerEntry.getPassword().equals(password)
      );
    }
    mSshServer.setUserAuthFactories(userAuthFactories);
    /* Enable SFTP commands */
    mSshServer.setCommandFactory(new ScpCommandFactory());
    List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
    namedFactoryList.add(new SftpSubsystemFactory());
    mSshServer.setSubsystemFactories(namedFactoryList);
    mSshServer.setForwardingFilter(new ForwardingFilter() {
      @Override
      public boolean canForwardX11(Session session, String requestType) {
        return true;
      }
      @Override
      public boolean canListen(SshdSocketAddress address, Session session) {
        return true;
      }
      @Override
      public boolean canConnect(Type type, SshdSocketAddress address, Session session) {
        return true;
      }
      @Override
      public boolean canForwardAgent(Session session, String requestType) {
        return true;
      }
    });
    /* start the server */
    mSshServer.start();
  }

  /**
   * Stops the SSH server.
   */
  public void stop() {
    if (mSshServer != null) {
      try {
        mSshServer.stop(true);
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
      mSshServer = null;
    }
  }

}
