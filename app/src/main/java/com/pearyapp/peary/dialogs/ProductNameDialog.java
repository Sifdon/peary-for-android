package com.pearyapp.peary.dialogs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pearyapp.peary.R;
import com.pearyapp.peary.communicators.DialogCommunicator;

/**
 * Created by Alexa on 27.04.2016.
 */
public class ProductNameDialog extends DialogFragment {

    private DialogCommunicator mComm;
    private String mProductName, mKey;
    private int mQuantity;
    private boolean mAutorenew;
    private EditText mProductInput;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mComm = (DialogCommunicator) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement DialogCommunicator");
        }

        mQuantity = getArguments().getInt("quantity");
        mAutorenew = getArguments().getBoolean("autorenew");
        mKey = getArguments().getString("key");

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View v = mInflater.inflate(R.layout.product_name_dialog, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mProductInput = (EditText) v.findViewById(R.id.product_input);
        builder.setTitle(R.string.product_name_title);
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
                    if (mProductInput.length() == 0) {
                        Toast.makeText(getActivity(), R.string.no_product_name_alert, Toast.LENGTH_SHORT).show();
                    } else {
                        mProductName = mProductInput.getText().toString();
                        mComm.valueFromDialog(mProductName, mQuantity, mAutorenew, mKey);
                        dismiss();
                    }
                }
            });
        }
    }

}
