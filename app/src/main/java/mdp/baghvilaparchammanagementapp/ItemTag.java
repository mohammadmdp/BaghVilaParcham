package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

class ItemTag{
    
    private long   id;
    private String name;
    
    ItemTag(long id, String name){
        setId(id);
        setName(name);
    }
    
    public long getId(){
        return id;
    }
    
    private void setId(long id){
        this.id = id;
    }
    
    @NonNull
    public String getName(){
        return name == null ? "" : name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    @NonNull
    public static ArrayList<ItemTag> getListFromStringJson(String jsonString) throws JSONException{
        ArrayList<ItemTag> itemNewComments = new ArrayList<>();
        
        JSONArray jsonArray = new JSONArray(jsonString);
        
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ItemTag itemNewComment =
                    new ItemTag(jsonObject.getLong("id"),
                                jsonObject.getString("name"));
            itemNewComments.add(itemNewComment);
        }
        
        return itemNewComments;
    }
    
    
}
