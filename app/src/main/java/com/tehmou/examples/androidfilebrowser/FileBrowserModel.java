package com.tehmou.examples.androidfilebrowser;

import android.content.SharedPreferences;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class FileBrowserModel {
    private static final String TAG = FileBrowserModel.class.getSimpleName();
    private static final String SELECTED_FOLDER_KEY = "selected_folder";

    private final String defaultPath;
    private final SharedPreferencesStore<File> selectedFolderStore;
    private final Observable<List<File>> filesListObservable;

    public FileBrowserModel(Function<File, Observable<List<File>>> getFiles,
                            String defaultPath,
                            SharedPreferences sharedPreferences) {
        this.defaultPath = defaultPath;
        selectedFolderStore = new SharedPreferencesStore<>(
                SELECTED_FOLDER_KEY,
                defaultPath,
                sharedPreferences,
                File::getAbsolutePath,
                File::new);

        filesListObservable = selectedFolderStore
                .getStream()
                .switchMap(file ->
                        getFiles.apply(file)
                                .subscribeOn(Schedulers.io()));
    }

    public Observable<File> getSelectedFolder() {
        return selectedFolderStore.getStream();
    }

    public void putSelectedFolder(File file) {
        selectedFolderStore.put(file);
    }

    public Observable<List<File>> getFilesList() {
        return filesListObservable;
    }

    public File getDefaultFolder() {
        return new File(defaultPath);
    }
}
