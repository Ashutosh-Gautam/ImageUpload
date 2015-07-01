package com.example.XPath;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.htmlcleaner.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class XPathExample extends Activity {
    /**
     * Called when the activity is first created.
     */
    private Context context;
    private static final int CAPTURE_IMAGE_CAPTURE_CODE = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    ArrayList<Bitmap> prorityList = new ArrayList<Bitmap>();
    GridView gridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = XPathExample.this;
        gridView = (GridView) findViewById(R.id.gridview);


        Button logobutton = (Button) findViewById(R.id.logobutton);
        logobutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_CAPTURE_CODE);
            }
        });
    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context context;

        public ImageAdapter(Context c)
        {
            context = c;
        }

        //---returns the number of images---
        public int getCount() {
            return bitmaps.size();
        }

        //---returns the ID of an item---
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(bitmaps.get(position));
            return imageView;
        }
    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    /*
     * Here we restore the fileUri again
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraVideo");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

        } else {
            return null;
        }

        return mediaFile;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_CAPTURE_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bmp = previewCapturedImage();
                gridView.setAdapter(new ImageAdapter(this));
                Toast.makeText(this, "Image Captured", Toast.LENGTH_LONG).show();
                RunnableDemo R1 = new RunnableDemo( bmp, bitmaps);
                try{
                    R1.start();
                } catch (RuntimeException e){
                    Toast.makeText(context, "Upload Failed retrying", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    class RunnableDemo implements Runnable {
        private Thread t;
        private String threadName;
        private Bitmap bitmaps;
        private ArrayList<Bitmap> bitmapssssss;

        RunnableDemo(Bitmap bitmaps, ArrayList<Bitmap> name){
            bitmapssssss = name;
            this.bitmaps = bitmaps;
            System.out.println("Creating " +  threadName );
        }
        public void run() {
            System.out.println("Running " +  threadName );
            try {
                System.out.println("Thread: " + threadName + ", " + bitmapssssss.size());
                // Let the thread sleep for a while.
                if(bitmaps.getDensity() == 100){
                    System.out.println("File Uploaded");
                    return;
                }

                Thread.sleep(30000);
                if (bitmapssssss.size()%2 == 0) {

                    bitmaps.setDensity(100);
                    prorityList.add(bitmaps);

                    throw new RuntimeException("Faking upload failure exception");
                }
            } catch (InterruptedException e) {
                System.out.println("Thread " +  threadName + " interrupted.");
            }

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    retryPriorityUploads();
                }
            });
            System.out.println("Thread " +  threadName + " exiting.");
        }

        public void start ()
        {
            System.out.println("Starting " +  threadName );
            if (t == null)
            {
                t = new Thread (this, threadName);
                t.start ();
            }
        }

    }

    private void retryPriorityUploads() {

        for(int i=0;i < prorityList.size();i++){
            RunnableDemo R1 = new RunnableDemo( prorityList.get(i), bitmaps);
            R1.start();
        }
    }

    /**
     * Display image from a path to ImageView
     */
    private Bitmap previewCapturedImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;

        Bitmap bitmap= null;
        try {

            bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            bitmaps.add(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
