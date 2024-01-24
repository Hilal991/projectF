package com.mubadara.mubadara2023.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mubadara.mubadara2023.Utils;
import com.mubadara.mubadara2023.databinding.ActivityLoginPhoneBinding;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {


    private ActivityLoginPhoneBinding binding;
    private ProgressDialog progressDialog;


    private FirebaseAuth firebaseAuth;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private static final String TAG="LOGIN_PHONE_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //For the Start show Phone INPUT HI AND OTP
        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.otpInputRl.setVisibility(View.GONE);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();

        phoneLoginCallBack();




       binding.toolbarBackbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();


           }
       });


       binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              validateData();
           }
       });






        binding.resendPtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resendVerificationCode(forceResendingToken);
            }
        });





        binding.verifyOtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp=binding.otpEt.getText().toString().trim();

                Log.d(TAG, "onClick: OTP:"+otp);

                if (otp.isEmpty()) {

                    binding.otpEt.setError("Enter OTP");
                    binding.otpEt.requestFocus();

                }
                    else if(otp.length()<6){

                        binding.otpEt.setError("OTP length must be 6 characters");
                        binding.otpEt.requestFocus();

                }
                    else {

                        verifyPhoneNumberWithCode(mVerificationId,otp);
                }


            }
        });



    }



    private String phoneCode="", phoneNumber="",phoneNumberWithCode="";
    private void validateData() {

        phoneCode=binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber=binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode=phoneCode+phoneNumber;

        Log.d(TAG, "validateData: phoneCode: "+phoneCode);
        Log.d(TAG, "validateData: phoneNumber: "+phoneNumber);
        Log.d(TAG, "validateData: phoneNumberWithCode: "+phoneNumberWithCode);


        if (phoneNumber.isEmpty()){
                
            //phone is not enter
            Utils.toast(this,"Please enter phone number");
        }
        else {
            startPhoneNumberVerification();
        }

    }

    private void startPhoneNumberVerification(){
        Log.d(TAG, "startPhoneNumberVerification: ");

        //show progress
        progressDialog.setMessage("Sending OTP to "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();


       PhoneAuthProvider.verifyPhoneNumber(options);

    }



    private void phoneLoginCallBack(){

        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");



                signWithPhoneAuthCredential(credential);
            }



            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: ",e);

                progressDialog.dismiss();

                Utils.toast(LoginPhoneActivity.this,""+e.getMessage());






            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);


                  mVerificationId=verificationId;
                  forceResendingToken= token;

                  progressDialog.dismiss();


                  binding.phoneInputRl.setVisibility(View.INVISIBLE);
                  binding.otpInputRl.setVisibility(View.VISIBLE);

                  Utils.toast(LoginPhoneActivity.this,"OTP Send to"+phoneNumberWithCode);

                  binding.loginLabelTv.setText("Please type the verification code send to"+phoneNumberWithCode);




            }
        };

    }

        private void verifyPhoneNumberWithCode(String VerificationId,String otp){
            Log.d(TAG, "verifyPhoneNumberWithCode: VerificationId:"+VerificationId);
            Log.d(TAG, "verifyPhoneNumberWithCode: otp:"+otp);
            progressDialog.setMessage("Verifying OTP");
            progressDialog.show();

            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(VerificationId, otp);
            signWithPhoneAuthCredential(credential);

        }

        private void   resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){

            Log.d(TAG, "resendVerificationCode: ForceResendingToken:"+token);

            progressDialog.setMessage("ResendingOTP to "+phoneNumberWithCode);
            progressDialog.show();


            PhoneAuthOptions options=
                    PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumberWithCode)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(mCallbacks)
                            .setForceResendingToken(token)
                            .build();


            PhoneAuthProvider.verifyPhoneNumber(options);
            }
    

        private void signWithPhoneAuthCredential(PhoneAuthCredential credential){
            Log.d(TAG, "signWithPhoneAuthCredential: ");
            progressDialog.setMessage("Logging IN");

            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Log.d(TAG, "onSuccess: ");

                            if(authResult.getAdditionalUserInfo().isNewUser()){

                                Log.d(TAG, "onSuccess: New User, Account created...");

                                   updateUserInfoDb();
                            }
                            else {

                                Log.d(TAG, "onSuccess: Existing User Logged In");
                                startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                                finishAffinity();

                            }

                        }
                    })



                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ",e );
                            progressDialog.dismiss();
                            Utils.toast(LoginPhoneActivity.this,"Failed to login due to "+e.getMessage());
                        }
                    });


        }

            private void  updateUserInfoDb(){

                Log.d(TAG, "updateUserInfoDb: ");


                progressDialog.setMessage("Saving user info");
                progressDialog.show();

                long timestamp=Utils.getTimestamp();
                String registerUserUid= firebaseAuth.getUid();


                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("name","");
                hashMap.put("phoneCode",""+phoneCode);
                hashMap.put("phoneNumber",""+phoneNumber);
                hashMap.put("dob","");
                hashMap.put("userType","Phone");
                hashMap.put("typingTo","");
                hashMap.put("profileImageUrl","");
                hashMap.put("timestamp",timestamp);
                hashMap.put("onlineStatus",true);
                hashMap.put("email","");
                hashMap.put("uid",registerUserUid);


                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                ref.child(registerUserUid)
                        .setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: User info saved");
                                progressDialog.dismiss();

                                startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                                finishAffinity();
                               
                             }
                        })
                        
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ", e);
                                progressDialog.dismiss();
                                Utils.toast(LoginPhoneActivity.this,"Filed to save user info due to "+e.getMessage());

                            }
                        });


            }

}