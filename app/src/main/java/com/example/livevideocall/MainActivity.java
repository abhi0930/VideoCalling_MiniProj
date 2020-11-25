package com.example.livevideocall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView contact_list;
    ImageView add_friends;
    private DatabaseReference contactref,userref;
    private String currentuserid;
    private FirebaseAuth mAuth;
    private String contact_name="";
    private String profile_pic="";
    private String CalledBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth= FirebaseAuth.getInstance();
        currentuserid=mAuth.getCurrentUser().getUid();

        contactref= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userref=FirebaseDatabase.getInstance().getReference().child("Users");


        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contact_list=findViewById(R.id.contact_list);
        add_friends=findViewById(R.id.findpeople);
        contact_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        add_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findpeople=new Intent(MainActivity.this,FindFriends.class);
                startActivity(findpeople);
                finish();
            }
        });

    }
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId())
            {
                case(R.id.navigation_home):
                    Intent intent=new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;

                case(R.id.navigation_notifications):
                    Intent notification=new Intent(MainActivity.this,NotificationsActivity.class);
                    startActivity(notification);
                    break;

                case(R.id.navigation_settings):
                    Intent settings=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(settings);
                    break;

                case(R.id.navigation_logout):
                    FirebaseAuth.getInstance().signOut();
                    Intent signout=new Intent(MainActivity.this,Registration_Activity.class);
                    startActivity(signout);
                    finish();
            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        verifyUser();
        
        checkForCall();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactref.child(currentuserid),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                final String listUserid=getRef(position).getKey();

                userref.child(listUserid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                        {
                            contact_name=snapshot.child("UserName").getValue().toString();
                            profile_pic=snapshot.child("Profile_Pic").getValue().toString();

                            holder.username.setText(contact_name);
                            Picasso.get().load(profile_pic).into(holder.profile_pic);
                        }

                        holder.call_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent toCall=new Intent(MainActivity.this,CallScreenActivity.class);
                                toCall.putExtra("visiter_id",listUserid);
                                startActivity(toCall);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_container,parent,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        contact_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        ImageView profile_pic;
        Button call_btn;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.saved_contact_name);
            profile_pic=itemView.findViewById(R.id.saved_contact_pic);
            call_btn=itemView.findViewById(R.id.video_call_btn);

        }
    }

    private void verifyUser()
    {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists())
                {
                    Intent settings=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(settings);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkForCall() {

        userref.child(currentuserid).child("Incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild("IsCalling"))
                {
                    CalledBy=snapshot.child("IsCalling").getValue().toString();

                    Intent toCall=new Intent(MainActivity.this,CallScreenActivity.class);
                    toCall.putExtra("visiter_id",CalledBy);
                    startActivity(toCall);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}