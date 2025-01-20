package mdp.baghvilaparchammanagementapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONObject;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import mdp.baghvilaparchammanagementapp.databinding.ActivityImagesBinding;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityImages extends AppCompatActivity{
    
    ActivityImagesBinding binding;
    AdapterImages adapterImages;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                .withListener(new MultiplePermissionsListener(){
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report){
                        if(report.areAllPermissionsGranted()){
                            afterPermissionsChecked();
                        }
                    }
                    
                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions,
                            PermissionToken token){/* ... */}
                }).check();
    
        binding.btnAddImages.setOnClickListener(v->startActivity(
                new Intent(ActivityImages.this, ActivityAddImages.class)));
    }
    
    
    
    static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; "
                                                             + "charset=utf-8");
    
    
    
    private void afterPermissionsChecked(){
        try{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            
            Tools.IHttpPostResultHandler handler = new Tools.IHttpPostResultHandler(){
                @Override
                public void OnResponse(Response response){
                    try{
                        ResponseBody body = response.body();
                        if(body != null){
                            String responseStr = body.string();
                            
                            App.log("getImages OnResponse: " + responseStr);
                            
                            runOnUiThread(()->{
    
                                try{
                                    adapterImages = new AdapterImages(
                                            ActivityImages.this,
                                            ItemImage.getListFromStringJson(responseStr));
                                    binding.progressBar2.setVisibility(View.GONE);
                                    binding.gvImages.setVisibility(View.VISIBLE);
                                    binding.gvImages.setAdapter(adapterImages);
    
                                    binding.etSearchImages.addTextChangedListener(new TextWatcher(){
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start,
                                                                      int count, int after){
            
                                        }
        
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before,
                                                                  int count){
            
                                        }
        
                                        @Override
                                        public void afterTextChanged(Editable s){
                                            adapterImages.getFilter().filter(s);
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
    
                            });
                        }
                        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                
                @Override
                public void OnFailure(){
                
                }
            };
    
            Tools.httpPost(requestParameters, App.url+"get_images.php", "get_images", handler);
            
        }catch(Exception|Error e){
            e.printStackTrace();
        }
        
    }
    
    public void removeAdapter(){
        binding.gvImages.setAdapter(null);
        adapterImages = null;
    }
    
}
