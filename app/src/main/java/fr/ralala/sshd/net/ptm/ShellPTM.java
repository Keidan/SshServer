package fr.ralala.sshd.net.ptm;

import android.util.Log;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InvertedShell;
import org.apache.sshd.server.shell.InvertedShellWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * A shell implementation using PTMX character file.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ShellPTM implements Factory<Command>, InvertedShell {
  private static final int FLAG_HOME = 1 << 2;
  private static final int FLAG_USER = 1 << 3;
  private static final int FLAG_GROUP = 1 << 4;
  private static final int FLAG_SHELL = 1 << 5;
  private static final int FLAG_LOGNAME = 1 << 6;
  private static final int FLAG_ALL = FLAG_HOME|FLAG_USER|FLAG_GROUP|FLAG_SHELL|FLAG_LOGNAME;
  private String mCmd;
  private String [] mArgs;
  private NativeProcessPTM mNativeProcessPTM;
  private ShellConfiguration mShellConfiguration;


  /**
   * Constructs a new Shell.
   * @param cfg The default configuration.
   * @param cmd The command to execute.
   * @param args The command arguments.
   */
  @SuppressWarnings("WeakerAccess")
  public ShellPTM(ShellConfiguration cfg, String cmd, String... args) {
    mCmd = cmd;
    mArgs = args;
    mShellConfiguration = cfg;
  }

  /**
   * Creates the InvertedShellWrapper for this custom shell.
   * @return InvertedShellWrapper
   */
  @Override
  public Command create() {
    return new InvertedShellWrapper(this);
  }

  /**
   * This method is called by the SSH server to destroy the command because the client has disconnected somehow.
   */
  @Override
  public void destroy() {
    mNativeProcessPTM.kill();
  }

  /**
   * Retrieve the exit value of the shell. This method must only be called when the shell is not alive anymore.
   * @return int
   */
  @Override
  public int exitValue() {
    return mNativeProcessPTM.waitFor();
  }


  /**
   * Returns the error stream of the shell.
   * @return InputStream
   */
  @Override
  public InputStream getErrorStream() {
    return mNativeProcessPTM.getErrorStream();
  }

  /**
   * Returns the output stream used to feed the shell (stdin).
   * @return OutputStream
   */
  @Override
  public OutputStream getInputStream() {
    return mNativeProcessPTM.getInputStream();
  }

  /**
   * Returns the output stream of the shell.
   * @return InputStream
   */
  @Override
  public InputStream getOutputStream() {
    return mNativeProcessPTM.getOutputStream();
  }

  /**
   * Check if the underlying shell is still alive.
   * @return boolean
   */
  @Override
  public boolean isAlive() {
    return mNativeProcessPTM.isAlive();
  }

  /**
   * Starts the command execution. All streams must have been set before calling this method.
   * @param env The environment to use.
   * @throws IOException If an exception is thrown.
   */
  @Override
  public void start(Environment env) throws IOException {
    Map<String, String> e = env.getEnv();
    List<String> envP = new ArrayList<>();
    int flags = FLAG_ALL;
    for (Map.Entry<String, String> entry : e.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if(key.equals("HOME") && !mShellConfiguration.isOverride()) {
        flags &= ~FLAG_HOME;
      } else if(key.equals("USER") && !mShellConfiguration.isOverride()) {
        flags &= ~FLAG_USER;
      } else if(key.equals("LOGNAME") && !mShellConfiguration.isOverride()) {
        flags &= ~FLAG_LOGNAME;
      } else if(key.equals("GROUP") && !mShellConfiguration.isOverride()) {
        flags &= ~FLAG_GROUP;
      } else if(key.equals("SHELL") && !mShellConfiguration.isOverride()) {
        flags &= ~FLAG_SHELL;
      }
      envP.add(key + "=" + value);
    }
    if((flags & FLAG_HOME) != 0)
      envP.add("HOME=" + mShellConfiguration.getHome());
    if((flags & FLAG_USER) != 0)
      envP.add("USER=" + mShellConfiguration.getUser());
    if((flags & FLAG_LOGNAME) != 0)
      envP.add("LOGNAME=" + mShellConfiguration.getUser());
    if((flags & FLAG_GROUP) != 0)
      envP.add("GROUP=" + mShellConfiguration.getGroup());
    if((flags & FLAG_SHELL) != 0)
      envP.add("SHELL=" + mShellConfiguration.getShell());
    mNativeProcessPTM = NativeProcessPTM.create(mCmd, mArgs, envP.toArray(new String[] { }));
  }

  /**
   * Sets the server session.
   * @param session The ServerSession in which this shell will be executed.
   */
  @Override
  public void setSession(ServerSession session) {
    Log.e(getClass().getSimpleName(), "Session: " + session);
  }
}
