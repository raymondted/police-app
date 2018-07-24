package com.project.raymond.reporttopolice;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    EditText InputEmail;
    Button resetPassword;
    TextView tvBack;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();


        InputEmail = findViewById(R.id.InputEmail);
        resetPassword = findViewById(R.id.resetPassword);
        tvBack = findViewById(R.id.tvBack);


        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResetPassword.this, LoginActivity.class));
            }
        });
        finish();


        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredResetEmail = InputEmail.getText().toString();
                if (TextUtils.isEmpty(enteredResetEmail)){
                    Toast.makeText(ResetPassword.this, "Email field can't be empty!", Toast.LENGTH_LONG).show();
                    return;
                }


                if (!(enteredResetEmail.contains("@"))){
                    Toast.makeText(ResetPassword.this, "Invalidate email entered", Toast.LENGTH_SHORT).show();
                    return;
                }


                mAuth.sendPasswordResetEmail(enteredResetEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(ResetPassword.this, "We have sent you instruction to rest your password in your email", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ResetPassword.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}

