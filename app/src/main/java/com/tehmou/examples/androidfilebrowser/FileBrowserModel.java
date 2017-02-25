package com.tehmou.examples.androidfilebrowser;

import android.util.Log;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private final BehaviorSubject<File> selectedFolder = BehaviorSubject.create();
    private final Observable<List<File>> filesListObservable;

    public FileBrowserModel(Func1<File, Observable<List<File>>> getFiles) {
        filesListObservable = selectedFolder
                .switchMap(file ->
                        getFiles.call(file)
                                .subscribeOn(Schedulers.io()));
    }

    public Observable<File> getSelectedFolder() {
        return selectedFolder.asObservable();
    }

    public void putSelectedFolder(File file) {
        Log.d(TAG, "Selected file: " + file);
        selectedFolder.onNext(file);
    }

    public Observable<List<File>> getFilesList() {
        return filesListObservable;
    }
}
