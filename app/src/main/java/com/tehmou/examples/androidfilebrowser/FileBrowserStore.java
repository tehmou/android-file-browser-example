package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class FileBrowserStore {
    // Cached values
    private File selectedFile;
    private List<File> filesList;

    // Subjects for updates
    private PublishSubject<File> selectedFileSubject = PublishSubject.create();
    private PublishSubject<List<File>> filesListSubject = PublishSubject.create();

    public FileBrowserStore(File root) {
        selectedFile = root;
    }

    public Observable<File> getSelectedFile() {
        if (selectedFile != null) {
            return selectedFileSubject.asObservable()
                    .startWith(selectedFile);
        } else {
            return selectedFileSubject.asObservable();
        }
    }

    public void putSelectedFile(File value) {
        selectedFile = value;
        selectedFileSubject.onNext(value);
    }

    public Observable<List<File>> getFilesList() {
        if (filesList != null) {
            return filesListSubject.asObservable()
                    .startWith(filesList);
        } else {
            return filesListSubject.asObservable();
        }
    }

    public void putFilesList(List<File> value) {
        filesList = value;
        filesListSubject.onNext(value);
    }
}
