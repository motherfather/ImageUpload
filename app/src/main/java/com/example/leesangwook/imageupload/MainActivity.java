package com.example.leesangwook.imageupload;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Uri uri;
    private Bitmap bitmap;
    private Bitmap final_bitmap;

    private TextView url_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select Image"), 1);
                final_bitmap = null;
            }
        });

        findViewById(R.id.upload_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageUpload().execute();
            }
        });
        findViewById(R.id.rotate_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (final_bitmap == null) {
                    final_bitmap = imgRotate(bitmap, 90);
                } else {
                    final_bitmap = imgRotate(final_bitmap, 90);
                }
                ((ImageView) findViewById(R.id.image_file)).setImageBitmap(final_bitmap);
                uri = getImageUri(getApplicationContext(), final_bitmap);
            }
        });
        url_text = (TextView)findViewById(R.id.image_url);
        url_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                intent.putExtra("url", url_text.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            uri = data.getData();
            // 같은듯...
            bitmap = BitmapFactory.decodeFile(getPath(uri));
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1000 || height > 1000) {
                width = width / 4;
                height = height / 4;
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            ((ImageView) findViewById(R.id.image_file)).setImageBitmap(bitmap);
            Log.w("path?", uri.getPath() + " << uri.getPath(), " + uri + " << uri, " + getPath(uri) + " << getPath(uri)");
        }
    }

    // bitmap 이미지를 원하는각으로 회전
    private Bitmap imgRotate(Bitmap bitmap, int angle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();

        return resizedBitmap;
    }

    // 경로 구하기
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
        cursor.moveToNext();
        return cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        // 역시 위아래 같은듯...
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
    }

    // bit -> uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private static final String IMGUR_CLIENT_ID = "39c074c1942156b";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private JSONObject imgur;
    private String url;

    private final OkHttpClient client = new OkHttpClient();

    public void run() throws Exception {
        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "Square Logo")
                .addFormDataPart("image", "logo-square.png",
                        RequestBody.create(MEDIA_TYPE_PNG, new File(getPath(uri))))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        imgur = new JSONObject(response.body().string());
        imgur = new JSONObject(String.valueOf(imgur.get("data")));
        url = imgur.getString("link");

    }

    public class ImageUpload extends AsyncTask<Void, Integer, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Upload...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (progressDialog != null)
                progressDialog.dismiss();
            url_text.setText(url);
        }
    }
}
