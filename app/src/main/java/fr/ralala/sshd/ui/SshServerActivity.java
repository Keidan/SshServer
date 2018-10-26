package fr.ralala.sshd.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.TextView;

import fr.ralala.sshd.R;

import fr.ralala.sshd.SshServerApplication;
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
  private SnackException mSnackException;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    setContentView(R.layout.activity_ssh);
    mApp = SshServerApplication.getApp(this);
    mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    // Adds custom toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // Check whether we're recreating a previously destroyed instance
    if (savedInstanceState == null || !savedInstanceState.containsKey(getClass().getSimpleName())) {
      mApp.getSshServerEntryFactory().load();
      mNetworkBroadcaster = new NetworkBroadcaster(this, this);
      mNetworkBroadcaster.load();
    }
    mSnackException = new SnackException(this);
    final AppCompatImageView ivRefreshHostAddress = findViewById(R.id.ivRefreshHostAddress);
    ivRefreshHostAddress.setOnClickListener((v) -> {
      ivRefreshHostAddress.clearAnimation();
      RotateAnimation anim = new RotateAnimation(0, 360, ivRefreshHostAddress.getWidth()/2, ivRefreshHostAddress.getHeight()/2);
      anim.setFillAfter(true);
      anim.setRepeatCount(0);
      anim.setDuration(1000);
      ivRefreshHostAddress.startAnimation(anim);
      onNetworkStatus(false);
    });

    ListView listView = findViewById(R.id.list);
    mAdapter = new SshServerEntriesArrayAdapter(this, mApp.getSshServerEntryFactory().list(), this);
    listView.setAdapter(mAdapter);

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
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
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_ssh, menu);
    return true;
  }


  /**
   * Called when the options item is clicked.
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_add:
        SshServerEditOrAddActivity.startActivity(this, SshServerEditOrAddActivity.NO_PORT);
        return true;
    }
    return false;
  }


  /**
   * Receive the result from a previous call to startActivityForResult
   *
   * @param requestCode The integer request code originally supplied to startActivityForResult.
   * @param resultCode  The integer result code returned by the child activity through its setResult().
   * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SshServerEditOrAddActivity.REQUEST_START_ACTIVITY && resultCode == RESULT_OK) {
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Handle the network status.
   *
   * @param down True if down.
   */
  public void onNetworkStatus(final boolean down) {
    ((TextView) findViewById(R.id.txtHostAddress)).setText((
        getResources().getString(R.string.host_address) + " " + (down ?
            getResources().getString(R.string.unknown) :
            mNetworkBroadcaster.getCurrentIpAddress())));
  }


  /**
   * Called when the user click on the start menu.
   *
   * @param sse The associated entry.
   * @return true started.
   */
  @Override
  public boolean onMenuStart(SshServerEntry sse) {
    if (sse != null) {
      sse.getSshServer().setSshServerEntry(sse);
      try {
        sse.getSshServer().start(mNetworkBroadcaster.getCurrentIpAddress());
        sse.setStarted(true);
        mAdapter.notifyDataSetChanged();
        NotificationFactory.show(this, sse);
        return true;
      } catch (Throwable e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        mSnackException.show(sse.getName() + " " + getString(R.string.error_return_an_exception), e);
      }
    }
    return false;
  }

  /**
   * Called when the user click on the stop menu.
   *
   * @param sse The associated entry.
   * @return true stopped.
   */
  @Override
  public boolean onMenuStop(SshServerEntry sse) {
    if (sse != null && sse.isStarted()) {
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
   *
   * @param sse The associated entry.
   */
  @Override
  public void onMenuEdit(SshServerEntry sse) {
    SshServerEditOrAddActivity.startActivity(this, sse.getPort());
  }

  /**
   * Called when the user click on the remove menu.
   *
   * @param sse The associated entry.
   */
  @Override
  public void onMenuRemove(SshServerEntry sse) {
    if (sse != null) {
      onMenuStop(sse);
      mApp.getSshServerEntryFactory().remove(sse);
      mAdapter.remove(sse);
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
    if (mNetworkBroadcaster != null) mNetworkBroadcaster.unload();
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
