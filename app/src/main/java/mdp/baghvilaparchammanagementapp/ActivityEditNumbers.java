package mdp.baghvilaparchammanagementapp;

import android.os.Bundle;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityEditNumbers extends AppCompatActivity{
    
    EditText etTell1,etTell2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_numbers);
        etTell1 = findViewById(R.id.et_tel1);
        etTell2 = findViewById(R.id.et_tel2);
        findViewById(R.id.btn_edit_numbers_send).setOnClickListener(v -> {
            if(!etTell1.getText().toString().trim().isEmpty()&&
               !etTell2.getText().toString().trim().isEmpty()){
                try{
                    JSONArray phones = new JSONArray();
                    JSONObject object = new JSONObject();
                    object.put(DBModelPhone.KEY_ID,1);
                    object.put(DBModelPhone.KEY_NUMBER, etTell1.getText().toString().trim());
                    phones.put(object);
                    object = new JSONObject();
                    object.put(DBModelPhone.KEY_ID, 2);
                    object.put(DBModelPhone.KEY_NUMBER, etTell2.getText().toString().trim());
                    phones.put(object);
                    DBModelPhone.resetPhones(phones,ActivityEditNumbers.this);
                    finish();
                }catch(Exception e){
                    e.printStackTrace();
                    App.toast(ActivityEditNumbers.this,
                              getResources().getString(R.string.error_happened));
                }
            }
        });
    
        try{
            JSONArray phones = DBModelPhone.getPhones(ActivityEditNumbers.this);
            if(phones.length() > 0){
                JSONObject object1 = phones.getJSONObject(0);
                JSONObject object2 = phones.getJSONObject(1);
                etTell1.setText(object1.getString(DBModelPhone.KEY_NUMBER));
                etTell2.setText(object2.getString(DBModelPhone.KEY_NUMBER));
            }
        }catch(Exception e){
            e.printStackTrace();
            App.toast(ActivityEditNumbers.this, getResources().getString(R.string.error_happened));
        }
    }
}
