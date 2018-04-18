package com.yhz.study.characterdrawing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    private static final int CHOOSE_IMAGE = 949;
    private TextView mTv;
    private boolean hasChoose = false;
    private String picPath="";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String CHARACTER = "/\\|()1{}$@B%8&WM#ZO0QLCJUYX*hkbdpqwmoahkbdpqwmzcvunxrjft[]?-_+~<>i!lI;:,\"^`'. ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.text);
        mTv.setTypeface(Typeface.MONOSPACE);//等宽字体
        displayChars();
        mTv.setOnLongClickListener(this);
    }
    private void displayChars(){
        mTv.setText(getChars(getRatio()));
    }
    private int getRatio(){
        int ratio=1;
        int fw = getFontWidth();
        int sw = getScreenWidth();
        int pw = getPicWidth();
        while((pw/ratio)*fw>sw) ratio++;
        Log.d("test", "getRatio: "+ratio+":"+fw+":"+sw+":"+pw);
        return ratio;

    }
    private int getPicWidth(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if(!hasChoose)
            BitmapFactory.decodeResource(getResources(),R.mipmap.change,options);
        else BitmapFactory.decodeFile(picPath,options);
        return options.outWidth;
    }
    private int getFontWidth(){
        mTv.setText(" ");//设置一个空格 用于测算一个空格的宽度
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mTv.measure(spec, spec);
        if(hasChoose) return mTv.getMeasuredWidth();
        return mTv.getMeasuredWidth()*3;
    }
    private int getScreenWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
    /**
     *
     * @param ratio 图片缩放比例 如 ratio=2 图片大小为原来的1/2；
     * @return 图片对应的字符串
     */
    private String getChars(int ratio){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ratio;
        Bitmap bitmap;
        if(!hasChoose)
            bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.change,options);
        else bitmap =BitmapFactory.decodeFile(picPath,options);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        StringBuilder stringBuilder = new StringBuilder();
        int[] pixels = new int[width * height+1];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int i=0;i<pixels.length;i++){
            int c =pixels[i];
            int r = Color.red(c);
            int g = Color.green(c);
            int b = Color.blue(c);
            stringBuilder.append(getChar(r,g,b));
            if(i!=0&&i%width==0) stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    private char getChar(int r,int g,int b){
        int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
        int unit = (256)/ CHARACTER.length();
        int index = gray/unit;
        if (index>= CHARACTER.length()) index = CHARACTER.length()-1;
        return CHARACTER.charAt(index);
    }


    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.text:
                if(checkPermission()) choosePic();
        }
        return false;
    }



    private void choosePic() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent,CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_IMAGE:
                if(resultCode==RESULT_OK&&null!=data.getData()){
                    String path = data.getData().getLastPathSegment();
                    Log.d("test", "onActivityResult: "+path);
                    if(null!=path){
                        hasChoose=true;
                        picPath = path;
                        displayChars();
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.text_size_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_1sp:
                mTv.setTextSize(COMPLEX_UNIT_SP,1);
                break;
            case R.id.option_2sp:
                mTv.setTextSize(COMPLEX_UNIT_SP,2);
                break;
            case R.id.option_3sp:
                mTv.setTextSize(COMPLEX_UNIT_SP,3);
                break;
            case R.id.option_4sp:
                mTv.setTextSize(COMPLEX_UNIT_SP,4);
                break;
        }
        displayChars();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                choosePic();
            }else{
                Toast.makeText(this,"未拥有权限",Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }else return true;
        }else return true;
        return false;
    }
}
