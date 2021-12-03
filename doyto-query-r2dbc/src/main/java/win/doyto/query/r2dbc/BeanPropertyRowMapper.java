package win.doyto.query.r2dbc;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BeanPropertyRowMapper
 *
 * @author f0rb on 2021-10-26
 */
@Slf4j
public class BeanPropertyRowMapper<E> implements RowMapper<E> {
    private final Class<E> mappedClass;
    private final Map<String, PropertyDescriptor> fieldMap = new LinkedHashMap<>();

    public BeanPropertyRowMapper(Class<E> mappedClass) {
        this.mappedClass = mappedClass;
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(mappedClass);
        Arrays.stream(propertyDescriptors)
              .filter(pd -> pd.getWriteMethod() != null)
              .forEach(pd -> this.fieldMap.put(pd.getName(), pd));
    }

    @Override
    public E apply(Row row, RowMetadata rowMetadata) {
        E entity = BeanUtils.instantiateClass(mappedClass);

        Object value;
        for (PropertyDescriptor pd : fieldMap.values()) {
            try {
                value = row.get(pd.getName(), pd.getPropertyType());
            } catch (IllegalArgumentException e) {
                log.error("Fail to get value for [{}]: {}", pd.getName(), e.getMessage());
                continue;
            }
            try {
                pd.getWriteMethod().invoke(entity, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Fail to invoke write method: {}-{}", pd.getWriteMethod().getName(), e.getMessage());
            }
        }
        return entity;
    }

}
