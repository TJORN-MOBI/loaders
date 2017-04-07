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

package mobi.tjorn.common;

/**
 * A class that implements common loader methods that do not depend
 * on how data are loaded.
 */
public class TaskLoaderDelegate<D, LM extends TaskLoaderDelegate.TaskLoaderMethods<D>> {
    protected final LM loader;
    protected D result;

    public TaskLoaderDelegate(LM loader) {
        this.loader = loader;
    }

    public void onStartLoading() {
        if (result != null) {
            loader.deliverResult(result);
        }
        if (loader.takeContentChanged() || result == null) {
            loader.forceLoad();
        }
    }

    public void onStopLoading() {
        loader.cancelLoadCompat();
    }

    public void onCanceled(D data) {
        if (data != null && !loader.isDataReleased(data)) {
            loader.releaseData(data);
        }
    }

    public void deliverResult(D data) {
        if (loader.isReset()) {
            if (data != null) {
                loader.releaseData(data);
            }
            return;
        }

        final D oldResult = result;
        result = data;

        if (loader.isStarted()) {
            loader.superDeliverResult(data);
        }

        if (oldResult != null && oldResult != data && !loader.isDataReleased(oldResult)) {
            loader.releaseData(oldResult);
        }
    }

    public void onReset() {
        loader.cancelLoadCompat();

        if (result != null && !loader.isDataReleased(result)) {
            loader.releaseData(result);
        }
        result = null;
    }

    /**
     *
     */
    public interface TaskLoaderMethods<D> {
        /**
         * Calls {@code super.deliverResult(data)} on {@link TaskLoaderDelegate#loader}.
         *
         * @param data Result to deliver.
         */
        void superDeliverResult(D data);

        /**
         * Reports data {@link D} states to {@link TaskLoaderDelegate}.
         *
         * @param data Data item whose state is being checked.
         * @return {@code false} for Not Released state. {@code true} for Released state.
         */
        boolean isDataReleased(D data);

        /**
         * Transitions data {@link D} from Not Released state to Released state.
         *
         * @param data Data item whose state is being changed.
         */
        void releaseData(D data);

        /**
         * Sends the result of the load to the registered listener.
         * Must be called from the process's main thread.
         *
         * @param data the result of the load
         */
        void deliverResult(D data);

        /**
         * Take the current flag indicating whether the loader's content had
         * changed while it was stopped.  If it had, true is returned and the
         * flag is cleared.
         */
        boolean takeContentChanged();

        /**
         * Force an asynchronous load. Unlike This will ignore a previously
         * loaded data set and load a new one.  You generally should only call this
         * when the loader is started -- that is, {@link #isStarted()} returns true.
         * <p>
         * Must be called from the process's main thread.
         */
        void forceLoad();

        /**
         * Return whether this load has been started.
         */
        boolean isStarted();

        /**
         * Return whether this load has been reset.  That is, either the loader
         * has not yet been started for the first time, or its reset()
         * has been called.
         */
        boolean isReset();

        /**
         * Attempt to cancel the current load task.
         * Must be called on the main thread of the process.
         *
         * <p>Cancellation is not an immediate operation, since the load is performed
         * in a background thread.  If there is currently a load in progress, this
         * method requests that the load be canceled, and notes this is the case;
         * once the background thread has completed its work its remaining state
         * will be cleared.  If another load request comes in during this time,
         * it will be held until the canceled load is complete.
         *
         * @return Returns <tt>false</tt> if the task could not be canceled,
         * typically because it has already completed normally, or
         * because startLoading() hasn't been called; returns
         * <tt>true</tt> otherwise.  When <tt>true</tt> is returned, the task
         * is still running and the OnLoadCanceledListener will be called
         * when the task completes.
         */
        boolean cancelLoadCompat();
    }
}
