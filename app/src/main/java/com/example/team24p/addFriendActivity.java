package com.example.team24p;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class addFriendActivity extends AppCompatActivity {


    private String userNameLoggedIn;
    private String searchLine;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef;
    public final ArrayList<User>userArrayList = new ArrayList<>();
    private ListView userListSearched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //intialize variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        userNameLoggedIn = getIntent().getStringExtra("userNameLoggedIn");
        searchLine = getIntent().getStringExtra("searchLine");
        mRef= mDatabase.getReference().child("Users");
        userListSearched = (ListView)findViewById(R.id.SearchedList);

        //get users that were relevant to the search from db
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object > userTable = (HashMap<String, Object>)  dataSnapshot.getValue();
                for (String key : userTable.keySet()) {
                    Map<String, Object> value = (HashMap<String, Object>) userTable.get(key);
                    User user = new User();
                    //get the username that contains chars from the searchline
                    if (value.get("username").toString().contains(searchLine)&&!value.get("username").equals(userNameLoggedIn)) {
                        user.setUserName(value.get("username").toString());
                        userArrayList.add(user);
                    }

                }

                userListSearched.setAdapter(null);
                userListSearched.setAdapter(new ListResources(addFriendActivity.this)); //add all the users relevant to the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

// custom user list of the search result - have plus button to add the user
    class ListResources extends BaseAdapter {
        ArrayList<User>mydata;
        User temp;
        int flag,flag2;
        Context context;
        String selectedKey1,selectedKey2;

        ListResources(Context context){
            this.context = context;
            mydata=userArrayList;

        }
        @Override
        public int getCount() {
            return userArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return userArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater =getLayoutInflater();
            View row = inflater.inflate(R.layout.addrow,parent,false);
            final TextView pending = (TextView)row.findViewById(R.id.pendingText);
            final FloatingActionButton accBut = (FloatingActionButton)row.findViewById(R.id.addFriendBut);
            temp = mydata.get(position);
            mRef = mDatabase.getReference().child("Friends");

            accBut.setVisibility(View.VISIBLE);
            pending.setVisibility(View.INVISIBLE);
            TextView userTextViewList = (TextView)row.findViewById(R.id.usernameText);
            userTextViewList.setText(temp.getUserName());
            flag = 0;
            flag2=0;

            //get the friend table from db and see if the searched user is already friend of mine
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> friendsTable = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : friendsTable.keySet()) {
                        Map<String, Object> value = (HashMap<String, Object>) friendsTable.get(key);
                        if (value.get("username").toString().equals(temp.getUserName())) { // if user name equal the friend added
                            Map<String, Object> friendlist = (HashMap<String, Object>) value.get("friendlist");
                            for (String key2 : friendlist.keySet()) {
                                Map<String, Object> value2 = (HashMap<String, Object>) friendlist.get(key2);

                                if (value2.get("username").equals(userNameLoggedIn) && value2.get("enabled").toString() == "true") {  //if user is friend
                                    accBut.setVisibility(View.INVISIBLE); //plus button will go invisible
                                    pending.setVisibility(View.INVISIBLE); //pending text will appear
                                }
                                temp.setId(key);
                            }
                            flag=1;
                        }
                        else if(value.get("username").toString().equals(userNameLoggedIn)){ //if username in friends equal the connected one
                            Map<String, Object> friendlist = (HashMap<String, Object>) value.get("friendlist");
                            for (String key2 : friendlist.keySet()) {
                                Map<String, Object> value2 = (HashMap<String, Object>) friendlist.get(key2);

                                if ((value2.get("username").equals(temp.getUserName()) && value2.get("enabled").toString() == "true")) {
                                    accBut.setVisibility(View.INVISIBLE);
                                    pending.setVisibility(View.INVISIBLE);
                                }
                                temp.setAdress(key);
                                //temp.setAdress(enables);

                            }
                            flag2=1;
                        }
                    }
                    //if user click add user button
                    accBut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(flag!=1){ //if there is no such user in friends table (first friend) of the added
                                Map<String, Object> user = new HashMap<>(); //create the user hash map and put details inside
                                Map<String, Object> newFriend = new HashMap<>();
                                Map<String, Object> friendlist = new HashMap<>();
                                newFriend.put("enabled",true);
                                newFriend.put("confirmed",false);
                                newFriend.put("username",userNameLoggedIn);
                                String key = mRef.push().getKey();

                                user.put(key,newFriend);
                                friendlist.put("friendlist",user);
                                friendlist.put("username",temp.getUserName());

                                mRef.child(key).setValue(friendlist);
                            }
                            else { //have friends already and just need to add more
                                Map<String, Object> newFriend = new HashMap<>();
                                newFriend.put("enabled",true);
                                newFriend.put("confirmed",false);
                                newFriend.put("username",userNameLoggedIn);
                                String key = mRef.push().getKey();
                                mRef.child(temp.getId()).child("friendlist").child(key).setValue(newFriend);
                            }

                            if(flag2!=1){ //if there is no such user of the added one in friends table of me
                                Map<String, Object> user = new HashMap<>();
                                Map<String, Object> newFriend = new HashMap<>();
                                Map<String, Object> friendlist = new HashMap<>();
                                newFriend.put("enabled",false);
                                newFriend.put("confirmed",false);
                                newFriend.put("username",temp.getUserName());
                                String key = mRef.push().getKey();

                                user.put(key,newFriend);
                                friendlist.put("friendlist",user);
                                friendlist.put("username",userNameLoggedIn);

                                mRef.child(key).setValue(friendlist);
                            }
                            else{ //he got friends and need to add one
                                Map<String, Object> newFriend = new HashMap<>();
                                newFriend.put("enabled",false);
                                newFriend.put("confirmed",false);
                                newFriend.put("username",temp.getUserName());
                                String key = mRef.push().getKey();
                                mRef.child(temp.getAdress()).child("friendlist").child(key).setValue(newFriend);
                            }



                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            return row;

        }
    }
}
