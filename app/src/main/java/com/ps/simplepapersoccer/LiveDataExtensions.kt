package com.ps.simplepapersoccer

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ps.simplepapersoccer.event.LiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun <T> withCoroutineScopeAsLiveData(scope: CoroutineScope, crossinline block: suspend CoroutineScope.() -> T): LiveData<T> {
    val liveData = MutableLiveData<T>()
    scope.launch {
        liveData.postValue(block())
    }
    return liveData
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = LiveEvent<T>()
    result.addSource(this) {
        if (Looper.myLooper() == Looper.getMainLooper()) result.value = it else result.postValue(it)
    }
    return result
}