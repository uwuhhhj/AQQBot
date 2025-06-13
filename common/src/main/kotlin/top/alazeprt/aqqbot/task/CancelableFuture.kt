package top.alazeprt.aqqbot.task

import java.util.concurrent.CompletableFuture

class CancelableFuture(private val cancelAction: () -> Unit) : CompletableFuture<Void>() {
    @Volatile
    var runningThread: Thread? = null

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        cancelAction.invoke()
        runningThread?.interrupt()
        return super.cancel(mayInterruptIfRunning)
    }
}
