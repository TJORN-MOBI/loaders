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
 *  A simple {@link BaseResult} that can be only in Released state.  Examples:
 *  <ul>
 *      <li>{@link String}</li>
 *      <li>{@link Integer}</li>
 *      <li>any other type that does not allocate resources and does not need to be released</li>
 *  </ul>
 */
public class SimpleResult<D> extends BaseResult<D, Throwable> {
    public SimpleResult(D data) {
        super(data, null);
    }

    public SimpleResult(Throwable error) {
        super(null, error);
    }

    @Override
    public boolean isReleased() {
        return true;
    }

    @Override
    public void release() {
    }
}
