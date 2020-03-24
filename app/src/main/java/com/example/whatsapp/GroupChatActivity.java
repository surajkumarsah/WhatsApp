package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName;

    private DatabaseReference groupRef;
    private ImageView image;

    private static final int GalleryPick = 1;
    private Uri ImageUri;
    String downloadUrl;

    private StorageReference GalleryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        groupRef = FirebaseDatabase.getInstance().getReference();
        GalleryRef = FirebaseStorage.getInstance().getReference().child("GroupImage").child(currentGroupName);

        initializeFields();
    }

    private void initializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        mScrollView = (ScrollView) findViewById(R.id.my_scrool_view);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.change_grp_name)
        {
            changeGroupName();
        }
        if (item.getItemId() == R.id.set_grp_img)
        {
            addGroupImage();
        }
        if (item.getItemId() == R.id.add_person)
        {

        }
        if (item.getItemId() == R.id.del_group)
        {

        }


        return true;
    }

    private void addGroupImage()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
        builder.setTitle("Select Image : ");

        image = new ImageView(GroupChatActivity.this);
        image.setImageResource(R.drawable.ic_person_black_24dp);
        builder.setView(image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final StorageReference filePath = GalleryRef.child(ImageUri.getLastPathSegment() + ".jpg");
                final UploadTask uploadTask = filePath.putFile(ImageUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        Toast.makeText(GroupChatActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(GroupChatActivity.this, "Image Uploaded Successfully.", Toast.LENGTH_SHORT).show();

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (!task.isSuccessful())
                                {
                                    throw task.getException();
                                }

                                downloadUrl = filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful())
                                {
                                    downloadUrl = task.getResult().toString();
                                    Toast.makeText(GroupChatActivity.this, "got image URL Successfully.", Toast.LENGTH_SHORT).show();
                                    saveDataToDB();
                                }
                            }
                        });
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();

    }

    private void saveDataToDB()
    {
        groupRef.child("Groups").child(currentGroupName).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.exists()))
                {
                    HashMap<String, Object> groupDetails = new HashMap<>();

                    groupDetails.put("imageUrl", downloadUrl);

                    groupRef.child("Groups").child(currentGroupName).child("image").updateChildren(groupDetails)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(GroupChatActivity.this, "Successfully Updated.", Toast.LENGTH_SHORT).show();

                                        sendToGroupChatActivity();
                                    }
                                    else
                                    {
                                        String error = task.getException().toString();
                                        Toast.makeText(GroupChatActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToGroupChatActivity()
    {
        Intent intent = new Intent(GroupChatActivity.this, GroupChatActivity.class);
        intent.putExtra("groupName", currentGroupName);
        startActivity(intent);
        finish();
    }

    private void openGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            ImageUri = data.getData();
            image.setImageURI(ImageUri);
        }
    }

    private void changeGroupName() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupChatActivity.this);
        builder1.setTitle("Enter Group Name : ");

        final EditText groupName = new EditText(GroupChatActivity.this);
        groupName.setHint("e.g : funn club");
        builder1.setView(groupName);

        builder1.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupNameText = groupName.getText().toString();

                if (TextUtils.isEmpty(groupNameText)) {
                    Toast.makeText(GroupChatActivity.this, "Please, Write Group Name", Toast.LENGTH_SHORT).show();
                } else {
                    changeGroupName1(groupNameText);
                }
            }
        });

        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder1.show();
    }

    private void changeGroupName1(final String groupName)
    {
        Query searchGroup = groupRef.child("Groups").equalTo(currentGroupName);

        groupRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(GroupChatActivity.this, groupName + "Created Successfully.", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }
}
