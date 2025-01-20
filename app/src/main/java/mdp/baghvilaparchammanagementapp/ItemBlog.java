package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

class ItemBlog{
    
    private long   id;
    private String title;
    private String shortText;
    
    ItemBlog(long id, String title, String shortText){
        setId(id);
        setTitle(title);
        setShortText(shortText);
    }
    
    public long getId(){
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }
    
    @NonNull
    public String getTitle(){
        return title == null ? "" : title;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    @NonNull
    public String getShortText(){
        return shortText == null ? "" : shortText;
    }
    
    public void setShortText(String shortText){
        this.shortText = shortText;
    }
    
    @NonNull
    public static ArrayList<ItemBlog> getListFromStringJson(String jsonString) throws JSONException{
        ArrayList<ItemBlog> itemBlogs = new ArrayList<>();
        
        JSONArray array = new JSONArray(jsonString);
        for(int i = 0; i < array.length(); i++){
            JSONObject object = array.getJSONObject(i);
            ItemBlog itemBlog =
                    new ItemBlog(object.getInt("id"),
                                 object.getString("title"),
                                 object.getString("short_text"));
            itemBlogs.add(itemBlog);
        }
        return itemBlogs;
    }
    
    
}
