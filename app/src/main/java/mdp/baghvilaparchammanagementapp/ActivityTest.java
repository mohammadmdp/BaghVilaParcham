package mdp.baghvilaparchammanagementapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import mdp.baghvilaparchammanagementapp.databinding.ActivityTestBinding;

public class ActivityTest extends AppCompatActivity {

    ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnTest.setOnClickListener(view -> PictureSelector.create(ActivityTest.this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .selectionMode(PictureConfig.SINGLE)
                .forResult(new OnResultCallbackListener<>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {


                        Uri originalImageUri = Uri.parse(result.get(0).getPath());

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(originalImageUri);

                            App.log("inputStream size: " + inputStream.available());

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            byte[] buffer = new byte[1024];
                            int read = 0;
                            while ((read = inputStream.read(buffer)) > 0) {
                                baos.write(buffer, 0, read);
                                baos.flush();
                            }
                            inputStream.close();

                            uploadData(baos.toByteArray());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//
//                        MediaType mediaType = MediaType.parse("image/*");
//
//                        OkHttpClient client = new OkHttpClient();
//
//                        UriRequestBody urb =
//                                new UriRequestBody(mediaType, getContentResolver(), originalImageUri,
//                                        currentWrittenBytes -> App.log("currentWrittenBytes: "+currentWrittenBytes));
//
//                        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                                .addFormDataPart("picture", "picture.jpg", urb)
//                                .build();
//
//                        Request request = new Request.Builder().url("https://baghvilaparcham.com:443/addd.php")
//                                .post(requestBody).build();
//
//                        try {
//                            client.newCall(request)
//                                    .enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        @Override
//                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                                            ResponseBody responseBody = response.body();
//                                            if(responseBody != null){
//                                                String s = responseBody.string();
//                                                App.log("btnSendAddFile : responseBody: " + s);
//                                                responseBody.close();
//                                            }
//                                        }
//                                    });
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }

                    @Override
                    public void onCancel() {
                        App.log("PictureSelector onCancel");
                    }
                }));
    }

    public static void uploadData(byte[] data) {
        new Thread(
                () -> {
                    HttpURLConnection connection;
                    DataOutputStream outputStream;
//                    DataInputStream inputStream;

                    StringBuilder response = new StringBuilder();

                    try {

                        URL url1 = new URL("https://baghvilaparcham.com:443/addd.php");
                        connection = (HttpURLConnection) url1.openConnection();

                        // Allow Inputs & Outputs
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setUseCaches(false);

                        // Enable POST method
                        connection.setRequestMethod("POST");

                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Content-Type", "multipart/form-data");

                        connection.setInstanceFollowRedirects(false);

                        outputStream = new DataOutputStream(connection.getOutputStream());

                        App.log("data size: " + data.length);

                        outputStream.write(data);

                        App.log("after outputStream.write(data)");

//                        inputStream = new DataInputStream(connection.getInputStream());

                        InputStream is = connection.getInputStream();

                        App.log("after connection.getInputStream");

                        BufferedReader br = new BufferedReader(new InputStreamReader(is));

                        App.log("before connection.getResponseCode");

                        int serverResponseCode = connection.getResponseCode();

                        String serverResponseMessage;
                        App.log("serverResponseCode: " + serverResponseCode);

                        while ((serverResponseMessage = br.readLine()) != null) {
                            response.append(serverResponseMessage);
                        }

                        outputStream.flush();
                        outputStream.close();
                        App.log("response:" + response);

                    } catch (Exception ex) {
                        response = new StringBuilder(ex.toString());
                        App.log("response:" + response);
                    }
                }
        ).start();


    }

    
}