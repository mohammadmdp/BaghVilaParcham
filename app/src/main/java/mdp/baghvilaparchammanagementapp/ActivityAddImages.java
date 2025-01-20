package mdp.baghvilaparchammanagementapp;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import androidx.appcompat.app.AppCompatActivity;
import mdp.baghvilaparchammanagementapp.databinding.ActivityAddImagesBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogSendingBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class ActivityAddImages extends AppCompatActivity{
    
    static String URL = App.url + "add_images.php";
    ArrayList<Uri>    originalImagesUris    = new ArrayList<>();
    ArrayList<Uri>    readyNewImagesUris    = new ArrayList<>();
    ArrayList<Bitmap> readyNewImagesBitmaps = new ArrayList<>();
    
    ArrayList<Boolean> isImageFlippedList = new ArrayList<>();
    JSONArray          phones;
    boolean            isInfoAdded;
    
    AdapterNewImages adapterNewImages;
    
    ActivityAddImagesBinding binding;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityAddImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                .withListener(new MultiplePermissionsListener(){
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report){
                        
                        afterPermissionChecked();
                    }
                    
                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions,
                            PermissionToken token){/* ... */}
                }).check();
    }
    
    void afterPermissionChecked(){
        
        binding.btnSelectImages.setOnClickListener(
                v->PictureSelector.create(ActivityAddImages.this)
                           .openGallery(PictureMimeType.ofImage())
                           .imageEngine(GlideEngine.createGlideEngine())
                           .selectionMode(PictureConfig.MULTIPLE)
                           .maxSelectNum(99)
                           .forResult(new OnResultCallbackListener<LocalMedia>(){
                               @Override
                               public void onResult(List<LocalMedia> result){
                                   try{
                                       afterPicsSelected(result);
                                   }catch(Exception e){
                                       e.printStackTrace();
                                       App.toast(ActivityAddImages.this,
                                                 getResources().getString(R.string.error_happened));
                                   }
                               }
                            
                               @Override
                               public void onCancel(){
                                   App.log("PictureSelector onCancel");
                               }
                           }));
        
        binding.btnApplyContactInfo.setOnClickListener(v->{
            App.log("btnApplyContactInfo");
            if(binding.etFileCodeAddImages.getText().toString().trim().equalsIgnoreCase("")){
                App.toast(ActivityAddImages.this,
                          getResources().getString(
                                  R.string.enter_file_code_first));
                return;
            }
            applyContactInfo();
        });
        
        binding.btnApplyContactInfo.setEnabled(true);
        
        binding.btnSendNewImages.setOnClickListener(v->{
            if(!isInfoAdded){
                App.toast(ActivityAddImages.this,
                          getResources()
                                  .getString(R.string.contact_info_not_added));
                return;
            }
            
            try{
                sendNewImages();
            }catch(Exception e){
                e.printStackTrace();
                App.toast(ActivityAddImages.this,
                          getResources().getString(R.string.error_happened));
            }
        });
    }
    
    private void sendNewImages() throws Exception{
        
        Random random = new Random();
        readyNewImagesUris = new ArrayList<>();
        for(int i = 0; i < readyNewImagesBitmaps.size(); i++){
            Bitmap bitmap = readyNewImagesBitmaps.get(i);
            String fileName = ActivityAddFile.getFileName(ActivityAddImages.this,
                                                          originalImagesUris.get(i));
            
            Uri uri = writeBitmapToNewFilePath(bitmap, fileName + random.nextInt());
            readyNewImagesUris.add(uri);
        }
        
        if(readyNewImagesUris.isEmpty()){
            App.toast(ActivityAddImages.this,
                      getResources()
                              .getString(R.string.no_images_selected));
            return;
        }
        
        Dialog d = new Dialog(ActivityAddImages.this);
        
        try{
            
            
            JSONObject object = new JSONObject();
            try{
                object.put(ItemFile.FILE_CODE,
                           binding.etFileCodeAddImages.getText().toString().trim());
                object.put("hash", App.HASH);
            }catch(Exception e){
                e.printStackTrace();
            }
            
            DialogSendingBinding dialogBind =
                    DialogSendingBinding.inflate(getLayoutInflater());
            d.setContentView(dialogBind.getRoot());
            d.setCanceledOnTouchOutside(false);
            d.show();
            
            MediaType mediaType = MediaType.parse("image/jpeg");
            
            final AtomicLong totalFilesSize = new AtomicLong(0);
            final AtomicLong writtenSize    = new AtomicLong(0);
            
            UriRequestBody.ProgressListener listener = (currentWrittenBytes)->{
                writtenSize.addAndGet(currentWrittenBytes);
                int percent = Math.round(100 * writtenSize.get() / totalFilesSize.get());
                dialogBind.pbDialogSending.setProgress(percent);
            };
            
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("data", object.toString());
            
            for(int i = 0; i < readyNewImagesUris.size(); i++){
                UriRequestBody urb =
                        new UriRequestBody(mediaType, getContentResolver(),
                                           readyNewImagesUris.get(i), listener);
                
                totalFilesSize.addAndGet(urb.contentLength());
                
                App.log("totalFilesSize added: " + (urb.contentLength() / 1024)
                        + "kb total: " + (totalFilesSize.get() / 1024) + "kb");
                
                builder.addFormDataPart("images[]", "picture" + i + ".jpg", urb);
            }
            
            RequestBody requestBody = builder.build();
            
            Request request = new Request.Builder().url(URL).post(requestBody).build();
            
            App.log("sendNewImages url: " + URL);
            App.log("sendNewImages data: " + object);
            
            List<ConnectionSpec> connectionSpecs =
                    Arrays.asList(ConnectionSpec.MODERN_TLS,
                                  ConnectionSpec.COMPATIBLE_TLS);
            
            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .connectionSpecs(connectionSpecs)
                            .socketFactory(new RestrictedSocketFactory(16 * 1024))
                            .build();
            
            
            client.newCall(request).enqueue(new Callback(){
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e){
                    
                    App.log("sendNewImages : responseBody: onFailure: "
                            + e.getMessage());
                    
                    App.toast(ActivityAddImages.this,
                              getResources().getString(R.string.error_happened));
                    
                    dialogBind.tvDialogSendingTitle.setText(
                            getResources().getString(R.string.error_happened));
                    
                    dialogBind.btnDialogSendingRetry.setVisibility(View.VISIBLE);
                    
                    dialogBind.btnDialogSendingRetry.setOnClickListener(v1->{
                        dialogBind.btnDialogSendingRetry.setVisibility(View.GONE);
                        dialogBind.tvDialogSendingTitle.setText(
                                getResources().getString(R.string.sending_please_wait));
                        client.newCall(request).enqueue(this);
                    });
                }
                
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response){
                    try{
                        onSendImagesResponse(response, d);
                    }catch(Exception e){
                        e.printStackTrace();
                        App.toast(ActivityAddImages.this,
                                  getResources().getString(R.string.error_happened));
                        d.cancel();
                    }
                }
            });
        }catch(Exception|Error e){
            e.printStackTrace();
            d.cancel();
        }
    }
    
    private void onSendImagesResponse(Response response, Dialog d) throws Exception{
        ResponseBody responseBody = response.body();
        if(responseBody != null){
            String s = responseBody.string();
            App.log("sendNewImages : responseBody: " + s);
            try{
                JSONObject responseJson = new JSONObject(s);
                if(!responseJson.getBoolean("error")){
                    runOnUiThread(()->{
                        d.cancel();
                        App.toast(ActivityAddImages.this, getString(R.string.done));
                        ActivityAddImages.this.finish();
                    });
                }else{
                    runOnUiThread(()->App.toast(
                            ActivityAddImages.this, getString(R.string.error_happened)));
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            responseBody.close();
        }
        d.cancel();
    }
    
    private void applyContactInfo(){
        try{
            phones = DBModelPhone.getPhones(ActivityAddImages.this);
            if(phones.length() <= 1){
                
                App.toast(ActivityAddImages.this,
                          getResources().getString(R.string.numbers_not_been_set));
                
                Intent intent = new Intent(ActivityAddImages.this,
                                           ActivityEditNumbers.class);
                startActivity(intent);
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
            App.toast(ActivityAddImages.this, getResources().getString(
                    R.string.error_happened));
        }
        
        readyNewImagesBitmaps = new ArrayList<>();
        for(Uri originalImage : originalImagesUris){
            
            try{
                
                Bitmap originalImageBitmap = getBitmapFromUri(originalImage);
                
                originalImageBitmap = resizeBitmap(originalImageBitmap);
                
                String code = binding.etFileCodeAddImages.getText().toString().trim();
                
                originalImageBitmap =
                        ActivityAddFile.overlayBitmap(ActivityAddImages.this,
                                                      originalImageBitmap, phones, code);
                
                readyNewImagesBitmaps.add(originalImageBitmap);

//                String fileName =
//                        ActivityAddFile.getFileName(ActivityAddImages.this, originalImage);
//
//                Uri newFilePath = writeBitmapToNewFilePath(resizedImageBitmap, fileName);
//
//
//                readyNewImagesUris.add(newFilePath);
            
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        adapterNewImages = new AdapterNewImages(ActivityAddImages.this);
        binding.gvNewImages.setAdapter(adapterNewImages);
        isInfoAdded = true;
    }
    
    private Bitmap resizeBitmap(Bitmap originalImageBitmap){
        int w, h;
        w = 1280;
        h = (int) (1280f / originalImageBitmap.getWidth() * originalImageBitmap
                                                                    .getHeight());
        
        return Bitmap.createScaledBitmap(originalImageBitmap, w, h,
                                         true);
    }
    
    public Bitmap getBitmapFromUri(Uri uri) throws FileNotFoundException{
        InputStream inputStream = getContentResolver().openInputStream(uri);
        
        return BitmapFactory.decodeStream(inputStream);
    }
    
    public Uri writeBitmapToNewFilePath(Bitmap bmp, String fileName) throws IOException{
        
        Uri newFilePath = createNewFilePath(fileName);
        
        OutputStream os = getContentResolver().openOutputStream(newFilePath, "rw");
        
        bmp.compress(Bitmap.CompressFormat.JPEG, 88, os);
        
        os.close();
        
        return newFilePath;
    }

//    public void replaceBitmap(Bitmap bmp, Uri imageFilePath) throws IOException{
//
//        OutputStream os = getContentResolver().openOutputStream(imageFilePath, "rwt");
//
//        bmp.compress(Bitmap.CompressFormat.JPEG, 88, os);
//
//        os.close();
//
//        App.log("replaceBitmap");
//    }
    
    public Uri createNewFilePath(String fileName){
        ContentValues values = new ContentValues();
        Random        random = new Random();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + "_" + random.nextInt());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, App.PIC_DIR);
        
        Set<String> volumeNames =
                MediaStore.getExternalVolumeNames(ActivityAddImages.this);
        
        
        App.log("getExternalVolumeNames: " + volumeNames.toArray()[0]);
        
        Uri contentUri = MediaStore.Files.getContentUri((String) volumeNames.toArray()[0]);
        
        return getContentResolver().insert(contentUri, values);
    }
    
    public Bitmap rotateBitmapRight(Bitmap source) throws IllegalArgumentException{
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                   true);
    }
    
    public Bitmap rotateBitmapLeft(Bitmap source) throws IllegalArgumentException{
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                   true);
    }
    
    public Bitmap rotateBitmapOriginal(Bitmap source, int position) throws Exception{
        
        if(!isImageFlippedList.get(position)){
            
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            
            isImageFlippedList.set(position, true);
            
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                       true);
        }
        isImageFlippedList.set(position, false);
        
        return source;
    }
    
    
    void afterPicsSelected(List<LocalMedia> result) throws Exception{
        App.log("PictureSelector onResult: " + result.get(0).getPath());
        
        originalImagesUris = new ArrayList<>();
        
        for(LocalMedia localMedia : result){
            originalImagesUris.add(Uri.parse(localMedia.getPath()));
            isImageFlippedList.add(false);
        }
        
        if(originalImagesUris.size() > 0){
            String picsSelected = String.format(Locale.ENGLISH, "(%d) %s",
                                                originalImagesUris.size(),
                                                getResources().getString(R.string.chosen_images));
            
            binding.btnSelectImages.setText(picsSelected);
        }
    }
    
    class AdapterNewImages extends BaseAdapter{
        
        Context context;
        
        AdapterNewImages(Context context){
            this.context = context;
        }
        
        @Override
        public int getCount(){
            return readyNewImagesBitmaps.size();
        }
        
        @Override
        public Bitmap getItem(int position){
            return readyNewImagesBitmaps.get(position);
        }
        
        @Override
        public long getItemId(int position){
            return position;
        }
        
        class VH{
            
            ImageView ivRemove;
            ImageView ivRotateRight, ivRotateOriginal, ivRotateLeft;
            ImageView ivImage;
        }
        
        void removeImage(int pos){
            isImageFlippedList.remove(pos);
            originalImagesUris.remove(pos);
            readyNewImagesBitmaps.remove(pos);
            notifyDataSetChanged();
            binding.btnSelectImages.setText(
                    String.format(Locale.ENGLISH, "(%d) %s", readyNewImagesBitmaps.size(),
                                  getResources().getString(R.string.chosen_images)));
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            VH vh;
            if(convertView == null){
                convertView = LayoutInflater.from(context)
                                      .inflate(R.layout.list_item_new_image, parent, false);
                vh = new VH();
                vh.ivImage = convertView.findViewById(R.id.iv_image_new);
                vh.ivRemove = convertView.findViewById(R.id.iv_remove_new_image);
                vh.ivRotateLeft = convertView.findViewById(R.id.iv_rotate_image_new_left);
                vh.ivRotateOriginal = convertView.findViewById(R.id.iv_rotate_image_new_original);
                vh.ivRotateRight = convertView.findViewById(R.id.iv_rotate_image_new_right);
                convertView.setTag(vh);
            }else{
                vh = (VH) convertView.getTag();
            }
            vh.ivRemove.setOnClickListener(v->removeImage(position));
            vh.ivImage.setImageBitmap(getItem(position));
            vh.ivRotateLeft.setOnClickListener(v->{
                try{
                    rotateImageLeft(position);
                }catch(Exception e){
                    e.printStackTrace();
                    App.toast(ActivityAddImages.this,
                              getResources().getString(R.string.error_happened));
                }
            });
            vh.ivRotateOriginal.setOnClickListener(v->{
                try{
                    rotateImageOriginal(position);
                }catch(Exception e){
                    e.printStackTrace();
                    App.toast(ActivityAddImages.this,
                              getResources().getString(R.string.error_happened));
                }
            });
            vh.ivRotateRight.setOnClickListener(v->{
                try{
                    rotateImageRight(position);
                }catch(Exception e){
                    e.printStackTrace();
                    App.toast(ActivityAddImages.this,
                              getResources().getString(R.string.error_happened));
                }
            });
            
            return convertView;
        }
        
        
    }
    
    public void rotateImageLeft(int position) throws Exception{
        Uri uri = originalImagesUris.get(position);
        
        Bitmap bmp = getBitmapFromUri(uri);
        
        bmp = rotateBitmapLeft(bmp);
    
        bmp = resizeBitmap(bmp);
        
        String code = binding.etFileCodeAddImages.getText().toString().trim();
        
        bmp = ActivityAddFile.overlayBitmap(ActivityAddImages.this,
                                            bmp, phones, code);
        
        readyNewImagesBitmaps.set(position, bmp);

//        AdapterNewImages adapterNewImages = new AdapterNewImages(ActivityAddImages.this);
//        binding.gvNewImages.setAdapter(adapterNewImages);
        
        adapterNewImages.notifyDataSetChanged();
        isInfoAdded = true;
    }
    
    public void rotateImageOriginal(int position) throws Exception{
        Uri uri = originalImagesUris.get(position);
        
        Bitmap bmp = getBitmapFromUri(uri);
        
        bmp = rotateBitmapOriginal(bmp, position);
    
        bmp = resizeBitmap(bmp);
        
        String code = binding.etFileCodeAddImages.getText().toString().trim();
        
        bmp = ActivityAddFile.overlayBitmap(ActivityAddImages.this,
                                            bmp, phones, code);
        
        readyNewImagesBitmaps.set(position, bmp);
        
        adapterNewImages.notifyDataSetChanged();
        isInfoAdded = true;
    }
    
    public void rotateImageRight(int position) throws Exception{
        Uri uri = originalImagesUris.get(position);
        
        Bitmap bmp = getBitmapFromUri(uri);
        
        bmp = rotateBitmapRight(bmp);
        
        bmp = resizeBitmap(bmp);
        
        String code = binding.etFileCodeAddImages.getText().toString().trim();
        
        bmp = ActivityAddFile.overlayBitmap(ActivityAddImages.this,
                                            bmp, phones, code);
        
        readyNewImagesBitmaps.set(position, bmp);
        
        adapterNewImages.notifyDataSetChanged();
        isInfoAdded = true;
    }
    
}
