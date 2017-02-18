package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class FileBrowserViewModel {
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private final BehaviorSubject<List<File>> filesSubject = BehaviorSubject.create();

    private final Observable<File> listItemClickObservable;
    private final Observable<Void> previousClickObservable;
    private final Observable<Void> rootClickObservable;
    private final File fileSystemRoot;
    private final Func1<File, Observable<List<File>>> getFiles;

    public FileBrowserViewModel(
            Observable<File> listItemClickObservable,
            Observable<Void> previousClickObservable,
            Observable<Void> rootClickObservable,
            File fileSystemRoot,
            Func1<File, Observable<List<File>>> getFiles) {
        this.listItemClickObservable = listItemClickObservable;
        this.previousClickObservable = previousClickObservable;
        this.rootClickObservable = rootClickObservable;
        this.fileSystemRoot = fileSystemRoot;
        this.getFiles = getFiles;
    }

    public void subscribe() {
        final BehaviorSubject<File> selectedFile =
                BehaviorSubject.create(fileSystemRoot);

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
                .subscribe(selectedFile));

        subscriptions.add(selectedFile
                .switchMap(file ->
                        getFiles.call(file)
                                .subscribeOn(Schedulers.io()))
                .subscribe(filesSubject::onNext));
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public Observable<List<File>> getFileListObservable() {
        return filesSubject.asObservable();
    }
}
