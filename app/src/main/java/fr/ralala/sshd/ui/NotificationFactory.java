package fr.ralala.sshd.ui;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import fr.ralala.sshd.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.ralala.sshd.net.SshServerEntry;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Notification factory.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class NotificationFactory {
  //use constant ID for notification used as group summary
  private static final int SUMMARY_ID = 0;
  private static final String GROUP_KEY = "fr.ralala.sshd.NotificationFactory";
  private static final String NFY_CHANNEL_ID = "MyChannelId_0";
  private static List<SshServerEntry> mEntries = new ArrayList<>();


  /**
   * Shows a system notification.
   * @param c The Android context.
   * @param sse The current entry to display in the notification.
   */
  public static void show(final Context c, SshServerEntry sse) {
    if (mEntries.isEmpty()) {
      mEntries.add(sse);
      NotificationManager manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
      if (manager != null) {
        NotificationChannel prevChannel = manager.getNotificationChannel(NFY_CHANNEL_ID);
        if (prevChannel == null) {
          createChannel(c, manager);
        }
      }
      buildNotification(c, sse);
    } else {
      mEntries.forEach((e) -> NotificationManagerCompat.from(c).cancel(e.getId()));
      if (sse != null)
        mEntries.add(sse);
      NotificationManager manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
      if (manager != null) {
        manager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_KEY, GROUP_KEY));
      }
      NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
      mEntries.forEach((e) -> style.addLine(buildText(c, e)));
      Notification summaryNotification =
          new NotificationCompat.Builder(c, NFY_CHANNEL_ID)
              .setContentTitle(c.getString(R.string.app_name))
              .setSmallIcon(R.mipmap.ic_launcher)
              //build summary info into InboxStyle template
              .setStyle(style
                  .setBigContentTitle(c.getString(R.string.servers_list))
                  .setSummaryText(mEntries.size() + " " + c.getString(R.string.servers)))
              //specify which group this notification belongs to
              .setGroup(GROUP_KEY)
              //set this notification as the summary for the group
              .setGroupSummary(true)
              .build();
      notificationManager.notify(SUMMARY_ID, summaryNotification);
    }
  }

  /**
   * Hides the notification.
   * @param c The Android context.
   * @param sse The current entry to hide.
   */
  public static void hide(final Context c, final SshServerEntry sse) {
    mEntries.remove(sse);
    NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      if (mEntries.isEmpty()) {
        NotificationManagerCompat.from(c).cancel(sse.getId());
        notificationManager.deleteNotificationChannel(NFY_CHANNEL_ID);
      } else {
        NotificationManagerCompat.from(c).cancel(SUMMARY_ID);
        notificationManager.deleteNotificationChannelGroup(GROUP_KEY);
        if(mEntries.size() == 1)
          mEntries.forEach((e) -> buildNotification(c, e));
        else
          show(c, null);
      }
    }
  }


  /**
   * Builds a single notification.
   * @param c The Android context.
   * @param sse The current entry.
   */
  private static void buildNotification(final Context c, final SshServerEntry sse) {
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0, new Intent(c, SshServerActivity.class), 0);
    Notification notification = new NotificationCompat.Builder(c, NFY_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(sse.getName())
        .setContentText(buildText(c, sse))
        .setOngoing(true)
        .setWhen(System.currentTimeMillis())
        .setContentIntent(contentIntent)
        .build();
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
    notificationManager.notify(sse.getId(), notification);
  }

  /**
   * Builds the text message.
   * @param c The Android context.
   * @param sse The current entry.
   * @return String
   */
  private static String buildText(final Context c, final SshServerEntry sse) {
    String msg = c.getString(R.string.notification_text_1, sse.getId(), sse.getPort());
    if (!sse.isAuthAnonymous())
      msg += c.getString(R.string.notification_text_2, sse.getUsername());
    return msg;
  }

  /**
   * Creates the notification channel.
   * @param c The Android context.
   * @param notificationManager The instance to the notification manager.
   */
  private static void createChannel(final Context c, NotificationManager notificationManager) {
    int importance = NotificationManager.IMPORTANCE_DEFAULT;
    NotificationChannel channel = new NotificationChannel(NFY_CHANNEL_ID, c.getString(R.string.app_name), importance);
    channel.enableLights(true);
    channel.enableVibration(true);
    channel.setLightColor(Color.GRAY);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManager.createNotificationChannel(channel);
  }
}
