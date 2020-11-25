package com.example.livevideocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class VideoCall extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY="47002284";
    private static String SESSION_ID="2_MX40NzAwMjI4NH5-MTYwNjI0MDI5OTM1NX53ZndsTDRHSFlwNWQxb2RqbTYzUi91WS9-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00NzAwMjI4NCZzaWc9MmRiN2FiNDcxYjI3NDI0NDJiMTZlYzA0YjZhMmIxYmFiYTExMTNmZjpzZXNzaW9uX2lkPTJfTVg0ME56QXdNakk0Tkg1LU1UWXdOakkwTURJNU9UTTFOWDUzWm5kc1REUkhTRmx3TldReGIyUnFiVFl6VWk5MVdTOS1mZyZjcmVhdGVfdGltZT0xNjA2MjQwNDMzJm5vbmNlPTAuMTA4OTcwODM2Njg0MzMxMTYmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYwODgzMjQzMSZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG=VideoCall.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ImageView cancel_call_btn;
    private FrameLayout mPublisherView;
    private FrameLayout mSubscriberView;

    private DatabaseReference userref;
    private String userID="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        cancel_call_btn=findViewById(R.id.call_cancel);

        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        userref= FirebaseDatabase.getInstance().getReference().child("Users");

        cancel_call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.child(userID).hasChild("Incoming"))
                        {
                            userref.child(userID).child("Incoming").removeValue();

                            if(mPublisher!=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                            }
                        }

                        if(snapshot.child(userID).hasChild("Outgoing"))
                        {
                            userref.child(userID).child("Outgoing").removeValue();

                            if(mPublisher!=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                            }

                        }
                        startActivity(new Intent(VideoCall.this,Registration_Activity.class));
                        finish();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        requestPermission();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoCall.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission()
    {
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this,perms))
        {
            //Initialize The Stream
           mPublisherView=findViewById(R.id.jisne_call_kiya_hai_container);
           mSubscriberView=findViewById(R.id.jisko_call_kiya_hai_container);

           mSession=new Session.Builder(this,API_KEY,SESSION_ID).build();
           mSession.setSessionListener(VideoCall.this);
           mSession.connect(TOKEN);

        }
        else
        {
            EasyPermissions.requestPermissions(this,"MIC and Camera Permissions Required to Access the App",RC_VIDEO_APP_PERM,perms);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
    //Publish The Stream to the Session
        Log.d(LOG_TAG,"Session Connected");
        mPublisher=new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCall.this);

        mPublisherView.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);


    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        //Receiver Receiving the Stream
        Log.d(LOG_TAG,"Session Received");

        if(mSubscriber==null)
        {
            mSubscriber=new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberView.addView(mSubscriber.getView());
            cancel_call_btn.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(LOG_TAG,"Session Dropped");

        if(mSubscriber!=null)
        {
            mSubscriber=null;
            mSubscriberView.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
