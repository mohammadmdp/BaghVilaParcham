package mdp.baghvilaparchammanagementapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import mdp.baghvilaparchammanagementapp.databinding.ListItemFileBinding;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AdapterFiles extends BaseAdapter implements Filterable{
    
    ItemFilter filter = new ItemFilter();
    
    @Override
    public Filter getFilter(){
        return filter;
    }
    
    static class VH{
        ListItemFileBinding listItemFileBinding;
        VH(ListItemFileBinding listItemFileBinding){
            this.listItemFileBinding = listItemFileBinding;
        }
    }
    
    private final ActivityFiles       context;
    private final ArrayList<ItemFile> itemFiles;
    private ArrayList<ItemFile> filteredFiles;
    
    AdapterFiles(@NonNull ActivityFiles context, @NonNull ArrayList<ItemFile> itemFiles){
        this.context = context;
        this.itemFiles = itemFiles;
        this.filteredFiles = itemFiles;
    }
    
    @Override
    public int getCount(){
        return filteredFiles.size();
    }
    
    
    @Override
    @NonNull
    public ItemFile getItem(int position){
        return filteredFiles.get(position);
    }
    
    
    @Override
    public long getItemId(int position){
        return position;
    }
    
    
    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        VH vh;
        if(convertView == null){
    
            ListItemFileBinding listItemFileBinding =
                    ListItemFileBinding.inflate(context.getLayoutInflater(), parent, false);
            
            vh = new VH(listItemFileBinding);
            
            convertView = vh.listItemFileBinding.getRoot();
            
            convertView.setTag(vh);
        }else{
            vh = ((VH) convertView.getTag());
        }
        
        if(getCount()>0){
            
            ItemFile itemFile = getItem(position);
            
            vh.listItemFileBinding.sdvFileItemImage.getHierarchy()
                    .setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
    
            Uri uri = Uri.parse(App.url + itemFile.getImageUrl());
            vh.listItemFileBinding.sdvFileItemImage.setImageURI(uri);
    
    
            vh.listItemFileBinding.tvFileTitle.setText(itemFile.getTitle());
            vh.listItemFileBinding.tvFileText.setText(itemFile.getText());
            vh.listItemFileBinding.tvPriceTitle.setText(itemFile.getPriceTitle());
            vh.listItemFileBinding.tvCode.setText(itemFile.getCode());
    
            vh.listItemFileBinding.btnRemove.setOnClickListener((v)->removeFile(itemFile));
    
            vh.listItemFileBinding.btnEdit.setOnClickListener((v)->{
        
                Bundle extras = new Bundle();
                extras.putString("edit", itemFile.getJsonStr());
                Intent intent = new Intent(context, ActivityAddFile.class);
                intent.putExtras(extras);
                context.startActivityForResult(intent, 0);
            });
    
            vh.listItemFileBinding.cvShareFile.setOnClickListener(v->{
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
        
                String builder = App.url
                                 + "?code="
                                 + itemFile.getCode()
                                 + "\n\n"
                                 + "کد: "
                                 + itemFile.getCode()
                                 + "\n\n"
                                 + itemFile.getTitle();
        
                sendIntent.putExtra(Intent.EXTRA_TEXT, builder);
                sendIntent.setType("text/plain");
        
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                context.startActivity(shareIntent);
            });
    
            vh.listItemFileBinding.cvOpenFile.setOnClickListener(v->{
                String url           = "http://baghvilaparcham.com/?code=" + itemFile.getCode();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            });
    
            if(itemFile.isSold()){
                vh.listItemFileBinding.btnSoldUnsold.setVisibility(View.VISIBLE);
            }else{
                vh.listItemFileBinding.btnSoldUnsold.setVisibility(View.INVISIBLE);
            }
        }
        
        
        return convertView;
    }
    
    @SuppressLint("SetTextI18n")
    private void removeFile(ItemFile itemFile){
    
        Dialog d = new Dialog(context);
    
        DialogConfirmBinding dialogConfirmBinding =
                DialogConfirmBinding.inflate(context.getLayoutInflater());
    
        d.setContentView(dialogConfirmBinding.getRoot());
        d.setCanceledOnTouchOutside(false);
    
        dialogConfirmBinding.pbDialogConfirm.setVisibility(View.GONE);
    
        dialogConfirmBinding.tvDialogTitle
                .setText(
                        context.getResources().getString(R.string.file_delete_confirm)
                        +"\n\n"
                        +"کد فایل:"
                        +" "
                        +itemFile.getCode());
    
        dialogConfirmBinding.btnDialogAccept.setOnClickListener(v1->{
        
            dialogConfirmBinding.pbDialogConfirm.setVisibility(View.VISIBLE);
            dialogConfirmBinding.btnDialogAccept.setVisibility(View.INVISIBLE);
            dialogConfirmBinding.btnDialogDeny.setVisibility(View.INVISIBLE);
    
    
            try{
                JSONObject requestParameters = new JSONObject();
                requestParameters.put("hash", App.HASH);
                requestParameters.put("code", itemFile.getCode());
                Tools.IHttpPostResultHandler handler =
                        new Tools.IHttpPostResultHandler(){
                            @Override
                            public void OnResponse(Response response){
                    
                                try{
                                    ResponseBody body = response.body();
                                    if(body != null){
                                        String responseStr = body.string();
                                        App.log("response: " + responseStr);
                            
                                        if(responseStr.contains("success")){
                                            context.runOnUiThread(()->{
                                                
                                                d.cancel();
                                                
                                                App.toast(context,
                                                          context.getResources()
                                                                  .getString(
                                                                          R.string
                                                                                  .remove_file_succeeded));
                                                context.removeAdapter();
                                                context.setAdapterFiles();
                                            });
                                        }else{
                                            OnFailure();
                                        }
                                    }
                        
                                }catch(Exception e){
                                    e.printStackTrace();
                                    OnFailure();
                                }
                            }
                
                            @Override
                            public void OnFailure(){
                                context.runOnUiThread(()->{
                                    App.toast(context,
                                              context.getResources()
                                                      .getString(R.string.remove_file_failed));
                        
                                    d.cancel();
                                });
                            }
                        };
    
                Tools.httpPost(
                        requestParameters,
                        App.url + "remove_file.php",
                        "removeFile", handler);
            }catch(JSONException e){
                e.printStackTrace();
                App.toast(context,
                          context.getResources().getString(R.string.remove_file_failed));
                d.cancel();
            }
    
        });
    
        dialogConfirmBinding.btnDialogDeny.setOnClickListener(v1->d.cancel());
    
        d.show();
        
    }
    
    class ItemFilter extends Filter{
        
        
        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            
            String filterString = constraint.toString();
            
            filteredFiles = new ArrayList<>();
            
            for(int i = 0; i < itemFiles.size(); i++){
                
                if(itemFiles.get(i).getCode().contains(filterString) ||
                   itemFiles.get(i).getDetails().contains(filterString) ||
                   itemFiles.get(i).getPrice().contains(filterString) ||
                   itemFiles.get(i).getText().contains(filterString) ||
                   itemFiles.get(i).getTitle().contains(filterString) ||
                   itemFiles.get(i).getHiddenText().contains(filterString)){
                    filteredFiles.add(itemFiles.get(i));
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
