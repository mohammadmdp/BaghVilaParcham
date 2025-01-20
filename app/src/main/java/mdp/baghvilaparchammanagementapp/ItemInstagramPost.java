package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

class ItemInstagramPost{
    
    private long   id;
    private String embedCode;
    
    ItemInstagramPost(long id, String embedCode){
        setId(id);
        setEmbedCode(embedCode);
    }
    
    public long getId(){
        return id;
    }
    
    private void setId(long id){
        this.id = id;
    }
    
    @NonNull
    public String getEmbedCode(){
        return embedCode == null ? "" : embedCode;
    }
    
    public void setEmbedCode(String embedCode){
        this.embedCode = embedCode;
    }
    
    @NonNull
    public static ArrayList<ItemInstagramPost> getListFromStringJson(String jsonString)
            throws JSONException{
        ArrayList<ItemInstagramPost> itemInstagramPosts = new ArrayList<>();
        
        JSONArray jsonArray = new JSONArray(jsonString);
        
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ItemInstagramPost itemInstagramPost =
                    new ItemInstagramPost(jsonObject.getLong("id"),
                                          jsonObject.getString("embed_code"));
            itemInstagramPosts.add(itemInstagramPost);
        }
        
        return itemInstagramPosts;
    }
    
    
}
