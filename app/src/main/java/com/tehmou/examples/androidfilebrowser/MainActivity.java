package com.tehmou.examples.androidfilebrowser;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHARED_PREFERENCES_NAME = "file_browser";

    private final CompositeDisposable viewSubscriptions =
            new CompositeDisposable();

    private final PublishSubject<Object> backEventObservable = PublishSubject.create();
    private final PublishSubject<Object> homeEventObservable = PublishSubject.create();

    private FileBrowserModel fileBrowserModel;
    private ListView listView;
    private FileListAdapter adapter;
    private FileBrowserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Android File Browser");

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new FileListAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        Observable<File> listItemClickObservable = createListItemClickObservable(listView);

        fileBrowserModel =
                new FileBrowserModel(
                        this::createFilesObservable,
                        Environment.getExternalStorageDirectory().getPath(),
                        getSharedPreferences(SHARED_PREFERENCES_NAME, 0)
                );
        viewModel = new FileBrowserViewModel(
                fileBrowserModel,
                listItemClickObservable,
                backEventObservable,
                homeEventObservable
        );

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            initWithPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeViewBinding();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseViewBinding();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            backEventObservable.onNext(new Object());
            return true;
        } else if (id == R.id.action_home) {
            homeEventObservable.onNext(new Object());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initWithPermissions();
    }

    private void initWithPermissions() {
        viewModel.subscribe();
    }

    private void makeViewBinding() {
        viewSubscriptions.add(
                viewModel.getFileListObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setFileList)
        );
    }

    private void releaseViewBinding() {
        viewSubscriptions.clear();
    }

    private void setFileList(List<File> files) {
        Log.d(TAG, "Updating adapter with " + files.size() + " items");
        adapter.clear();
        adapter.addAll(files);
    }

    private List<File> getFiles(final File f) {
        List<File> fileList = new ArrayList<>();
        File[] files = f.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.isHidden() && file.canRead()) {
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }

    Observable<List<File>> createFilesObservable(
            final File f) {
        return Observable.create(emitter -> {
            try {
                final List<File> fileList = getFiles(f);
                emitter.onNext(fileList);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    Observable<File> createListItemClickObservable(ListView listView) {
        return Observable.create(emitter ->
                listView.setOnItemClickListener(
                        (parent, view, position, id) -> {
                            final File file = (File) view.getTag();
                            Log.d(TAG, "Selected: " + file);
                            if (file.isDirectory()) {
                                emitter.onNext(file);
                            }
                        }));
    }
}
