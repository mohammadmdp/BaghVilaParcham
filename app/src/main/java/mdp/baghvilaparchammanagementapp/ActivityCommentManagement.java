package mdp.baghvilaparchammanagementapp;

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
import mdp.baghvilaparchammanagementapp.databinding.ActivityCommentManagementBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemNewCommentBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityCommentManagement extends AppCompatActivity{
    
    ActivityCommentManagementBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityCommentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        toggleMainViews(false);
        
        try{
            getNewComments();
        }catch(Exception e){
            e.printStackTrace();
            toggleMainViews(true);
            App.toast(this, getResources().getString(R.string.error_happened));
        }
        
        binding.btnApproveAllComments.setOnClickListener(v->{
            try{
                approveAllComments();
            }catch(JSONException e){
                e.printStackTrace();
                App.toast(ActivityCommentManagement.this,
                          getResources().getString(R.string.error_happened));
            }
        });
        
    }
    
    private void approveAllComments() throws JSONException{
        toggleMainViews(false);
        JSONObject requestParameters = new JSONObject();
        
        requestParameters.put("hash", App.HASH);
        
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
                                        
                                        try{
                                            getNewComments();
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
                        toggleMainViews(true);
                        runOnUiThread(()->App.toast(ActivityCommentManagement.this,
                                                    getResources()
                                                            .getString(R.string.error_happened)));
                    }
                };
        
        Tools.httpPost(
                requestParameters,
                App.url + "approve_all_comments.php",
                "approveAllComments", handler);
    }
    
    private void getNewComments() throws Exception{
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
                                
                                ArrayList<ItemNewComment> itemNewComments =
                                        ItemNewComment.getListFromStringJson(responseStr);
                                
                                setAdapter(itemNewComments);
                                
                                if(itemNewComments.size() < 1){
                                    binding.btnApproveAllComments.setVisibility(View.INVISIBLE);
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
                toggleMainViews(true);
                App.toast(ActivityCommentManagement.this,
                          getResources().getString(R.string.error_happened));
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url + "get_new_comments.php",
                "getNewComments", handler);
        
    }
    
    private void setAdapter(ArrayList<ItemNewComment> itemNewComments){
        binding.rvNewComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNewComments.setAdapter(new RVAdapter(itemNewComments));
    }
    
    void toggleMainViews(boolean on){
        if(on){
            binding.btnApproveAllComments.setVisibility(View.VISIBLE);
            binding.rvNewComments.setVisibility(View.VISIBLE);
            binding.pbActivityCommentManagement.setVisibility(View.INVISIBLE);
        }else{
            binding.btnApproveAllComments.setVisibility(View.INVISIBLE);
            binding.rvNewComments.setVisibility(View.INVISIBLE);
            binding.pbActivityCommentManagement.setVisibility(View.VISIBLE);
        }
    }
    
    
    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.VH>{
        
        
        ArrayList<ItemNewComment> itemNewComments;
        @NonNull
        ArrayList<Boolean> positionLoadingStatus;
        
        public RVAdapter(ArrayList<ItemNewComment> itemNewComments){
            this.itemNewComments = itemNewComments;
            this.positionLoadingStatus = new ArrayList<>();
            for(int i = 0; i < this.itemNewComments.size(); i++){
                this.positionLoadingStatus.add(false);
            }
        }
        
        class VH extends RecyclerView.ViewHolder{
            
            ListItemNewCommentBinding binding;
            
            public VH(@NonNull ListItemNewCommentBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ListItemNewCommentBinding binding = ListItemNewCommentBinding.inflate(
                    ActivityCommentManagement.this.getLayoutInflater(), parent, false);
            
            return new VH(binding);
        }
        
        
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position){
            
            toggleListItemViews(holder, positionLoadingStatus.get(position));
            
            holder.binding.tvNewCommentUsernameItem.setText(
                    itemNewComments.get(position).getUsername());
            
            holder.binding.tvNewCommentTextItem.setText(itemNewComments.get(position).getText());
            
            holder.binding.btnApproveComment.setOnClickListener(v->{
                setStatusLoading(position, true);
                try{
                    approveComment(position);
                }catch(JSONException e){
                    e.printStackTrace();
                    App.toast(ActivityCommentManagement.this,
                              getResources().getString(R.string.error_happened));
                    setStatusLoading(position, false);
                }
            });
            
            holder.binding.btnDenyComment.setOnClickListener(v->{
                setStatusLoading(position, true);
                try{
                    denyComment(position);
                }catch(JSONException e){
                    e.printStackTrace();
                    App.toast(ActivityCommentManagement.this,
                              getResources().getString(R.string.error_happened));
                    setStatusLoading(position, false);
                }
            });
        }
        
        private void denyComment(int position) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            requestParameters.put("id", itemNewComments.get(position).getId());
            
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
                                                getNewComments();
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
                                App.toast(ActivityCommentManagement.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "deny_comment.php",
                    "denyComment", handler);
        }
        
        private void setStatusLoading(int position, boolean isLoading){
            positionLoadingStatus.set(position, isLoading);
            notifyDataSetChanged();
        }
        
        private void approveComment(int position) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            requestParameters.put("id", itemNewComments.get(position).getId());
            requestParameters.put("post_id", itemNewComments.get(position).getPostId());
            
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
                                                getNewComments();
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
                                App.toast(ActivityCommentManagement.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "approve_comment.php",
                    "approveComment", handler);
        }
        
        private void toggleListItemViews(VH holder, boolean isLoading){
            if(isLoading){
                holder.binding.pbNewCommentItem.setVisibility(View.VISIBLE);
                holder.binding.btnApproveComment.setVisibility(View.INVISIBLE);
                holder.binding.btnDenyComment.setVisibility(View.INVISIBLE);
                holder.binding.tvNewCommentTextItem.setVisibility(View.INVISIBLE);
                holder.binding.tvNewCommentUsernameItem.setVisibility(View.INVISIBLE);
            }else{
                holder.binding.pbNewCommentItem.setVisibility(View.INVISIBLE);
                holder.binding.btnApproveComment.setVisibility(View.VISIBLE);
                holder.binding.btnDenyComment.setVisibility(View.VISIBLE);
                holder.binding.tvNewCommentTextItem.setVisibility(View.VISIBLE);
                holder.binding.tvNewCommentUsernameItem.setVisibility(View.VISIBLE);
            }
        }
        
        
        @Override
        public int getItemCount(){
            return itemNewComments.size();
        }
    }
}
