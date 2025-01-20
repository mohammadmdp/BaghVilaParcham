package mdp.baghvilaparchammanagementapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.facebook.drawee.drawable.ScalingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import mdp.baghvilaparchammanagementapp.databinding.DialogConfirmBinding;
import mdp.baghvilaparchammanagementapp.databinding.ListItemImageBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AdapterImages extends BaseAdapter implements Filterable{
    
    ItemFilter filter = new ItemFilter();
    
    @Override
    public Filter getFilter(){
        return filter;
    }
    
    static class VH{
        ListItemImageBinding binding;
    }
    
    private final ActivityImages       context;
    private final ArrayList<ItemImage> itemImages;
    private ArrayList<ItemImage> filteredImages;
    
    
    AdapterImages(@NonNull ActivityImages context, @NonNull ArrayList<ItemImage> images){
        this.context = context;
        this.itemImages = images;
        this.filteredImages = images;
    }
    
    @Override
    public int getCount(){
        return filteredImages.size();
    }
    
    
    @Override
    @NonNull
    public ItemImage getItem(int position){
        return filteredImages.get(position);
    }
    
    
    @Override
    public long getItemId(int position){
        return position;
    }
    
    private void removeItem(int position){
        itemImages.remove(position);
        filteredImages.remove(position);
        notifyDataSetChanged();
    }
    
    
    @SuppressLint({"MissingPermission", "HardwareIds"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        VH vh;
        if(convertView == null){
            vh = new VH();
            vh.binding = ListItemImageBinding.inflate(context.getLayoutInflater());
            convertView = vh.binding.getRoot();
            convertView.setTag(vh);
        }else{
            vh = ((VH) convertView.getTag());
        }
        
        if(getCount()>0){
            ItemImage itemImage = getItem(position);
            vh.binding.ivImage.getHierarchy()
                    .setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
    
            Uri uri = Uri.parse(itemImage.getImgUrl());
    
            vh.binding.ivImage.setImageURI(uri);
    
            vh.binding.ivRemoveNewImage.setTag(position);
            vh.binding.ivRemoveNewImage.setOnClickListener((v)->{
                Dialog d = new Dialog(context);
                DialogConfirmBinding dialogConfirmBinding =
                        DialogConfirmBinding.inflate(context.getLayoutInflater());
        
                d.setContentView(dialogConfirmBinding.getRoot());
                d.setCanceledOnTouchOutside(false);
        
                dialogConfirmBinding.pbDialogConfirm.setVisibility(View.GONE);
                dialogConfirmBinding.tvDialogTitle
                        .setText(context.getResources().getString(R.string.image_delete_confirm));
        
                dialogConfirmBinding.btnDialogAccept.setOnClickListener(v1->{
                    dialogConfirmBinding.pbDialogConfirm.setVisibility(View.VISIBLE);
                    dialogConfirmBinding.btnDialogAccept.setVisibility(View.INVISIBLE);
                    dialogConfirmBinding.btnDialogDeny.setVisibility(View.INVISIBLE);
    
    
    
                    try{
                        JSONObject requestParameters = new JSONObject();
                        requestParameters.put("hash", App.HASH);
                        requestParameters.put("img_id", itemImage.getId());
                        Tools.IHttpPostResultHandler handler =
                                new Tools.IHttpPostResultHandler(){
                                    @Override
                                    public void OnResponse(Response response){
                                        ResponseBody responseBody = response.body();
                                        if(responseBody != null){
                                            try{
                                                String s = responseBody.string();
                                                App.log("remove_image.php onResponse: " + s);
    
                                                onImageRemoveResponse(s, v);
                                                responseBody.close();
                                            }catch(Exception e){
                                                e.printStackTrace();
                                                OnFailure();
                                            }
                                        }
    
                                        context.runOnUiThread(d::cancel);
                                    }
                
                                    @Override
                                    public void OnFailure(){
                                        context.runOnUiThread(()->{
                                            App.toast(context,
                                                      context.getResources().getString(
                                                              R.string.remove_image_failed));
        
                                            d.cancel();
                                        });
                                    }
                                };
    
                        Tools.httpPost(requestParameters,
                                       App.url + "remove_image.php",
                                       "ivRemoveNewImage",
                                       handler);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
            
                });
                dialogConfirmBinding.btnDialogDeny.setOnClickListener(v1->d.cancel());
        
                d.show();
            });
        }
    
        
        return convertView;
    }
    
    private void onImageRemoveResponse(String s, View v){
        if(s.contains("success")){
            context.runOnUiThread(()->{
                App.toast(context,
                          context.getResources()
                                  .getString(
                                          R.string
                                                  .remove_image_succeeded));
                removeItem((Integer) v.getTag());
            });
        }else{
            context.runOnUiThread(()->App.toast(
                    context,
                    context.getString(R.string.error_happened)));
        }
    }
    
    class ItemFilter extends Filter{
        
        
        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            
            String filterString = constraint.toString();
            
            filteredImages = new ArrayList<>();
            
            for(int i = 0; i < itemImages.size(); i++){
                
                if(itemImages.get(i).getCode().equalsIgnoreCase(filterString)){
                    filteredImages.add(itemImages.get(i));
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
