package fr.ralala.sshd.net.ptm;



import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Native Process using PTM character file.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class NativeProcessPTM {
	
  static {
    System.loadLibrary("native-process-ptm");
  }

  @SuppressWarnings("FieldCanBeLocal")
  private int mPid = 0; /* Used from JNI code */
  private int mDescriptor = -1; /* Used from JNI code */
  private FileOutputStream mStdIn;
  private FileInputStream mStdErr;
  private FileInputStream mStdOut;
  private boolean mAlive = false;

  /**
   * Creates a process.
   * @param cmd The command to execute.
   * @param args The list of arguments.
   * @param env The exec env.
   * @return the file descriptor of the started process.
   *
   */
  public static NativeProcessPTM create(String cmd, String [] args, String [] env) throws IOException {
    try {
      NativeProcessPTM me = create0(cmd, args, env);
      if (me != null) {
        @SuppressWarnings("JavaReflectionMemberAccess")
        Constructor<FileDescriptor> fileDescriptorConstructor = FileDescriptor.class.getDeclaredConstructor(Integer.TYPE);
        fileDescriptorConstructor.setAccessible(true);
        FileDescriptor fd = fileDescriptorConstructor.newInstance(me.mDescriptor);
        fileDescriptorConstructor.setAccessible(false);
        me.mStdOut = new FileInputStream(fd);
        me.mStdErr = new FileInputStream(fd);
        me.mStdIn = new FileOutputStream(fd);
        me.mAlive = true;
      }
      return me;
    } catch(Throwable t) {
      Log.e(NativeProcessPTM.class.getSimpleName(), "Exception: " + t.getMessage(), t);
      throw new IOException(t);
    }
  }

  /**
   * Kill the current process.
   */
  @SuppressWarnings("WeakerAccess")
  public void kill() {
    android.os.Process.killProcess(mPid);
    mAlive = false;
  }

  /**
   * Creates a forked process.
   * @param cmd The command to execute.
   * @param args The list of arguments.
   * @param env The exec env.
   * @return this.
   *
   */
  private static native NativeProcessPTM create0(String cmd, String [] args, String [] env) throws IOException;

  /**
   * Wait for the process associated with the receiver to finish executing.
   * @return The exit value of the Process being waited on
   *
   */
  public native int waitFor();

  /**
   * Check if the underlying shell is still alive.
   * @return boolean
   */
  @SuppressWarnings("WeakerAccess")
  public boolean isAlive() {
    return mAlive;
  }

  /**
   * Returns the OutputStream of stderr.
   * @return InputStream
   */
  @SuppressWarnings("WeakerAccess")
  public InputStream getErrorStream() {
    return mStdErr;
  }

  /**
   * Returns the OutputStream of stdout.
   * @return InputStream
   */
  @SuppressWarnings("WeakerAccess")
  public InputStream getOutputStream() {
    return mStdOut;
  }

  /**
   * Returns the OutputStream of stdin.
   * @return OutputStream
   */
  @SuppressWarnings("WeakerAccess")
  public OutputStream getInputStream() {
    return mStdIn;
  }
}
