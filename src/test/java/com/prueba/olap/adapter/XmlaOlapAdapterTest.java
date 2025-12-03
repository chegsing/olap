package com.prueba.olap.adapter;

import com.prueba.olap.service.dto.AggregationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class XmlaOlapAdapterTest {

    @Test
    void query_parsesSimpleXmlaResponse() {
        String fakeResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<root><row><Region>EMEA</Region><Sales>100</Sales></row></root>";

        var adapter = new XmlaOlapAdapter(envelope -> fakeResponse, "TestCatalog");
        AggregationResponse resp = adapter.query("MDX: SELECT ...");

        Assertions.assertNotNull(resp);
        Assertions.assertEquals(1, resp.getRows().size());
        Assertions.assertEquals("EMEA", resp.getRows().get(0).getValues().get("Region"));
        Assertions.assertEquals("100", resp.getRows().get(0).getValues().get("Sales"));
    }

    @Test
    void query_rejectsNonMdx() {
        var adapter = new XmlaOlapAdapter(envelope -> "", "TestCatalog");
        Assertions.assertThrows(IllegalArgumentException.class, () -> adapter.query("SELECT * FROM table"));
    }
}
