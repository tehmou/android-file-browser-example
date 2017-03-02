package com.tehmou.examples.androidfilebrowser;

import android.util.Log;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private final PublishSubject<File> selectedFolderSubject = PublishSubject.create();
    private File selectedFolder;
    private final BehaviorSubject<List<File>> filesListSubject = BehaviorSubject.create();

    public FileBrowserModel(Func1<File, Observable<List<File>>> getFiles) {
        selectedFolderSubject
                .switchMap(file ->
                        getFiles.call(file)
                                .subscribeOn(Schedulers.io()))
                .subscribe(filesListSubject::onNext);
    }

    public Observable<File> getSelectedFolder() {
        if (selectedFolder == null) {
            return selectedFolderSubject.asObservable();
        }

        // Return the stream but starting with the last value first
        return selectedFolderSubject.asObservable()
                .startWith(selectedFolder);
    }

    public void putSelectedFolder(File file) {
        Log.d(TAG, "Selected file: " + file);
        selectedFolder = file;
        selectedFolderSubject.onNext(file);
    }

    public Observable<List<File>> getFilesList() {
        return filesListSubject.asObservable();
    }
}
