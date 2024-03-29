package com.zconnect.zutto.zconnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class setDetails extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageButton userProfile;
    private EditText userName;
    private Button submit;
    private Uri mImageUri=null;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfile;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_details);
        String call = getIntent().getStringExtra("caller");
        Toast.makeText(this,call,Toast.LENGTH_LONG).show();

        userProfile = (ImageButton) findViewById(R.id.profileImage);
        userName = (EditText) findViewById(R.id.username);
        submit = (Button) findViewById(R.id.submitDetails);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageProfile = FirebaseStorage.getInstance().getReference().child("Profile");
        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAccountSetup();
            }
        });

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
    }


    private void startAccountSetup() {
        final String username = userName.getText().toString().trim();
        final String userId = mAuth.getCurrentUser().getUid();
        final String userEmail = mAuth.getCurrentUser().getEmail();

        mProgress.setMessage("Setting Account..");
        if (!TextUtils.isEmpty(username) && mImageUri!=null)
        {
            mProgress.show();
            StorageReference filePath = mStorageProfile.child(mImageUri.getLastPathSegment() + mAuth.getCurrentUser().getUid());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    DatabaseReference currentUser = mDatabaseUsers.child(userId);
                    currentUser.child("Username").setValue(username);
                    currentUser.child("Email").setValue(userEmail);
                    currentUser.child("Image").setValue(downloadUri);

                    mProgress.dismiss();

                    Intent setDetailsIntent = new Intent(setDetails.this, home.class);
                    setDetailsIntent.putExtra("type","new");
                    setDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setDetailsIntent);
                }
            });
        } else {

            String message;
            if(mImageUri==null)
            {
                message = "Please select image";
            }
            else
            {
                message="Enter all fields";
            }
            Snackbar snack = Snackbar.make(userName, message, Snackbar.LENGTH_LONG);
            TextView snackBarText = (TextView) snack.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackBarText.setTextColor(Color.WHITE);
            snack.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.teal900));
            snack.show();
            // Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri= result.getUri();
                userProfile.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
