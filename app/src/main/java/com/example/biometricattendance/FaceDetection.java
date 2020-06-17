package com.example.biometricattendance;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class FaceDetection extends AppCompatActivity {
    Intent intent;
    String dir_path;
    String type;
    static  final int WIDTH= 128;
    static  final int HEIGHT= 128;;
//    Bundle extras = getIntent().getExtras();
//    String dir_path,type;
//    if (extras != null){
//        dir_path = extras.getString("dir_path");
//        type = extras.getString("type");
//    }

    private Uri imageUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;
    Button button, submit_face;
    private ImageView preview;
    private Bitmap cropped_face;
    private int saving_flag= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        /////////////
        intent = getIntent();
        dir_path = intent.getStringExtra("dir_path");
        type = intent.getStringExtra("type");
        ///////////////
        button = (Button) findViewById(R.id.button);
        preview = findViewById(R.id.preview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
//                inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
                popup.inflate(R.menu.camera_button_menu);
//                MenuItem item = (MenuItem) popup.getMenu();
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.take_photo_using_camera:
                                startCameraIntentForResult();
                                return true;

                            case R.id.select_images_from_local:
                                startChooseImageIntentForResult();

                                return true;

                            default:
                                return false;
                        }

                    }

                });
            }
        });
//        button = (Button)findViewById(R.id.button);
        //        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startChooseImageIntentForResult();
//            }
//        });

        submit_face=(Button)findViewById(R.id.submit_face);
        submit_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saving_flag == 1){
                    storeImage(cropped_face);
                    Toast.makeText(FaceDetection.this, "Your face has been saved.", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Image not selected. Please select an image first.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable=true;



        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.c,
                options);

        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);



        FaceDetector faceDetector = new
                FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();
        if(!faceDetector.isOperational()){
//            new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
            Toast.makeText(getApplicationContext(),"Could not set up the face detector!",Toast.LENGTH_LONG).show();
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }
        preview.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    private void startCameraIntentForResult() {
        // Clean up last time's image
        imageUri = null;
        preview.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            Toast.makeText(getApplicationContext(), "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", Toast.LENGTH_SHORT).show();
        }
    }


    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap im_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                saving_flag=1;
                draw_box_and_show(im_bitmap);
                File image = new File(String.valueOf(imageUri));
                image.delete();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "BITMAP coversion did not work", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.getData();
            try {
                Bitmap im_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                saving_flag=1;
                draw_box_and_show(im_bitmap);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "BITMAP coversion did not work", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            saving_flag=1;
//            detect_output = (Task<List<Face>>) imageProcessor.getOutput();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
//            detect_output = (Task<List<Face>>) imageProcessor.getOutput();
            Toast.makeText(getApplicationContext(), "Request CODE not as expected", Toast.LENGTH_SHORT).show();
        }

        //////////////////////////////////////////////////////
        /*
        Uri selectedImage = data.getData();
        //ImageView imageView = (ImageView) findViewById(R.id.preview);+
        //imageView.setImageURI(selectedImage);
        try {
            Bitmap im_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            saving_flag=1;
            draw_box_and_show(im_bitmap);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "BITMAP coversion did not work", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        */
        ///////////////////////////////////////////////////
    }

    private void draw_box_and_show(Bitmap myBitmap){

        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);



        FaceDetector faceDetector = new
                FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();
        if(!faceDetector.isOperational()){
//            new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
            Toast.makeText(getApplicationContext(),"Could not set up the face detector!",Toast.LENGTH_LONG).show();
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }

        if(faces.size() !=0){
            Face thisFace = faces.valueAt(0);

            cropped_face=Bitmap.createBitmap(myBitmap,(int)(thisFace.getPosition().x),(int)(thisFace.getPosition().y),(int)(thisFace.getWidth()),(int)(thisFace.getHeight()));

        }
        else{
            cropped_face = BitmapFactory.decodeResource(
                    getApplicationContext().getResources(),
                    R.drawable.c);
            Toast.makeText(this, "Unable to find any face", Toast.LENGTH_SHORT).show();
        }


        ImageView imageView = (ImageView) findViewById(R.id.preview);
        imageView.setImageDrawable(new BitmapDrawable(getResources(),cropped_face));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(dir_path);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.


        // Create a media file name
//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String[] t = dir_path.split("/");
        String id = t[t.length-1];
        String mImageName = "_"+
                type+".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + id + mImageName);
        return mediaFile;
    }

    private File getTrainingDir(){

        String[] t = dir_path.split("/");
        String id = t[t.length-1];
        String training_path = dir_path = Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Training";

        File directory = new File(training_path);
        if (! directory.exists()){
            directory.mkdirs();
        }

        File mediaStorageDir = new File(training_path);
        File mediaFile;

        String mImageName = "_"+
                type+".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + id + mImageName);
        return mediaFile;

    }


    private void storeImage(Bitmap image) {
        image = Bitmap.createScaledBitmap(image, WIDTH, HEIGHT, false);
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Toast.makeText(getApplicationContext(),"Error creating media file, check storage permissions: ",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(),"File not found: ",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Error accessing file: ",Toast.LENGTH_SHORT).show();
        }

        //////////////

        File trainingFile = getTrainingDir();
        if (trainingFile == null) {
            Toast.makeText(getApplicationContext(),"Error creating media file, check storage permissions: ",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(trainingFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(),"File not found: ",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Error accessing file: ",Toast.LENGTH_SHORT).show();
        }
    }
}
