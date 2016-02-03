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

import android.content.Loader;

/**
 * A class that implements common loader methods that do not depend
 * on how data are loaded.
 */
public abstract  class LoaderDelegate<D, L extends Loader<D> & LoaderDelegate.SuperCaller<D>> {
    private final L loader;
    private D result;

    public LoaderDelegate(L loader) {
        this.loader = loader;
    }

    /**
     * Reports data {@link D} states to {@link LoaderDelegate}.
     * @param data Data item whose state is being checked.
     * @return {@code false} for Not Released state. {@code true} for Released state.
     */
    protected abstract boolean isDataReleased(D data);

    /**
     * Transitions data {@link D} from Not Released state to Released state.
     * @param data Data item whose state is being changed.
     */
    protected abstract void releaseData(D data);

    public void onStartLoading() {
        if (result != null) {
            loader.deliverResult(result);
        }
        if (loader.takeContentChanged() || result == null) {
            loader.forceLoad();
        }
    }

    public void onStopLoading() {
        loader.cancelLoad();
    }

    public void onCanceled(D data) {
        if (data != null && !isDataReleased(data)) {
            releaseData(data);
        }
    }

    public void deliverResult(D data) {
        if (loader.isReset()) {
            if (data != null) {
                releaseData(data);
            }
            return;
        }

        final D oldResult = result;
        result = data;

        if (loader.isStarted()) {
            loader.superDeliverResult(data);
        }

        if (oldResult != null && oldResult != data && !isDataReleased(oldResult)) {
            releaseData(oldResult);
        }
    }

    protected void onReset() {
        loader.cancelLoad();

        if (result != null && !isDataReleased(result)) {
            releaseData(result);
        }
        result = null;
    }

    public interface SuperCaller<D> {
        void superDeliverResult(D data);
    }
}
