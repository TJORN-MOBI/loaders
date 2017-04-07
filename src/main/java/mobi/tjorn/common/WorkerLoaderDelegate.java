package mobi.tjorn.common;

import android.os.Handler;
import android.os.OperationCanceledException;

import mobi.tjorn.content.loaders.WorkerLoader;

/**
 *
 */

public class WorkerLoaderDelegate<D, LM extends WorkerLoaderDelegate.WorkerLoaderMethods<D>> extends TaskLoaderDelegate<D, LM> {
    private final Object lock = new Object();
    private final Handler dispatcher = new Handler();
    private final Worker<D> worker;
    private ResultListener<D> resultListener;

    public WorkerLoaderDelegate(LM loader, Worker<D> worker) {
        super(loader);
        this.worker = worker;
    }

    public void onForceLoad() {
        loader.cancelLoadCompat();
        synchronized (lock) {
            resultListener = new ResultListenerImpl();
            worker.start(resultListener);
        }
    }

    public boolean onCancelLoad() {
        synchronized (lock) {
            if (resultListener != null) {
                worker.cancel();
                resultListener = null;
                return true;
            }
            return false;
        }
    }

    public interface WorkerLoaderMethods<D> extends TaskLoaderMethods<D> {

        /**
         * Called if the task was canceled before it was completed.  Gives the class a chance
         * to clean up post-cancellation and to properly dispose of the result.
         *
         * @param data The value that was returned by loadInBackground(), or null
         *             if the task threw {@link OperationCanceledException}.
         */
        void onCanceled(D data);
    }

    /**
     * A worker that loads its data on a worker thread.  The worker thread may run
     * in native code and deliver results through JNI - a scenario the {@link WorkerLoader}
     * was specifically designed for.
     *
     * @param <D> Data item to load.
     */
    public interface Worker<D> {
        /**
         * Called on UI thread to start loading data.
         *
         * @param listener A listener to receive results.
         */
        void start(ResultListener<D> listener);

        /**
         * Called on UI thread to cancel loading process.
         * The implementation may or may not interrupt the worker thread.
         * The implementation does not have to call {@link ResultListener#onResult(Object)}
         * after {@link Worker#cancel()} is called; if it does, the result is released
         * ({@link TaskLoaderDelegate.TaskLoaderMethods#releaseData(Object)})
         * and ignored (not delivered).
         */
        void cancel();
    }

    /**
     * Used by the {@link Worker} to deliver results of loading processes.
     *
     * @param <D> Loaded data item.
     */
    public interface ResultListener<D> {
        /**
         * <p>
         * Called by the {@link Worker} to deliver results of loading processes.
         * If this method is called after {@link Worker#cancel()} is called, the {@code result}
         * is released
         * ({@link TaskLoaderDelegate.TaskLoaderMethods#releaseData(Object)})
         * and ignored (not delivered).
         * </p>
         * <p>
         * It is encouraged to call this method directly on a worker thread.
         * </p>
         *
         * @param result Loaded results.
         */
        void onResult(D result);
    }


    private class ResultListenerImpl implements ResultListener<D> {
        @Override
        public void onResult(final D result) {
            synchronized (lock) {
                final boolean canceled = this != resultListener;
                resultListener = null;
                dispatcher.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canceled) {
                            loader.onCanceled(result);
                        } else {
                            loader.deliverResult(result);
                        }
                    }
                });
            }
        }
    }
}
