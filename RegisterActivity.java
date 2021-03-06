package com.project.raymond.reporttopolice;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName,etPhoneNumber,etEmail,etPassword;
    private TextView gotologin;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //        Getting firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();


        Button bRegister = findViewById(R.id.bRegister);
        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        gotologin  = findViewById(R.id.tetx_next);


        gotologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredName, enteredPhone, enteredEmail, enteredPassword;

                enteredName = etName.getText().toString();
                enteredPhone = etPhoneNumber.getText().toString();
                enteredEmail = etEmail.getText().toString();
                enteredPassword= etPassword.getText().toString();


                if (TextUtils.isEmpty(enteredName)){
                    Toast.makeText(RegisterActivity.this, "Enter your name!", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(enteredPhone)){
                    Toast.makeText(RegisterActivity.this, "Enter your phone number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(enteredEmail)){
                    Toast.makeText(RegisterActivity.this, "Enter your email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(enteredEmail.contains("@"))){
                    Toast.makeText(RegisterActivity.this, "Invalid email entered", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(enteredPassword)){
                    Toast.makeText(RegisterActivity.this, "Enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (enteredPassword.length() < 6){
                    etPassword.setError(getString(R.string.minimum_password));
                    return;
                }

//                create user
                mAuth.createUserWithEmailAndPassword(enteredEmail,enteredPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                Toast.makeText(RegisterActivity.this, "createdUserWithEmail : onComplete:"+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                if (!task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Authentication failed."+task.getException(), Toast.LENGTH_SHORT).show();
                                }else{
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
        }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu1, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.privacy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://raymondted.000webhostapp.com/privacy.html")));
                break;

            case R.id.help:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://raymondted.000webhostapp.com/privacy.html")));
                break;
        }
        return true;
        }}

