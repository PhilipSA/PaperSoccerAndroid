package com.ps.simplepapersoccer

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ps.simplepapersoccer.event.LiveEventMediatorLiveData

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun <X, Y> switchToCompletableMap(trigger: LiveData<X>,
                                  func: (value: X?) -> LiveData<Y>): LiveEventMediatorLiveData<Boolean> {
    val liveDataReader = LiveEventMediatorLiveData<Boolean>()
    liveDataReader.addSource(trigger, object : Observer<X> {
        internal var mSource: LiveData<Y>? = null

        override fun onChanged(x: X?) {
            val newLiveData = func(x)
            if (mSource === newLiveData) {
                return
            }
            if (mSource != null) {
                liveDataReader.removeSource(mSource!!)
            }
            mSource = newLiveData
            if (mSource != null) {
                liveDataReader.addSource(mSource!!) {
                    y -> if (y is Boolean) liveDataReader.setWrappedValue(y) else liveDataReader.setWrappedValue(true)
                }
            }
        }
    })
    return liveDataReader
}

fun <X, Y> switchToSingleEventMap(trigger: LiveData<X>,
                                  func: (value: X) -> LiveData<Y>?): LiveEventMediatorLiveData<Y> {
    val liveDataReader = LiveEventMediatorLiveData<Y>()
    liveDataReader.addSource(trigger, object : Observer<X> {
        internal var mSource: LiveData<Y>? = null

        override fun onChanged(x: X) {
            val newLiveData = func(x)
            if (mSource === newLiveData) {
                return
            }
            if (mSource != null) {
                liveDataReader.removeSource(mSource!!)
            }
            mSource = newLiveData
            if (mSource != null) {
                liveDataReader.addSource(mSource!!) {
                    y -> liveDataReader.setWrappedValue(y)
                }
            }
        }
    })
    return liveDataReader
}
