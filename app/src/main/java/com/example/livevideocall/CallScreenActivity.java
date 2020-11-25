
package com.example.livevideocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallScreenActivity extends AppCompatActivity {

    private TextView username;
    private ImageView profile_image;
    private ImageView cancel_call,accept_call;
    private String rec_userid="";
    private String called_username="";
    private String called_profile_pic="";
    private String caller_userid="",caller_profile_pic="",caller_username="",detect_touch="",outgoingId="",incomingID="";
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        caller_userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        rec_userid=getIntent().getExtras().get("visiter_id").toString();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        username=findViewById(R.id.called_username);
        profile_image=findViewById(R.id.called_profile_pic);
        cancel_call=findViewById(R.id.decline_call);
        accept_call=findViewById(R.id.make_call);

        cancel_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detect_touch="Touched";
                
                cancelCall();
            }
        });

        accept_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final HashMap<String,Object> callPickMap=new HashMap<>();
                callPickMap.put("picked","picked");

                userRef.child(caller_userid).child("Incoming").updateChildren(callPickMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Intent intent=new Intent(CallScreenActivity.this,VideoCall.class);
                            startActivity(intent);
                        }
                    }
                });

            }
        });

        setProfileInfo();
    }

    private void cancelCall() {
        //Caller_Side//
        userRef.child(caller_userid).child("Outgoing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("HasCalled"))
                {
                    outgoingId=snapshot.child("HasCalled").getValue().toString();

                    userRef.child(outgoingId).child("Incoming").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                userRef.child(caller_userid).child("Outgoing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        startActivity(new Intent(CallScreenActivity.this,Registration_Activity.class));
                                        finish();
                                        
                                    }
                                });
                            }

                        }
                    });
                }
                else
                {
                    startActivity(new Intent(CallScreenActivity.this,Registration_Activity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Reciever Side//
        userRef.child(caller_userid).child("Incoming").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("HasCalled"))
                {
                    incomingID=snapshot.child("IsCalling").getValue().toString();

                    userRef.child(incomingID).child("Outgoing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                userRef.child(caller_userid).child("Incoming").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        startActivity(new Intent(CallScreenActivity.this,Registration_Activity.class));
                                        finish();

                                    }
                                });
                            }

                        }
                    });
                }
                else
                {
                    startActivity(new Intent(CallScreenActivity.this,Registration_Activity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(rec_userid).exists())
                {
                    called_profile_pic=snapshot.child(rec_userid).child("Profile_Pic").getValue().toString();
                    called_username=snapshot.child(rec_userid).child("UserName").getValue().toString();

                    username.setText(called_username);
                    Picasso.get().load(called_profile_pic).into(profile_image);
                }
                if(snapshot.child(caller_userid).exists())
                {
                    caller_profile_pic=snapshot.child(caller_userid).child("Profile_Pic").getValue().toString();
                    caller_username=snapshot.child(caller_userid).child("UserName").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    protected void onStart() {

        super.onStart();

        userRef.child(rec_userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!detect_touch.equals("Touched") && !snapshot.hasChild("Outgoing") && !snapshot.hasChild("Incoming"))
                {
                    final HashMap<String,Object> OutgoingInfo=new HashMap<>();
                    OutgoingInfo.put("HasCalled",rec_userid);

                    userRef.child(caller_userid).child("Outgoing")
                            .updateChildren(OutgoingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                final HashMap<String,Object> IncomingInfo=new HashMap<>();
                                IncomingInfo.put("IsCalling",caller_userid);

                                userRef.child(rec_userid).child("Incoming").updateChildren(IncomingInfo);
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(caller_userid).hasChild("Incoming") && !snapshot.child(caller_userid).hasChild("Outgoing"))
                {
                    accept_call.setVisibility(View.VISIBLE);
                }

                if(snapshot.child(rec_userid).child("Incoming").hasChild("picked"))
                {
                    Intent intent=new Intent(CallScreenActivity.this,VideoCall.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
