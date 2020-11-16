package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText edemail,edpassword;
    FirebaseAuth mAuth;
    String TAG;
    ProgressBar progressBar;
    TextView signuptext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        edemail=findViewById(R.id.email);
        edpassword=findViewById(R.id.password);
        TAG="***Login";
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progress_bar);
        signuptext=findViewById(R.id.login_text);
        findViewById(R.id.sign_up_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));

            }
        });
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                signuptext.setVisibility(View.GONE);
                mAuth.signInWithEmailAndPassword(edemail.getText().toString(), edpassword.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    progressBar.setVisibility(View.GONE);
                                    signuptext.setVisibility(View.VISIBLE);
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    signuptext.setVisibility(View.VISIBLE);

                                }

                                // ...
                            }
                        });
            }
        });


    }
}