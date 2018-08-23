package com.project.raymond.reporttopolice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CriminalActivity extends AppCompatActivity implements LocationListener {

    EditText crime_details;
    Button bSubmit, getLocationBtn;
    TextView locationText, txtProgress;
    ProgressBar progressBar;

    LocationManager locationManager;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private String currentUID, downloadUrl;

    ImageView bPhoto;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;

    private Uri videoUri;
    VideoView videoUpload;
    StorageReference videoRef;
    private static final int REQUEST_CODE = 101;

    Date Date = new Date();
    String dateString = Date.toString();

    int upload_amount=0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criminal);
        getLocation();


        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("CRIMES").child(currentUID).child(dateString);
        storageReference = FirebaseStorage.getInstance().getReference();
        videoRef = storageReference.child("/user/" + currentUID + "/crimevideos/"+dateString+".3gp");

        crime_details = findViewById(R.id.crime_details);
        bSubmit = findViewById(R.id.bSubmit);
        bPhoto = findViewById(R.id.bPhoto);
        getLocationBtn = findViewById(R.id.getLocationBtn);
        locationText = findViewById(R.id.locationText);
        locationText.setText(" ");
        ImageView bPhoto = findViewById(R.id.bPhoto);
        videoUpload = findViewById(R.id.videoUpload);
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        txtProgress.setVisibility(GONE);

        progressBar.setVisibility(GONE);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo(v);


            }
        });

        bPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                SelectImage();
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationText.setText(locationText.getText() + ", " + addresses.get(0).getFeatureName() + ". ");
            locationText.setText(locationText.getText() + ", " + addresses.get(0).getSubLocality() + ". ");
            locationText.setText(locationText.getText() + ", " + addresses.get(0).getLocality() + ". ");
        } catch (Exception e) {

        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private void AddCriminal() {

        String location = locationText.getText().toString().trim();
        String details = crime_details.getText().toString().trim();


        if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please tap on GET LOCATION button", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(details)) {
            Toast.makeText(this, "Please enter further details", Toast.LENGTH_LONG).show();
        }
        else {
            progressBar.setVisibility(VISIBLE);

            HashMap postData = new HashMap();
            postData.put("location", location);
            postData.put("details", details);


            dbRef.updateChildren(postData).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CriminalActivity.this, "You have successfully reported the incident to the police", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CriminalActivity.this, MainActivity.class);
                        startActivity(intent);
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
            upload_amount++;


            storageReference = FirebaseStorage.getInstance().getReference().child("/user/" + currentUID + "/crimeimages/");

            final StorageReference filePath = storageReference.child(dateString + ".jpg");
            filePath.putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                dbRef.child("Crime_Image").setValue(downloadUrl);
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
            upload_amount++;

            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("/user/" + currentUID + "/crimeimages/");

            assert imageUrl != null;
            filePath.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                dbRef.child("crime_image").setValue(downloadUrl);
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CriminalActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                upload_amount++;

                videoUri = data.getData();
                Toast.makeText(this, "Video saved to: \n" + videoUri, Toast.LENGTH_LONG).show();
                videoUpload.setVideoURI(videoUri);
                MediaController mediaController = new MediaController(this);
                videoUpload.setMediaController(mediaController);
                videoUpload.start();
                videoUpload.canPause();
                videoUpload.canSeekForward();
                videoUpload.canSeekBackward();

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Video recording has been cancelled.", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "Video capture failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Uploading video
    public void uploadVideo(final View view) {
        if (upload_amount>0) {
            if (videoUri != null) {
                final UploadTask uploadTask = videoRef.putFile(videoUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CriminalActivity.this,
                                "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String videoUrl = uploadTask.getResult().getStorage().getDownloadUrl().toString();
                                dbRef.child("crime_video").setValue(videoUrl);
                                Toast.makeText(CriminalActivity.this, "Upload complete",
                                        Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(GONE);
                            }
                        }).addOnProgressListener(
                        new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                updateProgress(taskSnapshot);
                            }
                        });
            }
            AddCriminal();
        }else {
            Toast.makeText(CriminalActivity.this, "You must either upload a photo or video or both.",Toast.LENGTH_LONG).show();
        }}

    public void updateProgress(UploadTask.TaskSnapshot taskSnapshot) {

        @SuppressWarnings("VisibleForTests") long fileSize =
                taskSnapshot.getTotalByteCount();

        @SuppressWarnings("VisibleForTests")
        long uploadBytes = taskSnapshot.getBytesTransferred();

        long progress = (100 * uploadBytes) / fileSize;
        progressBar.setProgress((int) progress);
        progressBar.setVisibility(VISIBLE);
        txtProgress.setVisibility(VISIBLE);
        txtProgress.setText("Uploading " + progress + "%");
        if (progress == 100) {
            txtProgress.setText("Uploaded " + progress + "%");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(CriminalActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.privacy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://raymondted.000webhostapp.com/privacy.html")));
                break;

            case R.id.help:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://raymondted.000webhostapp.com/privacy.html")));
                break;

            case R.id.Logout:
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                    Intent intent = new Intent(CriminalActivity.this, LoginActivity.class);
                    startActivity(intent);
                } }
        return true;
        }

    public void dispatchTakeVideoIntent(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, REQUEST_CODE);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do you want to exit this page?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(CriminalActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();}
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }}