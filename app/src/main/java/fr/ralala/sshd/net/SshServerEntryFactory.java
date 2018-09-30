package fr.ralala.sshd.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
      String v[] = value.toString().split("\\|");
      mEntries.add(new SshServerEntry(name, Integer.parseInt(B64.decode(v[0])),
          v.length > 1 ? B64.decode(v[1]) : null,
          v.length > 2 ? B64.decode(v[2]) : null));
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
      String s = B64.encode("" + se.getPort()) + "|" + B64.encode("" + se.getUsername()) + "|" + B64.encode("" + se.getPassword());
      e.putString(se.getName(), s);
    }
    e.apply();
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
