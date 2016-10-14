package chandramouli.goru.helloworld;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.IOException;

import chandramouli.goru.helloworld.backend.myApi.MyApi;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "iHf1tJ7TsFZM7rKDzeFtz91Qk";
    private static final String TWITTER_SECRET = "bpnXLf3P1YupEW74rrRJySlFhs0bjacGKr6lJvdVatOuqPPPTu";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String mPhoneNumber;

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    writeNewUser(user.getUid(), "ChandraMouli", "mouli1382@gmail.com", mPhoneNumber);
                } else {
                    // User is signed out
                    Log.e(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();


        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();

                mPhoneNumber = session.getPhoneNumber();

                Log.e(TAG, "phone number = " + mPhoneNumber);
                Log.e(TAG, "email = " + session.getEmail().toString());
                Log.e(TAG, "token = " + session.getAuthToken().token + " secret = " + session.getAuthToken().secret);


                new EndpointsAsyncTask().execute(new Pair<Context, DigitsSession>(MainActivity.this, session));
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
    }

    private void startSignIn(String customeToken) {
        // Initiate sign in with custom token
        // [START sign_in_custom]
        mAuth.signInWithCustomToken(customeToken)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCustomToken", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END sign_in_custom]
    }

    private void writeNewUser(String userId, String name, String email, String phoneNumber) {
        User user = new User(name, email, phoneNumber);

        mDatabase.child("users").child(userId).setValue(user);
    }

    class EndpointsAsyncTask extends AsyncTask<Pair<Context, DigitsSession>, Void, String> {
        private MyApi myApiService = null;
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, DigitsSession>... params) {
            if (myApiService == null) {  // Only do this once
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("https://helloworld-146218.appspot.com/_ah/api/")
//                        .setRootUrl("http://192.168.1.6:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                // end options for devappserver

                myApiService = builder.build();
            }

            context = params[0].first;
            DigitsSession digitsSession = params[0].second;

            try {
                return myApiService.createCustomToken(digitsSession.getAuthToken().token, digitsSession.getPhoneNumber()).execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(EndpointsAsyncTask.class.getSimpleName(), "Firebase JWT = " + result);
            startSignIn(result);
        }
    }
}


