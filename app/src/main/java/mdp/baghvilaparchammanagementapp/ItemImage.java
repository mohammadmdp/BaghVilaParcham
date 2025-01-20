package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ItemImage{
    
    private String imgUrl, code, id;
    
    private ItemImage(String imgUrl, String code, String id){
        setImgUrl(imgUrl);
        setCode(code);
        setId(id);
    }
    
    @NonNull
    static ArrayList<ItemImage> getListFromStringJson(String response) throws JSONException{
        ArrayList<ItemImage> itemFiles = new ArrayList<>();
        
        JSONArray  jsonArray = new JSONArray(response);
        JSONObject jsonObject;
        for(int i = 0; i < jsonArray.length(); i++){
            jsonObject = jsonArray.getJSONObject(i);
            itemFiles.add(new ItemImage(
                    jsonObject.getString("img"),
                    jsonObject.getString("doc_id"),
                    jsonObject.getString("img_id")
            ));
        }
        
        return itemFiles;
    }
    
    String getImgUrl(){
        return imgUrl;
    }
    
    private void setImgUrl(String imgUrl){
        this.imgUrl = imgUrl == null ? "" : imgUrl;
    }
    
    String getCode(){
        return code;
    }
    
    private void setCode(String code){
        this.code = code == null ? "" : code;
    }
    
    String getId(){
        return id;
    }
    
    private void setId(String id){
        this.id = id == null ? "" : id;
    }
}
