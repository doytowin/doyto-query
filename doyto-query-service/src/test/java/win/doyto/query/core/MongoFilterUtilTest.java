package win.doyto.query.core;

import org.bson.Document;
import org.bson.codecs.StringCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.test.TestQuery;
import win.doyto.query.data.inventory.InventoryQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MongoFilterUtilTest
 *
 * @author f0rb on 2021-11-24
 */
class MongoFilterUtilTest {
    @Test
    void filterWithEq() {
        CodecRegistry codecRegistry = CodecRegistries.fromCodecs(new StringCodec());
        Bson filters = MongoFilterUtil.buildFilter(TestQuery.builder().username("test").build());
        assertEquals("{\"username\": \"test\"}", filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @Test
    void filterWithContain() {
        CodecRegistry codecRegistry = CodecRegistries.fromCodecs(new StringCodec());
        Bson filters = MongoFilterUtil.buildFilter(InventoryQuery.builder().itemContain("test").build());
        assertEquals("{\"item\": {\"$regex\": \"test\", \"$options\": \"\"}}", filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }
}