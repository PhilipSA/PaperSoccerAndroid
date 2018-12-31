package com.ps.simplepapersoccer.event

import com.ps.simplepapersoccer.event.LiveEventLiveData

class CompletableLiveEventLiveData: LiveEventLiveData<Boolean>() {
    fun call() {
        setWrappedValue(true)
    }
}