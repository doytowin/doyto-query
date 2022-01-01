/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.sql;

/**
 * StringJoiner
 *
 * @author f0rb on 2019-06-04
 */
class StringJoiner {
    private final char[] joiner;
    private final String[] list;
    private int count = 0;
    private int cursor = 0;

    public StringJoiner(String joiner, int size) {
        this.joiner = joiner.toCharArray();
        list = new String[size];
    }

    public StringJoiner append(String str) {
        list[cursor++] = str;
        count += str.length();
        // 追加扩容策略
        return this;
    }

    public boolean isEmpty() {
        return cursor <= 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }

        String[] strings = list;
        int joinerLength = joiner.length;
        int capacity = count + joinerLength * (cursor - 1);
        char[] chars = new char[capacity];

        String str = strings[0];
        int length = str.length();
        str.getChars(0, length, chars, 0);

        int location = length;

        for (int i = 1; i < cursor; i++) {

            System.arraycopy(joiner, 0, chars, location, joinerLength);
            location += joinerLength;

            str = strings[i];
            length = str.length();
            str.getChars(0, length, chars, location);

            location += length;
        }

        return new String(chars);
    }

}
