package com.example.finaldeneme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {
    EditText editEmail, editPassword, editName, editSurname;
    Button btnLogin, btnSign;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSign = findViewById(R.id.btnSign);
        editName = findViewById(R.id.editName);
        editSurname = findViewById(R.id.editSurname);
        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                String name = editName.getText().toString();
                String surname = editSurname.getText().toString();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                    Toast.makeText(SignUp.this, "Lütfen gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "kayıt başarılı", Toast.LENGTH_SHORT).show();
                                        String uid = task.getResult().getUser().getUid();
                                        User user = new User(name, surname, email);

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference userRef = db.collection("Kullanici").document(uid);
                                        userRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("SignUp", "Kullanıcı Firestore'a eklendi.");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("SignUp", "Firestore'a kullanıcı eklenirken hata oluştu", e);
                                            }
                                        });

                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);
                                    } else {
                                        // Hata durumu
                                        Log.e("SignUp", "kayıt başarısız", task.getException());
                                        Toast.makeText(SignUp.this, "kayıt başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}