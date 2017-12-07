package com.pearyapp.peary.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pearyapp.peary.R;
import com.pearyapp.peary.communicators.DialogCommunicator;

/**
 * Created by Alexa on 13.03.2016.
 */
public class QuantityDialog extends DialogFragment {

    private DialogCommunicator mComm;
    private int mQuantity;
    private EditText mQuantityField;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mComm = (DialogCommunicator) activity;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.quantity_dialog, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mQuantityField = (EditText) v.findViewById(R.id.quantity_field);
        builder.setTitle(R.string.quantity_title);
        builder.setView(v);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Do nothing here because we override this button later to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });


        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mQuantityField.length() == 0) {
                        Toast.makeText(getActivity(), R.string.no_quantity_alert, Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(mQuantityField.getText().toString()) == 0) {
                        Toast.makeText(getActivity(), R.string.quantity_alert, Toast.LENGTH_SHORT).show();
                    } else {
                        mQuantity = Integer.parseInt(mQuantityField.getText().toString());
                        mComm.valueFromDialog(null, mQuantity, false, null);
                        dismiss();
                    }
                }
            });
        }
    }


}
