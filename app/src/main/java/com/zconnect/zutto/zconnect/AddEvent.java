package com.zconnect.zutto.zconnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class AddEvent extends AppCompatActivity {

    String eventDate;
    String dateString;
    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST = 7;
    private ImageButton mAddImage;
    private android.support.design.widget.TextInputEditText mEventName;
    private android.support.design.widget.TextInputEditText mEventDescription;
    private Button mPostBtn;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FrameLayout CalendarButton;
    private ProgressDialog mProgress;
    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {
            eventDate = date.toString();
            Toast.makeText(AddEvent.this, eventDate, Toast.LENGTH_SHORT).show();
            dateString = date.toString();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mAddImage = (ImageButton) findViewById(R.id.imageButton);
        mEventName = (TextInputEditText) findViewById(R.id.name);
        mEventDescription = (TextInputEditText) findViewById(R.id.description);
        mPostBtn = (Button) findViewById(R.id.post);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ZConnect/Event/Posts");

        CalendarButton = (FrameLayout)findViewById(R.id.dateAndTime);

        mProgress = new ProgressDialog(this);

        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        CalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startPosting();
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("JccJc");
        mProgress.show();
        final String eventNameValue = mEventName.getText().toString().trim();
        final String eventDescriptionValue = mEventDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(eventNameValue) && !TextUtils.isEmpty(eventDescriptionValue) && mImageUri != null) {
            //1
            StorageReference filepath = mStorage.child("EventImage").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    DatabaseReference newPost = mDatabase.push();
                    String key = newPost.getKey();
                    newPost.child("Key").setValue(key);
                    newPost.child("EventName").setValue(eventNameValue);
                    newPost.child("EventDescription").setValue(eventDescriptionValue);
                    newPost.child("EventImage").setValue(downloadUri.toString());
                    newPost.child("EventDate").setValue(eventDate);
                    newPost.child("FormatDate").setValue(dateString);


                    mProgress.dismiss();
                    startActivity(new Intent(AddEvent.this, AllEvents.class));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();// takes image that the user added
            mAddImage.setImageURI(mImageUri);//sets the image to mAddImage button

        }
    }
}
