package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class FileBrowserViewModel {
    private final CompositeDisposable subscriptions = new CompositeDisposable();

    private final BehaviorSubject<List<File>> filesSubject = BehaviorSubject.create();

    private final Observable<File> listItemClickObservable;
    private final Observable<Object> previousClickObservable;
    private final Observable<Object> rootClickObservable;
    private final File fileSystemRoot;
    private final Function<File, Observable<List<File>>> getFiles;

    public FileBrowserViewModel(
            Observable<File> listItemClickObservable,
            Observable<Object> previousClickObservable,
            Observable<Object> rootClickObservable,
            File fileSystemRoot,
            Function<File, Observable<List<File>>> getFiles) {
        this.listItemClickObservable = listItemClickObservable;
        this.previousClickObservable = previousClickObservable;
        this.rootClickObservable = rootClickObservable;
        this.fileSystemRoot = fileSystemRoot;
        this.getFiles = getFiles;
    }

    public void subscribe() {
        final BehaviorSubject<File> selectedFile =
                BehaviorSubject.createDefault(fileSystemRoot);

        Observable<File> previousFileObservable =
                previousClickObservable
                        .map(event ->
                                selectedFile.getValue()
                                        .getParentFile());

        Observable<File> rootFileObservable =
                rootClickObservable
                        .map(event -> fileSystemRoot);

        subscriptions.add(Observable.merge(
                listItemClickObservable,
                previousFileObservable,
                rootFileObservable)
                .subscribe(selectedFile::onNext));

        subscriptions.add(selectedFile
                .switchMap(file ->
                        getFiles.apply(file)
                                .subscribeOn(Schedulers.io()))
                .subscribe(filesSubject::onNext));
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public Observable<List<File>> getFileListObservable() {
        return filesSubject.hide();
    }
}
