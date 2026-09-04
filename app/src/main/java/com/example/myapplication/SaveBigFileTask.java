package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class SaveBigFileTask extends AsyncTask<byte[], String, String> {

    private String fileType;
    private String fileName;
    private String fileDirectory;

    private File file;
    FileOutputStream fos;

    protected SaveBigFileTask(String myType)
    {
        this(myType, "file");
    }

    protected SaveBigFileTask(String myType, String myName)
    {
        fileType = myType;
        fileName = myName;
        file = new File(Environment.getExternalStorageDirectory(), fileName + "." + fileType);
        if (file.exists()) {
            file.delete();
        }
        try {
            fos = new FileOutputStream(file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void closeTask()
    {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(byte[]... myFile) {

        try {
            fos.write(myFile[0]);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return(null);
    }
}