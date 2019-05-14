package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * PageList
 *
 * @author f0rb
 * @date 2019-05-13
 */
@Getter
@AllArgsConstructor
public class PageList<T> {
    public final List<T> list;
    public final long total;
}
