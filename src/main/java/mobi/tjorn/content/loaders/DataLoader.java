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
public abstract class DataLoader<D> extends BaseLoader<D> {
    private D result;

    public DataLoader(Context context) {
        super(context);
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
    public void onCanceled(D data) {
        if (data != null && !isDataReleased(data)) {
            releaseData(data);
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
}
