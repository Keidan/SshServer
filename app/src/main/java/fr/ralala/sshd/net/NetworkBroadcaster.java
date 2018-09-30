package fr.ralala.sshd.net;

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
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Network helper functions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class NetworkBroadcaster extends BroadcastReceiver {
  private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
  private NetworkStatusListener mNetworkStatusListener;
  private TelephonyManager mTelephonyManager;
  private Context mContext;
  private ConnectivityManager mConnectivityManager;

  public NetworkBroadcaster(final Context c, final NetworkStatusListener networkStatusListener) {
    mContext = c;
    mNetworkStatusListener = networkStatusListener;
    mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  /**
   * Starts broadcaster for the Wi-Fi states changes.
   */
  public void load() {
    /* Start broadcaster for the Wi-Fi and the Bluetooth states changes */
    IntentFilter filters = new IntentFilter();
    filters.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    mContext.registerReceiver(this, filters);
    PhoneStateListener callStateListener = new PhoneStateListener() {
      public void onDataConnectionStateChanged(int state) {
        switch (state) {
          case TelephonyManager.DATA_CONNECTED:
            if (!isWifi()) {
              Log.d(getClass().getSimpleName(), "DATA_CONNECTED");
              mNetworkStatusListener.onNetworkStatus(false);
            }
            break;
          case TelephonyManager.DATA_DISCONNECTED:
            if (!isWifi()) {
              Log.d(getClass().getSimpleName(), "DATA_DISCONNECTED");
              mNetworkStatusListener.onNetworkStatus(true);
            }
            break;
        }
      }
    };
    mTelephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
  }

  /**
   * Stops broadcaster for the Wi-Fi states changes.
   */
  public void unload() {
    mContext.unregisterReceiver(this);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String action = intent.getAction();
    if (action != null && action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
      int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
      switch (state) {
        case WifiManager.WIFI_STATE_DISABLED:
        case WifiManager.WIFI_STATE_DISABLING:
          Log.d(getClass().getSimpleName(), "WIFI_STATE_DISABLED/WIFI_STATE_DISABLING");
          mNetworkStatusListener.onNetworkStatus(true);
          break;
        case WifiManager.WIFI_STATE_ENABLED:
        case WifiManager.WIFI_STATE_ENABLING:
          Log.d(getClass().getSimpleName(), "WIFI_STATE_ENABLED/WIFI_STATE_ENABLING");
          mNetworkStatusListener.onNetworkStatus(false);
          break;
      }
    }
  }

  /**
   * Converts an IP to digital format into an IPv4 String.
   * @param i The IP address to convert.
   * @return String (IPv4).
   */
  public static String intToIp(int i) {
    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
  }

  /**
   * Tests if the input IP address corresponds to an IP address in v4 format.
   * @param ip The IP address to test.
   * @return boolean
   */
  public static boolean isIPv4Address(final String ip) {
    return IPV4_PATTERN.matcher(ip).matches();
  }

  /**
   * Tests if the connectivity type is WiFi.
   * @return boolean
   */
  private boolean isWifi() {
    NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
    return (info != null && info.getType() == ConnectivityManager.TYPE_WIFI);
  }

  /**
   * Returns the current IP address.
   * @return IP or R.string.unknown
   */
  public String getCurrentIpAddress() {
    final String unknown = mContext.getString(R.string.unknown);
    try {
      // Skip if no connection
      if (mConnectivityManager.getActiveNetworkInfo() == null) return unknown;
      if (isWifi()) {
        Log.d(getClass().getSimpleName(), "wifi mode");
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(wifi == null) return unknown;
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
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
          NetworkInterface intf = en.nextElement();
          for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!inetAddress.isLoopbackAddress() && Inet4Address.class.isInstance(inetAddress)) {
              Log.d(getClass().getSimpleName(), "gsm: " + inetAddress.getHostAddress());
              return inetAddress.getHostAddress();
            }
          }
        }
      }
    } catch (final Exception ex) {
      Log.e(NetworkBroadcaster.class.getSimpleName(),
          "Exception in Get IP Address: " + ex.getMessage(), ex);
    }
    return unknown;
  }

}
