package win.doyto.query.data;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Table;

import static com.mongodb.client.model.Filters.eq;
import static win.doyto.query.core.MongoFilterUtil.buildFilter;

/**
 * MongoDataAccess
 *
 * @author f0rb on 2021-11-23
 */
@Slf4j
public class MongoDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {
    private final Class<E> entityClass;
    @Getter
    private final MongoCollection<Document> collection;

    public MongoDataAccess(MongoClient mongoClient, Class<E> testEntityClass) {
        this.entityClass = testEntityClass;
        Table table = testEntityClass.getAnnotation(Table.class);
        MongoDatabase database = mongoClient.getDatabase(table.catalog());
        this.collection = database.getCollection(table.name());
    }

    @Override
    public List<E> query(Q query) {
        FindIterable<Document> findIterable = collection.find(buildFilter(query));
        List<E> list = new ArrayList<>();
        findIterable.forEach((Consumer<Document>) document -> {
            E e = BeanUtil.parse(document.toJson(), entityClass);
            if (log.isDebugEnabled()) {
                log.debug("Entity parsed: {}", BeanUtil.stringify(e));
            }
            list.add(e);
        });
        return list;
    }

    @Override
    public long count(Q query) {
        return collection.countDocuments(buildFilter(query));
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        return null;
    }

    @Override
    public E get(IdWrapper<I> w) {
        FindIterable<Document> findIterable = collection.find(eq("_id", w.getId()));
        for (Document document : findIterable) {
            return BeanUtil.parse(document.toJson(), entityClass);
        }
        return null;
    }

    @Override
    public int delete(IdWrapper<I> w) {
        return 0;
    }

    @Override
    public int delete(Q query) {
        return (int) collection.deleteMany(buildFilter(query)).getDeletedCount();
    }

    @Override
    public void create(E e) {
    }

    @Override
    public int update(E e) {
        return 0;
    }

    @Override
    public int patch(E e) {
        return 0;
    }

    @Override
    public int patch(E e, Q q) {
        return 0;
    }

    @Override
    public List<I> queryIds(Q query) {
        return null;
    }
}
