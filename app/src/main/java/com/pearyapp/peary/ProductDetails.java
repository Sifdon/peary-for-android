package com.pearyapp.peary;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pearyapp.peary.communicators.DialogCommunicator;
import com.pearyapp.peary.dialogs.QuantityDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ProductDetails extends AppCompatActivity implements View.OnClickListener, DialogCommunicator {

    private RelativeLayout mExpiryLayout;
    private TextView mExpiryView, mQuantityView;
    private String mExpiryDate, mInputDate, mKey;
    private int mQuantity;
    private boolean autorenew;
    private Drawable defaultBackground;
    private DatePickerDialog datePickerDialog;
    private CheckBox autoCheckbox;
    private EditText productInput;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Produkt hinzufügen");

        productInput = (EditText) findViewById(R.id.product_name_input);
        productInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (productInput.getText().length() >= 0 && (event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    productInput.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(productInput.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        mQuantityView = (TextView) findViewById(R.id.quantity_field);
        mExpiryView = (TextView) findViewById(R.id.expiry_date);

        RelativeLayout quantityLayout = (RelativeLayout) findViewById(R.id.quantity_layout);
        quantityLayout.setOnClickListener(this);

        mExpiryLayout = (RelativeLayout) findViewById(R.id.expiry_layout);
        mExpiryLayout.setOnClickListener(this);
        defaultBackground = mExpiryLayout.getBackground();

        RelativeLayout autorenewLayout = (RelativeLayout) findViewById(R.id.autorenew_layout);
        autorenewLayout.setOnClickListener(this);
        autoCheckbox = (CheckBox) findViewById(R.id.autorenew_checkbox);
        autoCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendar = Calendar.getInstance();
                if (!isChecked) {
                    autorenew = false;
                    mExpiryLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            datePickerDialog = new DatePickerDialog(ProductDetails.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    Calendar dateCalendar = Calendar.getInstance();
                                    dateCalendar.set(Calendar.YEAR, year);
                                    dateCalendar.set(Calendar.MONTH, monthOfYear);
                                    dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    mExpiryDate = simpleDateFormat.format(dateCalendar.getTime());
                                    mExpiryView.setText(mExpiryDate);

                                }
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.show();
                        }
                    });
                    mExpiryLayout.setBackground(defaultBackground);

                } else {
                    autorenew = true;
                    mExpiryLayout.setOnClickListener(null);
                    mExpiryLayout.setBackgroundColor(Color.LTGRAY);
                    calendar.add(Calendar.DATE, 2);
                    mExpiryDate = simpleDateFormat.format(calendar.getTime());
                }

            }
        });

        TextView autorenewDesc = (TextView) findViewById(R.id.autorenew_desc);
        autorenewDesc.setText(getString(R.string.autorenew_descr, 2, getString(R.string.days_dativ)));

        mQuantity = Integer.parseInt(mQuantityView.getText().toString());
        calendar.add(Calendar.DATE, 14);
        mExpiryDate = simpleDateFormat.format(calendar.getTime());
        mExpiryView.setText(mExpiryDate);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();

            productInput.setText(extras.getString("productname"));

            mQuantity = extras.getInt("quantity");
            mQuantityView.setText(String.valueOf(mQuantity));

            mExpiryDate = extras.getString("expirydate");
            mExpiryView.setText(mExpiryDate);
            mInputDate = extras.getString("inputdate");

            autorenew = extras.getBoolean("autorenew");
            if (autorenew) {
                autoCheckbox.setChecked(true);
                mExpiryLayout.setBackgroundColor(Color.LTGRAY);
                mExpiryLayout.setOnClickListener(null);
            } else {
                autoCheckbox.setChecked(false);
            }

            mKey = extras.getString("key");
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //to fridge or update
                if (productInput.getText().length() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("productname", productInput.getText().toString());
                    intent.putExtra("quantity", mQuantity);
                    intent.putExtra("expirydate", mExpiryDate);
                    intent.putExtra("inputdate", mInputDate);
                    intent.putExtra("autorenew", autorenew);
                    intent.putExtra("key", mKey);
                    setResult(RESULT_OK, intent);
                    this.finish();
                } else {
                    Toast.makeText(this, "Bitte trage einen Produktnamen ein!", Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {

        calendar = Calendar.getInstance();

        switch (v.getId()) {
            case R.id.quantity_layout:
                QuantityDialog quantityDialog = new QuantityDialog();
                quantityDialog.show(getFragmentManager(), "QuantityDialog");
                break;
            case R.id.expiry_layout:
                datePickerDialog = new DatePickerDialog(ProductDetails.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(Calendar.YEAR, year);
                        dateCalendar.set(Calendar.MONTH, monthOfYear);
                        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        mExpiryDate = simpleDateFormat.format(dateCalendar.getTime());
                        mExpiryView.setText(mExpiryDate);

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.autorenew_layout:
                if (autoCheckbox.isChecked()) {
                    autoCheckbox.setChecked(true);
                    autorenew = false;
                    mExpiryLayout.setOnClickListener(this);
                    mExpiryLayout.setBackground(defaultBackground);

                } else {
                    autoCheckbox.setChecked(false);
                    autorenew = true;
                    mExpiryLayout.setOnClickListener(null);
                    mExpiryLayout.setBackgroundColor(Color.LTGRAY);
                    calendar.add(Calendar.DATE, 2);
                    mExpiryDate = simpleDateFormat.format(calendar.getTime());
                }
                autoCheckbox.setChecked(!autoCheckbox.isChecked());
                break;

        }
    }


    @Override
    public void valueFromDialog(String data, int number, boolean statement, String key) {
        this.mQuantity = number;
        mQuantityView.setText(String.valueOf(mQuantity));
    }
}
