package com.wedevol.fcmtest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

/**
 * Main Activity for FCM Token
 */
public class MainActivity extends AppCompatActivity implements IRequestListener {

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "149763630937";
    public static final String FCM_SERVER_CONNECTION = "@fcm.googleapis.com";
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "ECHO";
    public static final Random RANDOM = new Random();

    private EditText editTextEcho;
    private TextView deviceText;
    private Button buttonUpstreamEcho;
    private TokenService tokenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "FCM Token creation logic");

        // Get variables reference
        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);

        //Get token from Firebase
        tokenService = new TokenService(this, this);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to get the token.");
                    return;
                }

                String token = task.getResult();

                Log.d(TAG, "Token: " + token);
                // NOTE if it prints null in the emulator first time maybe it is due to a delay problem
                deviceText.setText(token);

                //Call the token service to save the token in the database

                tokenService.registerTokenInDB(token);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to get the token : " + e.getLocalizedMessage());
            }
        });



        buttonUpstreamEcho.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Echo Upstream message logic");
                String message = editTextEcho.getText().toString();
                //Log.d(TAG, "Message: " + message + ", recipient: " + token);
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(RANDOM.nextInt()))
                        .addData("message", message)
                        .addData("action", BACKEND_ACTION_ECHO)
                        .build());
                // To send a message to other device through the XMPP Server, you should add the
                // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
                Log.d(TAG, "Upstream message sent");
            }
        });

    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "Error trying to register the token in the DB: " + message);
    }
}
