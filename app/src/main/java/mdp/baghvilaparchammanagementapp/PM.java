package mdp.baghvilaparchammanagementapp;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 Public Methods
 */
class PM{
    
    private static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG  = MediaType.parse("image/png");
    private static final MediaType MEDIA_TYPE_JPG  = MediaType.parse("image/jpg");
    
    /**
     A blocking POST http request. It needs to be run on a different {@link Thread}
     */
    @NonNull
    private static String postHttp(@NonNull String url, @NonNull JSONObject requestParams,
                                   @Nullable String callerName) throws Exception{
        
        App.log(callerName + " data:" + requestParams.toString());
        App.log(callerName + " url:" + url);
        
        RequestBody requestBody = RequestBody.create(requestParams.toString(), MEDIA_TYPE_JSON);
        
        Request request = new Request.Builder()
                                  .url(url)
                                  .post(requestBody)
                                  .build();
        
        OkHttpClient client = new OkHttpClient.Builder()
                                      .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS,
                                                                     ConnectionSpec.COMPATIBLE_TLS))
                                      .build();
        
        Response     response       = client.newCall(request).execute();
        ResponseBody responseBody   = response.body();
        String       responseString = "";
        if(responseBody != null){
            responseString = responseBody.string();
            responseBody.close();
        }
        
        App.log(callerName + " onSuccess:" + responseString);
        return responseString.trim();
    }
    
    public void uploadImage(File imageFile, String url) throws Exception{
        
        OkHttpClient client = new OkHttpClient.Builder()
                                      .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS,
                                                                     ConnectionSpec.COMPATIBLE_TLS))
                                      .build();
    
        
        Request request = new Request.Builder()
                                  .url(url)
                                  .post(RequestBody.create(MEDIA_TYPE_JPG,imageFile))
                                  .build();
        
        try(Response response = client.newCall(request).execute()){
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody responseBody = response.body();
            if(responseBody != null){
                App.log(responseBody.string());
            }
        }
    }
    
}
