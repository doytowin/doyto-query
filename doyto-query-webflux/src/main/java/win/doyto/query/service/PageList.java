package win.doyto.query.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * PageList
 *
 * @author f0rb on 2021-10-29
 */
@Getter
@AllArgsConstructor
public class PageList<T> {
    private final List<T> list;
    private final long total;
}