package com.tehmou.examples.androidfilebrowser;

import android.content.SharedPreferences;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class SharedPreferencesStore<T> {
    private final BehaviorSubject<T> subject;

    public SharedPreferencesStore(final String key,
                                  final String defaultValue,
                                  final SharedPreferences sharedPreferences,
                                  final Func1<T, String> serialize,
                                  final Func1<String, T> deserialize) {
        T initialValue = deserialize.call(
                sharedPreferences.getString(key, defaultValue)
        );
        subject = BehaviorSubject.create(initialValue);
        subject.subscribe(value ->
                sharedPreferences.edit()
                        .putString(key, serialize.call(value))
                        .commit()
        );
    }

    public void put(T value) {
        subject.onNext(value);
    }

    public Observable<T> getStream() {
        return subject.asObservable();
    }
}
