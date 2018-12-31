package com.ps.simplepapersoccer.event

import androidx.lifecycle.Observer

open class LiveEvent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    internal fun getContentIfNotHandled(): Boolean? {
        return if (hasBeenHandled) {
            true
        } else {
            hasBeenHandled = true
            return false
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}

class EventObserver<T>(private val onEventUnhandledContent: (T?) -> Unit) : Observer<LiveEvent<T>> {
    override fun onChanged(event: LiveEvent<T>?) {
        if (event?.getContentIfNotHandled() == false) {
            onEventUnhandledContent(event.peekContent())
        }
    }
}