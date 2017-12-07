package com.pearyapp.peary.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.pearyapp.peary.R;

/**
 * Created by Alexa on 04.09.2016.
 */
public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditTextEmail;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEditTextEmail = (EditText) findViewById(R.id.edit_text_email);
        Button resetBtn = (Button) findViewById(R.id.reset_password);
        resetBtn.setOnClickListener(this);
        Button backBtn = (Button) findViewById(R.id.back);
        backBtn.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset_password:
                String email = mEditTextEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email) || mEditTextEmail.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplication(), "Bitte gib deine registrierte E-Mailadresse ein", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ResetPasswordActivity.this, "Wir haben dir soeben eine E-Mail mit einer Anleitung zum Passwort zurücksetzen geschickt.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ResetPasswordActivity.this, "Ein Fehler ist aufgetreten, bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                                    }
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                }
                break;
            case R.id.back:
                finish();
                break;
        }
    }
}
