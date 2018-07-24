package com.project.raymond.reporttopolice;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class CriminalActivity extends AppCompatActivity {

    EditText street_name, city_town_name, crime_details;
    Spinner spinner1;
    Button bSubmit;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private String currentUID, downloadUrl;

    private Uri fileUri;
    VideoView v;
    ImageView bPhoto;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    public static final int MEDIA_TYPE_VIDEO = 2;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    public static CriminalActivity ActivityContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criminal);

        ActivityContext = this;

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("crimes").child(currentUID);
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        street_name = findViewById(R.id.street_name);
        city_town_name = findViewById(R.id.city_town_name);
        crime_details = findViewById(R.id.crime_details);
        spinner1 = findViewById(R.id.spinner1);
        bSubmit = findViewById(R.id.bSubmit);
        bPhoto = findViewById(R.id.bPhoto);
        ImageView bPhoto = findViewById(R.id.bPhoto);
        v = findViewById(R.id.v);

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCriminal();


            }
        });

        bPhoto.setOnClickListener(new OnClickListener() {
                                      @Override
                                      public void onClick(View view) {

                                          SelectImage();
                                      }
                                  }
        );
    }

    public void dispatchTakeVideoIntent(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
    }

    private static Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraVideo");

        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }

        java.util.Date date = new java.util.Date();
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }


    private void AddCriminal() {

        String street = street_name.getText().toString().trim();
        String city = city_town_name.getText().toString().trim();
        String counties = spinner1.getSelectedItem().toString();
        String details = crime_details.getText().toString().trim();

        if (TextUtils.isEmpty(street)) {
            Toast.makeText(this, "Enter Street Name", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Enter City name", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(details)) {
            Toast.makeText(this, "Please enter further details", Toast.LENGTH_LONG).show();
        } else {

            HashMap postData = new HashMap();
            postData.put("street", street);
            postData.put("city", city);
            postData.put("county", counties);
            postData.put("details", details);

            dbRef.updateChildren(postData).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CriminalActivity.this, "You have successfully reported the incident to the police", Toast.LENGTH_LONG).show();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CriminalActivity.this, "Error occurred" + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void SelectImage() {

        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CriminalActivity.this);
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
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }

            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {

            Bundle bundle = data.getExtras();
            if (bundle != null) ;
            assert bundle != null;
            final Bitmap bmp = (Bitmap) bundle.get("data");
            bPhoto.setImageBitmap(bmp);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bmp != null;
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();


            storageReference = FirebaseStorage.getInstance().getReference().child(currentUID + ".jpg");

            final StorageReference filePath = storageReference.child(currentUID + ".jpg");
            filePath.putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CriminalActivity.this, "image successfully stored to firebase storage", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                dbRef.child("personsimage").setValue(downloadUrl);
                            }
                        });


                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CriminalActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (requestCode == SELECT_FILE && resultCode == RESULT_OK && data != null) {
            Uri imageUrl = data.getData();
            bPhoto.setImageURI(imageUrl);

            final StorageReference filePath = storageReference.child(currentUID + ".jpg");

            assert imageUrl != null;
            filePath.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CriminalActivity.this, "Data Successfully sent to the Police", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                dbRef.child("profileimage").setValue(downloadUrl);
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CriminalActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (requestCode == REQUEST_VIDEO_CAPTURE) {

            if (resultCode == RESULT_OK) {

                assert data != null;
                Uri fileUri = data.getData();
                v.setVideoURI(fileUri);

                Toast.makeText(this, "Video saved to: " + data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "User cancelled the video capture.",Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "Video capture failed.", Toast.LENGTH_LONG).show();
            }
        }
    }
}