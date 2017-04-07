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

import mobi.tjorn.content.loaders.ResultTaskLoader;

/**
 * A result whose lifecycle is managed by {@link ResultTaskLoader}.
 * The {@link Result} states are:
 * <ul>
 *     <li>Not Released</li>
 *     <li>Released</li>
 * </ul>
 * Implement this interface on your own result if neither {@link BaseResult} nor
 * {@link SimpleResult} fit your requirements.
 */
public interface Result {
    /**
     * Reports result states to {@link ResultTaskLoader}.
     * @return {@code false} for Not Released state. {@code true} for Released state.
     */
    boolean isReleased();

    /**
     * Transitions {@link Result} from Not Released state to Released state.
     */
    void release();
}