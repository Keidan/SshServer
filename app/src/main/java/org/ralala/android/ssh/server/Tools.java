package org.ralala.android.ssh.server;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import org.ralala.android.ssh.server.net.SshServerEntry;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Tools functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Tools {

  public static void vibrate250ms(final Context c) {
    vibrate(c, 250);
  }

  public static void vibrate(final Context c, final int delay) {
    Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
    v.vibrate(delay);
  }

  public static void showNotification(final Context c, final NotificationManager mNM, final SshServerEntry sse) {
    // In this sample, we'll use the same text for the ticker and the expanded notification
    CharSequence text = sse.getName();
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0, new Intent(c, SshServerActivity.class), 0);
    String msg = "Started on port " + sse.getPort();
    if(!sse.isAuthAnonymous())
      msg += " for user name " + sse.getUsername();
    // Set the info for the views that show in the notification panel.
    Notification notification = new Notification.Builder(c)
        .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
        .setTicker(text)  // the status text
        .setWhen(System.currentTimeMillis())  // the time stamp
        .setContentTitle(text)  // the label of the entry
        .setContentText(msg)  // the contents of the entry
        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
        .build();

    // Send the notification.
    mNM.notify(sse.getId(), notification);
  }

  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(c.getResources().getString(message));
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });
    alertDialog.show();
  }


  public static void toast(final Context c, final String message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = c.getResources().getDrawable(R.mipmap.ic_launcher);
      final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
      final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
      tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
      tv.setCompoundDrawablePadding(5);
    }
    toast.show();
  }

  public static void toast(final Context c, final int message) {
    toast(c, c.getResources().getString(message));
  }


  public static final void forcePopupMenuIcons(final PopupMenu popup) {
    try {
      Field[] fields = popup.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(popup);
          Class<?> classPopupHelper = Class.forName(menuPopupHelper
              .getClass().getName());
          Method setForceIcons = classPopupHelper.getMethod(
              "setForceShowIcon", boolean.class);
          setForceIcons.invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String b64encode(final String s) {
    try {
      byte[] data = s == null ? "null".getBytes("UTF-8") : s.getBytes("UTF-8");
      return Base64.encodeToString(data, Base64.NO_WRAP);
    } catch (UnsupportedEncodingException e1) {
      return "bnVsbA=="; /* null */
    }
  }

  public static String b64decode(final String s) {
    try {
      byte[] data = Base64.decode(s.getBytes("UTF-8"), Base64.NO_WRAP);
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return "null";
    }
  }
}
