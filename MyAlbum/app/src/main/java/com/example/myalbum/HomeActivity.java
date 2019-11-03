package com.example.myalbum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.example.myalbum.events.OnClickEvent;
import com.example.myalbum.utilities.UtilityListeners;

public class HomeActivity extends Activity implements ActivityCallBacks{
    //Page widgets
    private AutoCompleteTextView searchBar;
    private ListView albumList;
    private Button searchButton;
    private Button addAlbumButton;
    private ProgressBar loadingCir;
    private AddAlbumDialog addAlbumDialog = null;
    private final static int FIND_ALBUM_THREADCODE = 1;

    //Auto-completes
    private ArrayList<String> hint;

    //Albums database
    private List<Album> allAlbums;

    //Events
    private OnClickEvent addAlbumButton_OnClick = new OnClickEvent();
    private OnClickEvent searchButton_OnClick = new OnClickEvent();

    //Listeners
    //Nothing here

    //Handlers
    private static class IncomingHandler extends Handler {
        private final WeakReference<HomeActivity> mActivity;

        IncomingHandler(HomeActivity homeActivity) {
            mActivity = new WeakReference<HomeActivity>(homeActivity);
        }
        @Override
        public void handleMessage(Message msg)
        {
            HomeActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg){
        if(msg.what == FIND_ALBUM_THREADCODE) {
            ArrayList<Album> filtered = (ArrayList<Album>) msg.obj;
            ArrayList<Album> source = (ArrayList<Album>) allAlbums;
            Intent newActivity = new Intent(this, SearchAlbumActivity.class);
            newActivity.putExtra("Source", source);
            newActivity.putExtra("Render Info", filtered);
            newActivity.putExtra("AutoComplete", hint);
            loadingCir.setVisibility(View.INVISIBLE);
            startActivity(newActivity);
        }
    }

    private IncomingHandler updateHandler = new IncomingHandler(HomeActivity.this);

    //Adapters
    private AlbumsAdapter albumsAdapter;
    private ArrayAdapter<String> autoCompleteAdapter;

    //Album related logic, to be moved to different class
    private void addAlbum(String name) {
        allAlbums.add(new Album(name));
        albumsAdapter.notifyDataSetChanged();
        hint.add(name);
        autoCompleteAdapter.notifyDataSetChanged();
        albumList.smoothScrollToPosition(albumsAdapter.getCount()-1);
    }
    private ArrayList<Album> findAlbumByName(List<Album> source, String name){
        if(name == null) return null;
        ArrayList<Album> result = new ArrayList<Album>();
        Album album;
        for (int i = 0; i < source.size();i++)
        {
            album = source.get(i);
            if (album.getAlbumName().equals(name))
            {
                result.add(album);
            }
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //Bind
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        addAlbumButton = findViewById(R.id.addAlbumButton);
        albumList = findViewById(R.id.albumList);
        loadingCir = findViewById(R.id.progress_circular);

        //Tests
        allAlbums = new ArrayList<Album>();
        allAlbums.add(new Album("Test Album"));
        allAlbums.add(new Album("Test Album 2"));
        allAlbums.add(new Album("Test Album 3"));

        hint = new ArrayList<String>();
        for (int i = 0; i < allAlbums.size();i++)
        {
            hint.add(allAlbums.get(i).getAlbumName());
        }

        //Add adapters
            //For displaying all albums
        albumsAdapter = new AlbumsAdapter(this,
                allAlbums,
                R.layout.albumlist_row);
        albumList.setAdapter(albumsAdapter);

            //For autocomplete field
        autoCompleteAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, hint);
        searchBar.setAdapter(autoCompleteAdapter);

        //Set listeners + events

            //Add Album Button
        addAlbumButton_OnClick.register(UtilityListeners.view_OnClick_ClearFocus(HomeActivity.this));
        addAlbumButton_OnClick.register(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addAlbumDialog == null)
                    addAlbumDialog = AddAlbumDialog.newInstance(HomeActivity.this,"Add Album Name");
                addAlbumDialog.show();
            }
        });
        addAlbumButton.setOnClickListener(addAlbumButton_OnClick);

            //Search Bar
        searchBar.setOnFocusChangeListener(UtilityListeners.editText_OnFocusChange(HomeActivity.this));

            //Search Button
        searchButton_OnClick.register(UtilityListeners.view_OnClick_ClearFocus(HomeActivity.this));
        searchButton_OnClick.register( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                final String find = (searchBar.getText()).toString();
                if(find.equals("")) {
                    return;
                }

                Thread findThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Album> filtered = findAlbumByName(allAlbums, find);
                        Message msg = updateHandler.obtainMessage(FIND_ALBUM_THREADCODE, filtered);
                        updateHandler.sendMessage(msg);
                    }
                });

                loadingCir.setVisibility(View.VISIBLE);
                findThread.run();
            }
        });
        searchButton.setOnClickListener(searchButton_OnClick);

            //Album List
        albumList.setOnItemClickListener(UtilityListeners.listView_OnItemClick_ClearFocus(HomeActivity.this));

    }

    @Override
    public void onMessageToActivity(String source, Bundle bundle) {
        switch (source){
            case "Add Album Dialog":
                addAlbum(bundle.getString("albumName"));
                break;
        }
    }
}
