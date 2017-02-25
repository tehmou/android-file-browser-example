package com.tehmou.examples.androidfilebrowser;

import android.util.Log;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private final BehaviorSubject<File> selectedFolder = BehaviorSubject.create();
    private final Observable<List<File>> filesListObservable;

    public FileBrowserModel(Function<File, Observable<List<File>>> getFiles) {
        filesListObservable = selectedFolder
                .switchMap(file ->
                        getFiles.apply(file)
                                .subscribeOn(Schedulers.io()));
    }

    public Observable<File> getSelectedFolder() {
        return selectedFolder.hide();
    }

    public void putSelectedFolder(File file) {
        Log.d(TAG, "Selected file: " + file);
        selectedFolder.onNext(file);
    }

    public Observable<List<File>> getFilesList() {
        return filesListObservable;
    }
}
