package org.ralala.android.ssh.server;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.ralala.android.ssh.server.adapter.SshServerEntriesArrayAdapter;
import org.ralala.android.ssh.server.adapter.SshServerEntriesArrayAdapterMenuListener;
import org.ralala.android.ssh.server.net.INetworkBroadcaster;
import org.ralala.android.ssh.server.net.NetworkBroadcaster;
import org.ralala.android.ssh.server.net.SshServerEntry;


/**
 *******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Main activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SshServerActivity extends AppCompatActivity implements SshServerEntriesArrayAdapterMenuListener, INetworkBroadcaster {
  private static final int              BACK_TIME_DELAY = 2000;
  private static final String           ANONYMOUS_TEXT  = "Anonymous";
  private SshServerApplication          app             = null;
  private SshServerEntriesArrayAdapter  adapter         = null;
  private static long                   lastBackPressed = -1;
  private NotificationManager           mNM             = null;
  private NetworkBroadcaster            broadcaster     = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    setContentView(R.layout.activity_ssh);
    ListView lv = (ListView)findViewById(R.id.list);
    app = SshServerApplication.getApp(this);
    // Check whether we're recreating a previously destroyed instance
    if (savedInstanceState == null || !savedInstanceState.containsKey(getClass().getSimpleName())) {
      app.loadEntries();
      broadcaster = new NetworkBroadcaster(this, this);
      broadcaster.load();
    }
    adapter = new SshServerEntriesArrayAdapter(
        this, R.layout.srv_listview_row, app.getEntries(), this);
    lv.setAdapter(adapter);

  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Save the user's current game state
    savedInstanceState.putInt(getClass().getSimpleName(), 0);

    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
  }

  /* Called when the refresh button is pressed */
  public void refreshHost(final View v) {
    networkStatus(false);
  }

  /* Called when the floating button is pressed */
  public void addServer(final View v) {
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle(R.string.add_server_msgbox_title);
    /* prepare the inflater and set the new content */
    LayoutInflater inflater = this.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.add_srv_box_content, null);
    dialogBuilder.setView(dialogView);
    /* Get the components */
    final EditText tport = (EditText) dialogView.findViewById(R.id.txtPort);
    final EditText tname = (EditText) dialogView.findViewById(R.id.txtName);
    final EditText tusername = (EditText) dialogView.findViewById(R.id.txtUsername);
    final EditText tpassword = (EditText) dialogView.findViewById(R.id.txtUserpwd);
    final TextView lpassword = (TextView) dialogView.findViewById(R.id.lblUserpwd);
    final TextView lusername = (TextView) dialogView.findViewById(R.id.lblUsername);
    final Spinner spAuthMode = (Spinner) dialogView.findViewById(R.id.spAuthMode);
    /* Init the common listener. */
    final DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int whichButton) {
        /* Click on the Positive button (OK) */
        if(whichButton == DialogInterface.BUTTON_POSITIVE) {
          final String name = tname.getText().toString().trim();
          final String port = tport.getText().toString().trim();
          String username = null;
          String password = null;
          if(name.isEmpty()) {
            Tools.showAlertDialog(SshServerActivity.this, R.string.error, R.string.error_no_name);
            return;
          } else if(port.isEmpty()) {
            Tools.showAlertDialog(SshServerActivity.this, R.string.error, R.string.error_no_port);
            return;
          } else if(app.containsEntry(name, port)) {
            Tools.showAlertDialog(SshServerActivity.this, R.string.error, R.string.error_already_present);
            return;
          }
          if(!spAuthMode.getSelectedItem().equals(ANONYMOUS_TEXT)) {
            username = tusername.getText().toString();
            password = tpassword.getText().toString();
          }
          app.addEntry(new SshServerEntry(name, Integer.parseInt(port), username, password));
          adapter.notifyDataSetChanged();
        }
        dialog.dismiss();
      }
    };
    /* init the spinner */
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.auth_mode_list, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spAuthMode.setAdapter(adapter);
    spAuthMode.setSelection(0);

    tusername.setVisibility(View.GONE);
    tpassword.setVisibility(View.GONE);
    lpassword.setVisibility(View.GONE);
    lusername.setVisibility(View.GONE);
    spAuthMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        int v = parentView.getItemAtPosition(position).toString().equals(ANONYMOUS_TEXT) ? View.GONE : View.VISIBLE;
        tusername.setVisibility(v);
        tpassword.setVisibility(v);
        lpassword.setVisibility(v);
        lusername.setVisibility(v);
      }
      @Override
      public void onNothingSelected(AdapterView<?> parentView) { }
    });
    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, ocl);
    dialogBuilder.setNegativeButton(R.string.cancel, ocl);
    tport.setText(R.string.default_port);
    tname.setText(R.string.empty);
    tusername.setText(R.string.empty);
    tpassword.setText(R.string.empty);
    /* show the dialog */
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
  }



  public void networkStatus(final boolean down) {
    ((TextView) findViewById(R.id.hostAddress)).setText(
            getResources().getString(R.string.host_address)  + " " + (down ?
            getResources().getString(R.string.unknown) :
              broadcaster.getCurrentIpAddress()));
  }


  @Override
  public boolean onMenuStart(SshServerEntry sse) {
    sse.getSshServer().setSshServerEntry(sse);
    try {
      sse.getSshServer().start(broadcaster.getCurrentIpAddress());
      sse.setStarted(true);
      adapter.notifyDataSetChanged();
      Tools.showNotification(this, mNM, sse);
      Tools.vibrate250ms(this);
      return true;
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      Tools.toast(this, sse.getName() + " return an exception: " + e.getMessage());
    }
    return false;
  }

  @Override
  public boolean onMenuStop(SshServerEntry sse) {
    if(sse.isStarted()) {
      sse.getSshServer().stop();
      sse.setStarted(false);
      adapter.notifyDataSetChanged();
      mNM.cancel(sse.getId());
      Tools.vibrate250ms(this);
    }
    return true;
  }

  @Override
  public void onMenuRemove(SshServerEntry sse) {
    onMenuStop(sse);
    SshServerApplication.getApp(this).deleteEntry(sse);
    adapter.remove(sse);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    cleanup();
  }

  private void cleanup() {
    if(broadcaster != null) broadcaster.unload();
    for(SshServerEntry sse : app.getEntries())  {
      sse.getSshServer().stop();
      sse.setStarted(false);
      mNM.cancel(sse.getId());
    }
  }

  @Override
  public void onBackPressed() {
    if (lastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      super.onBackPressed();
      cleanup();
      Process.killProcess(android.os.Process.myPid());
    } else {
      Tools.toast(this, R.string.on_double_back_exit_text);
    }
    lastBackPressed = System.currentTimeMillis();
  }
}
