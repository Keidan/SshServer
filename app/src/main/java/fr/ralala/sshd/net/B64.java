package fr.ralala.sshd.net;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Base 64 management.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class B64 {

  private B64() {
  }

  /**
   * Encodes the input string in base64.
   * @param s The plain text to encode.
   * @return base64.
   */
  public static String encode(final String s) {
    try {
      byte[] data = s == null ? "null".getBytes("UTF-8") : s.getBytes("UTF-8");
      return Base64.encodeToString(data, Base64.NO_WRAP);
    } catch (UnsupportedEncodingException e1) {
      return "bnVsbA=="; /* null */
    }
  }

  /**
   * Decodes the input string that is encoded in base64.
   * @param s The base64 string to decode.
   * @return plain text.
   */
  public static String decode(final String s) {
    try {
      byte[] data = Base64.decode(s.getBytes("UTF-8"), Base64.NO_WRAP);
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return "null";
    }
  }
}
