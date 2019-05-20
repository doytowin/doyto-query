package win.doyto.query.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * MapperTableDriver
 *
 * @author f0rb
 */
@Slf4j
public class MapperTableDriver extends XMLLanguageDriver {

    private Set<Class<?>> knownMapperSet = new HashSet<>();
    private MapperTable lastMapperTable;

    /**
     * 替换SQL的@table@为{@link MapperTable}配置的表名
     * <p>
     * 用例场景
     * 1.继承IMapper的mapper表名替换
     * 2.继承IMapper的mapper的多个方法的表名替换
     * 3.含有未继承IMapper的mapper
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        MapperTable mapperTable = lastMapperTable;
        Collection<Class<?>> mappers = configuration.getMapperRegistry().getMappers();
        if (knownMapperSet.size() < mappers.size()) {
            for (Class<?> clazz : mappers) {
                if (!knownMapperSet.contains(clazz)) {
                    mapperTable = clazz.getAnnotation(MapperTable.class);
                    if (mapperTable != null) {
                        lastMapperTable = mapperTable;
                        break;
                    }
                }
            }
            // mappers是无序的
            knownMapperSet.addAll(mappers);
        }
        if (mapperTable != null) {
            script = script.replaceAll("@\\{table}", mapperTable.value());
        } else {
            throw new IllegalStateException("@MapperTable unconfigured!");
        }

        return super.createSqlSource(configuration, script, parameterType);
    }
}
