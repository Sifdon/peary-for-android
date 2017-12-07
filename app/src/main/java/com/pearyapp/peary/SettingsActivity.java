package com.pearyapp.peary;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pearyapp.peary.login.LoginActivity;
import com.pearyapp.peary.utils.AppCompatPreferenceActivity;
import com.pearyapp.peary.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static FirebaseAuth sAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        sAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    finish();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                }
            }
        };
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);

    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AccountPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.logout) {
            sAuth.signOut();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            sAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        private EditTextPreference mChangeName, mChangeEmail, mChangePassword;
        private Preference mDeleteAccount;
        private String mUsername, mEmail, mPassword, mUid;
        private FirebaseUser mUser;
        private DatabaseReference mShoppinglist, mFridge, mUserList;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_account);
            setHasOptionsMenu(true);

            mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                mUsername = mUser.getDisplayName();
                mEmail = mUser.getEmail();
                mUid = mUser.getUid();
            }

            mChangeName = (EditTextPreference) findPreference("change_name");
            mChangeName.setText(mUsername);
            mChangeName.setSummary(mUsername);
            mChangeName.setDefaultValue(mUsername);
            mChangeName.setOnPreferenceChangeListener(this);

            mChangeEmail = (EditTextPreference) findPreference("change_email");
            mChangeEmail.setText(mEmail);
            mChangeEmail.setSummary(mEmail);
            mChangeEmail.setDefaultValue(mEmail);
            mChangeEmail.setOnPreferenceChangeListener(this);

            mChangePassword = (EditTextPreference) findPreference("change_password");
            mChangePassword.setText("");
            mChangePassword.setOnPreferenceChangeListener(this);


            mDeleteAccount = findPreference("delete_account");
            mDeleteAccount.setOnPreferenceClickListener(this);

            mShoppinglist = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_SHOPPING_LIST).child(mUid);
            mFridge = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIDGE_LIST).child(mUid);
            mUserList = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS).child(mUid);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "change_name":
                    if (TextUtils.isEmpty(newValue.toString()) || newValue.toString().trim().equals("")) {
                        Toast.makeText(getActivity(), "Du musst einen neuen Benutzernamen angeben, um den aktuellen Benutzernamen zu ändern!", Toast.LENGTH_LONG).show();
                    } else {
                        mUsername = newValue.toString().trim();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(mUsername).build();
                        mUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mChangeName.setText(mUsername);
                                    mChangeName.setSummary(mUsername);
                                    mChangeName.setDefaultValue(mUsername);
                                    Map<String, Object> updateUsername = new HashMap<>();
                                    updateUsername.put(Constants.FIREBASE_PROPERTY_USERNAME, mUsername);
                                    mUserList.updateChildren(updateUsername);
                                } else {
                                    Toast.makeText(getActivity(), "Etwas ist schief gelaufen, bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    break;
                case "change_email":
                    if (TextUtils.isEmpty(newValue.toString()) ||
                            newValue.toString().trim().equals("") ||
                            mEmail.equals(newValue.toString().trim().toLowerCase()) ||
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(newValue.toString().trim().toLowerCase()).matches()) {
                        Toast.makeText(getActivity(), "Du musst eine neue gültige E-Mail-Adresse angeben, um deine aktuelle E-Mail-Adresse zu ändern!", Toast.LENGTH_LONG).show();
                    } else {
                        mEmail = newValue.toString().trim().toLowerCase();
                        mUser.updateEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mChangeEmail.setText(mEmail);
                                    mChangeEmail.setSummary(mEmail);
                                    mChangeEmail.setDefaultValue(mEmail);
                                    Toast.makeText(getActivity(), "Deine E-Mail-Adresse wurde erfolgreich geändert, bitte melde dich mit deiner neuen E-Mail-Adresse an!", Toast.LENGTH_LONG).show();
                                    sAuth.signOut();
                                } else {
                                    Toast.makeText(getActivity(), "Etwas ist schief gelaufen, bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    break;
                case "change_password":
                    if (TextUtils.isEmpty(newValue.toString()) || newValue.toString().trim().equals("")) {
                        Toast.makeText(getActivity(), "Du musst eine neues Passwort wählen, um das aktuelle Passwort zu ändern!", Toast.LENGTH_LONG).show();
                    } else if (newValue.toString().trim().length() < 6) {
                        Toast.makeText(getActivity(), getString(R.string.minimum_password), Toast.LENGTH_SHORT).show();
                    } else {
                        mPassword = newValue.toString().trim();
                        mUser.updatePassword(mPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Dein Passwort wurde erfolgreich geändert, bitte melde dich mit deinem neuen Passwort an!", Toast.LENGTH_LONG).show();
                                    sAuth.signOut();
                                } else {
                                    Toast.makeText(getActivity(), "Etwas ist schief gelaufen, bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    break;
            }

            return false;
        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "delete_account":
                    if (mUser != null) {
                        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.pref_delete_account)).setMessage(getString(R.string.delete_account_msg)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDatabase();
                                mUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Dein Account und deine Daten wurden nun gelöscht!", Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                        } else {
                                            Toast.makeText(getActivity(), "Etwas ist schief gelaufen, bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setIcon(R.drawable.ic_warning_black_24dp).show();

                    }
                    break;
            }
            return false;
        }

        private void clearDatabase() {
            mShoppinglist.removeValue();
            mFridge.removeValue();
            mUserList.removeValue();
        }

    }
}
