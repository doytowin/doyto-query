package win.doyto.query.mongodb.filter;

import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.core.test.TestQuery;
import win.doyto.query.mongodb.entity.BsonDeserializer;
import win.doyto.query.mongodb.test.geo.GeoQuery;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;
import win.doyto.query.util.BeanUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MongoFilterUtilTest
 *
 * @author f0rb on 2021-11-24
 */
class MongoFilterBuilderTest {

    private CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(
                    new StringCodec(), new IntegerCodec(), new DateCodec(),
                    new DocumentCodec(), new BsonDocumentCodec()
            ),
            CodecRegistries.fromProviders(new IterableCodecProvider())
    );

    @ParameterizedTest
    @CsvSource({
            "{}, {}",
            "{\"username\": \"test\"}, {\"username\": \"test\"}",
            "{\"usernameContain\": \"admin\"}, '{\"username\": {\"$regex\": \"admin\", \"$options\": \"\"}}'",
            "{\"idLt\": 20}, {\"id\": {\"$lt\": 20}}",
            "{\"idLe\": 20}, {\"id\": {\"$lte\": 20}}",
            "{\"createTimeLt\": \"2021-11-24\"}, {\"createTime\": {\"$lt\": {\"$date\": 1637712000000}}}",
            "{\"createTimeGt\": \"2021-11-24\"}, {\"createTime\": {\"$gt\": {\"$date\": 1637712000000}}}",
            "{\"createTimeGe\": \"2021-11-24\"}, {\"createTime\": {\"$gte\": {\"$date\": 1637712000000}}}",
            "'{\"idIn\": [1,2,3]}', '{\"id\": {\"$in\": [[1, 2, 3]]}}'",
            "'{\"idNotIn\": [1,2,3]}', '{\"id\": {\"$nin\": [[1, 2, 3]]}}'",
            "{\"userLevel\": \"VIP\"}, {\"userLevel\": 0}",
            "{\"userLevelNot\": \"VIP\"}, {\"userLevel\": {\"$ne\": 0}}",

    })
    void testFilterSuffix(String data, String expected) {
        TestQuery query = BeanUtil.parse(data, TestQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"size\":{\"hLt\":15}} | {\"size.h\": {\"$lt\": 15}}",
            "{\"size\":{\"hLt\":15,\"unit\":{\"name\":\"inch\"}}}" +
                    "| {\"size.h\": {\"$lt\": 15}, \"size.unit.name\": \"inch\"}",
    }, delimiter = '|')
    void testNestedFilter(String data, String expected) {
        InventoryQuery query = BeanUtil.parse(data, InventoryQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "item,desc | {\"item\": -1}",
            "item,asc | {\"item\": 1}",
            "item | {\"item\": 1}",
            "item,desc;qty,asc | {\"item\": -1, \"qty\": 1}",
            "item;qty,asc | {\"item\": 1, \"qty\": 1}",
    }, delimiter = '|')
    void buildSort(String sort, String expected) {
        Bson orderBy = MongoFilterBuilder.buildSort(sort);
        assertEquals(expected, orderBy.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"locNear\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc\": {\"$near\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"locNearSphere\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc\": {\"$nearSphere\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"loc2Near\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc2\": {\"$nearSphere\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"locCenter\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"radius\": 5.0}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$center\": [[1.0, 1.0], 5.0]}}}",
            "{\"locCenterSphere\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"radius\": 5.0}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$centerSphere\": [[1.0, 1.0], 5.0]}}}",
            "{\"locBox\": {\"p1\": {\"x\": 1.0, \"y\": 2.0}, \"p2\": {\"x\": 2.0, \"y\": 1.0}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}",
            "{\"locBsonBox\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}" +
                    "| {\"locBson\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}",
            "{\"locPy\": [[1.0, 1.0], [1.0, 2.0], [2.0, 2.0], [2.0, 1.0]]}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$polygon\": [[1.0, 1.0], [1.0, 2.0], [2.0, 2.0], [2.0, 1.0]]}}}",
    }, delimiter = '|')
    void testGeoQuery(String data, String expected) {
        BeanUtil.register(Bson.class, new BsonDeserializer());
        GeoQuery query = BeanUtil.parse(data, GeoQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
             "{\"locPolygon\": [[1.0, 1.0], [1.0, 2.0]]}  | Polygon query should provide at lease 3 points.",
    }, delimiter = '|')
    void failureCaseForGeoQuery(String data, String message) {
        GeoQuery query = BeanUtil.parse(data, GeoQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals("{}", filters.toBsonDocument(Document.class, codecRegistry).toJson(), message);
    }
}