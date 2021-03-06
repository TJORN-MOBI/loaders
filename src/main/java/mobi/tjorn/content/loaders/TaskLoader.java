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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Build;

import mobi.tjorn.content.common.BaseResult;
import mobi.tjorn.content.common.SimpleResult;
import mobi.tjorn.content.common.TaskLoaderDelegate;

/**
 * A loader that extends {@link android.content.AsyncTaskLoader}
 * and uses {@link android.os.AsyncTask} to load its data.
 * The data is loaded by {@link AsyncTaskLoader#loadInBackground()} method.
 * This loader that manages lifecycle of its data {@link D} parameter.
 * The data {@link D} states are:
 * <ul>
 * <li>Not Released</li>
 * <li>Released</li>
 * </ul>
 * If your data {@link D} parameter does not have a means to report loading error,
 * you might consider {@link ResultTaskLoader} and {@link BaseResult}.
 * If your data {@link D} parameter is always in Released state (e.g., {@link String}),
 * please take a look at {@link SimpleResultTaskLoader} and {@link SimpleResult}.
 */
public abstract class TaskLoader<D> extends AsyncTaskLoader<D> implements TaskLoaderDelegate.TaskLoaderMethods<D> {
    private final TaskLoaderDelegate<D, TaskLoaderDelegate.TaskLoaderMethods<D>> delegate;

    public TaskLoader(Context context) {
        super(context);
        this.delegate = new TaskLoaderDelegate<D, TaskLoaderDelegate.TaskLoaderMethods<D>>(this);
    }

    @Override
    protected void onStartLoading() {
        delegate.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        delegate.onStopLoading();
    }

    @Override
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
    public boolean cancelLoadCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return cancelLoad();
        } else {
            return onCancelLoad();
        }
    }
}
