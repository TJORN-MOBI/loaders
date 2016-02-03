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

/**
 * A loader that extends {@link android.content.AsyncTaskLoader}
 * and uses {@link android.os.AsyncTask} to load its data.
 * The data is loaded by {@link AsyncTaskLoader#loadInBackground()} method.
 * This loader that manages lifecycle of its data {@link D} parameter.
 * The data {@link D} states are:
 * <ul>
 *     <li>Not Released</li>
 *     <li>Released</li>
 * </ul>
 * If your data {@link D} parameter does not have a means to report loading error,
 * you might consider {@link ResultLoader} and {@link BaseResult}.
 * If your data {@link D} parameter is always in Released state (e.g., {@link String}),
 * please take a look at {@link SimpleResultLoader} and {@link SimpleResult}.
 */
public abstract class TaskLoader<D> extends AsyncTaskLoader<D> implements LoaderDelegate.SuperCaller<D> {
    private final LoaderDelegate<D, TaskLoader<D>> delegate = new LoaderDelegate<D, TaskLoader<D>>(this) {
        @Override
        protected boolean isDataReleased(D data) {
            return TaskLoader.this.isDataReleased(data);
        }

        @Override
        protected void releaseData(D data) {
            TaskLoader.this.releaseData(data);
        }
    };

    public TaskLoader(Context context) {
        super(context);
    }

    /**
     * Reports data {@link D} states to {@link TaskLoader}.
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
    public void superDeliverResult(D data) {
        super.deliverResult(data);
    }
}
