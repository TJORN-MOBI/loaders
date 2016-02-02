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

/**
 *
 */
public abstract class ExternalLoader<D> extends BaseExternalLoader<D> {
    private final Object lock = new Object();
    private final ExternalWorker<D> worker;
    private ResultListener<D> resultListener;
    private D result;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public ExternalLoader(Context context, ExternalWorker<D> worker) {
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

    public interface ExternalWorker<D> {
        void start(ResultListener<D> listener);
        void cancel();
    }

    public interface ResultListener<D> {
        void onResult(D result);
    }

    private class ResultListenerImpl implements ResultListener<D> {
        @Override
        public void onResult(D result) {
            synchronized (lock) {
                if (this == resultListener) {
                    deliverResult(result);
                    resultListener = null;
                }
            }
        }
    }
}
