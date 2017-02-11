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
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final PublishSubject<Object> backEventObservable = PublishSubject.create();
    private final PublishSubject<Object> homeEventObservable = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Android File Browser");

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

        final ListView listView = (ListView) findViewById(R.id.list_view);
        FileListAdapter adapter =
                new FileListAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        final File root = new File(
                Environment.getExternalStorageDirectory().getPath());
        final BehaviorSubject<File> selectedDir =
                BehaviorSubject.createDefault(root);

        Observable<File> listItemClickObservable = createListItemClickObservable(listView);

        Observable<File> fileChangeBackEventObservable =
                backEventObservable
                        .map(event -> selectedDir.getValue().getParentFile());

        Observable<File> fileChangeHomeEventObservable =
                homeEventObservable
                        .map(event -> root);

        Observable.merge(
                listItemClickObservable,
                fileChangeBackEventObservable,
                fileChangeHomeEventObservable)
                .subscribe(selectedDir);

        selectedDir
                .subscribeOn(Schedulers.io())
                .doOnNext(file -> Log.d(TAG, "Selected file: " + file))
                .switchMap(file ->
                        createFilesObservable(file)
                                .subscribeOn(Schedulers.io()))
                .doOnNext(list -> Log.d(TAG, "Found " + list.size() + " files"))
                .doOnNext(list -> Log.d(TAG, "Processing " + list.size() + " files"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        files -> {
                            Log.d(TAG, "Updating adapter with " + files.size() + " items");
                            adapter.clear();
                            adapter.addAll(files);
                        },
                        e -> Log.e(TAG, "Error reading files", e),
                        () -> Log.d(TAG, "Completed"));
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
