package com.example.livevideocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notification_list;
    private DatabaseReference frndrequestref,contactref,userref;
    private String currentuserid;
    private String currentuserstate="new";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth= FirebaseAuth.getInstance();
        currentuserid=mAuth.getCurrentUser().getUid();

        frndrequestref= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactref=FirebaseDatabase.getInstance().getReference().child("Contacts");
        userref=FirebaseDatabase.getInstance().getReference().child("Users");

        notification_list=findViewById(R.id.notification_list);
        notification_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(frndrequestref.child(currentuserid),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,NotificationsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder notificationsViewHolder, int i, @NonNull Contacts contacts) {

                notificationsViewHolder.addfrnd.setVisibility(View.VISIBLE);
                notificationsViewHolder.cancelfrnd.setVisibility(View.VISIBLE);

                 final String listUserid=getRef(i).getKey();
                DatabaseReference requestType=getRef(i).child("Request_Type").getRef();
                requestType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String type=dataSnapshot.getValue().toString();
                            if(type.equals("Recieved"))
                            {
                                notificationsViewHolder.cardview.setVisibility(View.VISIBLE);
                                userref.child(listUserid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("Profile_Pic"))
                                        {
                                            final String img=dataSnapshot.child("Profile_Pic").getValue().toString();

                                            Picasso.get().load(img).into(notificationsViewHolder.profile_pic);
                                        }
                                        final String name=dataSnapshot.child("UserName").getValue().toString();
                                        notificationsViewHolder.username.setText(name);

                                        notificationsViewHolder.addfrnd.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                contactref.child(currentuserid).child(listUserid).child("Contacts").setValue("Saved_Contacts").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            contactref.child(listUserid).child(currentuserid).child("Contacts").setValue("Saved_Contacts").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        frndrequestref.child(currentuserid).child(listUserid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    frndrequestref.child(listUserid).child(currentuserid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(NotificationsActivity.this,"Contact Successfully Added",Toast.LENGTH_LONG).show();
                                                                                            }

                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });

                                                                    }

                                                                }
                                                            });
                                                        }

                                                    }
                                                });

                                            }
                                        });

                                        notificationsViewHolder.cancelfrnd.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                frndrequestref.child(currentuserid).child(listUserid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            frndrequestref.child(currentuserid).child(listUserid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        Toast.makeText(NotificationsActivity.this,"Cancelled!",Toast.LENGTH_LONG).show();
                                                                    }

                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else
                            {
                                notificationsViewHolder.cardview.setVisibility(View.INVISIBLE);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_activity_layout,parent,false);
                NotificationsViewHolder viewHolder=new NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notification_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
      TextView username;
      ImageView profile_pic;
      Button addfrnd;
      Button cancelfrnd;
      RelativeLayout cardview;


        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.username);
            profile_pic=itemView.findViewById(R.id.search_profile_pic);
            addfrnd=itemView.findViewById(R.id.accept_frnd_btn);
            cancelfrnd=itemView.findViewById(R.id.cancel_frnd_btn);
            cardview=itemView.findViewById(R.id.card_view);
        }
    }

   // private void cancelrequest()
   // {


   // }


   // private void acceptrequest()
   // {

   // }
}
