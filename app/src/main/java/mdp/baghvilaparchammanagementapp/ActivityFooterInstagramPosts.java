package mdp.baghvilaparchammanagementapp;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mdp.baghvilaparchammanagementapp.databinding.ActivityFooterInstagramPostsBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogEditInstagramPostBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemInstagramPostBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityFooterInstagramPosts extends AppCompatActivity{
    
    ArrayList<ItemInstagramPost>        itemInstagramPosts;
    ActivityFooterInstagramPostsBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityFooterInstagramPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        try{
            getInstagramPosts();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void setAdapter(){
        
        binding.rvInstagramPosts.setHasFixedSize(true);
        binding.rvInstagramPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInstagramPosts.setAdapter(new InstagramPostAdapter());
        
        
    }
    
    void toggleMainViews(boolean on){
        if(on){
            binding.rvInstagramPosts.setVisibility(View.VISIBLE);
            binding.pbInstagramPosts.setVisibility(View.INVISIBLE);
        }else{
            binding.rvInstagramPosts.setVisibility(View.INVISIBLE);
            binding.pbInstagramPosts.setVisibility(View.VISIBLE);
        }
    }
    
    private void getInstagramPosts() throws Exception{
        
        toggleMainViews(false);
        
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
                                toggleMainViews(true);
                                
                                itemInstagramPosts =
                                        ItemInstagramPost.getListFromStringJson(responseStr);
                                
                                setAdapter();
                                
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
                runOnUiThread(()->{
                    toggleMainViews(true);
                    App.toast(ActivityFooterInstagramPosts.this,
                              getResources().getString(R.string.error_happened));
                });
                
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url + "get_instagram_posts.php",
                "getInstagramPosts", handler);
        
    }
    
    private void editInstagramPost(int pos) throws JSONException{
        
        Dialog dialog = new Dialog(ActivityFooterInstagramPosts.this);
        DialogEditInstagramPostBinding dialogBinding =
                DialogEditInstagramPostBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        
        
        dialogBinding.btnDialogSendInstagramPostEmbedCode.setOnClickListener(v->{
            String embedCode =
                    dialogBinding.etDialogInstagramPostEmbedCode.getText().toString().trim();
            
            dialogBinding.btnDialogSendInstagramPostEmbedCode.setVisibility(View.INVISIBLE);
            dialogBinding.btnDialogEditInstagramPostPasteText.setVisibility(View.INVISIBLE);
            dialogBinding.btnDialogEditInstagramPostClearText.setVisibility(View.INVISIBLE);
            dialogBinding.pbDialogInstagramPost.setVisibility(View.VISIBLE);
            
            JSONObject requestParameters = new JSONObject();
            
            try{
                requestParameters.put("hash", App.HASH);
                requestParameters.put("id", pos + 1);
                requestParameters.put("embed_code", embedCode);
            }catch(JSONException e){
                e.printStackTrace();
            }
            
            
            Tools.IHttpPostResultHandler handler =
                    new Tools.IHttpPostResultHandler(){
                        @Override
                        public void OnResponse(Response response){
                            try{
                                
                                
                                ResponseBody body = response.body();
                                if(body != null){
                                    String responseStr = body.string();
                                    App.log("response: " + responseStr);
                                    
                                    
                                    runOnUiThread(()->{
                                        try{
                                            JSONObject object = new JSONObject(responseStr);
                                            if(!object.getString("message")
                                                        .contains("success")){
                                                OnFailure();
                                            }else{
                                                dialog.cancel();
                                                getInstagramPosts();
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
                            runOnUiThread(()->{
                                dialogBinding.pbDialogInstagramPost.setVisibility(View.INVISIBLE);
                                dialogBinding.btnDialogSendInstagramPostEmbedCode
                                        .setVisibility(View.VISIBLE);
                                dialogBinding.btnDialogEditInstagramPostPasteText
                                        .setVisibility(View.VISIBLE);
                                dialogBinding.btnDialogEditInstagramPostClearText
                                        .setVisibility(View.VISIBLE);
                                App.toast(ActivityFooterInstagramPosts.this,
                                          getResources().getString(R.string.error_happened));
                            });
                            
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "edit_instagram_post.php",
                    "editInstagramPost", handler);
        });
        
        
        dialogBinding.etDialogInstagramPostEmbedCode.setFocusable(true);
        dialogBinding.etDialogInstagramPostEmbedCode.setFocusedByDefault(true);
        
        dialog.show();
        
        dialogBinding.etDialogInstagramPostEmbedCode.setText(
                itemInstagramPosts.get(pos).getEmbedCode());
        
        dialogBinding.btnDialogEditInstagramPostClearText.setOnClickListener(
                v->dialogBinding.etDialogInstagramPostEmbedCode.setText(""));
        
        dialogBinding.btnDialogEditInstagramPostPasteText.setOnClickListener(v->{
            dialogBinding.etDialogInstagramPostEmbedCode.requestFocus();
            
            ClipboardManager clipboardManager =
                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if(clipboardManager != null){
                ClipData clipData = clipboardManager.getPrimaryClip();
                if(clipData != null){
                    ClipData.Item item = clipData.getItemAt(0);
                    if(item != null){
                        String clipText = item.getText().toString();
                        dialogBinding.etDialogInstagramPostEmbedCode.setText(clipText);
                    }
                }
            }
        });
    }
    
    class InstagramPostAdapter extends
            RecyclerView.Adapter<InstagramPostAdapter.InstagramPostViewHolder>{
        
        class InstagramPostViewHolder extends RecyclerView.ViewHolder{
            
            ListItemInstagramPostBinding binding;
            
            public InstagramPostViewHolder(ListItemInstagramPostBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        @NonNull
        @Override
        public InstagramPostAdapter.InstagramPostViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType){
            return new InstagramPostAdapter.InstagramPostViewHolder(
                    ListItemInstagramPostBinding.inflate(getLayoutInflater(), parent, false));
        }
        
        @Override
        public void onBindViewHolder(@NonNull InstagramPostAdapter.InstagramPostViewHolder holder,
                                     int position){
            holder.binding.tvInstagramPostItem.setText(
                    itemInstagramPosts.get(position).getEmbedCode());
            holder.binding.tvInstagramPostItemNumber.setText(String.valueOf(position + 1));
            
            holder.binding.btnEditInstagramPost.setOnClickListener(v->{
                try{
                    
                    
                    editInstagramPost(position);
                }catch(JSONException e){
                    e.printStackTrace();
                    App.toast(ActivityFooterInstagramPosts.this,
                              getResources().getString(R.string.error_happened));
                }
            });
        }
        
        
        @Override
        public int getItemCount(){
            return itemInstagramPosts.size();
        }
    }
}
