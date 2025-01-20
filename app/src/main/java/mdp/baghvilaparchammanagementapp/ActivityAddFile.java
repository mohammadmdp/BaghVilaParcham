package mdp.baghvilaparchammanagementapp;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import mdp.baghvilaparchammanagementapp.databinding.ActivityAddFileBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogConfirmBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogSendingBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class ActivityAddFile extends AppCompatActivity {


    Uri finalImageUri, originalImageUri;

    Bitmap finalImageBitmap;

    boolean isImageFlipped = false;

    ActivityAddFileBinding binding;

    boolean isEditing;

    boolean isImageSelected = false;
    private String URL = App.url + "addFile.php";
    JSONArray phones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String editData = extras.getString("edit");
            if (editData != null) {
                try {
                    JSONObject object = new JSONObject(editData);
                    binding.etFileCode.setText(object.getString(ItemFile.FILE_CODE));
                    binding.etFileTitle.setText(object.getString(ItemFile.FILE_TITLE));
                    binding.etFileText.setText(object.getString(ItemFile.FILE_TEXT));
                    binding.etPriceTitle.setText(object.getString(ItemFile.FILE_PRICE_TITLE));
                    binding.etPrice.setText(object.getString(ItemFile.FILE_PRICE));
                    binding.etArea.setText(object.getString(ItemFile.FILE_AREA));
                    binding.etFileDetails.setText(object.getString(ItemFile.FILE_DETAILS));
                    binding.etFileHiddenText.setText(object.getString(ItemFile.FILE_HIDDEN_TEXT));
                    binding.swSold.setChecked(object.getInt(ItemFile.FILE_SOLD) > 0);
                    binding.swOffer.setChecked(object.getInt(ItemFile.FILE_OFFER) > 0);
                    isEditing = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        afterPermissionChecked();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions,
                            PermissionToken token) {/* ... */}
                }).check();


    }

    private void afterPermissionChecked() {

        if (isEditing) {
            binding.ivFilePicture.setVisibility(View.GONE);
            binding.btnSelectPicture.setVisibility(View.GONE);
            binding.tvLabelFilePicture.setVisibility(View.GONE);
        }

        binding.btnSelectPicture.setOnClickListener(
                v -> {
                    if (binding.etFileCode.getText().toString().trim().equalsIgnoreCase("")) {
                        App.toast(ActivityAddFile.this,
                                getResources().getString(
                                        R.string.enter_file_code_first));
                        return;
                    }
                    try {
                        phones = DBModelPhone.getPhones(ActivityAddFile.this);
                        if (phones.length() <= 1) {
                            App.toast(ActivityAddFile.this,
                                    getResources().getString(
                                            R.string.numbers_not_been_set));
                            Intent intent = new Intent(ActivityAddFile.this,
                                    ActivityEditNumbers.class);
                            startActivity(intent);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        App.toast(ActivityAddFile.this, getResources().getString(
                                R.string.error_happened));
                    }

                    PictureSelector.create(ActivityAddFile.this)
                            .openGallery(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.SINGLE)
                            .forResult(new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {

                                    onPictureSelectorResult(result);

                                }

                                @Override
                                public void onCancel() {
                                    App.log("PictureSelector onCancel");
                                }
                            });

                });

        binding.btnSendAddFile.setOnClickListener(v -> {
            Dialog d = new Dialog(ActivityAddFile.this);
            try {


                String code = binding.etFileCode.getText().toString().trim(),
                        title = binding.etFileTitle.getText().toString().trim(),
                        text = binding.etFileText.getText().toString().trim(),
                        pTitle = binding.etPriceTitle.getText().toString().trim(),
                        price = binding.etPrice.getText().toString().trim(),
                        area = binding.etArea.getText().toString().trim(),
                        details = binding.etFileDetails.getText().toString().trim(),
                        hiddenText = binding.etFileHiddenText.getText().toString().trim();

                if (code.isEmpty() || title.isEmpty() || text.isEmpty() || pTitle.isEmpty() ||
                        price.isEmpty() || area.isEmpty() || details.isEmpty()) {

                    App.toast(ActivityAddFile.this,
                            getResources().getString(R.string.all_fields_must_be_filled));
                    return;
                }


                if (isEditing) {
                    URL = App.url + "edit_file.php";
                }

                JSONObject object = new JSONObject();
                try {
                    object.put("hash", App.HASH);
                    object.put(ItemFile.FILE_CODE, code);
                    object.put(ItemFile.FILE_TITLE, title);
                    object.put(ItemFile.FILE_TEXT, text);
                    object.put(ItemFile.FILE_PRICE_TITLE, pTitle);
                    object.put(ItemFile.FILE_PRICE, Double.valueOf(price));
                    object.put(ItemFile.FILE_AREA, Double.valueOf(area));
                    object.put(ItemFile.FILE_DETAILS, details);
                    object.put(ItemFile.FILE_HIDDEN_TEXT, hiddenText);
                    object.put(ItemFile.FILE_SOLD, binding.swSold.isChecked() ? 1 : 0);
                    object.put(ItemFile.FILE_OFFER, binding.swOffer.isChecked() ? 1 : 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DialogSendingBinding dialogBind =
                        DialogSendingBinding.inflate(getLayoutInflater());

                d.setContentView(dialogBind.getRoot());
                d.setCanceledOnTouchOutside(false);
                d.show();

                try {
                    MediaType mediaType = MediaType.parse("image/*");

                    final AtomicLong totalFilesSize = new AtomicLong(0);
                    final AtomicLong writtenSize = new AtomicLong(0);

                    UriRequestBody.ProgressListener listener = (currentWrittenBytes) -> {
                        writtenSize.addAndGet(currentWrittenBytes);
                        int percent = Math.round(100 * writtenSize.get() / totalFilesSize.get());
                        dialogBind.pbDialogSending.setProgress(percent);
                        App.log("percent: " + percent +
                                " written: " + (writtenSize.get() / 1024) +
                                " of " + (totalFilesSize.get() / 1024));
                    };


                    MultipartBody.Builder builder = new MultipartBody.Builder();

                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("data", object.toString());

                    if (isEditing) {
                        builder.addFormDataPart("hash", App.HASH);
                    } else {
                        finalImageUri = writeBitmapToNewFilePath(finalImageBitmap, originalImageUri);

                        UriRequestBody urb =
                                new UriRequestBody(mediaType, getContentResolver(), finalImageUri,
                                        listener);
                        totalFilesSize.set(urb.contentLength());

                        builder.addFormDataPart("picture", "picture.jpg", urb);
                    }

                    RequestBody requestBody = builder.build();

                    Request request = new Request.Builder().url(URL).post(requestBody).build();

                    List<ConnectionSpec> connectionSpecs =
                            Arrays.asList(ConnectionSpec.MODERN_TLS,
                                    ConnectionSpec.COMPATIBLE_TLS);

                    ArrayList<Protocol> list = new ArrayList<>();
                    list.add(Protocol.HTTP_1_1);

                    OkHttpClient client =
                            new OkHttpClient.Builder()
                                    .connectionSpecs(connectionSpecs)
                                    .socketFactory(new RestrictedSocketFactory(2 * 1024))
                                    .protocols(list)
                                    .retryOnConnectionFailure(false)
                                    .writeTimeout(10, TimeUnit.MINUTES)
                                    .callTimeout(30, TimeUnit.MINUTES)
                                    .connectTimeout(60, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.MINUTES)
                                    .build();


                    App.log("addFile url: " + URL);
                    App.log("addFile data: " + object);


                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                            App.log("btnSendAddFile : responseBody: onFailure: "
                                    + e.getMessage());

                            App.toast(ActivityAddFile.this,
                                    getResources().getString(R.string.error_happened));

                            ActivityAddFile.this.runOnUiThread(() -> {

                                dialogBind.pbDialogSending.setProgress(0);
                                writtenSize.set(0);

                                dialogBind.tvDialogSendingTitle.setText(
                                        getResources().getString(R.string.error_happened));

                                dialogBind.btnDialogSendingRetry.setVisibility(View.VISIBLE);

                                dialogBind.btnDialogSendingRetry.setOnClickListener(v1 -> {


                                    dialogBind.btnDialogSendingRetry.setVisibility(View.GONE);
                                    dialogBind.tvDialogSendingTitle.setText(
                                            getResources().getString(R.string.sending_please_wait));
                                    client.newCall(request).enqueue(this);
                                });
                            });


                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            try {
                                onSendFileResponse(response, d);
                            } catch (Exception | Error e) {
                                e.printStackTrace();
                                App.toast(ActivityAddFile.this,
                                        getResources().getString(R.string.error_happened));
                                d.cancel();
                            }
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    App.toast(ActivityAddFile.this,
                            getResources().getString(R.string.error_happened));
                    d.cancel();
                }

            } catch (Exception | Error e) {
                e.printStackTrace();
                App.toast(ActivityAddFile.this,
                        getResources().getString(R.string.error_happened));
                d.cancel();
            }

        });

        binding.ivRotateImageRight.setOnClickListener(v -> {
            try {
                finalImageBitmap = getBitmapFromUri(originalImageUri);

                finalImageBitmap = rotateBitmapRight(finalImageBitmap);

                finalImageBitmap = resizeBitmap(finalImageBitmap);

                String code = binding.etFileCode.getText().toString().trim();

                finalImageBitmap = overlayBitmap(ActivityAddFile.this, finalImageBitmap, phones, code);

                binding.ivFilePicture.setImageBitmap(finalImageBitmap);

            } catch (Exception e) {
                e.printStackTrace();
                App.toast(ActivityAddFile.this, getResources().getString(R.string.error_happened));
            }
        });
        binding.ivRotateImageLeft.setOnClickListener(v -> {
            try {
                finalImageBitmap = getBitmapFromUri(originalImageUri);

                finalImageBitmap = rotateBitmapLeft(finalImageBitmap);

                finalImageBitmap = resizeBitmap(finalImageBitmap);

                String code = binding.etFileCode.getText().toString().trim();

                finalImageBitmap = overlayBitmap(ActivityAddFile.this, finalImageBitmap, phones, code);

                binding.ivFilePicture.setImageBitmap(finalImageBitmap);

            } catch (Exception e) {
                e.printStackTrace();
                App.toast(ActivityAddFile.this, getResources().getString(R.string.error_happened));
            }
        });
        binding.ivRotateImageOriginal.setOnClickListener(v -> {
            try {
                finalImageBitmap = getBitmapFromUri(originalImageUri);

                finalImageBitmap = rotateBitmapOriginal(finalImageBitmap);

                finalImageBitmap = resizeBitmap(finalImageBitmap);

                String code = binding.etFileCode.getText().toString().trim();

                finalImageBitmap = overlayBitmap(ActivityAddFile.this, finalImageBitmap, phones, code);

                binding.ivFilePicture.setImageBitmap(finalImageBitmap);

            } catch (Exception e) {
                e.printStackTrace();
                App.toast(ActivityAddFile.this, getResources().getString(R.string.error_happened));
            }
        });
    }

    private void onSendFileResponse(@NotNull Response response, Dialog d) throws Exception {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String s = responseBody.string();
            App.log("btnSendAddFile : responseBody: " + s);
            if (s.toLowerCase(Locale.ENGLISH).contains("success")) {
                runOnUiThread(() -> {
                    d.cancel();
                    App.toast(ActivityAddFile.this, getString(R.string.done));

                });
            } else {
                runOnUiThread(() -> App.toast(
                        ActivityAddFile.this, getString(R.string.error_happened)));
            }
            responseBody.close();
        }
        d.cancel();
    }

    private void onPictureSelectorResult(List<LocalMedia> result) {

        Dialog dialog = new Dialog(ActivityAddFile.this);

        DialogConfirmBinding confirmBinding = DialogConfirmBinding.inflate(getLayoutInflater());

        dialog.setContentView(confirmBinding.getRoot());

        confirmBinding.tvDialogTitle.setText(
                getResources().getString(R.string.processing_image_please_wait));

        confirmBinding.btnDialogDeny.setVisibility(View.INVISIBLE);
        confirmBinding.btnDialogAccept.setVisibility(View.INVISIBLE);
        confirmBinding.pbDialogConfirm.setVisibility(View.VISIBLE);

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        originalImageUri = Uri.parse(result.get(0).getPath());

        try {

            finalImageBitmap = getBitmapFromUri(originalImageUri);

            finalImageBitmap = resizeBitmap(finalImageBitmap);

            String code = binding.etFileCode.getText().toString().trim();

            finalImageBitmap = overlayBitmap(ActivityAddFile.this, finalImageBitmap, phones, code);

            binding.ivFilePicture.setImageBitmap(finalImageBitmap);

            isImageSelected = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.cancel();
    }

    public Uri writeBitmapToNewFilePath(Bitmap bmp, Uri uri) throws IOException {

        Uri newFilePath = createNewFilePath(uri);

        OutputStream os = getContentResolver().openOutputStream(newFilePath, "rw");

        bmp.compress(Bitmap.CompressFormat.JPEG, 88, os);

        os.close();

        return newFilePath;
    }

    public Bitmap rotateBitmapRight(Bitmap source) throws IllegalArgumentException {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    public Bitmap rotateBitmapLeft(Bitmap source) throws IllegalArgumentException {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    public Bitmap rotateBitmapOriginal(Bitmap source) throws IllegalArgumentException {
        if (!isImageFlipped) {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            isImageFlipped = true;
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                    true);
        }
        isImageFlipped = false;
        return source;
    }

    private Uri createNewFilePath(Uri uri) {
        ContentValues values = new ContentValues();

        String fileName = ActivityAddFile.getFileName(ActivityAddFile.this, uri);
        Random random = new Random();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + "_" + random.nextInt());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, App.PIC_DIR);

        Set<String> externalVolumeNames =
                MediaStore.getExternalVolumeNames(ActivityAddFile.this);

        Uri cUri = MediaStore.Files.getContentUri((String) externalVolumeNames.toArray()[0]);

        return getContentResolver().insert(cUri, values);
    }

    public static Bitmap resizeBitmap(Bitmap bmp) {
        int w, h;
        w = 1280;
        h = (int) (1280f / bmp.getWidth() * bmp.getHeight());

        return Bitmap.createScaledBitmap(bmp, w, h, true);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        return BitmapFactory.decodeStream(inputStream);
    }

    public static Bitmap overlayBitmap(Context context, Bitmap bg, JSONArray phones,
                                       String code) {
        Bitmap result = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), bg.getConfig());
        Bitmap overlayBitmap;
        Canvas overlayCanvas;
        Canvas resultCanvas = new Canvas(result);
        String text;

        resultCanvas.drawBitmap(bg, new Matrix(), null);

        try {
            text = phones.getJSONObject(1).getString(DBModelPhone.KEY_NUMBER);
            overlayBitmap = Bitmap.createBitmap(320, 74, Bitmap.Config.ARGB_8888);
            overlayCanvas = new Canvas(overlayBitmap);
            drawText(context, overlayCanvas, text, false, false);
            resultCanvas.drawBitmap(overlayBitmap, 20, result.getHeight() - 74, null);
        } catch (Exception e) {
            e.printStackTrace();
            App.toast(context, context.getResources().getString(R.string.error_happened));
        }

        try {
            text = phones.getJSONObject(0).getString(DBModelPhone.KEY_NUMBER);
            overlayBitmap = Bitmap.createBitmap(320, 74, Bitmap.Config.ARGB_8888);
            overlayCanvas = new Canvas(overlayBitmap);
            drawText(context, overlayCanvas, text, false, false);
            resultCanvas.drawBitmap(overlayBitmap, 20, result.getHeight() - 158, null);
        } catch (Exception e) {
            e.printStackTrace();
            App.toast(context, context.getResources().getString(R.string.error_happened));
        }

        try {
            text = "Code: " + code;
            overlayBitmap = Bitmap.createBitmap(320, 74, Bitmap.Config.ARGB_8888);
            overlayCanvas = new Canvas(overlayBitmap);
            drawText(context, overlayCanvas, text, true, false);
            resultCanvas.drawBitmap(overlayBitmap, result.getWidth() - 340,
                    result.getHeight() - 74, null);
        } catch (Exception e) {
            e.printStackTrace();
            App.toast(context, context.getResources().getString(R.string.error_happened));
        }

        try {
            text = "@BaghVilaParcham";
            overlayBitmap = Bitmap.createBitmap(320, 74, Bitmap.Config.ARGB_8888);
            overlayCanvas = new Canvas(overlayBitmap);
            drawText(context, overlayCanvas, text, false, true);
            resultCanvas.drawBitmap(overlayBitmap, (result.getWidth() / 2) - 160,
                    result.getHeight() - 74, null);
        } catch (Exception e) {
            e.printStackTrace();
            App.toast(context, context.getResources().getString(R.string.error_happened));
        }

        overlayBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);
        drawLogo(context, overlayCanvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        resultCanvas.drawBitmap(overlayBitmap, (bg.getWidth() / 2) - 100f, 20, paint);


        return result;
    }

    private static void drawLogo(Context context, Canvas c) {
        Paint paint = new Paint();
        RectF rectF;
        rectF = new RectF(0f, 0f, 200f, 200f);
        drawCircle(context, rectF, c);
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        logo = Bitmap.createScaledBitmap(logo, 160, 160, true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setAlpha(15);
        c.drawBitmap(logo, 20, 20, paint);
    }

    private static void drawCircle(Context context, RectF rectF, Canvas c) {
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(android.R.color.black, context.getTheme()));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(5);
        c.drawCircle(rectF.right / 2, rectF.bottom / 2, 90, paint);

        paint.setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAlpha(10);
        c.drawCircle(rectF.right / 2, rectF.bottom / 2, 90, paint);
    }

    private static void drawTextBG(Context context, Canvas c) {
        Paint paint = new Paint();
        Bitmap textFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.frame);
        textFrame = Bitmap.createScaledBitmap(textFrame, 320, 74, true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        c.drawBitmap(textFrame, 0, 0, paint);
    }

    private static void initTextPaint(Context context, Paint paint, boolean isCode, boolean isId) {
        paint.setTextSize(isId ? 28 : 36);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(
                context.getResources()
                        .getColor(isCode ? R.color.overlayTextRed :
                                R.color.overlayText, context.getTheme()));
    }

    private static void drawText(Context context, Canvas overlayCanvas,
                                 String text, boolean isCode, boolean isId) {
        Paint paint = new Paint();
        initTextPaint(context, paint, isCode, isId);

        drawTextBG(context, overlayCanvas);

        overlayCanvas.drawText(text, 160 - (paint.measureText(text) / 2), isId ? 46 : 49, paint);
    }

    @NonNull
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut;
            if (result != null) {
                cut = result.lastIndexOf(File.separator);
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        if (result == null) {
            result = new Random().nextInt() + ".jpg";
        }
        return result;
    }

}
