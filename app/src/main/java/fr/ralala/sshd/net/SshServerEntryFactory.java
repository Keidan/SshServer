package fr.ralala.sshd.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.sshd.net.ptm.ShellConfiguration;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * SSH server entry factory.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServerEntryFactory {
  private static final String VERSION_TAG = "vtag1_";
  private List<SshServerEntry> mEntries;
  private SharedPreferences mSharedPreferences;


  public SshServerEntryFactory(Context context) {
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    mEntries = new ArrayList<>();
  }

  /**
   * Loads the list of entries.
   */
  public void load() {
    Map<String, ?> li = mSharedPreferences.getAll();
    li.forEach((name, value) -> {
      if(name.startsWith(VERSION_TAG) && name.length() > VERSION_TAG.length()) {
        try {
          /* port|username|userpassword|home|user|group|shell|override */
          String v[] = value.toString().split("\\|");
          ShellConfiguration cfg = new ShellConfiguration(
              B64.decode(v[3]), B64.decode(v[4]),
              B64.decode(v[5]), B64.decode(v[6]),  Boolean.parseBoolean(B64.decode(v[7])));
          SshServerEntry entry = new SshServerEntry(name.substring(VERSION_TAG.length()),
              Integer.parseInt(B64.decode(v[0])),
              B64.decode(v[1]),
              B64.decode(v[2]), cfg);
          mEntries.add(entry);
        } catch(Exception e) {
          Log.e(getClass().getSimpleName(), "Exception with tag name '" + name + "': " + e.getMessage(), e);
        }
      } else {
        /* old version support. */
        ShellConfiguration cfg = new ShellConfiguration(
            ShellConfiguration.DEFAULT_HOME, ShellConfiguration.DEFAULT_USER,
            ShellConfiguration.DEFAULT_GROUP, ShellConfiguration.DEFAULT_SHELL,
            ShellConfiguration.DEFAULT_OVERRIDE);
        String v[] = value.toString().split("\\|");
        SshServerEntry entry = new SshServerEntry(name, Integer.parseInt(B64.decode(v[0])),
            v.length > 1 ? B64.decode(v[1]) : null,
            v.length > 2 ? B64.decode(v[2]) : null, cfg);
        mEntries.add(entry);

        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.remove(name);
        e.putString(VERSION_TAG + name, formatEntry(entry));
        e.apply();
      }
    });
    mEntries.sort(Comparator.comparing(SshServerEntry::getName));
  }

  /**
   * Saves the list of entries.
   */
  public void save() {
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.clear();
    mEntries.sort(Comparator.comparing(SshServerEntry::getId));
    for (SshServerEntry se : mEntries) {
      e.putString(VERSION_TAG + se.getName(), formatEntry(se));
    }
    e.apply();
  }

  /**
   * Formats the entry to String.
   * @param entry The entry to format.
   * @return String.
   */
  private String formatEntry(SshServerEntry entry) {
    /* port|username|userpassword|home|user|group|shell|override */
    ShellConfiguration cfg = entry.getShellConfiguration();
    return B64.encode("" + entry.getPort())
        + "|" + B64.encode(validateString(entry.getUsername()))
        + "|" + B64.encode(validateString(entry.getPassword()))
        + "|" + B64.encode(validateString(cfg.getHome()))
        + "|" + B64.encode(validateString(cfg.getUser()))
        + "|" + B64.encode(validateString(cfg.getGroup()))
        + "|" + B64.encode(validateString(cfg.getShell()))
        + "|" + B64.encode("" + cfg.isOverride());
  }

  /**
   * Validate the input string.
   * @param str The string to validate.
   * @return String
   */
  private String validateString(String str) {
    return (str != null && str.isEmpty()) ? null : str;
  }

  /**
   * Adds an entry.
   * @param se The entry to add.
   */
  public void add(final SshServerEntry se) {
    mEntries.add(se);
    mEntries.sort(Comparator.comparing(SshServerEntry::getName));
    save();
  }

  /**
   * Removes an entry.
   * @param se The entry to remove.
   */
  public void remove(final SshServerEntry se) {
    mEntries.remove(se);
    save();
  }

  /**
   * Returns the entry to the specified index.
   * @param index The specified index.
   * @return SshServerEntry
   */
  public SshServerEntry get(int index) {
    return mEntries.get(index);
  }
  /**
   * Returns the list of entries.
   * @return List<SshServerEntry>
   */
  public List<SshServerEntry> list() {
    return mEntries;
  }

  /**
   * Tests if the name and port are contained in the internal entries list.
   * @param name The name to test.
   * @param port The port to test.
   * @return boolean.
   */
  public boolean contains(final String name, final String port) {
    final AtomicBoolean found = new AtomicBoolean(false);
    mEntries.forEach((sse) -> {
      if (sse.getName().equals(name) || String.valueOf(sse.getPort()).equals(port))
        found.set(true);
    });
    return found.get();
  }
}
