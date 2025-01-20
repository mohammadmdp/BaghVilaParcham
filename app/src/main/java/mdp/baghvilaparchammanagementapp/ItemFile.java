package mdp.baghvilaparchammanagementapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ItemFile{
    
    private String code, title, text, priceTitle, price, area, details, imageUrl, hiddenText;
    private boolean sold, offer;
    static String FILE_CODE        = "code";
    static String FILE_TITLE       = "title";
    static String FILE_TEXT        = "text";
    static String FILE_PRICE_TITLE = "price_title";
    static String FILE_PRICE       = "price";
    static String FILE_AREA        = "area";
    static String FILE_DETAILS     = "details";
    static String FILE_IMAGE_URL   = "image_url";
    static String FILE_SOLD        = "sold";
    static String FILE_OFFER       = "offer";
    static String FILE_HIDDEN_TEXT = "hidden_text";
    
    private ItemFile(String code, String title, String text, String priceTitle,
                     String price, String area, String details, String imageUrl,
                     boolean sold, boolean offer, String hiddenText){
        setCode(code);
        setTitle(title);
        setText(text);
        setPriceTitle(priceTitle);
        setPrice(price);
        setArea(area);
        setDetails(details);
        setImageUrl(imageUrl);
        setSold(sold);
        setOffer(offer);
        setHiddenText(hiddenText);
    }
    
    @NonNull
    static ArrayList<ItemFile> getListFromStringJson(String response) throws JSONException{
        ArrayList<ItemFile> itemFiles = new ArrayList<>();
        JSONArray           jsonArray = new JSONArray(response);
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            itemFiles.add(new ItemFile(
                    jsonObject.getString(FILE_CODE),
                    jsonObject.getString(FILE_TITLE),
                    jsonObject.getString(FILE_TEXT),
                    jsonObject.getString(FILE_PRICE_TITLE),
                    jsonObject.getString(FILE_PRICE),
                    jsonObject.getString(FILE_AREA),
                    jsonObject.getString(FILE_DETAILS),
                    jsonObject.getString(FILE_IMAGE_URL),
                    jsonObject.getInt(FILE_SOLD) > 0,
                    jsonObject.getInt(FILE_OFFER) > 0,
                    jsonObject.getString(FILE_HIDDEN_TEXT)
            ));
        }
        
        return itemFiles;
    }
    
    @NonNull
    String getCode(){
        return code;
    }
    
    @NonNull
    String getJsonStr(){
        JSONObject object = new JSONObject();
        try{
            object.put(FILE_CODE, getCode());
            object.put(FILE_TITLE, getTitle());
            object.put(FILE_TEXT, getText());
            object.put(FILE_PRICE_TITLE, getPriceTitle());
            object.put(FILE_PRICE, getPrice());
            object.put(FILE_AREA, getArea());
            object.put(FILE_DETAILS, getDetails());
            object.put(FILE_IMAGE_URL, getImageUrl());
            object.put(FILE_SOLD, isSold() ? 1 : 0);
            object.put(FILE_OFFER, isOffer() ? 1 : 0);
            object.put(FILE_HIDDEN_TEXT, getHiddenText());
        }catch(Exception e){
            e.printStackTrace();
        }
        return object.toString();
    }
    
    private void setCode(@Nullable String code){
        this.code = code == null ? "" : code;
    }
    
    @NonNull
    String getTitle(){
        return title;
    }
    
    private void setTitle(@Nullable String title){
        this.title = title == null ? "" : title;
    }
    
    @NonNull
    public String getText(){
        return text;
    }
    
    private void setText(@Nullable String text){
        this.text = text == null ? "" : text;
    }
    
    @NonNull
    String getPriceTitle(){
        return priceTitle;
    }
    
    private void setPriceTitle(@Nullable String priceTitle){
        this.priceTitle = priceTitle == null ? "" : priceTitle;
    }
    
    @NonNull
    public String getPrice(){
        return price;
    }
    
    private void setPrice(@Nullable String price){
        this.price = price == null ? "" : price;
    }
    
    @NonNull
    public String getArea(){
        return area;
    }
    
    private void setArea(@Nullable String area){
        this.area = area == null ? "" : area;
    }
    
    @NonNull
    public String getDetails(){
        return details;
    }
    
    private void setDetails(@Nullable String details){
        this.details = details == null ? "" : details;
    }
    
    @NonNull
    String getImageUrl(){
        return imageUrl;
    }
    
    private void setImageUrl(@Nullable String imageUrl){
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }
    
    public boolean isSold(){
        return sold;
    }
    
    public void setSold(boolean sold){
        this.sold = sold;
    }
    
    public boolean isOffer(){
        return offer;
    }
    
    public void setOffer(boolean offer){
        this.offer = offer;
    }
    
    @NonNull
    public String getHiddenText(){
        return hiddenText == null ? "" : hiddenText;
    }
    
    public void setHiddenText(String hiddenText){
        this.hiddenText = hiddenText;
    }
}
