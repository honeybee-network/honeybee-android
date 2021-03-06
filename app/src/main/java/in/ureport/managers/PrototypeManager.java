package in.ureport.managers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import in.ureport.R;

/**
 * Created by johncordeiro on 7/29/15.
 */
public class PrototypeManager {

    public static void showPrototypeAlert(Context context) {
        showPrototypeAlert(context, null);
    }

    public static void showPrototypeAlert(Context context, DialogInterface.OnClickListener onNeutralButtonClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage("This function is not implemented yet in this prototype. Wait for the next version.")
                .setNeutralButton(R.string.confirm_neutral_dialog_button, onNeutralButtonClickListener).create();
        alertDialog.show();
    }

}
