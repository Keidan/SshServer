package fr.ralala.sshd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import fr.ralala.sshd.R;
import fr.ralala.sshd.SshServerApplication;
import fr.ralala.sshd.net.SshServerEntry;
import fr.ralala.sshd.net.SshServerEntryFactory;
import fr.ralala.sshd.net.ptm.ShellConfiguration;

public class SshServerEditOrAddActivity extends AppCompatActivity {
  public static final int REQUEST_START_ACTIVITY = 100;
  private static final String ACTIVITY_EXTRA_PORT =  "ACTIVITY_EXTRA_port";
  public static final int NO_PORT =  -1;
  private TextInputEditText mTxtPort;
  private TextInputEditText mTxtName;
  private TextInputEditText mTxtUsername;
  private TextInputEditText mTxtUserpwd;
  private TextInputLayout mTilUserpwd;
  private TextInputLayout mTilUsername;
  private CheckBox mCbSys;
  private TextInputEditText mTxtSysUser;
  private TextInputEditText mTxtSysGroup;
  private TextInputEditText mTxtSysHome;
  private Spinner mSpAuthMode;
  private SshServerEntryFactory mFactory;
  private SshServerEntry mSshServerEntry;

  /**
   * Starts the activity.
   * @param activity The caller activity (for the result).
   * @param port The port used for the extra part (NO_PORT for add mode).
   */
  public static void startActivity(final AppCompatActivity activity, final int port) {
    Intent intent = new Intent(activity, SshServerEditOrAddActivity.class);
    if(port != NO_PORT)
      intent.putExtra(ACTIVITY_EXTRA_PORT, port);
    activity.startActivityForResult(intent, REQUEST_START_ACTIVITY);
  }

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_ssh_server);
    mFactory = SshServerApplication.getApp(this).getSshServerEntryFactory();
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mTxtPort = findViewById(R.id.txtPort);
    mTxtName = findViewById(R.id.txtName);
    mTxtUsername = findViewById(R.id.txtUsername);
    mTxtUserpwd = findViewById(R.id.txtUserpwd);
    mTilUserpwd = findViewById(R.id.tilUserpwd);
    mTilUsername = findViewById(R.id.tilUsername);
    mCbSys = findViewById(R.id.cbSys);
    mTxtSysUser = findViewById(R.id.txtSysUser);
    mTxtSysGroup = findViewById(R.id.txtSysGroup);
    mTxtSysHome = findViewById(R.id.txtSysHome);
    mSpAuthMode = findViewById(R.id.spAuthMode);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.auth_mode_list, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
    mSpAuthMode.setAdapter(adapter);
    mSpAuthMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        int v = !parentView.getItemAtPosition(position).toString().equals(getString(R.string.normal)) ? View.GONE : View.VISIBLE;
        mTxtUsername.setVisibility(v);
        mTxtUserpwd.setVisibility(v);
        mTilUserpwd.setVisibility(v);
        mTilUsername.setVisibility(v);
      }
      @Override
      public void onNothingSelected(AdapterView<?> parentView) { }
    });

    /* default states */
    mSshServerEntry = null;
    Bundle extras = getIntent().getExtras();
    Object value;
    if(extras != null && (value = extras.get(ACTIVITY_EXTRA_PORT)) != null) {
      if(Integer.class.isInstance(value))
        mSshServerEntry = mFactory.findByPort((Integer)value);
    }
    mSpAuthMode.setSelection((mSshServerEntry == null || mSshServerEntry.isAuthAnonymous()) ? 0 : 1);
    mTxtUsername.setVisibility(View.GONE);
    mTxtUserpwd.setVisibility(View.GONE);
    mTilUserpwd.setVisibility(View.GONE);
    mTilUsername.setVisibility(View.GONE);
    mTxtPort.setText(mSshServerEntry == null ? getString(R.string.default_port) : String.valueOf(mSshServerEntry.getPort()));
    mTxtName.setText(mSshServerEntry == null ? "" : mSshServerEntry.getName());
    mTxtUsername.setText(mSshServerEntry == null || mSshServerEntry.getUsername() == null ? "" : mSshServerEntry.getUsername());
    mTxtUserpwd.setText(mSshServerEntry == null || mSshServerEntry.getPassword() == null ? "" : mSshServerEntry.getPassword());
    ShellConfiguration cfg = mSshServerEntry == null ? null : mSshServerEntry.getShellConfiguration();
    mCbSys.setChecked(cfg != null && cfg.isOverride());
    mTxtSysUser.setText(cfg == null || cfg.getUser() == null ? ShellConfiguration.DEFAULT_USER : cfg.getUser());
    mTxtSysGroup.setText(cfg == null || cfg.getGroup() == null ? ShellConfiguration.DEFAULT_GROUP : cfg.getGroup());
    mTxtSysHome.setText(cfg == null || cfg.getHome() == null ? ShellConfiguration.DEFAULT_HOME : cfg.getHome());
  }


  /**
   * Called when the options menu is clicked.
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_add_ssh_server, menu);
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
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.menu_cancel:
        onBackPressed();
        return true;
      case R.id.menu_add:
        if(validate()) {
          setResult(RESULT_OK);
          finish();
        }
        return true;
    }
    return false;
  }



  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  /**
   * Validates the forms.
   * @return false on error.
   */
  private boolean validate() {
    final String unknown = getString(R.string.unknown);
    final String name = mTxtName.getText() == null ? "" : mTxtName.getText().toString().trim();
    final String port = mTxtPort.getText() == null ? "" : mTxtPort.getText().toString().trim();
    final boolean override = mCbSys.isChecked();
    final String sysUser = mTxtSysUser.getText() == null ? "" : mTxtSysUser.getText().toString().trim();
    final String sysGroup = mTxtSysGroup.getText() == null ? "" : mTxtSysGroup.getText().toString().trim();
    String sysHome = mTxtSysHome.getText() == null ? "" : mTxtSysHome.getText().toString().trim();
    String username = null;
    String password = null;
    if(name.isEmpty()) {
      UIHelper.shakeError(mTxtName, getString(R.string.error_no_name));
      return false;
    } else if(port.isEmpty()) {
      UIHelper.shakeError(mTxtPort, getString(R.string.error_no_port));
      return false;
    } else if(mSshServerEntry == null && mFactory.contains(name, port)) {
      UIHelper.showAlertDialog(this, R.string.error, R.string.error_already_present);
      return false;
    } else if(mSshServerEntry != null) {
      mFactory.remove(mSshServerEntry);
      if(mFactory.contains(name, port)) {
        UIHelper.showAlertDialog(this, R.string.error, R.string.error_already_present);
        mFactory.add(mSshServerEntry); /* restore */
        return false;
      }
    }
    if(sysHome.isEmpty())
      sysHome = "/";
    if(!mSpAuthMode.getSelectedItem().equals(unknown)) {
      if(mSpAuthMode.getSelectedItem().equals(getString(R.string.anonymous))) {
        username = null;
        password = null;
      } else {
        username = mTxtUsername.getText() == null ? null : mTxtUsername.getText().toString();
        password = mTxtUserpwd.getText() == null ? null : mTxtUserpwd.getText().toString();
      }
    }
    ShellConfiguration cfg = new ShellConfiguration(sysHome, sysUser, sysGroup, ShellConfiguration.DEFAULT_SHELL, override);
    mFactory.add(new SshServerEntry(name, Integer.parseInt(port), username, password, cfg));
    return true;
  }
}
