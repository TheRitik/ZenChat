package com.example.zenchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zenchat.R;
import android.Manifest;
import com.example.zenchat.databinding.ActivityMainBinding;
import com.example.zenchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    String [] permissions = new String[] {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private  int requestCode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        database.getReference().child("profiles")
                .child(currentUser.getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);

                                Glide.with(MainActivity.this)
                                        .load(user.getProfile())
                                        .into(binding.profileImage);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        binding.find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                    Toast.makeText(MainActivity.this, "Call Finding...", Toast.LENGTH_SHORT).show();
                }
                else{
                    askPermission();
                }
            }
        });

    }
    void askPermission (){
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }

    private boolean isPermissionGranted(){
        for(String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}