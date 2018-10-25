package fr.ralala.sshd.ui;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.design.widget.CoordinatorLayout;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.sshd.R;

/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * Simple snack bar that allows you to display an exception.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SnackException {
  private CoordinatorLayout mCoordinatorLayout;
  private Activity mActivity;

  SnackException(Activity activity) {
    mActivity = activity;
    mCoordinatorLayout = activity.findViewById(R.id.coordinatorLayout);
  }

  /**
   * Shows the snack.
   *
   * @param message   The message to display.
   * @param throwable The exception.
   */
  public void show(final String message, final Throwable throwable) {
    final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
    snackbar.setAction(mActivity.getString(R.string.more), (view) -> {
      processAction(throwable);
      snackbar.dismiss();
    });
    snackbar.show();
  }

  /**
   * Called when the user click on the action button.
   */
  private void processAction(final Throwable throwable) {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
    dialogBuilder.setTitle(mActivity.getString(R.string.exception));
    LayoutInflater inflater = mActivity.getLayoutInflater();
    final ViewGroup viewGroup = null;
    final View dialogView = inflater.inflate(R.layout.alert_dialog_exception, viewGroup);
    dialogBuilder.setView(dialogView);
    dialogBuilder.setCancelable(true);
    final TextView txtName = dialogView.findViewById(R.id.txtName);
    final TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
    List<String> contents = extractThrowable(throwable);
    if (txtName != null)
      txtName.setText(contents.get(0));
    if (txtMessage != null)
      txtMessage.setText(contents.get(1));
    dialogBuilder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
    /* show the dialog */
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
  }

  /**
   * Extracts an exception in a List[0: message, 1: the stacktrace]
   * @param throwable The throwable to extract.
   * @return List<String>
   */
  private List<String> extractThrowable(final Throwable throwable) {
    List<String> list = new ArrayList<>(2);
    String message = throwable.getMessage();
    /* Test if the exception starts with a class name eg: java/io/IOException: description*/
    if(message.matches("^(\\w+/+)+\\w+:\\s")) {
      message = message.substring(message.indexOf(':') + 2);
    }
    list.add(message);
    StackTraceElement [] elements = throwable.getStackTrace();
    StringBuilder sb = new StringBuilder(elements.length + 1);
    sb.append(throwable.toString()).append("\n");
    for(StackTraceElement el : elements)
      sb.append("  at ").append(el.getClassName()).append(".").append(el.getMethodName())
          .append(" (").append(el.getFileName()).append(":").append(el.getLineNumber()).append(")").append("\n");
    list.add(sb.toString());
    return list;
  }
}
