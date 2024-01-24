package com.mubadara.mubadara2023.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mubadara.mubadara2023.databinding.ActivityMainBinding;
import com.mubadara.mubadara2023.fragments.AccountFragment;
import com.mubadara.mubadara2023.fragments.ChatsFragment;
import com.mubadara.mubadara2023.fragments.HomeFragment;
import com.mubadara.mubadara2023.fragments.MyAdsFragment;
import com.mubadara.mubadara2023.R;
import com.mubadara.mubadara2023.Utils;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private static final String TAG="MAIN_TAG";



    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //firebase
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startLoginOption();


        }else {
           updateFCMToken();
           askNotificationPermission();




        }


        showHomeFragment();

        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {


                    showHomeFragment();

                    return true;
                } else if (itemId == R.id.menu_chat) {

                    //char
                    if(firebaseAuth.getCurrentUser()== null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOption();
                        return false;

                    } else {
                        showChatsFragment();


                        return true;


                    }

                }

                else if (itemId == R.id.my_ads) {
                    if(firebaseAuth.getCurrentUser()== null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOption();
                        return false;
                    }
                    else {
                        showMyAdsFragment();
                        return true;

                    }
                }

                else if (itemId == R.id.menu_account) {
                    if(firebaseAuth.getCurrentUser()== null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOption();
                            return false;


                    } else {


                        showAccountFragment();


                        return true;
                    }


                } else {


                    return false;
                }

            }


        });

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AdCreateActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });

    }

    private void showHomeFragment() {

        binding.toolbarTitleTv.setText("Home");

        //show Fragment
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeFragment");
        fragmentTransaction.commit();

    }

    private void showChatsFragment() {
        binding.toolbarTitleTv.setText("Chats");

        //show Fragment
        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ChatsFragment");
        fragmentTransaction.commit();

    }

    private void showMyAdsFragment() {
        binding.toolbarTitleTv.setText("My Ads");

        //show Fragment
        MyAdsFragment fragment = new MyAdsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "MyAdsFragment");
        fragmentTransaction.commit();

    }

    private void showAccountFragment() {
        binding.toolbarTitleTv.setText("Account");

        //show Fragment
        AccountFragment fragment = new AccountFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "AccountFragment");
        fragmentTransaction.commit();

    }

    private void startLoginOption() {

        startActivity(new Intent(this, LoginOptionsActivity.class));
    }

    private void updateFCMToken(){
        String myUid=""+firebaseAuth.getUid();
        Log.d(TAG, "updateFCMToken: myUid"+myUid);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        Log.d(TAG, "onSuccess: token:"+token);

                        HashMap<String,Object>hashMap =new HashMap<>();
                        hashMap.put("fcmToken",token);

                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Token Updated...!");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Log.e(TAG, "updateFCMToken: onFailure: ",e );

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "updateFCMToken: onFailure: ",e );

                    }
                });

    }

    private void askNotificationPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)== PackageManager.PERMISSION_DENIED){
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);

            }
        }
    }

    private ActivityResultLauncher<String> requestNotificationPermission= registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: Notification Permission STATUS "+isGranted);


                }
            }
    );
}