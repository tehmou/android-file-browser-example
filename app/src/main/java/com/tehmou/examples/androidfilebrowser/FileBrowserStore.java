package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class FileBrowserStore {
    final BehaviorSubject<File> selectedFile;
    final BehaviorSubject<List<File>> filesList;

    public FileBrowserStore(File root) {
        selectedFile = BehaviorSubject.create(root);
        filesList = BehaviorSubject.create(new ArrayList<>());
    }

    public Observable<File> getSelectedFile() {
        return selectedFile.asObservable();
    }

    public void putSelectedFile(File value) {
        selectedFile.onNext(value);
    }

    public Observable<List<File>> getFilesList() {
        return filesList.asObservable();
    }

    public void putFilesList(List<File> value) {
        filesList.onNext(value);
    }
}
