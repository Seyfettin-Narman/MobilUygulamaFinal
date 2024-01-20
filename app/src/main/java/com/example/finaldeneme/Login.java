package com.example.finaldeneme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText textEmail,textPassword;
    Button LoginButton,btnSignUp;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        textEmail = findViewById(R.id.textEmail);
        textPassword = findViewById(R.id.textPassword);
        LoginButton  = findViewById(R.id.LoginButton);
        btnSignUp = findViewById(R.id.btnSignUp);
        auth = FirebaseAuth.getInstance();

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignUp.class));
            }
        });

    }

    private void Login(){
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(Login.this,"Lütfen email ve password alanını boş bırakmayınız", Toast.LENGTH_SHORT).show();
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Login.this,"giriş başarılı", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,MainActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(Login.this,"email ya da parola hatalı", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
            );
        }

    }
}