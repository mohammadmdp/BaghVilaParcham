package mdp.baghvilaparchammanagementapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mdp.baghvilaparchammanagementapp.databinding.ActivityEditTagsBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogAnswerCommentBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemTagBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityEditTags extends AppCompatActivity{
    
    ActivityEditTagsBinding binding;
    ArrayList<ItemTag> itemTags;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityEditTagsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP|ItemTouchHelper.DOWN, 0){
        
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target){
            
                int from = viewHolder.getAbsoluteAdapterPosition();
                int to   = target.getAbsoluteAdapterPosition();
            
                Collections.swap(itemTags, from, to);
    
                //noinspection ConstantConditions
                binding.rvEditTags.getAdapter().notifyItemMoved(from, to);
            
                return true;
            }
        
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
            
            }
        };
        
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(binding.rvEditTags);
        
        
        try{
            getTags();
        }catch(Exception e){
            e.printStackTrace();
            App.log(getResources().getString(R.string.error_happened));
        }
        
        binding.btnApplyTagChanges.setOnClickListener(v -> {
    
            try{
                updateTags();
            }catch(JSONException e){
                e.printStackTrace();
                App.log(getResources().getString(R.string.error_happened));
            }
    
        });
        
        binding.btnAddTag.setOnClickListener(v -> {
            addTag();
        });
        
        
        
    }
    
    private void addTag(){
        Dialog dialog = new Dialog(this);
        DialogAnswerCommentBinding dBinding =
                DialogAnswerCommentBinding.inflate(getLayoutInflater());
        
        dialog.setContentView(dBinding.getRoot());
        dBinding.tvDialogLabelAnswerComment.setText(getResources().getText(R.string.tag_name));
        dBinding.btnDialogSendAnswerComment.setOnClickListener(v -> {
            dBinding.pbDialogAnswerComment.setVisibility(View.VISIBLE);
            dBinding.btnDialogSendAnswerComment.setVisibility(View.INVISIBLE);
    
    
            try{
                JSONObject requestParameters = new JSONObject();
                requestParameters.put("hash", App.HASH);
                requestParameters.put(
                        "tag",
                        dBinding.etDialogAnswerCommentText.getText().toString().trim());
                requestParameters.put("tag_id", itemTags.size());
    
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
                                                
                                                if(!responseStr.contains("success")){
                                                    OnFailure();
                                                }
                                                
                                                dialog.cancel();
                                                
                                                getTags();
                                    
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
                                    dBinding.pbDialogAnswerComment.setVisibility(View.INVISIBLE);
                                    dBinding.btnDialogSendAnswerComment.setVisibility(View.VISIBLE);
                                    App.toast(ActivityEditTags.this,
                                              getResources().getString(R.string.error_happened));
                                });
                    
                            }
                        };
    
                Tools.httpPost(
                        requestParameters,
                        App.url + "add_tag.php",
                        "addTag", handler);
            }catch(JSONException e){
                e.printStackTrace();
                dBinding.pbDialogAnswerComment.setVisibility(View.INVISIBLE);
                dBinding.btnDialogSendAnswerComment.setVisibility(View.VISIBLE);
                App.log(getResources().getString(R.string.error_happened));
                
            }
    
        });
        dialog.show();
    }
    
    private void updateTags() throws JSONException{
        toggleMainViews(false);
    
        JSONObject requestParameters = new JSONObject();
    
        requestParameters.put("hash", App.HASH);
    
        JSONArray array = new JSONArray();
    
        for(int i = 0; i < itemTags.size(); i++){
            JSONObject object = new JSONObject();
            object.put("id", i+1);
            object.put("name", itemTags.get(i).getName());
            array.put(object);
        }
    
        
        requestParameters.put("tags", array.toString());
    
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
                                if(!responseStr.contains("success")){
                                    OnFailure();
                                }
                                
                                getTags();
                            
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
                    App.toast(ActivityEditTags.this,
                              getResources().getString(R.string.error_happened));
                });
            
            }
        };
    
        Tools.httpPost(
                requestParameters,
                App.url + "update_tags.php",
                "updateTags", handler);
    }
    
    private void getTags() throws Exception{
        
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
                                
                                itemTags = ItemTag.getListFromStringJson(responseStr);
                                
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
                runOnUiThread(() ->{
                    toggleMainViews(true);
                    App.toast(ActivityEditTags.this,
                              getResources().getString(R.string.error_happened));
                });
                
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url + "get_tags.php",
                "getTags", handler);
        
    }
    
    private void setAdapter(){
    
        binding.rvEditTags.setHasFixedSize(true);
        binding.rvEditTags.setAdapter(new TagAdapter());
        binding.rvEditTags.setLayoutManager(new LinearLayoutManager(this));
    
        
    }
    
    void toggleMainViews(boolean on){
        if(on){
            binding.rvEditTags.setVisibility(View.VISIBLE);
            binding.btnAddTag.setVisibility(View.VISIBLE);
            binding.btnApplyTagChanges.setVisibility(View.VISIBLE);
            binding.pbEditTags.setVisibility(View.INVISIBLE);
        }else{
            binding.rvEditTags.setVisibility(View.INVISIBLE);
            binding.btnAddTag.setVisibility(View.INVISIBLE);
            binding.btnApplyTagChanges.setVisibility(View.INVISIBLE);
            binding.pbEditTags.setVisibility(View.VISIBLE);
        }
    }
    
    class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder>{
    
        class TagViewHolder extends RecyclerView.ViewHolder{
            
            ListItemTagBinding binding;
    
            public TagViewHolder(ListItemTagBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    
        @NonNull
        @Override
        public TagAdapter.TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            return new TagViewHolder(
                    ListItemTagBinding.inflate(getLayoutInflater(),parent,false));
        }
    
        @Override
        public void onBindViewHolder(@NonNull TagAdapter.TagViewHolder holder, int position){
            holder.binding.tvItemTagName.setText(itemTags.get(position).getName());
    
            holder.binding.btnRemoveTag.setOnClickListener(v -> {
                try{
                    removeTag(position+1);
                }catch(JSONException e){
                    e.printStackTrace();
                    App.toast(ActivityEditTags.this,
                              getResources().getString(R.string.error_happened));
                }
            });
        }
    
        @Override
        public int getItemCount(){
            return itemTags.size();
        }
    }
    
    private void removeTag(int id) throws JSONException{
        
        toggleMainViews(false);
        
        JSONObject requestParameters = new JSONObject();
        requestParameters.put("hash", App.HASH);
        requestParameters.put("tag_id", id);
    
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
                                        
                                        if(!responseStr.contains("success")){
                                            OnFailure();
                                            
                                        }
                                    
                                        getTags();
                                    
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
                            App.toast(ActivityEditTags.this,
                                      getResources().getString(R.string.error_happened));
                        });
                    
                    }
                };
    
        Tools.httpPost(
                requestParameters,
                App.url + "remove_tag.php",
                "removeTag", handler);
    }
    
}
