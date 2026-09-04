package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

class SaveFileTask extends AsyncTask<byte[], String, String> {

    private String fileType;
    private String fileName = "file";
    private String fileDirectory;

    @Override
    protected String doInBackground(byte[]... myFile) {
        File file=new File(Environment.getExternalStorageDirectory(), fileName + "." + fileType);

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream fos=new FileOutputStream(file.getPath());

            fos.write(myFile[0]);
            fos.close();
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return(null);
    }

    void  setFileType(String myType)
    {
        fileType = myType;
    }

    void setFileName(String myName)
    {
        fileName = myName;
    }

    void saveMyFile(byte[] myFile, String myType)
    {
        setFileType(myType);
        doInBackground(myFile);
    }

    void saveMyFile(byte[] myFile, String myType, String myName)
    {
        setFileType(myType);
        setFileName(myName);
        doInBackground(myFile);
    }


}