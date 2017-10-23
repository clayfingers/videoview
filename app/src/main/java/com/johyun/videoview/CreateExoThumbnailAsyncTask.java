package com.johyun.videoview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by JH on 2017. 10. 18..
 */

public class CreateExoThumbnailAsyncTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = CreateExoThumbnailAsyncTask.class.getSimpleName();

    public interface CreateExoThumbnailCompleteListener {
        void onCreateComplete(Bitmap bitmap);
    }

    private CreateExoThumbnailCompleteListener createExoThumbnailCompleteListener;
    private Bitmap bitmap;


    public CreateExoThumbnailAsyncTask(CreateExoThumbnailCompleteListener createExoThumbnailCompleteListener) {
        this.createExoThumbnailCompleteListener = createExoThumbnailCompleteListener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        Log.d(TAG, "CreateThumbnailAsyncTask doInBackground");

        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(strings[0], new HashMap<String, String>());
            bitmap = mediaMetadataRetriever.getFrameAtTime(3000);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result) {
            Log.d(TAG, "CreateThumbnailAsyncTask onPostExecute");
            createExoThumbnailCompleteListener.onCreateComplete(bitmap);
        }
    }
}

