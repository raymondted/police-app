package com.project.raymond.reporttopolice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

private Button mLogoutBtn;
private FirebaseAuth mAuth;
private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){

                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };
        mLogoutBtn = findViewById(R.id.bLogout);

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
            }
        });
    }

    public void bCrime_Click(View view) {

        Intent intent = new Intent(MainActivity.this, CriminalActivity.class);
        startActivity(intent);
    }

    public void bMissingP_Click(View view) {

        Intent intent = new Intent(MainActivity.this, MissingPersonActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }


}
