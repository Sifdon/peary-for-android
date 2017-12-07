package com.pearyapp.peary.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pearyapp.peary.MainActivity;
import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.User;
import com.pearyapp.peary.utils.Constants;
import com.pearyapp.peary.utils.Utils;

/**
 * Created by Alexa on 20.05.2016.
 */
public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mFirebaseRef;
    private EditText mEditTextUsername, mEditTextEmail, mEditTextPassword;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        Button mBtnRegister = (Button) findViewById(R.id.btn_create_account);
        mEditTextUsername = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmail = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPassword = (EditText) findViewById(R.id.edit_text_password_create);
        TextView textViewSignIn = (TextView) findViewById(R.id.tv_sign_in);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mBtnRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_account:
                registerUser();
                break;
            case R.id.tv_sign_in:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    private void registerUser() {
        final String username = mEditTextUsername.getText().toString().trim();
        final String email = mEditTextEmail.getText().toString().trim().toLowerCase();
        String password = mEditTextPassword.getText().toString().trim();
        boolean validEmail = (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Bitte trage deinen Namen ein!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Bitte trage deine E-Mail-Adresse ein!", Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(email) && !validEmail) {
            Toast.makeText(this, "Diese E-Mail-Adresse ist nicht gültig, bitte trage eine gültige E-Mail-Adresse ein!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bitte wähle ein Passwort!", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.minimum_password), Toast.LENGTH_SHORT).show();
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        //Create Username
                        final FirebaseUser mUser = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

                        if (mUser != null) {
                            mUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        final String encodedEmail = Utils.encodeEmail(email);
                                        User user = new User(username, encodedEmail, mFirebaseRef.push().getKey());
                                        mFirebaseRef.child(mUser.getUid()).setValue(user);
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es später noch einmal!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CreateAccountActivity.this, "Registrierung war nicht erfolgreich. Bitte überprüfe deine Internetverbindung und versuche es nochmal!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}
