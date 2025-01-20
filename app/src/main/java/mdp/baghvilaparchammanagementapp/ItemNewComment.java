package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;

class ItemNewComment{
    
    private long   id;
    private long   postId;
    private String username;
    private String text;
    private String commentEmail;
    
    ItemNewComment(long id, long postId, String username, String text, String commentEmail){
        setId(id);
        setPostId(postId);
        setUsername(username);
        setText(text);
        setCommentEmail(commentEmail);
    }
    
    public long getId(){
        return id;
    }
    
    private void setId(long id){
        this.id = id;
    }
    
    public long getPostId(){
        return postId;
    }
    
    private void setPostId(long postId){
        this.postId = postId;
    }
    
    @NonNull
    public String getUsername(){
        return username == null ? "" : username;
    }
    
    private void setUsername(String username){
        this.username = username;
    }
    
    @NonNull
    public String getText(){
        return text == null ? "" : text;
    }
    
    private void setText(String text){
        this.text = text;
    }
    
    @NonNull
    public String getCommentEmail(){
        return commentEmail == null ? "" : commentEmail;
    }
    
    private void setCommentEmail(String commentEmail){
        this.commentEmail = commentEmail;
    }
    
    @NonNull
    public static ArrayList<ItemNewComment> getListFromStringJson(String jsonString)
            throws JSONException{
        ArrayList<ItemNewComment> itemNewComments = new ArrayList<>();
        
        JSONArray array = new JSONArray(jsonString);
        for(int i = 0; i < array.length(); i++){
            JSONObject object = array.getJSONObject(i);
            ItemNewComment itemNewComment =
                    new ItemNewComment(object.getInt("id"),
                                       object.getInt("post_id"),
                                       object.getString("username"),
                                       object.getString("text"),
                                       object.getString("comment_email"));
            itemNewComments.add(itemNewComment);
        }
        return itemNewComments;
    }
    
    
}
