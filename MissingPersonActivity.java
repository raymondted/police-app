package com.project.raymond.reporttopolice;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class MissingPersonActivity extends AppCompatActivity {

    EditText mpName, mpAge, mpLastSeen, mpDetails;

    ImageView bPhoto;
    String selectedRadioBtn;
    RadioGroup radiogroup;
    RadioButton radio1,radio2;

    String name, age, gender, lastseen, details;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;

    private DatabaseReference databaseReferenceMP;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private String currentUID,downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person);

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        databaseReferenceMP = FirebaseDatabase.getInstance().getReference().child("Missing Persons").child(currentUID);
        storageReference = FirebaseStorage.getInstance().getReference().child("PersonsImages");

        radiogroup = findViewById(R.id.radioGroup);
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);
        mpName = findViewById(R.id.mp_name);
        mpAge = findViewById(R.id.mp_age);
        mpLastSeen = findViewById(R.id.mp_last_seen);
        mpDetails = findViewById(R.id.mp_details);
        bPhoto = findViewById(R.id.select_img);
        Button bRegister_mp = findViewById(R.id.bRegister_mp);

       bRegister_mp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AddMissingPerson();
               if (radio1.isChecked()){
                   selectedRadioBtn = radio1.getText().toString();
               }else if (radio2.isChecked()){
                   selectedRadioBtn = radio2.getText().toString();
               }
           }
       });
        bPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
    }


    private void AddMissingPerson(){
            String name = mpName.getText().toString().trim();
            String age = mpAge.getText().toString().trim();
            String lastseen = mpLastSeen.getText().toString().trim();
            String details = mpDetails.getText().toString().trim();

            if (TextUtils.isEmpty(name)){
                Toast.makeText(this, "Enter Missing Person's Name", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(age)){
                Toast.makeText(this, "Enter Missing Person's Age", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(lastseen)){
                Toast.makeText(this, "Enter Missing Person's Last Seen date", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(details)){
                Toast.makeText(this, "Please enter further details", Toast.LENGTH_LONG).show();
            }else {

                HashMap postData = new HashMap();
                postData.put("Name",name);
                postData.put("Age",age);
                postData.put("Last Seen",lastseen);
                postData.put("Details",details);

                databaseReferenceMP.updateChildren(postData).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete( Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MissingPersonActivity.this, "Missing Person Details Successfully Reported to Police", Toast.LENGTH_LONG).show();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(MissingPersonActivity.this, "Error occurred" + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
    }


    private void SelectImage() {

        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MissingPersonActivity.this);
        builder.setTitle("Upload Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }

                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }

            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null && data.getExtras() != null) {

            Bundle bundle = data.getExtras();
            Bitmap bmp = (Bitmap) bundle.get("data");
            bPhoto.setImageBitmap(bmp);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte [] byteArray = byteArrayOutputStream.toByteArray();


            final StorageReference filePath = storageReference.child(currentUID +".jpg");
            filePath.putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MissingPersonActivity.this, "Your report has been filed to the police", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                databaseReferenceMP.child("Image of Missing Person").setValue(downloadUrl);
                            }
                        });


        }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(MissingPersonActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    } }
            });
                    }


        else if(requestCode == SELECT_FILE && resultCode== RESULT_OK && data !=null) {
            Uri imageUrl = data.getData();
            bPhoto.setImageURI(imageUrl);

            final StorageReference filePath = storageReference.child(currentUID +".jpg");
            filePath.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MissingPersonActivity.this, "You have successfully sent report to the police", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                databaseReferenceMP.child("Image of Missing Person").setValue(downloadUrl);
                            }
                        });
                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(MissingPersonActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}



