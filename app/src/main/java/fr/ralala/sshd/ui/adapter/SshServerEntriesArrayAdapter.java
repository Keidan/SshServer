package fr.ralala.sshd.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ralala.android.ssh.server.R;

import fr.ralala.sshd.net.SshServerEntry;
import fr.ralala.sshd.ui.UIHelper;

import java.util.List;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Used by the listview to represent the server entries
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServerEntriesArrayAdapter extends ArrayAdapter<SshServerEntry> {
  private static final int ID = R.layout.srv_listview_row;
  private Context mContext;
  private List<SshServerEntry> mItems;
  private SshServerEntriesArrayAdapterMenuListener mListener;

  private class ViewHolder {
    ImageView icon;
    TextView name;
    TextView data;
    ImageView menu;
  }

  public SshServerEntriesArrayAdapter(final Context context,
                                      final List<SshServerEntry> objects,
                                      final SshServerEntriesArrayAdapterMenuListener listener) {
    super(context, ID, objects);
    mContext = context;
    mItems = objects;
    mListener = listener;
  }

  @Override
  public SshServerEntry getItem(final int i) {
    return mItems.get(i);
  }


  @Override
  public @NonNull View getView(final int position, final View convertView,
                      @NonNull final ViewGroup parent) {
    final Resources r = mContext.getResources();
    View v = convertView;
    ViewHolder holder;
    if ( v == null ) {
      final LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      final ViewGroup vg = null;
      v = vi.inflate(ID, vg);
      holder = new ViewHolder();
      holder.icon = v.findViewById(R.id.icon);
      holder.name = v.findViewById(R.id.name);
      holder.data = v.findViewById(R.id.data);
      holder.menu = v.findViewById(R.id.menu);
      v.setTag (holder);
    }
    else {
      /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }

    final SshServerEntry o = mItems.get(position);
    if (o != null) {
      if (holder.icon != null)
        holder.icon.setImageDrawable(ContextCompat.getDrawable(mContext, o.isStarted() ? R.drawable.ic_on : R.drawable.ic_off));
      if (holder.name != null)
        holder.name.setText(o.getName());
      if (holder.data != null) {
        String d = r.getString(R.string.lbl_port) + " " + o.getPort();
        if(!o.isAuthAnonymous())
          d += "\n" + r.getString(R.string.lbl_user) + " " + o.getUsername() +  "\n" +
              r.getString(R.string.lbl_userpwd) + " " +
              (o.getPassword() == null ? r.getString(R.string.no) : r.getString(R.string.yes));
        else
          d += "\n" + r.getString(R.string.anonymous);
        holder.data.setText(d);
      }
      /* Show the popup menu if the user click on the 3-dots item. */
      try {
        holder.menu.setOnClickListener((view) -> {
          switch (view.getId()) {
            case R.id.menu:
              final PopupMenu popup = new PopupMenu(mContext, view);
              popup.getMenuInflater().inflate(R.menu.popup_listview, popup.getMenu());
              /* Init the default behaviour */
              popup.getMenu().findItem(R.id.stop).setEnabled(o.isStarted());
              popup.getMenu().findItem(R.id.start).setEnabled(!o.isStarted());
              /* Force the icons display and displays the popupmenu */
              UIHelper.forcePopupMenuIconsAndShow(mContext, popup, view);
              popup.setOnMenuItemClickListener((item) -> {
                boolean started = false;
                switch (item.getItemId()) {
                  case R.id.start:
                    if(mListener.onMenuStart(o)) started = true;
                    break;
                  case R.id.stop:
                    if(mListener.onMenuStop(o)) started = false;
                    break;
                  case R.id.edit:
                    mListener.onMenuEdit(o);
                    break;
                  case R.id.delete:
                    mListener.onMenuRemove(o);
                    break;
                  default:
                    break;
                }
                /* Change the menus state */
                popup.getMenu().findItem(R.id.stop).setEnabled(started);
                popup.getMenu().findItem(R.id.start).setEnabled(!started);
                return true;
              });
              break;
            default:
              break;
          }
        });
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    return v;
  }

}
