package pvt.pvt;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import retrofit2.*;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.*;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginScreen extends AppCompatActivity {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);
    private CallbackManager callbackManager = CallbackManager.Factory.create();

    private ProgressBar progressBar;
    private LoginButton fbLoginButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbLoginButton.setVisibility(View.GONE);
        AccessToken accesstoken = AccessToken.getCurrentAccessToken();

        if (accesstoken != null) {
            //Autologin
            progressBar.setVisibility(View.VISIBLE);
            fbLoginButton.setVisibility(View.GONE);

            LoginManager.getInstance().registerCallback(callbackManager, new LogintoFacebookCallback());

            //Autologin
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        } else {
            fbLoginButton.setVisibility(View.VISIBLE);
            fbLoginButton.registerCallback(callbackManager, new LogintoFacebookCallback());
            fbLoginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    fbLoginButton.setVisibility(View.GONE);
                }
            });
        }
    }

    private class LogintoFacebookCallback implements FacebookCallback<LoginResult> {
        @Override
        public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                @Override
                public void onCompleted(JSONObject user, GraphResponse response) {
                    try {

                        JSONObject fbUser = new JSONObject();
                        fbUser.put("facebook_id", Long.parseLong(user.getString("id")));
                        fbUser.put("first_name", user.getString("name").split(" ")[0]);
                        fbUser.put("last_name", user.getString("name").split(" ")[1]);

                        Call<ResponseBody> makeCall = herokuService.createUser(fbUser);
                        makeCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                new CountDownTimer(500, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                    }

                                    public void onFinish() {
                                        homeView();
                                        finish();
                                    }
                                }.start();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "Anslutningsfel: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                resetLoginButton();
                            }
                        });
                    } catch (org.json.JSONException | ClassCastException | NullPointerException e) {
                        Log.v("LoginScreen", "Fel: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Fel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        resetLoginButton();
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            Log.v("LoginScreen, onCancel", "connecting to facebook was canceled");
            resetLoginButton();
        }

        @Override
        public void onError(FacebookException exception) {
            Log.v("LoginScreen, onFailure", "Unable to connect to facebook");
            Toast.makeText(getApplicationContext(), "facebook fel: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            resetLoginButton();
        }
    }

    private void resetLoginButton() {
        progressBar.setVisibility(View.GONE);
        fbLoginButton.setVisibility(View.VISIBLE);
    }

    private void homeView() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

