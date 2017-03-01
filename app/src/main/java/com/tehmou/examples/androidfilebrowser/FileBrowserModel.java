package com.tehmou.examples.androidfilebrowser;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private static final String SELECTED_FOLDER_KEY = "selected_folder";

    private final String defaultPath;
    private final BehaviorSubject<File> selectedFolder;
    private final Observable<List<File>> filesListObservable;

    public FileBrowserModel(Function<File, Observable<List<File>>> getFiles,
                            String defaultPath,
                            SharedPreferences sharedPreferences) {
        this.defaultPath = defaultPath;
        final String persistedSelectedFolderPath = sharedPreferences
                .getString(SELECTED_FOLDER_KEY, defaultPath);
        final File initialSelectedFolder = new File(persistedSelectedFolderPath);
        selectedFolder = BehaviorSubject.createDefault(initialSelectedFolder);
        selectedFolder
                .observeOn(Schedulers.io())
                .subscribe(folder ->
                        sharedPreferences.edit()
                                .putString(SELECTED_FOLDER_KEY, folder.getAbsolutePath())
                                .commit());

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

    public File getDefaultFolder() {
        return new File(defaultPath);
    }
}
