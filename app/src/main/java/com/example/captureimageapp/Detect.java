package com.example.captureimageapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.controls.templates.ThumbnailTemplate;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.captureimageapp.R;
import com.example.captureimageapp.ml.ConvertedModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Detect extends AppCompatActivity {
    int SELECT_PICTURE = 200;
    ImageView IVPreviewImage;
   Button BCameraOpen;
   TextView result;
   int imageSize = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        // One Button
        Button BSelectImage;

        // One Preview Image
        BSelectImage = findViewById(R.id.mGalleryButton);
        IVPreviewImage = findViewById(R.id.mPhotoImageView);
        BCameraOpen=findViewById(R.id.mCameraButton);
        result = findViewById(R.id.mResultTextView);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        BCameraOpen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // check if the camera permission is granted
                if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 3);
            }else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }}
        });


    }
        void imageChooser() {

            // create an instance of the
            // intent of the type image
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);

            // pass the constant to compare it
            // with the returned requestCode
            startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
        }

    public void classifyImage(Bitmap image){
        try {
            ConvertedModel model = ConvertedModel.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer= ByteBuffer.allocateDirect(4 * imageSize * imageSize *3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues, 0,image.getWidth(),0, 0,image.getWidth(), image.getHeight());
           int pixel=0;
            for (int i =0; i<imageSize; i++){
                for (int j= 0; j< imageSize; j++){
                    int val=intValues[pixel++];//RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f/1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ConvertedModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();
            int maxpos =0;
            float maxConfidence = 0;
            for (int i=0; i<confidence.length; i++){
                if (confidence[i]> maxConfidence){
                    maxConfidence = confidence[i];
                    maxpos= i;
                }
            }
            String[] classes={"berry_blotch","berry_disease","die_back","Healthy","Unknown"};
            result.setText( classes[maxpos]);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
//                Uri captureImage = data.getData();
//                if (null != captureImage) {
//                    // update the preview image in the layout
//                    IVPreviewImage.setImageURI(captureImage);
//                    captureImage = Bitmap.createScaledBitmap(captureImage, 128,128,false);
//                    classifyImage(captureImage);
                Uri dat= data.getData();
                Bitmap image= null;
                try {
                    image= MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                }catch (IOException e){
                    e.printStackTrace();
                }
                IVPreviewImage.setImageBitmap(image);
               image = Bitmap.createScaledBitmap(image, 128,128,false);
                classifyImage(image);
                }



        else{
            Bitmap captureImage= (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(captureImage.getWidth(), captureImage.getHeight());
            captureImage= ThumbnailUtils.extractThumbnail(captureImage, dimension, dimension);
            //set capture image to imageview
            IVPreviewImage.setImageBitmap(captureImage);
            captureImage = Bitmap.createScaledBitmap(captureImage, 128,128,false);
            classifyImage(captureImage);
        }
    }}}










