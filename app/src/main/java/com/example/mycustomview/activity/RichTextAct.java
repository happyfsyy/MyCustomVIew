package com.example.mycustomview.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.viewgroup.RichTextEditor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RichTextAct extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE=1023;
    private static final int REQUEST_CODE_CAPTURE_CAMERA=1024;
    private RichTextEditor editor;
    private View btn1,btn2,btn3;
    private View.OnClickListener btnListener;
    private static final File PHOTO_DIR=new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera");
    private File mCurrentPhotoFile;//照相机拍照得到的图片

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_text);
        editor=findViewById(R.id.richEditor);
        btnListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.hideKeyBoard();
                if(v.getId()==btn1.getId()){
                    if(ContextCompat.checkSelfPermission(RichTextAct.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            !=PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(RichTextAct.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_PICK_IMAGE);
                    }else{
                        pickPicture();
                    }
                }else if(v.getId()==btn2.getId()){
                    if(ContextCompat.checkSelfPermission(RichTextAct.this,Manifest.permission.CAMERA)
                            !=PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(RichTextAct.this,new String[]{Manifest.permission.CAMERA},REQUEST_CODE_CAPTURE_CAMERA);
                    }else{
                        takePhoto();
                    }
                }else if(v.getId()==btn3.getId()){

                }
            }
        };
        btn1=findViewById(R.id.button1);
        btn2=findViewById(R.id.button2);
        btn3=findViewById(R.id.button3);
        btn1.setOnClickListener(btnListener);
        btn2.setOnClickListener(btnListener);
        btn3.setOnClickListener(btnListener);
    }

    private void pickPicture(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_CODE_PICK_IMAGE);
    }
    private void takePhoto(){
        try{
//            PHOTO_DIR.mkdirs();
            File dir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            mCurrentPhotoFile=new File(dir,getPhotoFilename());
            Intent intent=getTakePhotoIntent(mCurrentPhotoFile);
            startActivityForResult(intent,REQUEST_CODE_CAPTURE_CAMERA);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private Intent getTakePhotoIntent(File f){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE,null);
        LogUtil.e(getPackageName());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f));
        return intent;
    }
    private String getPhotoFilename(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat=new SimpleDateFormat("'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date)+".jpg";
    }
    public String getRealFilePath(final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode!=RESULT_OK){
            return;
        }
        if(requestCode==REQUEST_CODE_PICK_IMAGE){
            Uri uri=data.getData();
            editor.insertImage(getRealFilePath(uri));
        }else if(requestCode==REQUEST_CODE_CAPTURE_CAMERA){
            editor.insertImage(mCurrentPhotoFile.getAbsolutePath());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_CODE_PICK_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPicture();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_CAPTURE_CAMERA:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }
}
