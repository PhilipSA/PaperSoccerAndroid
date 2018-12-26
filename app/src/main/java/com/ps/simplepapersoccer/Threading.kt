package com.ps.simplepapersoccer

import android.os.AsyncTask

class ThreadRunner<T>(
        private val block: () -> T,
        private val postExecute: ((T?, Throwable?) -> Unit)?
) : AsyncTask<Unit, Unit, ThreadRunner.Result<T>>() {

    override fun doInBackground(vararg params: Unit?): Result<T> {
        return try {
            Result(block(), null)
        } catch (ex: Throwable) {
            Result(null, ex)
        }
    }

    override fun onPostExecute(result: Result<T>) {
        if (postExecute != null) {
            postExecute.invoke(result.result, result.exception)
        } else if (result.exception != null) {
            throw result.exception
        }
    }

    class Result<out T>(val result: T?, val exception: Throwable?)
}

fun runOnBgThread(block: () -> Unit): ThreadRunner<Unit> {
    val tr = ThreadRunner(block, null)
    tr.execute()
    return tr
}

fun <T> runOnBgThread(backgroundBlock: () -> T, postExecute: ((T?, Throwable?) -> Unit)? = null) : ThreadRunner<T> {
    val tr =  ThreadRunner(backgroundBlock, postExecute)
    tr.execute()
    return tr
}