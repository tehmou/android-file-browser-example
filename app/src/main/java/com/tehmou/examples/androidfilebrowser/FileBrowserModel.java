package com.tehmou.examples.androidfilebrowser;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private static final String SELECTED_FOLDER_KEY = "selected_folder";

    private final String defaultPath;
    private final BehaviorSubject<File> selectedFolder;
    private final Observable<List<File>> filesListObservable;

    public FileBrowserModel(Func1<File, Observable<List<File>>> getFiles,
                            String defaultPath,
                            SharedPreferences sharedPreferences) {
        this.defaultPath = defaultPath;
        final String persistedSelectedFolderPath = sharedPreferences
                .getString(SELECTED_FOLDER_KEY, defaultPath);
        final File initialSelectedFolder = new File(persistedSelectedFolderPath);
        selectedFolder = BehaviorSubject.create(initialSelectedFolder);
        selectedFolder
                .observeOn(Schedulers.io())
                .subscribe(folder ->
                        sharedPreferences.edit()
                                .putString(SELECTED_FOLDER_KEY, folder.getAbsolutePath())
                                .commit());

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

    public File getDefaultFolder() {
        return new File(defaultPath);
    }
}
