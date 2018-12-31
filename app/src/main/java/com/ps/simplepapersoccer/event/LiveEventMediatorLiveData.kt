package com.ps.simplepapersoccer.event

import androidx.lifecycle.MediatorLiveData

open class LiveEventMediatorLiveData<T>: MediatorLiveData<LiveEvent<T?>>() {
    fun setWrappedValue(value: T?) {
        super.setValue(LiveEvent(value))
    }
}