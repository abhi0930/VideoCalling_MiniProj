package com.example.livevideocall;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lve_videocallchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriends extends AppCompatActivity {

    RecyclerView search_list;
    EditText search;
    private String srch="";
    private DatabaseReference user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        user= FirebaseDatabase.getInstance().getReference("Users");


        search_list=findViewById(R.id.search_result_list);
        search=findViewById(R.id.search);
        search_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                srch=s.toString();
                onStart();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=null;

        if(srch.equals(""))
        {
            options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(user,Contacts.class).build();

        }
        else {
            options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(user.orderByChild("UserName").startAt(srch).endAt(srch+ "\uf8ff"),Contacts.class).build();
        }

        final FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> findFriendsViewHolderFirebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, final int i, @NonNull final Contacts contacts)
            {
                findFriendsViewHolder.contact_name.setText(contacts.getUserName());
                findFriendsViewHolder.contact_name.setTextColor(Color.parseColor("#000000"));
                Picasso.get().load(contacts.getProfile_Pic()).into(findFriendsViewHolder.contact_pic);
                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visiter_user_id=getRef(i).getKey();
                        Intent toprofile=new Intent(FindFriends.this,Profile.class);
                        toprofile.putExtra("visiter_user_id",visiter_user_id);
                        toprofile.putExtra("UserName",contacts.getUserName());
                        toprofile.putExtra("Status",contacts.getStatus());
                        toprofile.putExtra("Profile_Pic",contacts.getProfile_Pic());
                        startActivity(toprofile);
                    }
                });



            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_layout,parent,false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        search_list.setAdapter(findFriendsViewHolderFirebaseRecyclerAdapter);
        findFriendsViewHolderFirebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView contact_name;
        ImageView contact_pic;
        RelativeLayout contact_view;
        Button add;


        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            contact_name=itemView.findViewById(R.id.contact_name);
            contact_pic=itemView.findViewById(R.id.contact_pic);
            contact_view=itemView.findViewById(R.id.contact_view);
            add=itemView.findViewById(R.id.add_frnd_btn);
            add.setVisibility(View.INVISIBLE);



        }
    }

}
