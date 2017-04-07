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

package mobi.tjorn.support.content.loaders;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.Loader;

import mobi.tjorn.common.BaseResult;
import mobi.tjorn.common.SimpleResult;
import mobi.tjorn.common.WorkerLoaderDelegate;
import mobi.tjorn.content.loaders.ResultWorkerLoader;
import mobi.tjorn.content.loaders.SimpleResultWorkerLoader;

/**
 * A loader that extends {@link Loader} and uses
 * {@link WorkerLoaderDelegate.Worker} to load its data.
 * The actual load happens on some worker thread.  The worker thread
 * may even run in native code and results can be delivered through JNI.
 * This loader manages lifecycle of its data {@link D} parameter.
 * The data {@link D} states are:
 * <ul>
 * <li>Not Released</li>
 * <li>Released</li>
 * </ul>
 * If your data {@link D} parameter does not have a means to report loading error,
 * you might consider {@link ResultWorkerLoader} and {@link BaseResult}.
 * If your data {@link D} parameter is always in Released state (e.g., {@link String}),
 * please take a look at {@link SimpleResultWorkerLoader} and {@link SimpleResult}.
 */
public abstract class WorkerLoader<D> extends Loader<D> implements WorkerLoaderDelegate.WorkerLoaderMethods<D> {
    private final WorkerLoaderDelegate<D, WorkerLoaderDelegate.WorkerLoaderMethods<D>> delegate;

    public WorkerLoader(Context context, WorkerLoaderDelegate.Worker<D> worker) {
        super(context);
        this.delegate = new WorkerLoaderDelegate<D, WorkerLoaderDelegate.WorkerLoaderMethods<D>>(this, worker);
    }

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
    public final void superDeliverResult(D data) {
        super.deliverResult(data);
    }

    @Override
    protected void onForceLoad() {
        delegate.onForceLoad();
    }

    @Override
    protected boolean onCancelLoad() {
        return delegate.onCancelLoad();
    }

    @Override
    public boolean cancelLoadCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return cancelLoad();
        } else {
            return onCancelLoad();
        }
    }
}