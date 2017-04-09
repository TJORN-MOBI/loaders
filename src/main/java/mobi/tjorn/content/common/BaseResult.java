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

package mobi.tjorn.content.common;

/**
 * A simple class that can be used as a {@link Result} of Result* Loaders.  Contains either
 * a {@link BaseResult#data} field or an {@link BaseResult#error} field.
 */
public abstract class BaseResult<D, T> implements Result {
    private final D data;
    private final T error;

    /**
     * Initializes new result.
     *
     * @param data  A value for {@link BaseResult#data} field.
     * @param error A value for {@link BaseResult#error} field.
     */
    protected BaseResult(D data, T error) {
        this.data = data;
        this.error = error;
    }

    /**
     * Returns value of {@link BaseResult#data} field.
     *
     * @return Value of {@link BaseResult#data} field.
     */
    public D getData() {
        return data;
    }

    /**
     * Returns value of {@link BaseResult#error} field.
     *
     * @return Value of {@link BaseResult#error} field.
     */
    public T getError() {
        return error;
    }

    /**
     * Checks if {@link BaseResult} has a non-{@code null} {@link BaseResult#error} field.
     *
     * @return {@code true} if {@link BaseResult#error} is not {@code null},
     * {@code false} otherwise.
     */
    public boolean hasError() {
        return error != null;
    }
}
