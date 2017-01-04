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

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    private final PublishSubject<Void> backEventObservable = PublishSubject.create();
    private final PublishSubject<Void> homeEventObservable = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            backEventObservable.onNext(null);
            return true;
        } else if (id == R.id.action_home) {
            homeEventObservable.onNext(null);
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
        final File root = new File(
                Environment.getExternalStorageDirectory().getPath());

        final FileBrowserStore store = new FileBrowserStore(root);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        FileListAdapter adapter =
                new FileListAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        Observable<File> listItemClickObservable =
                Observable.create(subscriber ->
                        listView.setOnItemClickListener(
                                (parent, view, position, id) -> {
                                    final File file = (File) view.getTag();
                                    Log.d(TAG, "Selected: " + file);
                                    if (file.isDirectory()) {
                                        subscriber.onNext(file);
                                    }
                                }));

        Observable<File> fileChangeBackEventObservable =
                backEventObservable
                        .withLatestFrom(store.getSelectedFile(),
                                (ignore, file) -> file.getParentFile());

        Observable<File> fileChangeHomeEventObservable =
                homeEventObservable
                        .map(event -> root);

        FileBrowserViewModel viewModel = new FileBrowserViewModel(
                store,
                listItemClickObservable,
                fileChangeBackEventObservable,
                fileChangeHomeEventObservable,
                this::createFilesObservable
        );

        viewModel.getFilesList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        files -> {
                            Log.d(TAG, "Updating adapter with " + files.size() + " items");
                            adapter.clear();
                            adapter.addAll(files);
                        },
                        e -> Log.e(TAG, "Error reading files", e),
                        () -> Log.d(TAG, "Completed"));

        viewModel.getSelectedFile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> setTitle(file.getAbsolutePath()));
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
        return Observable.create(subscriber -> {
            try {
                final List<File> fileList = getFiles(f);
                subscriber.onNext(fileList);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
