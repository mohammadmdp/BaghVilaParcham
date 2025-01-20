package mdp.baghvilaparchammanagementapp;

import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class Tools{
   
   public static void httpPost(JSONObject requestParameters, String url,
                               String caller, IHttpPostResultHandler handler){
       
       Request request;
       
       RequestBody body = RequestBody.create(requestParameters.toString(), ActivityFiles.MEDIA_TYPE_JSON);
       request = new Request.Builder().url(url).post(body).build();
       
       App.log(caller + " url: " + url + " data: " + requestParameters);
       
    
       List<ConnectionSpec> connectionSpecs =
               Arrays.asList(ConnectionSpec.MODERN_TLS);
       
       OkHttpClient client =
               new OkHttpClient.Builder()
                       .connectionSpecs(connectionSpecs)
                       .proxy(Proxy.NO_PROXY)
                       .connectTimeout(60, TimeUnit.SECONDS)
                       .socketFactory(new RestrictedSocketFactory(1024 * 1024))
                       .build();
    
       
       client.newCall(request).enqueue(new Callback(){
           @Override
           public void onFailure(@NonNull Call call, @NonNull IOException e){
               App.log(caller + " onFailure: " + e.getMessage());
               e.printStackTrace();
               handler.OnFailure();
           }
           
           @Override
           public void onResponse(@NonNull Call call, @NonNull Response response){
               App.log(caller + " onResponse");
               handler.OnResponse(response);
           }
       });
   }
   
   public interface IHttpPostResultHandler{
       
       void OnResponse(Response response);
       
       void OnFailure();
   }
}
