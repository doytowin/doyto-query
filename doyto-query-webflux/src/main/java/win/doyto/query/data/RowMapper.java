package win.doyto.query.data;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.util.function.BiFunction;

/**
 * RowMapper
 *
 * @author f0rb on 2021-09-02
 */
public interface RowMapper<E> extends BiFunction<Row, RowMetadata, E> {
}
