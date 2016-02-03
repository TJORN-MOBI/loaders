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
import android.os.Handler;

/**
 *
 */
public abstract class WorkerLoader<D> extends BaseWorkerLoader<D> {
    private final Object lock = new Object();
    private final Handler dispatcher = new Handler();
    private final Worker<D> worker;
    private ResultListener<D> resultListener;
    private D result;

    public WorkerLoader(Context context, Worker<D> worker) {
        super(context);
        this.worker = worker;
    }

    protected abstract boolean isDataReleased(D data);
    protected abstract void releaseData(D data);

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
        }
        if (takeContentChanged() || result == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    public void onCanceled(D data) {
        if (data != null && !isDataReleased(data)) {
            releaseData(data);
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
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

    @Override
    public void deliverResult(D data) {
        if (isReset()) {
            if (data != null) {
                releaseData(data);
            }
            return;
        }

        final D oldResult = result;
        result = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldResult != null && oldResult != data && !isDataReleased(oldResult)) {
            releaseData(oldResult);
        }
    }

    @Override
    protected void onReset() {
        cancelLoad();

        if (result != null && !isDataReleased(result)) {
            releaseData(result);
        }
        result = null;
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