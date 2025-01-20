package mdp.baghvilaparchammanagementapp;

import android.app.Dialog;
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
import mdp.baghvilaparchammanagementapp.databinding.ActivityBlogManagementBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogConfirmBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemBlogBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityBlogManagement extends AppCompatActivity{
    
    ActivityBlogManagementBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    
        binding = ActivityBlogManagementBinding.inflate(getLayoutInflater());
        
        setContentView(binding.getRoot());
    
        toggleMainViews(false);
    
        try{
            getBlogs();
        }catch(Exception e){
            e.printStackTrace();
            toggleMainViews(true);
            App.toast(this, getResources().getString(R.string.error_happened));
        }
    }
    
    private void getBlogs() throws JSONException{
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
                            
                                ArrayList<ItemBlog> itemBlogs =
                                        ItemBlog.getListFromStringJson(responseStr);
                            
                                setAdapter(itemBlogs);
                            
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
                toggleMainViews(true);
                App.toast(ActivityBlogManagement.this,
                          getResources().getString(R.string.error_happened));
            }
        };
    
        Tools.httpPost(
                requestParameters,
                App.url + "get_blogs.php",
                "getBlogs", handler);
    }
    
    private void setAdapter(ArrayList<ItemBlog> itemBlogs){
        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBlogs.setAdapter(new ActivityBlogManagement.RVAdapter(itemBlogs));
    }
    
    private void toggleMainViews(boolean on){
        if(on){
            binding.pbActivityBlogManagement.setVisibility(View.INVISIBLE);
            binding.rvBlogs.setVisibility(View.VISIBLE);
        }else{
            binding.rvBlogs.setVisibility(View.INVISIBLE);
            binding.pbActivityBlogManagement.setVisibility(View.VISIBLE);
        }
    }
    
    private class RVAdapter extends RecyclerView.Adapter<ActivityBlogManagement.RVAdapter.VH>{
        
        
        ArrayList<ItemBlog> itemBlogs;
        @NonNull
        ArrayList<Boolean> positionLoadingStatus;
        
        public RVAdapter(ArrayList<ItemBlog> itemBlogs){
            this.itemBlogs = itemBlogs;
            this.positionLoadingStatus = new ArrayList<>();
            for(int i = 0; i < this.itemBlogs.size(); i++){
                this.positionLoadingStatus.add(false);
            }
        }
        
        class VH extends RecyclerView.ViewHolder{
            
            ListItemBlogBinding binding;
            
            public VH(@NonNull ListItemBlogBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        @NonNull
        @Override
        public ActivityBlogManagement.RVAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent,
                                                                         int viewType){
            ListItemBlogBinding binding = ListItemBlogBinding.inflate(
                    ActivityBlogManagement.this.getLayoutInflater(), parent, false);
            
            return new ActivityBlogManagement.RVAdapter.VH(binding);
        }
        
        
        @Override
        public void onBindViewHolder(@NonNull ActivityBlogManagement.RVAdapter.VH holder,
                                     int position){
            
            toggleListItemViews(holder, positionLoadingStatus.get(position));
            
            holder.binding.tvBlogTitleItem.setText(
                    itemBlogs.get(position).getTitle());
            
            holder.binding.tvBlogShortTextItem.setText(itemBlogs.get(position).getShortText());
            
            holder.binding.btnRemoveBlog.setOnClickListener(v->{
    
                Dialog dialog = new Dialog(ActivityBlogManagement.this);
                DialogConfirmBinding confirmBinding =
                        DialogConfirmBinding.inflate(getLayoutInflater());
                dialog.setContentView(confirmBinding.getRoot());
    
                confirmBinding.tvDialogTitle.setText(R.string.irriversible_action);
                confirmBinding.btnDialogDeny.setOnClickListener(v1 -> dialog.cancel());
                confirmBinding.btnDialogAccept.setOnClickListener(v1 -> {
                    setStatusLoading(position, true);
                    try{
                        removeBlog(position);
                    }catch(JSONException e){
                        e.printStackTrace();
                        App.toast(ActivityBlogManagement.this,
                                  getResources().getString(R.string.error_happened));
                        setStatusLoading(position, false);
                    }
                    dialog.cancel();
                });
                
                dialog.show();
                
                
            });
            
        }
        
        private void removeBlog(int position) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            requestParameters.put("id", itemBlogs.get(position).getId());
            
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
                                        if(responseStr.contains("success")){
                                            toggleMainViews(false);
                                            try{
                                                getBlogs();
                                            }catch(Exception e){
                                                e.printStackTrace();
                                                OnFailure();
                                            }
                                        }else{
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
                                setStatusLoading(position, false);
                                App.toast(ActivityBlogManagement.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "remove_blog.php",
                    "removeBlog", handler);
        }
        
        private void setStatusLoading(int position, boolean isLoading){
            positionLoadingStatus.set(position, isLoading);
            notifyDataSetChanged();
        }
        
        private void toggleListItemViews(ActivityBlogManagement.RVAdapter.VH holder,
                                         boolean isLoading){
            if(isLoading){
                holder.binding.pbBlogItem.setVisibility(View.VISIBLE);
                holder.binding.btnRemoveBlog.setVisibility(View.INVISIBLE);
                holder.binding.tvBlogTitleItem.setVisibility(View.INVISIBLE);
                holder.binding.tvBlogShortTextItem.setVisibility(View.INVISIBLE);
            }else{
                holder.binding.pbBlogItem.setVisibility(View.INVISIBLE);
                holder.binding.btnRemoveBlog.setVisibility(View.VISIBLE);
                holder.binding.tvBlogTitleItem.setVisibility(View.VISIBLE);
                holder.binding.tvBlogShortTextItem.setVisibility(View.VISIBLE);
            }
        }
        
        
        @Override
        public int getItemCount(){
            return itemBlogs.size();
        }
    }
}
