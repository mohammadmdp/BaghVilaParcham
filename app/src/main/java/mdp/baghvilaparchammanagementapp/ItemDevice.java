package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

class ItemDevice{
    
    private String  title;
    private boolean approved;
    private String  hash;
    
    private ItemDevice(String title, boolean approved, String hash){
        setTitle(title);
        setApproved(approved);
        setHash(hash);
    }
    
    @NonNull
    static ArrayList<ItemDevice> getListFromStringJson(String jsonString) throws JSONException{
        ArrayList<ItemDevice> itemFiles = new ArrayList<>();
        
        JSONArray jsonArray = new JSONArray(jsonString);
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            itemFiles.add(new ItemDevice(
                    jsonObject.getString("title"),
                    jsonObject.getInt("approved") > 0,
                    jsonObject.getString("hash")
            ));
        }
        
        return itemFiles;
    }
    
    String getTitle(){
        return title;
    }
    
    private void setTitle(String title){
        this.title = title == null ? "" : title;
    }
    
    public boolean isApproved(){
        return approved;
    }
    
    public void setApproved(boolean approved){
        this.approved = approved;
    }
    
    @NonNull
    public String getHash(){
        return hash == null ? "" : hash;
    }
    
    public void setHash(String hash){
        this.hash = hash;
    }
}
