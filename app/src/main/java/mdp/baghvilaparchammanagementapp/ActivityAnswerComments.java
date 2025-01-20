package mdp.baghvilaparchammanagementapp;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mdp.baghvilaparchammanagementapp.databinding.ActivityAnswerCommentsBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogAnswerCommentBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogConfirmBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemAnswerCommentBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityAnswerComments extends AppCompatActivity{
    
    ActivityAnswerCommentsBinding binding;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        binding = ActivityAnswerCommentsBinding.inflate(getLayoutInflater());
        
        setContentView(binding.getRoot());
        
        binding.etlSearchAnswerComment.setVisibility(View.INVISIBLE);
        
        try{
            getComments();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void getComments() throws Exception{
        
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
                                
                                ArrayList<ItemNewComment> itemNewComments =
                                        ItemNewComment.getListFromStringJson(responseStr);
                                
                                setAdapter(itemNewComments);
                                
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
                App.toast(ActivityAnswerComments.this,
                          getResources().getString(R.string.error_happened));
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url+"get_comments.php",
                "getComments", handler);
        
    }
    
    private void toggleMainViews(boolean show){
        if(show){
            binding.rvAnswerComments.setVisibility(View.VISIBLE);
            binding.pbAnswerComments.setVisibility(View.INVISIBLE);
        }else{
            binding.rvAnswerComments.setVisibility(View.INVISIBLE);
            binding.pbAnswerComments.setVisibility(View.VISIBLE);
        }
    }
    
    private void setAdapter(ArrayList<ItemNewComment> itemNewComments){
        binding.rvAnswerComments.setLayoutManager(new LinearLayoutManager(this));
        
        RVAdapter adapter = new RVAdapter(itemNewComments);
        
        binding.rvAnswerComments.setAdapter(adapter);
        
        binding.etlSearchAnswerComment.setVisibility(View.VISIBLE);
        binding.etSearchAnswerComment.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after){
            
            }
        
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count){
            
            }
        
            @Override
            public void afterTextChanged(Editable s){
                adapter.getFilter().filter(s);
            }
        });
    }
    
    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.VH> implements Filterable{
        ItemFilter filter = new ItemFilter();
    
        @Override
        public Filter getFilter(){
            return filter;
        }
        
        ArrayList<ItemNewComment> itemComments, filteredComments;
        
        public RVAdapter(ArrayList<ItemNewComment> itemComments){
            this.itemComments = itemComments;
            this.filteredComments = itemComments;
        }
    
    
        class VH extends RecyclerView.ViewHolder{
            
            ListItemAnswerCommentBinding binding;
            
            public VH(@NonNull ListItemAnswerCommentBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        @NonNull
        @Override
        public RVAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ListItemAnswerCommentBinding binding = ListItemAnswerCommentBinding.inflate(
                    ActivityAnswerComments.this.getLayoutInflater(), parent, false);
            
            return new RVAdapter.VH(binding);
        }
        
        
        @Override
        public void onBindViewHolder(@NonNull RVAdapter.VH holder, int position){
            
            if(getItemCount()>0){
                holder.binding.tvAnswerCommentUsernameItem.setText(
                        filteredComments.get(position).getUsername());
                
                holder.binding.tvAnswerCommentTextItem.setText(
                        filteredComments
                                .get(position).getText().replaceAll("<br/>","\n"));
    
                holder.binding.btnAnswerComment.setOnClickListener(v->{
        
                    Dialog dialog = new Dialog(ActivityAnswerComments.this);
        
                    DialogAnswerCommentBinding binding = DialogAnswerCommentBinding.inflate(
                            ActivityAnswerComments.this.getLayoutInflater());
        
                    dialog.setContentView(binding.getRoot());
        
                    binding.btnDialogSendAnswerComment.setOnClickListener(v1->{
                        String text = binding.etDialogAnswerCommentText.getText().toString();
            
                        if(!text.trim().isEmpty()){
                            binding.btnDialogSendAnswerComment.setVisibility(View.INVISIBLE);
                            binding.pbDialogAnswerComment.setVisibility(View.VISIBLE);
                            try{
                                sendAnswer(text, filteredComments.get(position).getUsername(),
                                           filteredComments.get(position).getPostId(),
                                           filteredComments.get(position).getCommentEmail(),
                                           dialog,
                                           binding);
                            }catch(JSONException e){
                                e.printStackTrace();
                                App.toast(ActivityAnswerComments.this,
                                          getResources().getString(R.string.error_happened));
                    
                                binding.btnDialogSendAnswerComment.setVisibility(View.VISIBLE);
                                binding.pbDialogAnswerComment.setVisibility(View.INVISIBLE);
                            }
                        }
            
                    });
        
                    dialog.show();
        
                });
    
                holder.binding.btnRemoveComment.setOnClickListener(v->{
        
                    Dialog dialog = new Dialog(ActivityAnswerComments.this);
        
                    DialogConfirmBinding binding = DialogConfirmBinding.inflate(
                            ActivityAnswerComments.this.getLayoutInflater());
        
                    dialog.setContentView(binding.getRoot());
        
                    binding.tvDialogTitle.setText(R.string.irriversible_action);
        
                    binding.btnDialogAccept.setOnClickListener(v1->{
            
                        try{
                            removeComment(
                                    filteredComments.get(position).getId(), binding, dialog,
                                    filteredComments.get(position).getPostId());
                        }catch(JSONException e){
                            e.printStackTrace();
                            App.toast(ActivityAnswerComments.this,
                                      getResources().getString(R.string.error_happened));
                            dialog.cancel();
                        }
            
                    });
        
                    binding.btnDialogDeny.setOnClickListener(v1->{
                        dialog.cancel();
                    });
        
                    dialog.show();
        
                });
            }
            
            
            
        }
        
        private void removeComment(long id, DialogConfirmBinding binding,
                                   Dialog dialog, long postId) throws JSONException{
            
            binding.pbDialogConfirm.setVisibility(View.VISIBLE);
            binding.btnDialogDeny.setVisibility(View.INVISIBLE);
            binding.btnDialogAccept.setVisibility(View.INVISIBLE);
            
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            requestParameters.put("id", id);
            requestParameters.put("post_id", postId);
            
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
                                            dialog.cancel();
                                            
                                            try{
                                                getComments();
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
                                binding.btnDialogDeny.setVisibility(View.VISIBLE);
                                binding.btnDialogAccept.setVisibility(View.VISIBLE);
                                binding.pbDialogConfirm.setVisibility(View.INVISIBLE);
                                App.toast(ActivityAnswerComments.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "remove_comment.php",
                    "removeComment", handler);
        }
        
        
        private void sendAnswer(String text, String username, long postId, String email,
                                Dialog dialog,
                                DialogAnswerCommentBinding binding) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", App.HASH);
            requestParameters.put("post_id", postId);
            requestParameters.put("email", email);
            requestParameters.put("answer", "@" + username + "<br/>"
                                            + text.replaceAll("\n", "<br/>"));
            
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
                                            dialog.cancel();
                                            
                                            try{
                                                getComments();
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
                                binding.btnDialogSendAnswerComment.setVisibility(View.VISIBLE);
                                binding.pbDialogAnswerComment.setVisibility(View.INVISIBLE);
                                App.toast(ActivityAnswerComments.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "answer_comment.php",
                    "sendAnswer", handler);
        }
        
        
        @Override
        public int getItemCount(){
            return filteredComments.size();
        }
    
        class ItemFilter extends Filter{
        
        
            @Override
            protected FilterResults performFiltering(CharSequence constraint){
            
                String filterString = constraint.toString();
            
                filteredComments = new ArrayList<>();
                
                if(filterString.trim().isEmpty()){
                    filteredComments = itemComments;
                    return null;
                }
            
                for(int i = 0; i < itemComments.size(); i++){
                
                    if(String.valueOf(itemComments.get(i).getPostId()).equalsIgnoreCase(filterString)){
                        filteredComments.add(itemComments.get(i));
                    }
                }
            
            
                return null;
            }
        
        
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){
                notifyDataSetChanged();
            }
        }
    }
    
    
}
