package com.example.biometricattendance;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;


import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
//import org.bytedeco.javacpp.freenect2;
import org.bytedeco.javacpp.opencv_core;
//import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
//import org.bytedeco.opencv.opencv_core.Mat;

//import org.opencv.android.Utils;
//import org.opencv.core.CvType;


import java.io.File;
import java.io.IOException;

//import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_PLAIN;
//import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
//import static org.bytedeco.javacpp.opencv_imgproc.resize;


public class StartBiometricAttendance extends AppCompatActivity {

    public static final String TAG = "FaceRecognizeActivity";
    opencv_face.FaceRecognizer faceRecognizer = opencv_face.FisherFaceRecognizer.create();
    private String[] nomes = {"", "Y Know You"};
    public static final double ACCEPT_LEVEL = 40.0D;

    private Button capture_button, btTrainModel;
    private Uri imageUri;
    private ImageView preview;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private Bitmap myBitmap, cropped_face;
    static  final int WIDTH= 128;
    static  final int HEIGHT= 128;;

    AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();

    TextView show_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Photos_Train_Qty in TrainHelper
        TrainHelper.setPhotosTrainQty(getApplicationContext());
        try {
            TrainHelper.train(getApplicationContext());
            //Toast.makeText(StartBiometricAttendance.this, "Face Recognition model ready!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(StartBiometricAttendance.this, "Error!! Could not start training the face recog model", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        setContentView(R.layout.activity_start_biometric_attendance);
        preview = (ImageView)findViewById(R.id.preview);
        capture_button = (Button)findViewById(R.id.capture_button);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraIntentForResult();
            }
        });

        show_name = (TextView)findViewById(R.id.textView);

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
            Toast.makeText(getApplicationContext(), "Opening Camera Intent", Toast.LENGTH_SHORT).show();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            //saving_flag=1;
            //draw_box_and_show(im_bitmap);
            //preview.setImageURI(imageUri);
            if(myBitmap!=null){
                recognize_face_and_show(myBitmap);
            }
            else{
                Toast.makeText(this, "Error Processing the image. Please try again.", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "BITMAP coversion did not work", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void recognize_face_and_show(Bitmap myBitmap){
        Bitmap recognized_face = recognizeFace(myBitmap);
//        if(recognized_face!=null){
//            ImageView imageView = (ImageView) findViewById(R.id.preview);
//            imageView.setImageDrawable(new BitmapDrawable(getResources(),recognized_face));
//        }
//        else {
//            Toast.makeText(this, "Face not recognized. Please try again.", Toast.LENGTH_SHORT).show();
//        }
        /*
        public class OpenCVFaceRecognizer {
            public static void main(String[] args) {
                String trainingDir = args[0];
                IplImage testImage = cvLoadImage(args[1]);

                File root = new File(trainingDir);

                FilenameFilter pngFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".png");
                    }
                };

                File[] imageFiles = root.listFiles(pngFilter);

                MatVector images = new MatVector(imageFiles.length);

                int[] labels = new int[imageFiles.length];

                int counter = 0;
                int label;

                IplImage img;
                IplImage grayImg;

                for (File image : imageFiles) {
                    // Get image and label:
                    img = cvLoadImage(image.getAbsolutePath());
                    label = Integer.parseInt(image.getName().split("\\-")[0]);
                    // Convert image to grayscale:
                    grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
                    //IplImage newImg = IplImage.cre
                    cvCvtColor(img, grayImg, CV_BGR2GRAY);
                    // Append it in the image list:
                    images.put(counter, grayImg);
                    // And in the labels list:
                    labels[counter] = label;
                    // Increase counter for next image:
                    counter++;
                }

                FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
                // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
                // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer()

                faceRecognizer.train(images, labels);

                // Load the test image:
                IplImage greyTestImage = IplImage.create(testImage.width(), testImage.height(), IPL_DEPTH_8U, 1);
                cvCvtColor(testImage, greyTestImage, CV_BGR2GRAY);

                // And get a prediction:
                int predictedLabel = faceRecognizer.predict(greyTestImage);
                System.out.println("Predicted label: " + predictedLabel);
            }
        }
        */

    }

    private Bitmap recognizeFace (Bitmap myBitmap){

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
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);


        if(faces.size() !=0){
            Face thisFace = faces.valueAt(0);

            cropped_face = Bitmap.createBitmap(myBitmap,(int)(thisFace.getPosition().x),(int)(thisFace.getPosition().y),(int)(thisFace.getWidth()),(int)(thisFace.getHeight()));
            cropped_face = Bitmap.createScaledBitmap(cropped_face, WIDTH, HEIGHT, false);

//            int posx = (int)thisFace.getPosition().x;
//            int posy = (int)thisFace.getPosition().y;
//            Bitmap finalBitmap = recognize(posx, posy, myBitmap, cropped_face);
            String finalId = recognize(cropped_face);
            String finalName = "";


            thisFace = faces.valueAt(0);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

            String detected_id = finalId.split(" -")[0];

            String dir_path = Environment.getExternalStorageDirectory()
                    + "/Android/data/"
                    + getApplicationContext().getPackageName() + "/Files/" + detected_id ;

            File file = new File(dir_path);
            File[] list = file.listFiles();

            if(list ==null){
                Toast.makeText(this, "Name not Found for: "+ dir_path, Toast.LENGTH_SHORT).show();
                return cropped_face;
            }
//            int count=0;
            for (File f:list){
                String file_name = f.getName();
                if(file_name.endsWith(".txt")){
                    finalName=file_name.split("\\.")[0];
                }
            }

            show_name.setText(finalName);
            ImageView imageView = (ImageView) findViewById(R.id.preview);
            imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
            return cropped_face;
        }
        else{
            Toast.makeText(this, "Unable to find any face", Toast.LENGTH_SHORT).show();
            return cropped_face;
        }

//        ImageView imageView = (ImageView) findViewById(R.id.preview);
//        imageView.setImageDrawable(new BitmapDrawable(getResources(),cropped_face));
    }


//    void train() {
//        Toast.makeText(getBaseContext(), "Start train: ", Toast.LENGTH_SHORT).show();
//    }


    ////////////////////////////////////////////////////////////////////////////////////// Recognition function
//    private void recognize(opencv_core.Rect dadosFace, opencv_core.Mat grayMat, opencv_core.Mat rgbaMat) {
//
//        opencv_core.Mat detectedFace = new opencv_core.Mat(grayMat, dadosFace);
//        resize(detectedFace, detectedFace, new Size(TrainHelper.IMG_SIZE,TrainHelper.IMG_SIZE));


    // To convert a Bitmap image from rgb to grayscale
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private boolean findRecogModel(){
        boolean trained;
        if(TrainHelper.isTrained()) {
            Toast.makeText(this, "Loading the trained Recognition model", Toast.LENGTH_SHORT).show();
//            File folder = new File(getFilesDir(), TrainHelper.TRAIN_FOLDER);
            File folder = new File(TrainHelper.TRAIN_FOLDER_PATH);
            File f = new File(folder, TrainHelper.FISHER_FACES_CLASSIFIER);
//                        faceRecognizer.load(f.getAbsolutePath());
            faceRecognizer.read(f.getAbsolutePath());
            trained = true;
        }
        else{
            Toast.makeText(this, "Please train the Recognition model first", Toast.LENGTH_SHORT).show();
            trained = false;
        }
        return trained;
    }

//    private String recognize(int face_x, int face_y, Bitmap myBitmap, Bitmap cropped_face) {
        private String recognize(Bitmap cropped_face) {

        /*  To convert "Bitmap" --> "opencv.Mat"
        opencv_core.Mat rgbaMat = new opencv_core.Mat(myBitmap.getWidth(), myBitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(myBitmap, rgbaMat);
        opencv_core.Mat detectedFace = new opencv_core.Mat(cropped_face.getWidth(), cropped_face.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(cropped_face, detectedFace);
        resize(detectedFace, detectedFace, new Size(TrainHelper.IMG_SIZE,TrainHelper.IMG_SIZE));
        */

//        // Set Photos_Train_Qty
//        TrainHelper.setPhotosTrainQty(getApplicationContext());

        // Check if a trained model exists
        boolean trained = findRecogModel();

        if(trained == false){
            return "Recognition Model Not found. CODE:5";
        }

        //To covert "Bitmap" --> "opencv.Mat" --> "javacpp.opencv_core.Mat"
        org.bytedeco.javacv.Frame frame1, frame2, frame3, frame4;


//        frame1 = converterToBitmap.convert(myBitmap);
//        final opencv_core.Mat mat1 = converterToMat.convert(frame1);
//        final opencv_core.Mat rgbaMat = new opencv_core.Mat((Pointer)null) { { address = mat1.address(); } };
        frame2 = converterToBitmap.convert(cropped_face);
        final opencv_core.Mat mat2 = converterToMat.convert(frame2);
        opencv_core.Mat detectedFace = new opencv_core.Mat((Pointer)null) { { address = mat2.address(); } };

        cvtColor(detectedFace, detectedFace, CV_BGR2GRAY);

        //        frame4 = converterToMat.convert(detectedFace);
//        Bitmap newBitmap = converterToBitmap.convert(frame4);
//        return newBitmap;

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        IntPointer label = new IntPointer(1);
        DoublePointer reliability = new DoublePointer(1);
        faceRecognizer.predict(detectedFace, label, reliability);
        int prediction = label.get(0);


        double acceptanceLevel = reliability.get(0);
        String name;
//        if (prediction == -1 || acceptanceLevel >= ACCEPT_LEVEL) {
        if (prediction == -1) {
            Toast.makeText(this, " Prediction is Negative 1", Toast.LENGTH_SHORT).show();
            name = getString(R.string.face_not_recognized);
        } else {
//            name = nomes[prediction] + " - " + acceptanceLevel;
            name = ""+ prediction + " - " + acceptanceLevel;
            /////Display label
            Toast.makeText(this, "Detected Face: "+ name, Toast.LENGTH_LONG).show();

        }
        return name;

        /*
        int x = face_x;
        int y = face_y;
        putText(rgbaMat, name, new opencv_core.Point(x, y), opencv_imgproc.CV_FONT_HERSHEY_PLAIN, 10, new opencv_core.Scalar(255,0,0,0));

        /////Display label
        Toast.makeText(this, "Detected Face: "+ name, Toast.LENGTH_LONG).show();


        opencv_core.Mat finalMat = new opencv_core.Mat((Pointer)null) { { address = rgbaMat.address(); } };
        frame3 = converterToMat.convert(finalMat);
        Bitmap finalBitmap = converterToBitmap.convert(frame3);
        return finalBitmap;

        */
    }

}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////