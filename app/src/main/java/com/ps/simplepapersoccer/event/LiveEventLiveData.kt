package com.ps.simplepapersoccer.event

import androidx.lifecycle.LiveData

open class LiveEventLiveData<T>: LiveData<LiveEvent<T?>>() {
    fun setWrappedValue (value: T?) {
        super.setValue(LiveEvent(value))
    }

    fun postWrappedValue(value: T?) {
        super.postValue(LiveEvent(value))
    }
}