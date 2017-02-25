package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class FileBrowserViewModel {
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private final BehaviorSubject<List<File>> filesSubject = BehaviorSubject.create();

    private final FileBrowserModel fileBrowserModel;

    private final Observable<File> listItemClickObservable;
    private final Observable<Void> previousClickObservable;
    private final Observable<Void> rootClickObservable;
    private final File fileSystemRoot;

    public FileBrowserViewModel(
            FileBrowserModel fileBrowserModel,
            Observable<File> listItemClickObservable,
            Observable<Void> previousClickObservable,
            Observable<Void> rootClickObservable,
            File fileSystemRoot) {
        this.fileBrowserModel = fileBrowserModel;
        this.listItemClickObservable = listItemClickObservable;
        this.previousClickObservable = previousClickObservable;
        this.rootClickObservable = rootClickObservable;
        this.fileSystemRoot = fileSystemRoot;
    }

    public void subscribe() {
        // Reset to file system root
        fileBrowserModel.putSelectedFolder(fileSystemRoot);

        Observable<File> previousFileObservable =
                previousClickObservable
                        .withLatestFrom(fileBrowserModel.getSelectedFolder(),
                                (ignore, selectedFile) -> selectedFile.getParentFile());

        Observable<File> rootFileObservable =
                rootClickObservable
                        .map(event -> fileSystemRoot);

        subscriptions.add(Observable.merge(
                listItemClickObservable,
                previousFileObservable,
                rootFileObservable)
                .subscribe(fileBrowserModel::putSelectedFolder));

        subscriptions.add(fileBrowserModel.getFilesList()
                .subscribe(filesSubject::onNext));
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public Observable<List<File>> getFileListObservable() {
        return filesSubject.asObservable();
    }
}
