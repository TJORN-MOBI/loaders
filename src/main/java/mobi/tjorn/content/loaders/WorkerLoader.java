/*
 * Copyright 2016 TJORN LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobi.tjorn.content.loaders;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;

/**
 * A loader that extends {@link android.content.Loader} and uses
 * {@link Worker} to load its data.
 * The actual load happens on some worker thread.  The worker thread
 * may even run in native code and results can be delivered through JNI.
 * This loader manages lifecycle of its data {@link D} parameter.
 * The data {@link D} states are:
 * <ul>
 *     <li>Not Released</li>
 *     <li>Released</li>
 * </ul>
 */
public abstract class WorkerLoader<D> extends Loader<D> implements LoaderDelegate.SuperCaller<D> {
    private final Object lock = new Object();
    private final Handler dispatcher = new Handler();
    private final Worker<D> worker;
    private ResultListener<D> resultListener;
    private final LoaderDelegate<D, WorkerLoader<D>> delegate = new LoaderDelegate<D, WorkerLoader<D>>(this) {
        @Override
        protected boolean isDataReleased(D data) {
            return WorkerLoader.this.isDataReleased(data);
        }

        @Override
        protected void releaseData(D data) {
            WorkerLoader.this.releaseData(data);
        }
    };

    public WorkerLoader(Context context, Worker<D> worker) {
        super(context);
        this.worker = worker;
    }

    /**
     * Reports data {@link D} states to {@link DataLoader}.
     * @param data Data item whose state is being checked.
     * @return {@code false} for Not Released state. {@code true} for Released state.
     */
    protected abstract boolean isDataReleased(D data);

    /**
     * Transitions data {@link D} from Not Released state to Released state.
     * @param data Data item whose state is being changed.
     */
    protected abstract void releaseData(D data);

    @Override
    protected void onStartLoading() {
        delegate.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        delegate.onStopLoading();
    }

    public void onCanceled(D data) {
        delegate.onCanceled(data);
    }

    @Override
    public void deliverResult(D data) {
        delegate.deliverResult(data);
    }

    @Override
    protected void onReset() {
        delegate.onReset();
    }

    @Override
    public void superDeliverResult(D data) {
        super.deliverResult(data);
    }

    @Override
    protected void onForceLoad() {
        cancelLoad();
        synchronized (lock) {
            resultListener = new ResultListenerImpl();
            worker.start(resultListener);
        }
    }

    @Override
    protected boolean onCancelLoad() {
        synchronized (lock) {
            if (resultListener != null) {
                worker.cancel();
                resultListener = null;
                return true;
            }
            return false;
        }
    }

    public interface Worker<D> {
        void start(ResultListener<D> listener);
        void cancel();
    }

    public interface ResultListener<D> {
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
                            onCanceled(result);
                        } else {
                            deliverResult(result);
                        }
                    }
                });
            }
        }
    }
}