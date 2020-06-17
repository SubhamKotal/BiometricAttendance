package com.example.biometricattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class AddNewStudent extends AppCompatActivity {
    Button create,add_new,done;
    ImageButton front,right,left,front2;
    EditText roll,name_text;
    String dir_path="temp";
    String id="0000",name;
    LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_student);
        layout = (LinearLayout)findViewById(R.id.layout);
        create = (Button)findViewById(R.id.button);
        front = (ImageButton)findViewById(R.id.imageButton1);
        right = (ImageButton) findViewById(R.id.imageButton2);
        left = (ImageButton) findViewById(R.id.imageButton3);
        front2 = (ImageButton) findViewById(R.id.imageButton4);
        add_new = (Button)findViewById(R.id.add_new);
        done = (Button)findViewById(R.id.done);
        roll = (EditText)findViewById(R.id.editText2);
        name_text = (EditText)findViewById(R.id.name_text);

        add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),AddNewStudent.class);
                startActivity(intent);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=roll.getText().toString();
                name = name_text.getText().toString();

                if(!id.equals("")){
                    dir_path = Environment.getExternalStorageDirectory()
                            + "/Android/data/"
                            + getApplicationContext().getPackageName()
                            + "/Files/"+id;
                    File directory = new File(dir_path);
                    if (! directory.exists()){
                        Toast.makeText(AddNewStudent.this, "Creating Directory!!", Toast.LENGTH_SHORT).show();
                        directory.mkdirs();
                    }

                    // Creating name.txt
                    File nameFile;
                    nameFile = new File(dir_path + File.separator + name + ".txt");
                    try {
                        nameFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(AddNewStudent.this, "Error in creating name file: "+dir_path + File.separator + name + ".txt", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


//                    directory.mkdirs();
                    Toast.makeText(getApplicationContext(),"Student Added. Proceed to add photos",Toast.LENGTH_SHORT).show();

                    if(layout.getVisibility()==View.INVISIBLE){
                        layout.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Error Creating Student "+"id: "+id,Toast.LENGTH_SHORT).show();
                }
            }
        });

        front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=roll.getText().toString();
                if(!id.equals("")){
                Intent intent=new Intent(getApplicationContext(),FaceDetection.class);
                intent.putExtra("dir_path",dir_path);
                intent.putExtra("type","1");
                startActivity(intent);}
                else{
                    Toast.makeText(getApplicationContext(), "Please Enter a valid Student ID.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=roll.getText().toString();
                if(!id.equals("")){
                    Intent intent=new Intent(getApplicationContext(),FaceDetection.class);
                    intent.putExtra("dir_path",dir_path);
                    intent.putExtra("type","2");
                    startActivity(intent);}
                else{
                    Toast.makeText(getApplicationContext(), "Please Enter a valid Student ID.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=roll.getText().toString();
                if(!id.equals("")){
                    Intent intent=new Intent(getApplicationContext(),FaceDetection.class);
                    intent.putExtra("dir_path",dir_path);
                    intent.putExtra("type","3");
                    startActivity(intent);}
                else{
                    Toast.makeText(getApplicationContext(), "Please Enter a valid Student ID.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        front2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=roll.getText().toString();
                if(!id.equals("")){
                    Intent intent=new Intent(getApplicationContext(), FaceDetection.class);
                    intent.putExtra("dir_path",dir_path);
                    intent.putExtra("type","4");
                    startActivity(intent);}
                else{
                    Toast.makeText(getApplicationContext(), "Please Enter a valid Student ID.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}


// android:requestLegacyExternalStorage="true"