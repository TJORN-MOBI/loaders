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

import mobi.tjorn.content.common.Result;

/**
 * A base Loader that manages lifecycle of its {@link Result}.
 */
public abstract class ResultTaskLoader<R extends Result> extends TaskLoader<R> {
    protected ResultTaskLoader(Context context) {
        super(context);
    }

    @Override
    public final boolean isDataReleased(R data) {
        return data.isReleased();
    }

    @Override
    public final void releaseData(R data) {
        data.release();
    }
}
