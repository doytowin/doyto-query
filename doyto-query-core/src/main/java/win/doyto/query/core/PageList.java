package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * PageList
 *
 * @author f0rb
 */
@Getter
@AllArgsConstructor
public class PageList<T> {
    private final List<T> list;
    private final long total;
}
