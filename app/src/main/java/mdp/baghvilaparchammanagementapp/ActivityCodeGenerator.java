package mdp.baghvilaparchammanagementapp;

import android.os.Bundle;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import mdp.baghvilaparchammanagementapp.databinding.ActivityCodeGeneratorBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemCodeBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityCodeGenerator extends AppCompatActivity{
    
    ActivityCodeGeneratorBinding binding;
    
    App app;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        binding = ActivityCodeGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        app = ((App) getApplication());
        
        binding.btnGenerateNewCode.setOnClickListener(v->generateCode());
        binding.btnRemoveCodes.setOnClickListener(v->removeCodes());
        
        try{
            getCodes();
        }catch(Exception e){
            e.printStackTrace();
            
            toggleViews(true);
            
            App.toast(ActivityCodeGenerator.this,
                      getResources().getString(R.string.error_happened));
        }
    }
    
    void toggleViews(boolean on){
        if(on){
            binding.btnGenerateNewCode.setVisibility(View.VISIBLE);
            binding.btnRemoveCodes.setVisibility(View.VISIBLE);
            binding.svGeneratedCodes.setVisibility(View.VISIBLE);
            
            binding.pbActivityCodeGenerator.setVisibility(View.INVISIBLE);
        }else{
            binding.btnGenerateNewCode.setVisibility(View.INVISIBLE);
            binding.btnRemoveCodes.setVisibility(View.INVISIBLE);
            binding.svGeneratedCodes.setVisibility(View.INVISIBLE);
            
            binding.pbActivityCodeGenerator.setVisibility(View.VISIBLE);
        }
    }
    
    private void generateCode(){
        toggleViews(false);
        
        try{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            
            Tools.IHttpPostResultHandler handler =
                    new Tools.IHttpPostResultHandler(){
                        @Override
                        public void OnResponse(Response response){
                            try{
                                
                                ResponseBody body = response.body();
                                if(body != null){
                                    String responseStr = body.string();
                                    App.log("response: " + responseStr);
                                    
                                    if(responseStr.contains("success")){
                                        getCodes();
                                        return;
                                    }
                                    
                                }
                                
                                
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            
                            OnFailure();
                        }
                        
                        @Override
                        public void OnFailure(){
                            runOnUiThread(() -> {
                                toggleViews(true);
                                App.toast(ActivityCodeGenerator.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "generate_code.php",
                    "generateCode", handler);
        }catch(Exception e){
            e.printStackTrace();
            
            toggleViews(true);
            
            App.toast(ActivityCodeGenerator.this,
                      getResources().getString(R.string.error_happened));
        }
    }
    
    private void removeCodes(){
        toggleViews(false);
        
        try{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            
            Tools.IHttpPostResultHandler handler =
                    new Tools.IHttpPostResultHandler(){
                        @Override
                        public void OnResponse(Response response){
                            try{
                                ResponseBody body = response.body();
                                if(body != null){
                                    String responseStr = body.string();
                                    App.log("response: " + responseStr);
                                    
                                    if(responseStr.contains("success")){
                                        getCodes();
                                        return;
                                    }
                                    
                                }
                                
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            
                            OnFailure();
                        }
                        
                        @Override
                        public void OnFailure(){
                            runOnUiThread(() -> {
                                toggleViews(true);
                                App.toast(ActivityCodeGenerator.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "remove_codes.php",
                    "removeCodes", handler);
            
        }catch(Exception e){
            e.printStackTrace();
            
            toggleViews(true);
            
            App.toast(ActivityCodeGenerator.this,
                      getResources().getString(R.string.error_happened));
        }
    }
    
    private void getCodes() throws Exception{
        
        runOnUiThread(() -> {
            toggleViews(false);
        });
        
        JSONObject requestParameters = new JSONObject();
        
        requestParameters.put("hash", App.HASH);
        
        Tools.IHttpPostResultHandler handler = new Tools.IHttpPostResultHandler(){
            @Override
            public void OnResponse(Response response){
                try{
                    
                    
                    ResponseBody body = response.body();
                    if(body != null){
                        String responseStr = body.string();
                        App.log("response: " + responseStr);
                        
                        
                        runOnUiThread(()->{
                            try{
                                toggleViews(true);
                                
                                binding.llSvGeneratedCodes.removeAllViews();
                                
                                JSONArray jsonArray = new JSONArray(responseStr);
                                
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    
                                    ListItemCodeBinding listItemCodeBinding =
                                            ListItemCodeBinding.inflate(getLayoutInflater(),
                                                                    binding.svGeneratedCodes,
                                                                    false);
    
                                    listItemCodeBinding.tvGeneratedCodeItem.setText(
                                            object.getString("code"));
                                    
                                    binding.llSvGeneratedCodes.addView(
                                            listItemCodeBinding.getRoot());
                                }
                                
                            }catch(Exception e){
                                e.printStackTrace();
                                OnFailure();
                            }
                        });
                    }
                    
                }catch(Exception e){
                    e.printStackTrace();
                    OnFailure();
                }
            }
            
            @Override
            public void OnFailure(){
                runOnUiThread(() -> {
                    toggleViews(true);
                    App.toast(ActivityCodeGenerator.this,
                              getResources().getString(R.string.error_happened));
                });
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url + "get_codes.php",
                "getCodes", handler);
        
    }
    
    
}
