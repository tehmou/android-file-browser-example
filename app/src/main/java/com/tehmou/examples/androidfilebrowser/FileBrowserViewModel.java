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

    public FileBrowserViewModel(
            FileBrowserModel fileBrowserModel,
            Observable<File> listItemClickObservable,
            Observable<Object> previousClickObservable,
            Observable<Object> rootClickObservable) {
        this.fileBrowserModel = fileBrowserModel;
        this.listItemClickObservable = listItemClickObservable;
        this.previousClickObservable = previousClickObservable;
        this.rootClickObservable = rootClickObservable;
    }

    public void subscribe() {
        Observable<File> previousFileObservable =
                previousClickObservable
                        .withLatestFrom(fileBrowserModel.getSelectedFolder(),
                                (ignore, selectedFile) -> selectedFile.getParentFile());

        Observable<File> rootFileObservable =
                rootClickObservable
                        .map(event -> fileBrowserModel.getDefaultFolder());

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
