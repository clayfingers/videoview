package com.johyun.videoview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Created by JH on 2017. 10. 18..
 */

public class CreateThumbnailAsyncTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = CreateThumbnailAsyncTask.class.getSimpleName();

    public interface CreateThumbnailCompleteListener {
        void onCreateComplete(ByteArrayOutputStream stream);
    }

    private CreateThumbnailCompleteListener createThumbnailCompleteListener;
    private ByteArrayOutputStream stream;

    public CreateThumbnailAsyncTask(CreateThumbnailCompleteListener createThumbnailCompleteListener) {
        this.createThumbnailCompleteListener = createThumbnailCompleteListener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        Log.d(TAG, "CreateThumbnailAsyncTask doInBackground");

        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(strings[0], new HashMap<String, String>());
            Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();

            stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
            createThumbnailCompleteListener.onCreateComplete(stream);
        }
    }
}

