package mdp.baghvilaparchammanagementapp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImageTranscoderType;
import com.facebook.imagepipeline.core.MemoryChunkType;

import java.io.File;

import androidx.annotation.Nullable;

public class App extends Application{
    
    public static String            url = "https://baghvilaparcham.com:443/";
    public static final String FOLDER = "BaghVilaParcham";
    public static final String PIC_DIR = Environment.DIRECTORY_DOCUMENTS + File.separator + FOLDER;
    
    public static String HASH = "";
    public static String DEVICE_TITLE = "";
    
    @Override
    public void onCreate(){
        super.onCreate();
        
        Fresco.initialize(this,
                          ImagePipelineConfig.newBuilder(this)
                                  .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                                  .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                                  .experiment().setNativeCodeDisabled(true)
                                  .build());
        
    
    }
    
    public static void log(@Nullable String log){
        Log.d("BVP", log == null ? "null" : log);
    }
    
    public static void toast(Context ctx, String text){
        
        ctx.getMainExecutor().execute(() ->Toast.makeText(ctx, text, Toast.LENGTH_LONG).show());
    }
}
