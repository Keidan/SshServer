package org.ralala.android.ssh.server.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ralala.android.ssh.server.R;
import org.ralala.android.ssh.server.net.SshServerEntry;
import org.ralala.android.ssh.server.Tools;

import java.util.List;

/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Used by the listview to represent the server entries
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SshServerEntriesArrayAdapter extends ArrayAdapter<SshServerEntry> {

  private Context                                  c        = null;
  private int                                      id       = 0;
  private List<SshServerEntry>                     items    = null;
  private SshServerEntriesArrayAdapterMenuListener listener = null;

  private class ViewHolder {
    ImageView icon;
    TextView name;
    TextView data;
    ImageView menu;
  }

  public SshServerEntriesArrayAdapter(final Context context, final int textViewResourceId,
                                       final List<SshServerEntry> objects,
                                       final SshServerEntriesArrayAdapterMenuListener listener) {
    super(context, textViewResourceId, objects);
    this.c = context;
    this.id = textViewResourceId;
    this.items = objects;
    this.listener = listener;
  }

  @Override
  public SshServerEntry getItem(final int i) {
    return items.get(i);
  }

  @Override
  public View getView(final int position, final View convertView,
                      final ViewGroup parent) {
    final Resources r = c.getResources();
    View v = convertView;
    ViewHolder holder;
    if ( v == null ) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.icon = (ImageView) v.findViewById(R.id.icon);
      holder.name = (TextView) v.findViewById(R.id.name);
      holder.data = (TextView) v.findViewById(R.id.data);
      holder.menu = (ImageView) v.findViewById(R.id.menu);
      v.setTag (holder);
    }
    else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }

    final SshServerEntry o = items.get(position);
    if (o != null) {
      if (holder.icon != null)
        holder.icon.setImageDrawable(r.getDrawable(o.isStarted() ? R.drawable.on : R.drawable.off));
      if (holder.name != null)
        holder.name.setText(o.getName());
      if (holder.data != null) {
        String d = "Port: " + o.getPort() + ", ";
        if(!o.isAuthAnonymous())
          d += "User: " + o.getUsername() +  ", Pwd: " + !(o.getPassword() == null);
        else
          d += "Anonymous";
        holder.data.setText(d);
      }
      /* Show the popup menu if the user click on the 3-dots item. */
      try {
        holder.menu.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            switch (v.getId()) {
              case R.id.menu:
                final PopupMenu popup = new PopupMenu(c, v);
                /* Force the icons display */
                Tools.forcePopupMenuIcons(popup);
                popup.getMenuInflater().inflate(R.menu.popup_listview,
                    popup.getMenu());
                /* Init the default behaviour */
                popup.getMenu().findItem(R.id.stop).setEnabled(o.isStarted());
                popup.getMenu().findItem(R.id.start).setEnabled(!o.isStarted());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                    boolean started = false;
                    switch (item.getItemId()) {
                      case R.id.start:
                        if(listener.onMenuStart(o)) started = true;
                        break;
                      case R.id.stop:
                        if(listener.onMenuStop(o)) started = false;
                        break;
                      case R.id.delete:
                        listener.onMenuRemove(o);
                        break;
                      default:
                        break;
                    }
                    /* Change the menus state */
                    popup.getMenu().findItem(R.id.stop).setEnabled(started);
                    popup.getMenu().findItem(R.id.start).setEnabled(!started);
                    return true;
                  }
                });
                break;
              default:
                break;
            }
          }
        });
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    return v;
  }

}
