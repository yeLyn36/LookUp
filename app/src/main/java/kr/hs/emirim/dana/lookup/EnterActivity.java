package kr.hs.emirim.dana.lookup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnterActivity extends AppCompatActivity {

    EditText input_code;
    EditText input_name;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupRef = mDatabase.child("groups");
    DatabaseReference memberRef;

    String timer;
    String code;
    String roomName;
    String name;
    String mode;
    int master = 0;
    Map<String, Object> addMember = new HashMap<>();
    ArrayList<String> keyValue = new ArrayList<>();
    Map<String, Object> memberMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        input_code = (EditText)findViewById(R.id.input_code);
        input_name = (EditText)findViewById(R.id.input_name);
        findViewById(R.id.start_bt).setOnClickListener(m_stBtnClick);


    }

    //enter room and add member
    public void enterRoom(){
        memberRef = groupRef.child(code).child("member");

        addMember.put(name, "user");
        memberRef.updateChildren(addMember);

    }//해당 코드가 DB 내에 있을 경우, 해당 코드의 데이터 키값을 받아서 member에 name : "user"형태로 추가


    Button.OnClickListener m_stBtnClick = new View.OnClickListener() {
        public void onClick(View v) {
            code = input_code.getText().toString();
            name = input_name.getText().toString();
            selectData(new MyCallback() {
                @Override
                public void onCallback(ArrayList<String> keyValue, Map<String, Object> memberValue, String roomName) { ;
                        if (keyValue.contains(code) && !(code.equals(""))) {
                            if(!(memberValue.containsKey(name)) && !(name.equals(""))){
                                enterRoom();
                                Intent intent = new Intent(EnterActivity.this, RoomActivity.class);
                                intent.putExtra("code", code);
                                intent.putExtra("name", name);
                                intent.putExtra("roomName", roomName);
                                intent.putExtra("timer", timer);
                                intent.putExtra("mode", mode);
                                intent.putExtra("master", master);
                                startActivity(intent);
                            } else {
                                input_name.setText("");
                                if(name.equals("")){
                                    input_name.setHint("닉네임을 입력하세요");
                                } else {
                                    input_name.setHint("존재하는 닉네임입니다.");
                                }
                            }
                        } else {
                            input_code.setText("");
                            input_code.setHint("올바른 코드를 입력하세요");
                        }
                    }
            });
        }
    };

    public void selectData(final MyCallback myCallback) {
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if(code.equals(ds.getKey())) {
                        timer = ((Map<String, Object>)ds.getValue()).get("timer").toString();
                        roomName = ((Map<String, Object>)ds.getValue()).get("name").toString();
                        if(timer.equals("")) {
                            mode = "사용자 지정";
                        } else {
                            mode = "타이머";
                        }
                        memberMap = (HashMap<String, Object>) ((HashMap<String, Object>) ds.getValue()).get("member");
                    }
                    keyValue.add(ds.getKey().toString()); //코드 값 받아오는 리스트
                }
                myCallback.onCallback(keyValue, memberMap, roomName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public interface MyCallback {
        void onCallback(ArrayList<String> keyValue, Map<String, Object> memberValue, String roomName);
    }
}


