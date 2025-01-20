package mdp.baghvilaparchammanagementapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

class UriRequestBody extends RequestBody{
    
    private static final int             SEGMENT_SIZE = 2048;
    private final        MediaType       contentType;
    private final        ContentResolver contentResolver;
    private final        Uri             uri;
    
    private final ProgressListener listener;
    
    public UriRequestBody(MediaType contentType, ContentResolver contentResolver, Uri uri
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
        long size;
        try (ParcelFileDescriptor pFd = contentResolver.openFileDescriptor(uri, "r")) {

            size = pFd.getStatSize();

//            pFd.detachFd();
        }

        return size;
    }
    
    @Nullable
    @Override
    public MediaType contentType(){
        return contentType;
    }
    
    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException{
        try(Source source = Okio.source(contentResolver.openInputStream(uri))){
            long read;

            while((read = source.read(sink.getBuffer(), SEGMENT_SIZE)) != -1){
                sink.flush();
                if(listener != null){
                    listener.transferred(read);
                }
                
            }
            sink.close();
        }
    }
    
    public interface ProgressListener{
        
        void transferred(long currentWrittenBytes);
    }
    
}
