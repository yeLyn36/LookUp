package kr.hs.emirim.dana.lookup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    private ListView rListView;
    List<ItemData> rArray = new ArrayList<ItemData>();
    TextView rTextView;

    private DatabaseReference mDatabase;
    DatabaseReference groupRef;
    AlertDialog.Builder builder;

    String code;
    String name;
    String own;
    Map<String, Object> memberList = new HashMap<>();
    ArrayList<String> namedata = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Intent intent = getIntent();
        code = intent.getExtras().getString("code");
        name = intent.getExtras().getString("name");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        groupRef = mDatabase.child("groups").child(code);
        rListView = (ListView)findViewById(R.id.nameList);

        showList(new RoomActivity.MyCallback() {
            @Override
            public void onCallback(Map<String, Object> List) {
                for (String key: List.keySet()) {
                    if(name.equals(key)){
                        own = List.get(key).toString();
                        Log.d("own", own);
                    }
                    namedata.add(key);
                }
                memberList = (HashMap<String, Object>) List;

                int nDatCnt = 0;
                final ArrayList<ItemData> dnameData = new ArrayList<>();
                for (int i=0; i< namedata.size(); i++){
                    ItemData nameItem = new ItemData();
                    nameItem.nameList = (i+1)+". "+ namedata.get(i);
                    dnameData.add(nameItem);
                }

                ListAdapter rAdapter = new ListAdapter(dnameData);
                rListView.setAdapter(rAdapter);

                rArray.add(new ItemData());

                rTextView = (TextView) findViewById(R.id.connectionCount);
                rTextView.setText(rAdapter.getCount()+"명");

                rListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //선택한 리스트값이 name과 같을 경우에만
                        outOfRoom();
                    }
                });
//=====================================================================================
            }
        });


        rTextView = (TextView) findViewById(R.id.roomPwd);
        rTextView.setText(code);

        TextPaint paint = rTextView.getPaint();
        float width = paint.measureText(code);

        Shader textShader = new LinearGradient(0, 0, width, rTextView.getTextSize(),
                new int[]{
                        Color.parseColor("#00B2FF"),
                        Color.parseColor("#00CDC1"),
                }, null, Shader.TileMode.CLAMP);
        rTextView.getPaint().setShader(textShader);

        findViewById(R.id.floatingBtn).setOnClickListener(floatingBtnClick);
    }

    Button.OnClickListener floatingBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("name", name);
            System.out.println("won" + own);
            outOfRoom();
        }
    };

    public void outOfRoom(){
        builder = new AlertDialog.Builder(RoomActivity.this, R.style.customDialog);
        builder.setTitle("방을 나가시겠습니까?");

        builder.setPositiveButton(" ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(own.equals("owner")) {
                    groupRef.removeValue();
                } else {
                    groupRef.child("member").child(name).removeValue();
                }
                Intent intent = new Intent(RoomActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 액티비티 스택에 쌓인 액티비티 제거
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton(" ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        Button yes = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Drawable img1 = ContextCompat.getDrawable(RoomActivity.this,R.drawable.yes);
        img1.setBounds(0, 0, 70, 70);
        yes.setCompoundDrawables(img1, null, null, null);

        Button no = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Drawable img2 = ContextCompat.getDrawable(RoomActivity.this,R.drawable.exit);
        img2.setBounds(0, 0, 70, 70);
        no.setCompoundDrawables(img2, null, null, null);
    }

    public void showList(final RoomActivity.MyCallback myCallback) {
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if(ds.getKey().toString().equals("member")){
                        Map<String, Object> membernames = (HashMap<String, Object>)ds.getValue();
                        for(String membersKey : membernames.keySet()){
                            memberList.put(membersKey, membernames.get(membersKey));
                        }
                    }
                }
                myCallback.onCallback(memberList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public interface MyCallback {
        void onCallback(Map<String, Object> memberList);
    }

}
