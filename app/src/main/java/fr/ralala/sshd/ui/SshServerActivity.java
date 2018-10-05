package fr.ralala.sshd.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import fr.ralala.sshd.R;

import fr.ralala.sshd.SshServerApplication;
import fr.ralala.sshd.net.SshServerEntryFactory;
import fr.ralala.sshd.net.ptm.ShellConfiguration;
import fr.ralala.sshd.ui.adapter.SshServerEntriesArrayAdapter;
import fr.ralala.sshd.net.NetworkStatusListener;
import fr.ralala.sshd.net.NetworkBroadcaster;
import fr.ralala.sshd.net.SshServerEntry;
import fr.ralala.sshd.ui.adapter.SshServerEntriesArrayAdapterMenuListener;


/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Main activity
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SshServerActivity extends AppCompatActivity implements NetworkStatusListener, SshServerEntriesArrayAdapterMenuListener {
  private static final int BACK_TIME_DELAY = 2000;
  private SshServerApplication mApp = null;
  private SshServerEntriesArrayAdapter mAdapter = null;
  private static long mLastBackPressed = -1;
  private NotificationManager mNotificationManager = null;
  private NetworkBroadcaster mNetworkBroadcaster = null;
  private Vibrator mVibrator;
  private MenuItem mMenuRefresh;
  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    setContentView(R.layout.activity_ssh);
    mApp = SshServerApplication.getApp(this);
    mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    // Check whether we're recreating a previously destroyed instance
    if (savedInstanceState == null || !savedInstanceState.containsKey(getClass().getSimpleName())) {
      mApp.getSshServerEntryFactory().load();
      mNetworkBroadcaster = new NetworkBroadcaster(this, this);
      mNetworkBroadcaster.load();
    }

    ListView lv = findViewById(R.id.list);
    mAdapter = new SshServerEntriesArrayAdapter(this, mApp.getSshServerEntryFactory().list(), this);
    lv.setAdapter(mAdapter);

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[] {
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.VIBRATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    }, 1);
  }



  /**
   * Called when the options menu is clicked.
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_ssh, menu);
    mMenuRefresh = menu.findItem(R.id.menu_refresh);
    if(mApp.getSshServerEntryFactory().list().isEmpty())
      mMenuRefresh.setVisible(false);
    return true;
  }


  /**
   * Called when the options item is clicked.
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_refresh:
        onNetworkStatus(false);
        return true;
      case R.id.menu_add:
        addServer(null);
        return true;
    }
    return false;
  }

  /**
   * Called when the add menu is pressed.
   * @param sse Reference entry (null to add).
   */
  private void addServer(SshServerEntry sse) {
    final String unknown = getString(R.string.unknown);
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle(sse == null ? R.string.add_server_msgbox_title :  R.string.edit_server_msgbox_title);
    /* prepare the inflater and set the new content */
    LayoutInflater inflater = this.getLayoutInflater();
    final ViewGroup viewGroup = null;
    final View dialogView = inflater.inflate(R.layout.add_srv_box_content, viewGroup);
    dialogBuilder.setView(dialogView);
    /* Get the components */
    final TextInputEditText tport = dialogView.findViewById(R.id.txtPort);
    final TextInputEditText tname = dialogView.findViewById(R.id.txtName);
    final TextInputEditText tusername = dialogView.findViewById(R.id.txtUsername);
    final TextInputEditText tpassword = dialogView.findViewById(R.id.txtUserpwd);
    final TextInputLayout lpassword = dialogView.findViewById(R.id.tilUserpwd);
    final TextInputLayout lusername = dialogView.findViewById(R.id.tilUsername);
    final CheckBox cbSys = dialogView.findViewById(R.id.cbSys);
    final TextInputEditText txtSysUser = dialogView.findViewById(R.id.txtSysUser);
    final TextInputEditText txtSysGroup = dialogView.findViewById(R.id.txtSysGroup);
    final TextInputEditText txtSysHome = dialogView.findViewById(R.id.txtSysHome);

    final Spinner spAuthMode = dialogView.findViewById(R.id.spAuthMode);
    /* Init the common listener. */
    final DialogInterface.OnClickListener ocl = (dialog, whichButton) -> {
      SshServerEntryFactory factory = mApp.getSshServerEntryFactory();
      final String name = tname.getText() == null ? "" : tname.getText().toString().trim();
      final String port = tport.getText() == null ? "" : tport.getText().toString().trim();
      final boolean override = cbSys.isChecked();
      final String sysUser = txtSysUser.getText() == null ? "" : txtSysUser.getText().toString().trim();
      final String sysGroup = txtSysGroup.getText() == null ? "" : txtSysGroup.getText().toString().trim();
      String sysHome = txtSysHome.getText() == null ? "" : txtSysHome.getText().toString().trim();
      String username = null;
      String password = null;
      if(name.isEmpty()) {
        UIHelper.shakeError(tname, getString(R.string.error_no_name));
        return;
      } else if(port.isEmpty()) {
        UIHelper.shakeError(tport, getString(R.string.error_no_port));
        return;
      } else if(sse == null && factory.contains(name, port)) {
        UIHelper.showAlertDialog(SshServerActivity.this, R.string.error, R.string.error_already_present);
        return;
      } else if(sse != null) {
        factory.remove(sse);
        if(factory.contains(name, port)) {
          UIHelper.showAlertDialog(SshServerActivity.this, R.string.error, R.string.error_already_present);
          factory.add(sse); /* restore */
          return;
        }
      }
      if(sysHome.isEmpty())
        sysHome = "/";
      if(!spAuthMode.getSelectedItem().equals(unknown)) {
        if(spAuthMode.getSelectedItem().equals(getString(R.string.anonymous))) {
          username = null;
          password = null;
        } else {
          username = tusername.getText() == null ? null : tusername.getText().toString();
          password = tpassword.getText() == null ? null : tpassword.getText().toString();
        }
      }
      ShellConfiguration cfg = new ShellConfiguration(sysHome, sysUser, sysGroup, ShellConfiguration.DEFAULT_SHELL, override);
      factory.add(new SshServerEntry(name, Integer.parseInt(port), username, password, cfg));
      mAdapter.notifyDataSetChanged();
      if(!mMenuRefresh.isVisible())
        mMenuRefresh.setVisible(true);
      dialog.dismiss();
    };
    /* init the spinner */
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.auth_mode_list, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spAuthMode.setAdapter(adapter);
    spAuthMode.setSelection((sse == null || sse.isAuthAnonymous()) ? 0 : 1);
    tusername.setVisibility(View.GONE);
    tpassword.setVisibility(View.GONE);
    lpassword.setVisibility(View.GONE);
    lusername.setVisibility(View.GONE);
    spAuthMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        int v = !parentView.getItemAtPosition(position).toString().equals(getString(R.string.normal)) ? View.GONE : View.VISIBLE;
        tusername.setVisibility(v);
        tpassword.setVisibility(v);
        lpassword.setVisibility(v);
        lusername.setVisibility(v);
      }
      @Override
      public void onNothingSelected(AdapterView<?> parentView) { }
    });
    /* attach the listeners and init the default values */
    dialogBuilder.setCancelable(false);
    dialogBuilder.setPositiveButton(R.string.ok, null);
    dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {});
    tport.setText(sse == null ? getString(R.string.default_port) : String.valueOf(sse.getPort()));
    tname.setText(sse == null ? "" : sse.getName());
    tusername.setText(sse == null || sse.getUsername() == null ? "" : sse.getUsername());
    tpassword.setText(sse == null || sse.getPassword() == null ? "" : sse.getPassword());

    ShellConfiguration cfg = sse == null ? null : sse.getShellConfiguration();
    cbSys.setChecked(cfg != null && cfg.isOverride());
    txtSysUser.setText(cfg == null || cfg.getUser() == null ? ShellConfiguration.DEFAULT_USER : cfg.getUser());
    txtSysGroup.setText(cfg == null || cfg.getGroup() == null ? ShellConfiguration.DEFAULT_GROUP : cfg.getGroup());
    txtSysHome.setText(cfg == null || cfg.getHome() == null ? ShellConfiguration.DEFAULT_HOME : cfg.getHome());

    /* show the dialog */
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> ocl.onClick(alertDialog, 0));
    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v) -> alertDialog.dismiss());
  }



  /**
   * Handle the network status.
   * @param down True if down.
   */
  public void onNetworkStatus(final boolean down) {
    ((TextView) findViewById(R.id.txtHostAddress)).setText((
            getResources().getString(R.string.host_address)  + " " + (down ?
            getResources().getString(R.string.unknown) :
              mNetworkBroadcaster.getCurrentIpAddress())));
  }


  /**
   * Called when the user click on the start menu.
   * @param sse The associated entry.
   * @return true started.
   */
  @Override
  public boolean onMenuStart(SshServerEntry sse) {
    if(sse != null) {
      sse.getSshServer().setSshServerEntry(sse);
      try {
        sse.getSshServer().start(mNetworkBroadcaster.getCurrentIpAddress());
        sse.setStarted(true);
        mAdapter.notifyDataSetChanged();
        NotificationFactory.show(this, sse);
        return true;
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        UIHelper.toast(this, sse.getName() + " " + getString(R.string.error_return_an_exception) + ": " + e.getMessage());
      }
    }
    return false;
  }

  /**
   * Called when the user click on the stop menu.
   * @param sse The associated entry.
   * @return true stopped.
   */
  @Override
  public boolean onMenuStop(SshServerEntry sse) {
    if(sse != null && sse.isStarted()) {
      sse.getSshServer().stop();
      sse.setStarted(false);
      mAdapter.notifyDataSetChanged();
      NotificationFactory.hide(this, sse);
      mVibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
      return true;
    }
    return false;
  }

  /**
   * Called when the user click on the edit menu.
   * @param sse The associated entry.
   */
  @Override
  public void onMenuEdit(SshServerEntry sse) {
    addServer(sse);
  }

  /**
   * Called when the user click on the remove menu.
   * @param sse The associated entry.
   */
  @Override
  public void onMenuRemove(SshServerEntry sse) {
    if(sse != null) {
      onMenuStop(sse);
      mApp.getSshServerEntryFactory().remove(sse);
      mAdapter.remove(sse);
      if(mApp.getSshServerEntryFactory().list().isEmpty())
        mMenuRefresh.setVisible(false);
    }
  }


  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    cleanup();
  }

  /**
   * Stops all servers and stop the broadcaster.
   */
  private void cleanup() {
    if(mNetworkBroadcaster != null) mNetworkBroadcaster.unload();
    mApp.getSshServerEntryFactory().list().forEach((sse) -> {
      sse.getSshServer().stop();
      sse.setStarted(false);
      mNotificationManager.cancel(sse.getId());
    });
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      super.onBackPressed();
      cleanup();
      Process.killProcess(android.os.Process.myPid());
    } else {
      UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
    }
    mLastBackPressed = System.currentTimeMillis();
  }
}
