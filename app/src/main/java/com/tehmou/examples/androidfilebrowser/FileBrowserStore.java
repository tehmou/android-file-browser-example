package com.tehmou.examples.androidfilebrowser;

import java.io.File;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class FileBrowserStore {
    final BehaviorSubject<File> selectedFile;

    public FileBrowserStore(File root) {
        selectedFile = BehaviorSubject.create(root);
    }

    public File getSelectedFileValue() {
        return selectedFile.getValue();
    }

    public Observable<File> getSelectedFile() {
        return selectedFile.asObservable();
    }

    public void putSelectedFile(File file) {
        selectedFile.onNext(file);
    }
}
