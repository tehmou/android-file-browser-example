package com.tehmou.examples.androidfilebrowser;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public class FileBrowserViewModel {
    private final CompositeDisposable subscriptions = new CompositeDisposable();

    private final BehaviorSubject<List<File>> filesSubject = BehaviorSubject.create();

    private final FileBrowserModel fileBrowserModel;

    private final Observable<File> listItemClickObservable;
    private final Observable<Object> previousClickObservable;
    private final Observable<Object> rootClickObservable;
    private final File fileSystemRoot;

    public FileBrowserViewModel(
            FileBrowserModel fileBrowserModel,
            Observable<File> listItemClickObservable,
            Observable<Object> previousClickObservable,
            Observable<Object> rootClickObservable,
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
        return filesSubject.hide();
    }
}
