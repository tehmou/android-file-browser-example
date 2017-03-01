package com.tehmou.examples.androidfilebrowser;

import android.content.SharedPreferences;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

public class SharedPreferencesStore<T> {
    private final BehaviorSubject<T> subject;

    public SharedPreferencesStore(final String key,
                                  final String defaultValue,
                                  final SharedPreferences sharedPreferences,
                                  final Function<T, String> serialize,
                                  final Function<String, T> deserialize) {
        T initialValue = null;
        try {
            initialValue = deserialize.apply(
                    sharedPreferences.getString(key, defaultValue)
            );
        } catch (Exception e) { }
        if (initialValue != null) {
            subject = BehaviorSubject.createDefault(initialValue);
        } else {
            subject = BehaviorSubject.create();
        }
        subject.subscribe(value ->
                sharedPreferences.edit()
                        .putString(key, serialize.apply(value))
                        .commit()
        );
    }

    public void put(T value) {
        subject.onNext(value);
    }

    public Observable<T> getStream() {
        return subject.hide();
    }
}
