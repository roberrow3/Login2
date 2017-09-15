package com.example.admin.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SignInButton mGoogleBtn;
    private static final int RC_SIGN_IN = 1;

    Button btnLogin;
    EditText input_email,input_password;
    TextView btnSignup,btnForgotPass;

    RelativeLayout activity_main;

    private FirebaseAuth auth;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MAIN_ACTIVITY";
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() !=null){

                    startActivity(new Intent(MainActivity.this, DashBoard.class));

                }

            }
        };
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            //profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(), DashBoard.class));
        }
        mGoogleBtn = (SignInButton) findViewById(R.id.googleBtn) ;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "You Got an Error", Toast.LENGTH_LONG).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //View
        btnLogin = (Button) findViewById(R.id.login_btn_login);
        input_email = (EditText) findViewById(R.id.login_email);
        input_password = (EditText) findViewById(R.id.login_password);
        btnSignup = (TextView) findViewById(R.id.login_btn_signup);
        btnForgotPass = (TextView) findViewById(R.id.login_btn_forgot_password);
        activity_main = (RelativeLayout) findViewById(R.id.activity_main);

        btnSignup.setOnClickListener(this);
        btnForgotPass.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        //Init Firebase Auth


        //check already session , if ok-> DashBoard
        if (auth.getCurrentUser() != null)
            startActivity(new Intent(MainActivity.this,DashBoard.class));

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_btn_forgot_password) {
            startActivity(new Intent(MainActivity.this, ForgotPassword.class));
            finish();
        }
        else  if (view.getId() == R.id.login_btn_signup) {
            startActivity(new Intent(MainActivity.this, SignUp.class));
            finish();
        }
        else  if (view.getId() == R.id.login_btn_login) {
          loginUser(input_email.getText().toString(),input_password.getText().toString());
        }

    }

    private void loginUser(String email, final String password) {
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            if (password.length() <6){
                                Snackbar snackBar =Snackbar.make(activity_main, "Password length must be over 6", Snackbar.LENGTH_SHORT);
                                snackBar.show();
                            }

                        }
                        else {
                            startActivity(new Intent(MainActivity.this, DashBoard.class));
                        }

                    }
                });
    }
    @Override
    protected void onStart(){
        super.onStart();

        auth.addAuthStateListener(mAuthListener);
    }
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        }else {

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
