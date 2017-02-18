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
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final CompositeSubscription viewSubscriptions =
            new CompositeSubscription();

    private final PublishSubject<Void> backEventObservable = PublishSubject.create();
    private final PublishSubject<Void> homeEventObservable = PublishSubject.create();

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
        viewSubscriptions.clear();
        viewModel.unsubscribe();
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
        final BehaviorSubject<File> selectedDir =
                BehaviorSubject.create(root);

        Observable<File> listItemClickObservable = createListItemClickObservable(listView);

        viewModel = new FileBrowserViewModel(
                listItemClickObservable,
                backEventObservable,
                homeEventObservable,
                root, this::createFilesObservable
        );

        viewModel.subscribe();
        viewSubscriptions.add(
                viewModel.getFileListObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setFileList)
        );
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

    Observable<File> createListItemClickObservable(ListView listView) {
        return Observable.create(subscriber ->
                listView.setOnItemClickListener(
                        (parent, view, position, id) -> {
                            final File file = (File) view.getTag();
                            Log.d(TAG, "Selected: " + file);
                            if (file.isDirectory()) {
                                subscriber.onNext(file);
                            }
                        }));
    }
}
