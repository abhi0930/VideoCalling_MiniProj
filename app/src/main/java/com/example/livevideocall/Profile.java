package com.example.livevideocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.lve_videocallchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    private String recieverid="";
    private String recieverpic="";
    private String recievername="";
    private String recieverstatus="";
    private String recieverdob="";
    private Button addfrnd;
    private Button decline_frnd;
    private ImageView profile_picture;
    private TextView profile_name;
    private TextView profile_status;
    private TextView birthdate;
    private DatabaseReference frndrequestref,contactsref;
    private FirebaseAuth mAuth;
    private String senderid;
    private String currentuserlog="new";
    private CardView profile_card;
    Adapter adapter;
    private String errorTAG="Cant Update";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prrofile);

        mAuth=FirebaseAuth.getInstance();
        senderid=mAuth.getCurrentUser().getUid();
        frndrequestref= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");

        recieverid=getIntent().getExtras().get("visiter_user_id").toString();
        recieverpic=getIntent().getExtras().get("Profile_Pic").toString();
        recievername=getIntent().getExtras().get("UserName").toString();
        recieverstatus=getIntent().getExtras().get("Status").toString();
        addfrnd=findViewById(R.id.add_frnd_btn);
        profile_picture=findViewById(R.id.profile_page_image);
        profile_name=findViewById(R.id.profile_page_username);
        profile_status=findViewById(R.id.profile_page_status);
        profile_card=findViewById(R.id.profile_card_view);




        Picasso.get().load(recieverpic).into(profile_picture);
        profile_name.setText(recievername);
        profile_status.setText(recieverstatus);

        OnClickEvents();





    }

   public void OnClickEvents()
    {
        frndrequestref.child(senderid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(recieverid))
                {
                    String requestType=snapshot.child(recieverid).child("Request_Type").getValue().toString();
                    if(requestType.equals("Sent"))
                    {
                        currentuserlog = "request_sent";
                        addfrnd.setText("Request Sent");
                    }
                    else if(requestType.equals("Recieved"))
                    {
                        currentuserlog="request_recieved";
                        addfrnd.setText("Accept");
                        decline_frnd.setVisibility(View.VISIBLE);
                        decline_frnd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelrequest();
                            }
                        });

                    }
                }
                else
                {
                    contactsref.child(senderid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if(snapshot.hasChild(recieverid))
                            {
                                currentuserlog="Friends";
                                addfrnd.setText("Remove Friend");
                            }
                            else
                            {
                                currentuserlog="new";
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d(errorTAG,"DataChanged "+error.getMessage());

            }
        });

       if(senderid.equals(recieverid))
           addfrnd.setVisibility(View.INVISIBLE);
       else
       {
           addfrnd.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if (currentuserlog.equals("new")) {
                       sendrequest();
                   }
                   if (currentuserlog.equals("request_sent")) {
                       cancelrequest();

                   }
                   if (currentuserlog.equals("request_recieved")) {
                       acceptrequest();
                   }
                   if (currentuserlog.equals("request_sent")) 
                   {
                       cancelrequest();

                   }
               }

           });
       }
       }


    private void acceptrequest()
    {
        contactsref.child(senderid).child(recieverid).child("Contacts").setValue("Saved_Contacts").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsref.child(recieverid).child(senderid).child("Contacts").setValue("Saved_Contacts").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                frndrequestref.child(senderid).child(recieverid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            frndrequestref.child(recieverid).child(senderid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    currentuserlog="Friends";
                                                    addfrnd.setText("Remove Contact");
                                                    decline_frnd.setVisibility(View.INVISIBLE);

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

    private void cancelrequest()
    {
        frndrequestref.child(senderid).child(recieverid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if(task.isSuccessful())
              {
                  frndrequestref.child(recieverid).child(senderid).child("Request_Type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          currentuserlog="new";
                          addfrnd.setText("Add Friend");

                      }
                  });
              }
            }
        });

    }

    private void sendrequest() {
        frndrequestref.child(senderid).child(recieverid).child("Request_Type").setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    frndrequestref.child(recieverid).child(senderid).child("Request_Type").setValue("Recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentuserlog = "request_sent";
                                addfrnd.setText("Request Sent");
                            }
                        }
                    });
                }

            }
        });

    }
}

