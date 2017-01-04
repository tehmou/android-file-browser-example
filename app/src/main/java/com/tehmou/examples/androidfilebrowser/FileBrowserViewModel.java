package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FileBrowserViewModel {
    private final FileBrowserStore store;

    public FileBrowserViewModel(FileBrowserStore store,
                                Observable<File> listItemClickObservable,
                                Observable<File> fileChangeBackEventObservable,
                                Observable<File> fileChangeHomeEventObservable,
                                Func1<File, Observable<List<File>>> createFilesObservable) {
        this.store = store;

        Observable.merge(
                listItemClickObservable,
                fileChangeBackEventObservable,
                fileChangeHomeEventObservable)
                .subscribe(store::putSelectedFile);

        store.getSelectedFile()
                .subscribeOn(Schedulers.io())
                .flatMap(createFilesObservable)
                .subscribe(store::putFilesList);
    }

    public Observable<File> getSelectedFile() {
        return store.getSelectedFile();
    }

    public Observable<List<File>> getFilesList() {
        return store.getFilesList();
    }
}
