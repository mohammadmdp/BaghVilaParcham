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
import mdp.baghvilaparchammanagementapp.databinding.ActivityViewDevicesBinding;
import mdp.baghvilaparchammanagementapp.databinding.DialogConfirmBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemDeviceBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityViewDevices extends AppCompatActivity{
    
    ActivityViewDevicesBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityViewDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        try{
            getDevices();
        }catch(JSONException e){
            e.printStackTrace();
            App.toast(ActivityViewDevices.this,
                      getResources().getString(R.string.error_happened));
            finish();
        }
    }
    
    void toggleMainViews(boolean on){
        if(on){
            binding.rvDevices.setVisibility(View.VISIBLE);
            binding.pbActivityViewDevices.setVisibility(View.INVISIBLE);
        }else{
            binding.rvDevices.setVisibility(View.INVISIBLE);
            binding.pbActivityViewDevices.setVisibility(View.VISIBLE);
        }
    }
    
    private void getDevices() throws JSONException{
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
                                
                                ArrayList<ItemDevice> itemDevices =
                                        ItemDevice.getListFromStringJson(responseStr);
                                
                                setAdapter(itemDevices);
                                
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
                App.toast(ActivityViewDevices.this,
                          getResources().getString(R.string.error_happened));
            }
        };
        
        Tools.httpPost(
                requestParameters,
                App.url + "get_devices.php",
                "getDevices", handler);
    }
    
    private void setAdapter(ArrayList<ItemDevice> itemDevices){
        binding.rvDevices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDevices.setAdapter(new RVAdapter(itemDevices));
    }
    
    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.VH>{
        
        
        ArrayList<ItemDevice> itemDevices;
        @NonNull
        ArrayList<Boolean> positionLoadingStatus;
        
        public RVAdapter(ArrayList<ItemDevice> itemDevices){
            this.itemDevices = itemDevices;
            this.positionLoadingStatus = new ArrayList<>();
            for(int i = 0; i < this.itemDevices.size(); i++){
                this.positionLoadingStatus.add(false);
            }
        }
        
        class VH extends RecyclerView.ViewHolder{
            
            ListItemDeviceBinding binding;
            
            public VH(@NonNull ListItemDeviceBinding binding){
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        @NonNull
        @Override
        public RVAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ListItemDeviceBinding binding = ListItemDeviceBinding.inflate(
                    ActivityViewDevices.this.getLayoutInflater(), parent, false);
            
            return new RVAdapter.VH(binding);
        }
        
        
        @Override
        public void onBindViewHolder(@NonNull RVAdapter.VH holder, int position){
            
            toggleListItemViews(holder, positionLoadingStatus.get(position));
            
            holder.binding.tvDeviceTitle.setText(
                    itemDevices.get(position).getTitle());
            
            if(itemDevices.get(position).isApproved()){
                holder.binding.btnApproveDevice.setVisibility(View.INVISIBLE);
            }else{
                holder.binding.btnApproveDevice.setOnClickListener(v->{
                    setStatusLoading(position, true);
                    try{
                        approveDevice(position);
                    }catch(JSONException e){
                        e.printStackTrace();
                        App.toast(ActivityViewDevices.this,
                                  getResources().getString(R.string.error_happened));
                        setStatusLoading(position, false);
                    }
                });
            }
            
            if(itemDevices.get(position).getHash().contains(App.HASH)){
                holder.binding.tvThisDevice.setVisibility(View.VISIBLE);
            }else{
                holder.binding.tvThisDevice.setVisibility(View.GONE);
            }
            
            
            holder.binding.btnRemoveDevice.setOnClickListener(v->{
                
                Dialog dialog = new Dialog(ActivityViewDevices.this);
                
                DialogConfirmBinding confirmBinding =
                        DialogConfirmBinding.inflate(ActivityViewDevices.this.getLayoutInflater());
                
                dialog.setContentView(confirmBinding.getRoot());
                
                confirmBinding.tvDialogTitle.setText(R.string.irriversible_action);
                
                confirmBinding.btnDialogDeny.setOnClickListener(v1->dialog.cancel());
                
                confirmBinding.btnDialogAccept.setOnClickListener(v1->{
                    setStatusLoading(position, true);
                    dialog.cancel();
                    try{
                        removeDevice(position);
                    }catch(JSONException e){
                        e.printStackTrace();
                        App.toast(ActivityViewDevices.this,
                                  getResources().getString(R.string.error_happened));
                        setStatusLoading(position, false);
                    }
                });
                
                dialog.show();
                
            });
        }
        
        private void removeDevice(int position) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", itemDevices.get(position).getHash());
            
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
                                            if(itemDevices.get(position).getHash()
                                                       .equalsIgnoreCase(App.HASH)){
                                                finish();
                                                return;
                                            }
                                            try{
                                                getDevices();
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
                                App.toast(ActivityViewDevices.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "remove_device.php",
                    "removeDevice", handler);
        }
        
        private void setStatusLoading(int position, boolean isLoading){
            positionLoadingStatus.set(position, isLoading);
            notifyDataSetChanged();
        }
        
        private void approveDevice(int position) throws JSONException{
            JSONObject requestParameters = new JSONObject();
            
            requestParameters.put("hash", itemDevices.get(position).getHash());
            
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
                                                getDevices();
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
                                App.toast(ActivityViewDevices.this,
                                          getResources().getString(R.string.error_happened));
                            });
                        }
                    };
            
            Tools.httpPost(
                    requestParameters,
                    App.url + "approve_device.php",
                    "approveDevice", handler);
        }
        
        private void toggleListItemViews(RVAdapter.VH holder, boolean isLoading){
            if(isLoading){
                holder.binding.pbListItemDevice.setVisibility(View.VISIBLE);
                holder.binding.btnApproveDevice.setVisibility(View.INVISIBLE);
                holder.binding.btnRemoveDevice.setVisibility(View.INVISIBLE);
                holder.binding.tvDeviceTitle.setVisibility(View.INVISIBLE);
                holder.binding.tvThisDevice.setVisibility(View.INVISIBLE);
            }else{
                holder.binding.pbListItemDevice.setVisibility(View.INVISIBLE);
                holder.binding.btnApproveDevice.setVisibility(View.VISIBLE);
                holder.binding.btnRemoveDevice.setVisibility(View.VISIBLE);
                holder.binding.tvDeviceTitle.setVisibility(View.VISIBLE);
                holder.binding.tvThisDevice.setVisibility(View.VISIBLE);
            }
        }
        
        
        @Override
        public int getItemCount(){
            return itemDevices.size();
        }
    }
    
}
