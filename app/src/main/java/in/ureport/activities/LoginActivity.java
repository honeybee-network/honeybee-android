package in.ureport.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import in.ureport.R;
import in.ureport.fragments.CredentialsLoginFragment;
import in.ureport.fragments.ForgotPasswordFragment;
import in.ureport.fragments.LoginFragment;
import in.ureport.fragments.SignUpFragment;
import in.ureport.helpers.ValueEventListenerAdapter;
import in.ureport.managers.CountryProgramManager;

import in.ureport.managers.FirebaseManager;
import in.ureport.managers.UserManager;
import in.ureport.models.User;
import in.ureport.network.UserServices;
import in.ureport.services.GcmRegistrationIntentService;

/**
 * Created by johncordeiro on 7/7/15.
 */
public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState == null) {
            addLoginFragment();
        }
        checkUserLoggedAndProceed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if(fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
    }

    private void checkUserLoggedAndProceed() {
        if(UserManager.isUserLoggedIn()) {
            loadUserAndContinue(FirebaseManager.getAuthUserKey());
        }
    }

    private void loadUserAndContinue(String authUserKey) {
        UserServices userServices = new UserServices();
        userServices.getUser(authUserKey, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    UserManager.countryCode = user.getCountry();
                    CountryProgramManager.switchCountryProgram(UserManager.countryCode);
                    startMainActivity();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void addLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, loginFragment)
                .commit();
    }

    @Override
    public void onLoginWithCredentials() {
        addCredentialsLoginFragment();
    }

    @Override
    public void onSkipLogin() {
        startMainActivity();
    }

    private void addCredentialsLoginFragment() {
        CredentialsLoginFragment credentialsLoginFragment = new CredentialsLoginFragment();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, credentialsLoginFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginWithSocialNetwork(final User user) {
        UserServices userServices = new UserServices();
        userServices.getUser(user.getKey(), new ValueEventListenerAdapter() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    onUserReady(dataSnapshot.getValue(User.class));
                else
                    addSignUpFragment(user);
            }
        });
    }

    @Override
    public void onSignUp() {
        addSignUpFragment();
    }

    @Override
    public void onUserReady(final User user) {
        UserServices userServices = new UserServices();
        userServices.keepUserOffline(user);

        UserManager.countryCode = user.getCountry();

        CountryProgramManager.switchCountryProgram(user.getCountry());
        startMainActivity();

        Intent gcmRegisterIntent = new Intent(this, GcmRegistrationIntentService.class);
        startService(gcmRegisterIntent);
    }

    @Override
    public void onForgotPassword() {
        ForgotPasswordFragment forgotPasswordFragment = new ForgotPasswordFragment();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.content, forgotPasswordFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPasswordReset() {
        getSupportFragmentManager().popBackStack();
        Toast.makeText(this, R.string.error_email_check, Toast.LENGTH_LONG).show();
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void addSignUpFragment() {
        addSignUpFragment(null);
    }

    private void addSignUpFragment(User user) {
        SignUpFragment signUpFragment = SignUpFragment.newInstance(user);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, signUpFragment)
                .addToBackStack(null)
                .commit();
    }
}
