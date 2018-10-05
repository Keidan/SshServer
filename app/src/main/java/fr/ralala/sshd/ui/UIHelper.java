package fr.ralala.sshd.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import fr.ralala.sshd.R;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * UI helper functions.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class UIHelper {

  private UIHelper() {
  }

  /**
   * Displays a message box.
   * @param c The Android context.
   * @param title The message box title.
   * @param message The message to display.
   */
  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(c.getResources().getString(message));
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
    alertDialog.show();
  }


  /**
   * Displays a toast.
   * @param c The Android context.
   * @param message The message to display.
   */
  public static void toast(final Context c, final String message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
    TextView tv = toast.getView().findViewById(android.R.id.message);
    if (null != tv) {
      Drawable drawable = ContextCompat.getDrawable(c, R.mipmap.ic_launcher);
      if(drawable != null) {
        final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
        tv.setCompoundDrawablePadding(5);
      }
    }
    toast.show();
  }

  /**
   * FForces the display of icons in the context menu and displays it.
   * @param c The Android context.
   * @param popup The associated popup menu.
   * @param view The view that has been passed to the constructor of the PopupMenu instance.
   */

  @SuppressLint("RestrictedApi")
  public static void forcePopupMenuIconsAndShow(final Context c, PopupMenu popup, View view) {
    MenuPopupHelper menuHelper = new MenuPopupHelper(c, (MenuBuilder) popup.getMenu(), view);
    menuHelper.setForceShowIcon(true);
    menuHelper.setGravity(Gravity.END);
    menuHelper.show();
  }

  /**
   * Shake a view on error.
   * @param owner The owner view.
   * @param errText The error text.
   */
  public static void shakeError(TextView owner, String errText) {
    TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
    shake.setDuration(500);
    shake.setInterpolator(new CycleInterpolator(5));
    if(owner != null) {
      if(errText != null)
        owner.setError(errText);
      owner.clearAnimation();
      owner.startAnimation(shake);
    }
  }

}
