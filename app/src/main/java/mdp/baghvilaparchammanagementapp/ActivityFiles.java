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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import mdp.baghvilaparchammanagementapp.databinding.ActivityFilesBinding;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityFiles extends AppCompatActivity{
    
    AdapterFiles adapterFiles;
    App          app;
    
    ActivityFilesBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        app = ((App) getApplication());
        binding.pbActivityFiles.setVisibility(View.VISIBLE);
        binding.gvFiles.setVisibility(View.INVISIBLE);
    
        binding.etSearch.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
        
            }
    
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
        
            }
    
            @Override
            public void afterTextChanged(Editable s){
                adapterFiles.getFilter().filter(s);
            }
        });
        
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                .withListener(new MultiplePermissionsListener(){
                    
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report){
                        if(report.areAllPermissionsGranted()){
                            setAdapterFiles();
                        }
                    }
                    
                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions,
                            PermissionToken token){
                    }
                }).check();
        
        
    }
    
    void setAdapterFiles(){
        try{
            getFiles();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; "
                                                                    + "charset=utf-8");
    
    private void getFiles() throws Exception{
        JSONObject requestParameters = new JSONObject();

        requestParameters.put("hash", App.HASH);

        Tools.IHttpPostResultHandler handler = new Tools.IHttpPostResultHandler(){
            @Override
            public void OnResponse(Response response){
                try{
                    ResponseBody body  = response.body();
                    if(body!=null){
                        String responseStr = body.string();
                        App.log("response: " + responseStr);
    
                        
    
                        runOnUiThread(()->{
                            try{
                                adapterFiles = new AdapterFiles(
                                        ActivityFiles.this,
                                        ItemFile.getListFromStringJson(responseStr));
                                binding.gvFiles.setAdapter(adapterFiles);
                                binding.pbActivityFiles.setVisibility(View.GONE);
                                binding.gvFiles.setVisibility(View.VISIBLE);
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
        
        Tools.httpPost(requestParameters, App.url + "get_files.php",
                       "getFiles",
                       handler);
        
    }
    
    
    void removeAdapter(){
        binding.gvFiles.setAdapter(null);
        adapterFiles = null;
        binding.pbActivityFiles.setVisibility(View.VISIBLE);
        binding.gvFiles.setVisibility(View.INVISIBLE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        
        removeAdapter();
        setAdapterFiles();
    }
}
