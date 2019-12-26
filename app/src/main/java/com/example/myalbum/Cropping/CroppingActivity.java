package com.example.myalbum.Cropping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myalbum.DAO.DatabaseHandler;
import com.example.myalbum.DTOs.Image;
import com.example.myalbum.R;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;

public class CroppingActivity extends Activity {

    Context context;
    int IDAlbum;
    int IDImage;
    Image image;
    Uri imageUri;
    CropImageView mCropView;

    TextView option_free;
    TextView option_3_4;
    TextView option_4_3;
    TextView option_9_16;
    TextView option_16_9;
    TextView option_square;
    TextView option_circular;
    TextView option_fit;

    ImageButton saveButton;
    ImageButton cancelButton;
    ImageButton rotateButton;
    ImageButton rotateCounterButton;


    void findLayoutView()
    {
        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        option_free = (TextView) findViewById(R.id.option_free);
        option_3_4 = (TextView) findViewById(R.id.option_3_4);
        option_4_3 = (TextView) findViewById(R.id.option_4_3);
        option_9_16 = (TextView) findViewById(R.id.option_9_16);
        option_16_9 = (TextView) findViewById(R.id.option_16_9);
        option_square = (TextView) findViewById(R.id.option_square);
        option_circular = (TextView) findViewById(R.id.option_circular);
        option_fit = (TextView) findViewById(R.id.option_fit);

        saveButton = findViewById(R.id.buttonSave);
        cancelButton = findViewById(R.id.buttonCancel);
        rotateButton = findViewById(R.id.rotateClockwise);
        rotateCounterButton = findViewById(R.id.rotateCounterClockwise);
    }

    @Override
    public boolean onNavigateUp(){
        ReturnToViewImage();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropping);

        //get bundle sent from hom_activity
        Intent myCallerIntent = getIntent();
        Bundle myBundle = myCallerIntent.getExtras();
        IDAlbum = myBundle.getInt("IDAlbum");
        IDImage = myBundle.getInt("IDImage");
        context = this;

        //GET SELECTED IMAGE
        image = DatabaseHandler.getInstance(CroppingActivity.this).getImageAt(IDAlbum,IDImage);

        //MAKE URI
        File file = new File(image.getUrlHinh());
        imageUri = Uri.fromFile(file);

        findLayoutView();

        mCropView.startLoad(

                imageUri,

                new LoadCallback() {
                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onSuccess() {}
                });

        //SET LISTENER
        option_free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCropMode(view);
            }
        });

        LinearLayout layout = findViewById(R.id.cropMode);
        for (int i = 0; i < layout.getChildCount();i++)
        {
            View option = layout.getChildAt(i);
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setCropMode(view);
                }
            });
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnToViewImage();
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
            }
        });

        rotateCounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCropView.startCrop(

                        imageUri,

                        new CropCallback() {
                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onSuccess(Bitmap cropped) {}

                        },

                        new SaveCallback() {
                            @Override
                            public void onError(Throwable e) {

                            }
                            @Override
                            public void onSuccess(Uri outputUri) {
                                mCropView.startLoad(

                                        outputUri,

                                        new LoadCallback() {
                                            @Override
                                            public void onError(Throwable e) { }

                                            @Override
                                            public void onSuccess() {}
                                        });
                            }

                        }
                );
            }
        });
    }

    void setCropMode(View view) {

        switch (view.getId()) {
            case R.id.option_free: {
                mCropView.setCropMode(CropImageView.CropMode.FREE);
                break;
            }

            case R.id.option_3_4: {
                mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
                break;
            }

            case R.id.option_4_3: {
                mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
                break;
            }

            case R.id.option_9_16: {
                mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
                break;
            }


            case R.id.option_16_9: {
                mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
                break;
            }

            case R.id.option_square: {
                mCropView.setCropMode(CropImageView.CropMode.SQUARE);
                break;
            }

            case R.id.option_circular: {
                mCropView.setCropMode(CropImageView.CropMode.CIRCLE);
                break;
            }

            case R.id.option_fit: {
                mCropView.setCropMode(CropImageView.CropMode.FIT_IMAGE);
                break;
            }
        }
    }

    void ReturnToViewImage()
    {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
