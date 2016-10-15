package org.ralala.android.ssh.server;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.ralala.android.ssh.server.net.SshServerEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SshServerApplication extends Application {
  private List<SshServerEntry> entries  = null;

  public SshServerApplication() {
    entries = new ArrayList<SshServerEntry>();
  }

  public void loadEntries() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    Map<String, ?> li = prefs.getAll();
    for(Map.Entry<String, ?> entry : li.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue().toString();
      String v[] = value.split("\\|");
      entries.add(new SshServerEntry(name, Integer.parseInt(Tools.b64decode(v[0])), Tools.b64decode(v[1]), Tools.b64decode(v[2])));
    }
    Collections.sort(entries, new Comparator<SshServerEntry>() {
      @Override
      public int compare(SshServerEntry a, SshServerEntry b) {
        return a.getName().compareTo(b.getName());
      }
    });
  }

  public void saveEntries() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor e = prefs.edit();
    e.clear();
    for(SshServerEntry se : entries) {
      String s = Tools.b64encode(""+se.getPort()) + "|" + Tools.b64encode(""+se.getUsername()) + "|" + Tools.b64encode(""+se.getPassword());
      e.putString(se.getName(), s);
    }
    e.apply();
  }

  public void addEntry(final SshServerEntry se) {
    entries.add(se);
    Collections.sort(entries, new Comparator<SshServerEntry>() {
      @Override
      public int compare(SshServerEntry a, SshServerEntry b) {
        return a.getName().compareTo(b.getName());
      }
    });
    saveEntries();
  }

  public void deleteEntry(final SshServerEntry se) {
    entries.remove(se);
    saveEntries();
  }

  public List<SshServerEntry> getEntries() {
    return entries;
  }

  public boolean containsEntry(final String name, final String port) {
    for(int i = 0; i < entries.size(); i++) {
      SshServerEntry se = entries.get(i);
      if(se.getName().equals(name) || (""+se.getPort()).equals(port)) return true;
    }
    return false;
  }


  public static SshServerApplication getApp(final Context c) {
    return (SshServerApplication) c.getApplicationContext();
  }

}
