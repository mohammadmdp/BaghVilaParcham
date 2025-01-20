package mdp.baghvilaparchammanagementapp;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

import mdp.baghvilaparchammanagementapp.databinding.ActivityMainBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogGetDeviceNameBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class ActivityMain extends AppCompatActivity{
    
    private static final int REQUEST_CODE = 2113;
    private static final int VIEW_DEVICE  = 2111;
    ActivityMainBinding      binding;
    String                   masterKeyAlias;
    SharedPreferences        sharedPreferences;
    SharedPreferences.Editor editor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        lockActivity();
        
        
        try{
            //noinspection deprecation
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            //noinspection deprecation
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            
        }catch(Exception|Error e){
            e.printStackTrace();
            App.toast(this, getResources().getString(R.string.error_happened));
        }
        
        OnSuccessListener<String> onSuccessListener = new OnSuccessListener<>() {
            @Override
            public void onSuccess(String s) {
                App.log("id: " + s);
                String hash = sharedPreferences.getString("hash", null);
                if (hash != null && hash.contains(s)) {
                    App.HASH = s;
                    App.DEVICE_TITLE = sharedPreferences.getString("title", null);
                    try {
                        authenticate();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        App.toast(ActivityMain.this,
                                getResources().getString(R.string.error_happened));
                        binding.btnMainActivityTryAgain.setVisibility(View.VISIBLE);
                        binding.btnMainActivityTryAgain.setOnClickListener(v -> this.onSuccess(s));
                        binding.pbMainActivity.setVisibility(View.INVISIBLE);
                    }
                } else {

                    Dialog dialog = new Dialog(ActivityMain.this);

                    DialogGetDeviceNameBinding dialogBinding =
                            DialogGetDeviceNameBinding
                                    .inflate(ActivityMain.this.getLayoutInflater());

                    dialog.setContentView(dialogBinding.getRoot());

                    dialogBinding.btnDialogSendDeviceName.setOnClickListener(v -> {
                        if (dialogBinding.etDialogGetDeviceNameTitle.getText().toString().trim()
                                .isEmpty()) {
                            App.toast(ActivityMain.this,
                                    getResources().getString(
                                            R.string.enter_device_name_in_the_white_area));
                            return;
                        }

                        editor = sharedPreferences.edit();
                        editor.putString("hash", s);
                        editor.putString("title",
                                dialogBinding.etDialogGetDeviceNameTitle.getText()
                                        .toString().trim());
                        editor.apply();


                        try {
                            App.HASH = s;
                            App.DEVICE_TITLE = dialogBinding.etDialogGetDeviceNameTitle.getText()
                                    .toString().trim();
                            authenticate();
                            dialog.cancel();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            App.toast(ActivityMain.this,
                                    getResources().getString(R.string.error_happened));
                            binding.btnMainActivityTryAgain.setVisibility(View.VISIBLE);
                            binding.btnMainActivityTryAgain
                                    .setOnClickListener(v2 -> this.onSuccess(s));
                            binding.pbMainActivity.setVisibility(View.INVISIBLE);
                        }
                    });

                    dialog.setCanceledOnTouchOutside(false);

                    dialog.show();
                }

            }

        };
        
        BiometricManager biometricManager = BiometricManager.from(this);
        switch(biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK|BIOMETRIC_STRONG|DEVICE_CREDENTIAL)){
            case BiometricManager.BIOMETRIC_SUCCESS:
    
                BiometricPrompt.AuthenticationCallback authenticationCallback =
                        new BiometricPrompt.AuthenticationCallback(){
                            @Override
                            public void onAuthenticationError(int errorCode,
                                                              @NonNull CharSequence errString){
                                super.onAuthenticationError(errorCode, errString);
                                App.log("onAuthenticationError");
                                finish();
                            }
    
                            @Override
                            public void onAuthenticationFailed(){
                                super.onAuthenticationFailed();
                                App.log("onAuthenticationFailed");
        
                            }
    
                            @Override
                            public void onAuthenticationSucceeded(
                                    @NonNull BiometricPrompt.AuthenticationResult result){
                                super.onAuthenticationSucceeded(result);
        
                                FirebaseAnalytics.getInstance(ActivityMain.this)
                                        .getAppInstanceId()
                                        .addOnSuccessListener(onSuccessListener);
        
                            }
                        };
    
                Executor mainExecutor = ContextCompat.getMainExecutor(this);
    
                BiometricPrompt biometricPrompt =
                        new BiometricPrompt(this, mainExecutor, authenticationCallback);
    
                @SuppressWarnings("deprecation")
                BiometricPrompt.PromptInfo promptInfo =
                        new BiometricPrompt.PromptInfo.Builder()
                                .setTitle(getResources()
                                                  .getString(R.string.enter_with_fingerprint))
                                .setDescription(
                                        getResources()
                                                .getString(
                                                        R.string.enter_with_fingerprint_to_use_the_app))
                                .setDeviceCredentialAllowed(true)
                                .build();
    
                biometricPrompt.authenticate(promptInfo);
                
                App.log("BIOMETRIC_SUCCESS");
    
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
    
                App.log("BIOMETRIC_ERROR_NO_HARDWARE");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
    
                App.log("BIOMETRIC_ERROR_HW_UNAVAILABLE");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent;
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
                    enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                          BIOMETRIC_STRONG|DEVICE_CREDENTIAL);
                }else{
                    enrollIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
                }
                //noinspection deprecation
                startActivityForResult(enrollIntent, REQUEST_CODE);
                App.log("BIOMETRIC_ERROR_NONE_ENROLLED");
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                App.log("BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED");
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                App.log("BIOMETRIC_ERROR_UNSUPPORTED");
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                App.log("BIOMETRIC_STATUS_UNKNOWN");
                break;
        }
        
        
    }
    
    private void authenticate() throws JSONException{
        binding.btnMainActivityTryAgain.setVisibility(View.INVISIBLE);
        binding.pbMainActivity.setVisibility(View.VISIBLE);
        binding.tvMainActivity.setText(getResources().getString(R.string.connecting_to_server));
        
        JSONObject requestParameters = new JSONObject();
        
        requestParameters.put("hash", App.HASH);
        requestParameters.put("title", App.DEVICE_TITLE);
        
        Tools.IHttpPostResultHandler handler = new Tools.IHttpPostResultHandler(){
            @Override
            public void OnResponse(Response response){
                try{
                    ResponseBody body = response.body();
                    if(body != null){
                        String responseStr = body.string();
                        App.log("response: " + responseStr);
                        
                        
                        runOnUiThread(()->{
                            if(responseStr.contains(App.HASH)){
                                unlockActivity();
                            }else if(responseStr.contains("authentication")){
                                failed(0);
                                binding.tvMainActivity.setText(
                                        getResources().getString(R.string.device_not_approved));
                            }else if(responseStr.contains("new")){
                                failed(0);
                                binding.tvMainActivity.setText(
                                        getResources()
                                                .getString(R.string.waiting_for_device_approval));
                            }else{
                                failed(0);
                            }
                        });
                    }else{
                        failed(R.string.error_happened);
                    }
                    
                }catch(Exception e){
                    e.printStackTrace();
                    failed(R.string.error_happened);
                }
            }
            
            @Override
            public void OnFailure(){
                failed(R.string.error_happened);
            }
            
            private void failed(int message){
                if(message > 0){
                        App.toast(ActivityMain.this, getResources().getString(message));
                }
                runOnUiThread(() -> {
                    binding.btnMainActivityTryAgain.setVisibility(View.VISIBLE);
                    binding.btnMainActivityTryAgain.setOnClickListener(v->{
                        try{
                            authenticate();
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    });
    
                    binding.pbMainActivity.setVisibility(View.INVISIBLE);
    
                });
            }
        };
        
        Tools.httpPost(requestParameters, App.url + "authenticate.php",
                       "authenticate",
                       handler);
    }
    
    private void lockActivity(){
        binding.svMainActivity.setVisibility(View.GONE);
        binding.clMainActivityLockLayout.setVisibility(View.VISIBLE);
        binding.tvMainActivity.setText(getResources().getString(R.string.waiting_for_fingerprint));
        binding.btnMainActivityTryAgain.setVisibility(View.INVISIBLE);
    }
    
    
    private void unlockActivity(){
        binding.svMainActivity.setVisibility(View.VISIBLE);
        binding.clMainActivityLockLayout.setVisibility(View.GONE);
        binding.tvMainActivity.setVisibility(View.GONE);
        
        binding.addFile.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityAddFile.class)));
        binding.viewFiles.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityFiles.class)));
        binding.viewImages.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityImages.class)));
        binding.editNumbers.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityEditNumbers.class)));
        binding.viewTags.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityEditTags.class)));
        //noinspection deprecation
        binding.viewDevices.setOnClickListener(
                (v)->startActivityForResult(
                        new Intent(ActivityMain.this, ActivityViewDevices.class),
                        VIEW_DEVICE));
        binding.codeGenerator.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityCodeGenerator.class)));
        binding.manageComments.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityCommentManagement.class)));
        binding.answerComments.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityAnswerComments.class)));
        binding.manageBlogs.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityBlogManagement.class)));
        binding.footerInstagramPosts.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityFooterInstagramPosts.class)));
        binding.btnTestActivity.setOnClickListener(
                (v)->startActivity(
                        new Intent(ActivityMain.this, ActivityTest.class)));
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == VIEW_DEVICE){
            startActivity(
                    new Intent(ActivityMain.this, ActivityMain.class));
            finish();
        }
    }
}
