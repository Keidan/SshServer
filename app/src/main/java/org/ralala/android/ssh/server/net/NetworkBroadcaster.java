package org.ralala.android.ssh.server.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.ralala.android.ssh.server.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;
/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Network helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class NetworkBroadcaster extends BroadcastReceiver {
  private static final Pattern  IPV4_PATTERN  = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
  private static final String   UNKNOWN       = "UNKNOWN";
  private INetworkBroadcaster   handler       = null;
  private TelephonyManager      tm            = null;
  private Context               context       = null;
  private ConnectivityManager   cm            = null;

  public NetworkBroadcaster(final Context c, final INetworkBroadcaster handler) {
    this.context = c;
    this.handler = handler;
    tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public void load() {
    /* Start broadcaster for the Wi-Fi and the Bluetooth states changes */
    IntentFilter filters = new IntentFilter();
    filters.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    context.registerReceiver(this, filters);
    PhoneStateListener callStateListener = new PhoneStateListener(){
      public void onDataConnectionStateChanged(int state){
        switch(state){
          case TelephonyManager.DATA_CONNECTED:
            if(!isWifi()) {
              Log.d(getClass().getSimpleName(), "DATA_CONNECTED");
              handler.networkStatus(false);
            }
            break;
          case TelephonyManager.DATA_DISCONNECTED:
            if(!isWifi()) {
              Log.d(getClass().getSimpleName(), "DATA_DISCONNECTED");
              handler.networkStatus(true);
            }
            break;
        }
      }
    };
    tm.listen(callStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
  }

  public void unload() {
    context.unregisterReceiver(this);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String action = intent.getAction();
    if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
      int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
      switch(state) {
        case WifiManager.WIFI_STATE_DISABLED:
        case WifiManager.WIFI_STATE_DISABLING:
          Log.d(getClass().getSimpleName(), "WIFI_STATE_DISABLED/WIFI_STATE_DISABLING");
          handler.networkStatus(true);
          break;
        case WifiManager.WIFI_STATE_ENABLED:
        case WifiManager.WIFI_STATE_ENABLING:
          Log.d(getClass().getSimpleName(), "WIFI_STATE_ENABLED/WIFI_STATE_ENABLING");
          handler.networkStatus(false);
          break;
      }
    }
  }

  public static String intToIp(int i) {
    return ( i & 0xFF) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ((i >> 24 ) & 0xFF );
 }

  public static boolean isIPv4Address(final String ip) {
    return IPV4_PATTERN.matcher(ip).matches();
  }

  private boolean isWifi() {
    NetworkInfo info = cm.getActiveNetworkInfo();
    return (info != null && info.getType() == ConnectivityManager.TYPE_WIFI);
  }

  public String getCurrentIpAddress() {
    try {
      // Skip if no connection, or background data disabled
      if (cm.getActiveNetworkInfo() == null || !cm.getBackgroundDataSetting()) return UNKNOWN;
      if (isWifi()) {
        Log.d(getClass().getSimpleName(), "wifi mode");
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        final String ip = intToIp(wifiInfo.getIpAddress());
        Log.d(getClass().getSimpleName(), "wifi: " + ip);
        if (isIPv4Address(ip)) {
          return ip;
        } else {
          final int delim = ip.indexOf('%'); // drop ip6 port suffix
          return delim < 0 ? ip : ip.substring(0, delim);
        }
      } else {
        Log.d(getClass().getSimpleName(), "gsm mode");
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
          NetworkInterface intf = en.nextElement();
          for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!inetAddress.isLoopbackAddress() && Inet4Address.class.isInstance(inetAddress)) {
              Log.d(getClass().getSimpleName(), "gsm: " + inetAddress.getHostAddress().toString());
              return inetAddress.getHostAddress().toString();
            }
          }
        }
      }
    } catch (final Exception ex) {
      Log.e(NetworkBroadcaster.class.getSimpleName(),
          "Exception in Get IP Address: " + ex.getMessage(), ex);
    }
    return "UNKNOWN";
  }

}
