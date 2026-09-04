package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private Button buttonDeck;
    private Button buttonGetFile;
    private EditText TBInput;
    private EditText ETType;

    private String MyPath;
    private byte myKey;
    private boolean myFlag;

    public String getRealPathFromURI(Uri contentUri) {  //Ezafe kardane ye if baraye bakhshe avale content Uri ke agar external storage bood ok she.
        //Environment.getExternalStorageDirectory()
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
        //"/storage/sdcard0/"
        if (contentUri.getPath().contains(":")) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
                return Environment.getExternalStorageDirectory() + File.separator + contentUri.getPath().split(":")[1];
            return contentUri.getPath().split(":")[1];
        } else {
            return Environment.getExternalStorageDirectory() + "/Download/" + DocumentFile.fromSingleUri(this, contentUri).getName();
        }
    }

    public byte[] hexStringToByteArrayDec(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (((byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16))) ^ myKey);

        }
        return data;
    }

    public byte[] myEncode(byte[] x, byte k) {
        for (int i = 0; i < x.length; i++)
            x[i] = (byte) (x[i] ^ k);
        return x;
    }

    public float fileDecode()
    {
        try {
            File file = new File(MyPath);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            long NowTime = System.currentTimeMillis();
            data = myEncode(data, myKey);

            new SaveFileTask().saveMyFile(data, ETType.getText().toString());

            long ThenTime = System.currentTimeMillis();
            return (float) (ThenTime - NowTime) / 1000;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public float bigFileDecode()
    {
        try {
            File file = new File(MyPath);
            FileInputStream fis = new FileInputStream(file);

            long fileLen = (long) file.length();
            int bufferSize = 1024*1024*32;
            byte[] buffer = new byte[bufferSize];

            long NowTime = System.currentTimeMillis();
            SaveBigFileTask mysft = new SaveBigFileTask(ETType.getText().toString());
            while(bufferSize < fileLen)
            {
                fis.read(buffer);
                buffer = myEncode(buffer, myKey);
                mysft.doInBackground(buffer);
                fileLen = fileLen - bufferSize;
            }
            byte[] lastBuf = new byte[(int) fileLen];
            fis.read(lastBuf);
            lastBuf = myEncode(lastBuf, myKey);
            mysft.doInBackground(lastBuf);

            mysft.closeTask();
            fis.close();

            long ThenTime = System.currentTimeMillis();
            return (float) (ThenTime - NowTime) / 1000;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonDeck = (Button) findViewById(R.id.Main_buttonDec);
        buttonGetFile = (Button) findViewById(R.id.Main_buttonGetF);
        TBInput = (EditText) findViewById(R.id.main_InputKey);
        ETType = (EditText) findViewById(R.id.main_InputType);

        buttonGetFile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent MyFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                MyFileIntent.setType("*/*");
                startActivityForResult(MyFileIntent, 10);
            }
        });

        buttonDeck.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)   //agar nashod too ghadimia, ehtiaji nist behesh pakesh kon.
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    myFlag = true;
                }
                else {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
                        myFlag = false;

/*                      Should we show an explanation?
                        if (shouldShowRequestPermissionRationale(
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                        } else {
                            // No explanation needed; request the permission

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }*/
                    } else {
                        myFlag = true;
                    }
                }
                if (myFlag) {
                    myKey = Byte.parseByte(TBInput.getText().toString());
                    Toast.makeText(getApplicationContext(), bigFileDecode() + " s", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), fileDecode() + " s", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {
            case 10:
                if(resultCode==RESULT_OK) {
                    MyPath = getRealPathFromURI(data.getData());
                    //if(data.getData().getScheme() != "file")  //getScheme is file or content
                    Toast.makeText(getApplicationContext(), MyPath, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myFlag = true;
                } else {
                    myFlag = false;
                }
                return;
            }
        }
    }

}