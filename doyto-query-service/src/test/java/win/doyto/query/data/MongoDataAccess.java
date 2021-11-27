package win.doyto.query.data;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.MongoFilterUtil;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Table;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static win.doyto.query.core.MongoFilterUtil.buildFilter;
import static win.doyto.query.core.MongoFilterUtil.buildSort;

/**
 * MongoDataAccess
 *
 * @author f0rb on 2021-11-23
 */
@Slf4j
public class MongoDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements DataAccess<E, I, Q> {
    private static final String MONGO_ID = "_id";
    private final Class<E> entityClass;
    @Getter
    private final MongoCollection<Document> collection;

    @SuppressWarnings("unchecked")
    public MongoDataAccess(MongoClient mongoClient, Class<E> testEntityClass) {
        this.entityClass = testEntityClass;
        Table table = testEntityClass.getAnnotation(Table.class);
        MongoDatabase database = mongoClient.getDatabase(table.catalog());
        this.collection = database.getCollection(table.name());
    }

    private void setObjectId(E entity, Document document) {
        ObjectId objectId = (ObjectId) document.get(MONGO_ID);
        if (entity instanceof ObjectIdAware) {
            ((ObjectIdAware) entity).setObjectId(objectId);
        }
    }

    private Bson getIdFilter(Object id) {
        return eq(MONGO_ID, new ObjectId(id.toString()));
    }

    private Bson buildFilterForChange(Q query) {
        return query.needPaging() ? in(MONGO_ID, queryOid(query)) : buildFilter(query);
    }

    @Override
    public List<E> query(Q query) {
        return queryColumns(query, entityClass);
    }

    @Override
    public long count(Q query) {
        return collection.countDocuments(buildFilter(query));
    }

    @Override
    public <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns) {
        List<V> list = new ArrayList<>();
        FindIterable<Document> findIterable = collection
                .find(buildFilter(query))
                .projection(Projections.include(columns));
        if (query.getSort() != null) {
            findIterable.sort(buildSort(query.getSort()));
        }
        if (query.needPaging()) {
            findIterable.skip(query.calcOffset()).limit(query.getPageSize());
        }
        findIterable.forEach((Consumer<Document>) document -> {
            V e;
            if (columns.length == 1) {
                e = document.getEmbedded(splitToKeys(columns[0]), clazz);
            } else {
                e = BeanUtil.parse(document.toJson(), clazz);
            }
            if (log.isDebugEnabled()) {
                log.debug("Entity parsed: {}", BeanUtil.stringify(e));
            }
            list.add(e);
        });
        return list;
    }

    private List<String> splitToKeys(String column) {
        return Arrays.asList(StringUtils.split(column, "\\."));
    }

    @Override
    public E get(IdWrapper<I> w) {
        FindIterable<Document> findIterable = collection.find(getIdFilter(w.getId()));
        for (Document document : findIterable) {
            return BeanUtil.parse(document.toJson(), entityClass);
        }
        return null;
    }

    @Override
    public int delete(IdWrapper<I> w) {
        return (int) collection.deleteOne(getIdFilter(w.getId())).getDeletedCount();
    }

    @Override
    public int delete(Q query) {
        Bson inId = buildFilterForChange(query);
        return (int) collection.deleteMany(inId).getDeletedCount();
    }

    @Override
    public void create(E entity) {
        Document document = BeanUtil.convertToIgnoreNull(entity, Document.class);
        collection.insertOne(document);
        setObjectId(entity, document);
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        List<Document> documents = new ArrayList<>();
        for (E entity : entities) {
            documents.add( BeanUtil.convertToIgnoreNull(entity, Document.class));
        }
        collection.insertMany(documents);
        int i = 0;
        for (E entity : entities) {
            setObjectId(entity, documents.get(i));
            i++;
        }
        return documents.size();
    }

    @Override
    public int update(E e) {
        Bson filter = getIdFilter(e.getId());
        Document replacement = BeanUtil.convertTo(e, Document.class);
        replacement.remove(MONGO_ID);
        return (int) collection.replaceOne(filter, replacement).getModifiedCount();
    }

    @Override
    public int patch(E e) {
        Bson updates = MongoFilterUtil.buildUpdates(e);
        return (int) collection.updateOne(getIdFilter(e.getId()), updates).getModifiedCount();
    }

    @Override
    public int patch(E e, Q q) {
        Bson updates = MongoFilterUtil.buildUpdates(e);
        Bson inId = buildFilterForChange(q);
        return (int) collection.updateMany(inId, updates).getModifiedCount();
    }

    @Override
    public List<I> queryIds(Q query) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<ObjectId> queryOid(Q query) {
        return queryColumns(query, ObjectId.class, MONGO_ID);
    }
}
