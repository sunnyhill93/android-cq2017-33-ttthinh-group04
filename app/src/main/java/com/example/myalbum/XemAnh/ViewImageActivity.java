package com.example.myalbum.XemAnh;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myalbum.AlbumsActivity.AlbumActivity;
import com.example.myalbum.AlbumsActivity.MoveCopyImageActivity;
import com.example.myalbum.Cropping.CroppingActivity;
import com.example.myalbum.DAO.DatabaseHandler;
import com.example.myalbum.DTOs.Album;
import com.example.myalbum.DTOs.Image;
import com.example.myalbum.EditingPhoto.PhotoEditorHandler;
import com.example.myalbum.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ViewImageActivity extends Activity {
    private int IDAlbum;
    private int IDImage;
    private List<Image> listImage;
    private ViewPager viewPager;
    Handler myHandler = new Handler();
    private int typeWallpager;
    private CustomAdapterViewPager customAdapterViewPager;
    private LinearLayout thumbnailsContainer;
    private int IDAlbumtoMove;
    private static final int ADD_IMAGE_TO_ALBUM = 90;
    private static final int ADD_IMAGE_TO_FAVOUR = 90;
    private static final int MOVE_IMGAE_TO_ALBUM = 100;
    private static final int EDIT_IMAGE= 101;
    private static final int CROP_IMAGE= 102;
    private  static  final  int VIEW_DETAILS = 95;

    int CurrentImage;


    private void getData() {
        //Get data from HomeActivity
        Intent callingIntent = getIntent();
        Bundle myBundle = callingIntent.getExtras();

        IDAlbum = myBundle.getInt("IDAlbum");
        IDImage = myBundle.getInt("IDImage");
        listImage = DatabaseHandler.getInstance(ViewImageActivity.this).getAllImageOfAlbum(IDAlbum);
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        viewPager = (ViewPager) findViewById(R.id.view_page);
        getData();

        Album album = DatabaseHandler.getInstance(this).getAlbum(IDAlbum);
        this.setTitle(album.getAlbumName());

        customAdapterViewPager = new CustomAdapterViewPager(this, listImage);

        viewPager.setAdapter(customAdapterViewPager);
        viewPager.setCurrentItem(IDImage);

        thumbnailsContainer = (LinearLayout) findViewById(R.id.container);

        inflateThumbnails();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_share) {
            int pos = viewPager.getCurrentItem();
            Uri uriToImage = Uri.parse(listImage.get(pos).getUrlHinh());
            File file = new File(uriToImage.getPath());
            if (uriToImage != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/jpg");
                startActivity(Intent.createChooser(shareIntent, "haha"));
            }

        }
        if (id == R.id.action_edit) {
            CurrentImage = viewPager.getCurrentItem();
            int temp = viewPager.getCurrentItem();

            Intent intent = new Intent(this, PhotoEditorHandler.class);
            Bundle myData = new Bundle();
            myData.putInt("IDAlbum", IDAlbum);
            myData.putInt("IDImage", temp);
            intent.putExtras(myData);
            startActivityForResult(intent, EDIT_IMAGE);
            return true;
        }

        if (id == R.id.action_crop) {
            CurrentImage = viewPager.getCurrentItem();
            int temp = viewPager.getCurrentItem();

            Intent intent = new Intent(this, CroppingActivity.class);
            Bundle myData = new Bundle();
            myData.putInt("IDAlbum", IDAlbum);
            myData.putInt("IDImage", temp);
            intent.putExtras(myData);
            startActivityForResult(intent, CROP_IMAGE);
            return true;
        }

        if (id == R.id.action_addtoalbum) {
            IDAlbumtoMove = viewPager.getCurrentItem();
            Intent newActivity = new Intent(ViewImageActivity.this, MoveCopyImageActivity.class);

            Bundle myData = new Bundle();

            newActivity.putExtras(myData);
            startActivityForResult(newActivity, ADD_IMAGE_TO_ALBUM);
        }
        if (id == R.id.action_delete) {
            int pos = viewPager.getCurrentItem();
            viewPager.setAdapter(null);
            listImage.remove(pos);
            customAdapterViewPager.notifyDataSetChanged();
            viewPager.setAdapter(customAdapterViewPager);
            thumbnailsContainer.removeAllViews();
            inflateThumbnails();
            new moveImageToAlbum().execute(-1, pos);


        }

        if (id == R.id.action_movetoalbum) {
            IDAlbumtoMove = viewPager.getCurrentItem();
            Intent newActivity = new Intent(ViewImageActivity.this, MoveCopyImageActivity.class);

            Bundle myData = new Bundle();

            newActivity.putExtras(myData);
            startActivityForResult(newActivity, MOVE_IMGAE_TO_ALBUM);

        }
        if (id == R.id.action_wallpager) {

            closeOptionsMenu();

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_wallpager);
            ImageView homescreen = (ImageView) dialog.findViewById(R.id.homescreen);
            ImageView lockscreen = (ImageView) dialog.findViewById(R.id.lockscreen);
            ImageView bothscreen = (ImageView) dialog.findViewById(R.id.bothscreen);

            homescreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeWallpager = 1;
                    new decodeFileAsync().execute();
                    dialog.dismiss();
                }
            });
            lockscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeWallpager = 2;
                    new decodeFileAsync().execute();
                    dialog.dismiss();

                }
            });
            bothscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeWallpager = 3;
                    new decodeFileAsync().execute();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        if(id == R.id.action_details)
        {
            Intent intent = new Intent(ViewImageActivity.this , DetailPicture.class);
            int temp = viewPager.getCurrentItem();
            Bundle myData = new Bundle();
            myData.putInt("IDAlbum", IDAlbum);
            myData.putInt("IDImage", temp);
            intent.putExtras(myData);
        startActivityForResult(intent, VIEW_DETAILS);
        }

        if(id == R.id.action_favor)
        {
            int picture = viewPager.getCurrentItem();
            Integer numberOfImages = DatabaseHandler.getInstance(ViewImageActivity.this).getNumberOfImages(-2);
            Image image = DatabaseHandler.getInstance(ViewImageActivity.this).getImageAt(IDAlbum, picture);
            DatabaseHandler.getInstance(ViewImageActivity.this).addImage(image.getUrlHinh(), numberOfImages, -2);
            Toast.makeText(ViewImageActivity.this, "Thành công", Toast.LENGTH_LONG).show();
        }

        return false;

    }


    private void inflateThumbnails() {
        for (int i = 0; i < listImage.size(); i++) {
            View imageLayout = getLayoutInflater().inflate(R.layout.item_thumbnails, null);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.thumbnail);
            imageView.setOnClickListener(onChangePageClickListener(i));

            Glide.with(this).load(listImage.get(i).getUrlHinh())
                    .placeholder(R.drawable.loading)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.drawable.error).into(imageView);
            thumbnailsContainer.addView(imageLayout);
        }
    }

    private View.OnClickListener onChangePageClickListener(final int i) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(i);


            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_IMAGE_TO_ALBUM) {
            Bundle myBundle = data.getBundleExtra("Result");
            int idalbum = myBundle.getInt("newIDAlbum");
            int idimage = IDAlbumtoMove;

            if (idalbum != IDAlbum) {
                new addImageToAlbum().execute(idalbum, idimage);
            }
        }
        if (resultCode == RESULT_OK && requestCode == MOVE_IMGAE_TO_ALBUM) {
            viewPager.setAdapter(null);
            listImage.remove(IDAlbumtoMove);
            customAdapterViewPager.notifyDataSetChanged();
            viewPager.setAdapter(customAdapterViewPager);
            thumbnailsContainer.removeAllViews();
            inflateThumbnails();

            Bundle myBundle = data.getBundleExtra("Result");
            int idalbum = myBundle.getInt("newIDAlbum");
            int idimage = IDAlbumtoMove;

            if (idalbum != IDAlbum) {
                new moveImageToAlbum().execute(idalbum, idimage);
            }
        }

        if (resultCode == RESULT_OK && requestCode == EDIT_IMAGE) {
            viewPager.setAdapter(null);
            customAdapterViewPager.notifyDataSetChanged();
            viewPager.setAdapter(customAdapterViewPager);
            viewPager.setCurrentItem(CurrentImage);
            thumbnailsContainer.removeAllViews();
            inflateThumbnails();
        }

        if (resultCode == RESULT_OK && requestCode == CROP_IMAGE) {
            viewPager.setAdapter(null);
            customAdapterViewPager.notifyDataSetChanged();
            viewPager.setAdapter(customAdapterViewPager);
            viewPager.setCurrentItem(CurrentImage);
            thumbnailsContainer.removeAllViews();
            inflateThumbnails();
        }


    }

    private class addImageToAlbum extends AsyncTask<Integer, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(ViewImageActivity.this);

        String waitMsg = "Wait\nProcess is being done... ";

        protected void onPreExecute() {
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //outside touch doesn't dismiss you
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            Integer numberOfImages = DatabaseHandler.getInstance(ViewImageActivity.this).getNumberOfImages(integers[0]);
            Image image = DatabaseHandler.getInstance(ViewImageActivity.this).getImageAt(IDAlbum, integers[1]);
            DatabaseHandler.getInstance(ViewImageActivity.this).addImage(image.getUrlHinh(), numberOfImages, integers[0]);
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... value) {
            super.onProgressUpdate(value);
        }

        protected void onPostExecute(final Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }
    }

    private class addImageToFavour extends AsyncTask<Integer, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(ViewImageActivity.this);

        String waitMsg = "Wait\nProcess is being done... ";

        protected void onPreExecute() {
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //outside touch doesn't dismiss you
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            Integer numberOfImages = DatabaseHandler.getInstance(ViewImageActivity.this).getNumberOfImages(-2);
            Image image = DatabaseHandler.getInstance(ViewImageActivity.this).getImageAt(IDAlbum, integers[1]);
            DatabaseHandler.getInstance(ViewImageActivity.this).addImage(image.getUrlHinh(), integers[1], -2);
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... value) {
            super.onProgressUpdate(value);
        }

        protected void onPostExecute(final Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }
    }




    private class RemoveImageTask extends AsyncTask<Integer, Integer, Void> {
        private final ProgressDialog dialog = new ProgressDialog(ViewImageActivity.this);

        String waitMsg = "Wait\nProcess is being done... ";

        protected void onPreExecute() {
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //outside touch doesn't dismiss you
            this.dialog.show();
        }


        @Override
        protected Void doInBackground(Integer... integers) {
            listImage.remove(integers[0]);
            DatabaseHandler.getInstance(ViewImageActivity.this).deleteImage(IDAlbum, integers[0]);

            for (int i = integers[0]; i < listImage.size(); i++) {
                DatabaseHandler.getInstance(ViewImageActivity.this).updateIDImage(listImage.get(i), i);
                listImage.get(i).setPos(i);
            }

            publishProgress(integers[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
        }

        protected void onPostExecute(final Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }

    }

    private class moveImageToAlbum extends AsyncTask<Integer, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(ViewImageActivity.this);

        String waitMsg = "Wait\nProcess is being done... ";

        protected void onPreExecute() {
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //outside touch doesn't dismiss you
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            Integer numberOfImages = DatabaseHandler.getInstance(ViewImageActivity.this).getNumberOfImages(integers[0]);

            Image image = DatabaseHandler.getInstance(ViewImageActivity.this).getImageAt(IDAlbum, integers[1]);
            DatabaseHandler.getInstance(ViewImageActivity.this).deleteImage(IDAlbum, integers[1]);

            for (int i = integers[1]; i < listImage.size(); i++) {
                DatabaseHandler.getInstance(ViewImageActivity.this).updateIDImage(listImage.get(i), i);
                listImage.get(i).setPos(i);
            }

            DatabaseHandler.getInstance(ViewImageActivity.this).addImageWithOldIDAlbum(image.getUrlHinh(), numberOfImages, integers[0], IDAlbum);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... value) {
        }

        protected void onPostExecute(final Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }
    }
    private class decodeFileAsync extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(ViewImageActivity.this);

        String waitMsg = "Wait\nProcess is being done... ";
        int pos;
        protected void onPreExecute() {
            pos = viewPager.getCurrentItem();
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //outside touch doesn't dismiss you
            this.dialog.show();

        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Uri uriImage = Uri.parse(listImage.get(pos).getUrlHinh());
                File file = new File(uriImage.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());

                if (typeWallpager == 1)
                    manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                else if (typeWallpager == 2)
                    manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                else
                    manager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... value) {
            Toast.makeText(ViewImageActivity.this, "Thành công", Toast.LENGTH_LONG).show();
            super.onProgressUpdate(value);
        }

        protected void onPostExecute(final Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }

    }


}
