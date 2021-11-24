package win.doyto.query.core;

import org.bson.Document;
import org.bson.codecs.DateCodec;
import org.bson.codecs.IntegerCodec;
import org.bson.codecs.StringCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.core.test.TestQuery;
import win.doyto.query.util.BeanUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MongoFilterUtilTest
 *
 * @author f0rb on 2021-11-24
 */
class MongoFilterUtilTest {

    private CodecRegistry codecRegistry = CodecRegistries.fromCodecs(
            new StringCodec(), new IntegerCodec(), new DateCodec());

    @ParameterizedTest
    @CsvSource({
            "{\"username\": \"test\"}, {\"username\": \"test\"}",
            "{\"usernameContain\": \"admin\"}, '{\"username\": {\"$regex\": \"admin\", \"$options\": \"\"}}'",
            "{\"idLt\": 20}, {\"id\": {\"$lt\": 20}}",
            "{\"idLe\": 20}, {\"id\": {\"$lte\": 20}}",
            "{\"createTimeLt\": \"2021-11-24\"}, {\"createTime\": {\"$lt\": {\"$date\": 1637712000000}}}",
            "{\"createTimeGt\": \"2021-11-24\"}, {\"createTime\": {\"$gt\": {\"$date\": 1637712000000}}}",
            "{\"createTimeGe\": \"2021-11-24\"}, {\"createTime\": {\"$gte\": {\"$date\": 1637712000000}}}",
    })
    void testFilterSuffix(String data, String expected) {
        TestQuery query = BeanUtil.parse(data, TestQuery.class);
        Bson filters = MongoFilterUtil.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

}