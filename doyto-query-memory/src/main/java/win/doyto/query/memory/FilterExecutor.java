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

package win.doyto.query.memory;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.QuerySuffix;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static win.doyto.query.core.QuerySuffix.*;

/**
 * FilterExecutor
 *
 * @author f0rb on 2021-12-10
 */
@SuppressWarnings("unchecked")
@UtilityClass
class FilterExecutor {

    static final Map<QuerySuffix, Matcher> map = new EnumMap<>(QuerySuffix.class);

    static {
        map.put(Like, new LikeMatcher());
        map.put(NotLike, new NotLikeMatcher());
        map.put(Start, new StartMatcher());
        map.put(End, new EndMatcher());
        map.put(Null, new NullMatcher());
        map.put(NotNull, new NotNullMatcher());
        map.put(In, (qv, ev) -> ((Collection<?>) qv).contains(ev));
        map.put(NotIn, (qv, ev) -> !((Collection<?>) qv).contains(ev));
        map.put(Gt, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) > 0);
        map.put(Lt, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) < 0);
        map.put(Ge, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) >= 0);
        map.put(Le, (qv, ev) -> ((Comparable<Object>) ev).compareTo(qv) <= 0);
        map.put(Not, (qv, ev) -> !qv.equals(ev));
    }

    static Matcher get(QuerySuffix querySuffix) {
        return map.getOrDefault(querySuffix, Object::equals);
    }

    static class LikeMatcher implements Matcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return StringUtils.contains(ev.toString(), qv.toString());
        }

        @Override
        public boolean isComparable(Object qv, Object ev) {
            return ev instanceof String;
        }
    }

    static class NotLikeMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return !super.doMatch(qv, ev);
        }
    }

    static class StartMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return StringUtils.startsWith(ev.toString(), qv.toString());
        }
    }

    static class EndMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return StringUtils.endsWith(ev.toString(), qv.toString());
        }
    }

    static class NotNullMatcher implements Matcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return ev != null;
        }

        @Override
        public boolean isComparable(Object qv, Object ev) {
            return true;
        }
    }

    static class NullMatcher extends NotNullMatcher {
        @Override
        public boolean doMatch(Object qv, Object ev) {
            return !super.doMatch(qv, ev);
        }
    }

}
