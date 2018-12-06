package jp.techacademy.osaki.toshihiro.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.ArrayList;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    ArrayList<Uri> imgList = new ArrayList<>();

    Timer mTimer;
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();

    Button mButton1;
    Button mButton2;
    Button mButton3;
    TextView textView;
    ImageView imageView;
    int play_flg = 0;  //0:停止 1:再生
    int fc = 0;  //imgfiles数
    int fn = 0;  //imgfiles選択No

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);//進む
        mButton2 = (Button) findViewById(R.id.button2);//戻る
        mButton3 = (Button) findViewById(R.id.button3);//再生停止
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        mButton1.setOnClickListener(new View.OnClickListener() {//進む
            @Override
            public void onClick(View v) {
                if (play_flg == 0) {
                    //0:停止中の時のみ作動
                    fn += 1;
                    if (fn > fc) fn = 0;
                    textView.setText(imgList.get(fn).getPath() + " fn=" + String.valueOf(fn) + "/" + String.valueOf(fc)); //"ﾌｧｲﾙ名:" +
                    imageView.setImageURI(imgList.get(fn));
                }
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {//戻る
            @Override
            public void onClick(View v) {
                if (play_flg == 0) {
                    //0:停止中の時のみ作動
                    fn -= 1;
                    if (fn < 0) fn = fc;
                    textView.setText(imgList.get(fn).getPath() + " fn=" + String.valueOf(fn) + "/" + String.valueOf(fc)); //"ﾌｧｲﾙ名:" +
                    imageView.setImageURI(imgList.get(fn));
                }
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {//再生停止
            @Override
            public void onClick(View v) {
                if (play_flg == 0) {
                    play_flg = 1;
                    mButton3.setText("停止");

                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mTimerSec += 0.1;

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //if (play_flg != 0) {
                                    fn += 1;
                                    if (fn > fc) fn = 0;
                                    textView.setText(imgList.get(fn).getPath() + " fn=" + String.valueOf(fn) + "/" + String.valueOf(fc)); //"ﾌｧｲﾙ名:" +
                                    imageView.setImageURI(imgList.get(fn));
                                    //}
                                }
                            });
                        }
                    }, 2000, 2000);

                } else {

                    play_flg = 0;
                    mButton3.setText("再生");

                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imgList.add(imageUri);

            } while (cursor.moveToNext());

            fc = imgList.size() - 1;
            fn = 0;
            textView.setText(imgList.get(fn).getPath() + " fn=" + String.valueOf(fn) + "/" + String.valueOf(fc)); //"ﾌｧｲﾙ名:" +
            imageView.setImageURI(imgList.get(fn));

            // 全てのイメージ要素ファイル名を表示
            StringBuilder stb = new StringBuilder();
            for (int i = 0; i <= fc; i++) {
                stb.append("\r\n" + imgList.get(i));
            }
            Log.d("ANDROID", "URI : " + stb);

        }
        cursor.close();
    }

    private void ContinueThread() {
        if (play_flg == 0) return;
        do {
            try {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fn += 1;
                        if (fn > fc) fn = 0;
                        textView.setText(imgList.get(fn).getPath() + " fn=" + String.valueOf(fn) + "/" + String.valueOf(fc)); //"ﾌｧｲﾙ名:" +
                        imageView.setImageURI(imgList.get(fn));
                    }
                });
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }
}