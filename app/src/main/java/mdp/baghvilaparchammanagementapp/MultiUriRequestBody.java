package mdp.baghvilaparchammanagementapp;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

class MultiUriRequestBody extends RequestBody{
    
    private static final int             SEGMENT_SIZE = 2048;
    private final        MediaType       contentType;
    private final        ContentResolver contentResolver;
    private final        Uri             uri;
    
    private final ProgressListener listener;
    
    public MultiUriRequestBody(MediaType contentType, ContentResolver contentResolver, Uri uri
            , @Nullable ProgressListener listener){
        if(uri == null){
            throw new NullPointerException("uri == null");
        }
        this.contentType = contentType;
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.listener = listener;
    }
    
    @Override
    public long contentLength() throws IOException{
        
        return contentResolver.openFileDescriptor(uri, "r").getStatSize();
    }
    
    @Nullable
    @Override
    public MediaType contentType(){
        return contentType;
    }
    
    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException{
        try(Source source = Okio.source(contentResolver.openInputStream(uri))){
            long total = 0;
            long read;
            
            while((read = source.read(sink.getBuffer(), SEGMENT_SIZE)) != -1){
                total += read;
                sink.flush();
                if(listener != null){
                    listener.transferred(total, contentLength());
                }
                
            }
        }
    }
    
    public interface ProgressListener{
        
        void transferred(long written, long total);
    }
}
