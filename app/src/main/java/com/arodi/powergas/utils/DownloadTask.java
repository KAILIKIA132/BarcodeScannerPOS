package com.arodi.powergas.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask {
    
    public static final String TAG = "Download Task";
    public Context context;
    public String downloadUrl = "", downloadFileName = "";
    
    public DownloadTask(Context context, String downloadUrl) {
        this.context = context;
        this.downloadUrl = downloadUrl;

        downloadFileName = "sale_"+downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1).trim()+".pdf";//Create file name by picking download file name from URL
        Log.e(TAG, downloadFileName);

        System.out.println("gghbbbb"+downloadUrl);
        System.out.println("gyyyygghbbbb"+downloadFileName);
        //Start Downloading Task
        new DownloadingTask().execute();
    }
    
    private class DownloadingTask extends AsyncTask<Void, Void, Void> {
        
        File file = null;
        File outputFile = null;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            System.out.println("hfhfhjfjhdjfjf"+result);
        }
        
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                URL url = new URL(downloadUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection
                
                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());
                    
                }
                
                
                //Get File if SD card is present
                if (new CheckSD().isSDCardPresent()) {
                    file = new File(Environment.getExternalStorageDirectory() + File.separator + "Powergas/");
                } else Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
                
                //If File is not present create directory
                if (!file.exists()) {
                    file.mkdir();
                    Log.e(TAG, "Directory Created.");
                }
                
                outputFile = new File(file, downloadFileName);//Create Output file in Main File
                
                //Create New File if not present
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }
                
                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
                
                InputStream is = c.getInputStream();//Get InputStream for connection
                
                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }
                
                //Close all connection after doing task
                fos.close();
                is.close();
                
            } catch (Exception e) {
                
                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }
            
            return null;
        }
    }
}