package com.prueba.olap.adapter;

import com.prueba.olap.service.dto.AggregationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

class JdbcOlapAdapterTest {

    @Test
    void query_replacesCubeViewAndReturnsRows() {
        var jdbc = Mockito.mock(JdbcTemplate.class);
        Mockito.when(jdbc.queryForList(Mockito.anyString())).thenReturn(List.of(Map.of("col", "v")));

        var adapter = new JdbcOlapAdapter(jdbc, "my_view");
        AggregationResponse resp = adapter.query("SELECT col FROM cube_view");

        Mockito.verify(jdbc).queryForList("SELECT col FROM my_view");
        Assertions.assertEquals(1, resp.getRows().size());
        Assertions.assertEquals("v", resp.getRows().get(0).getValues().get("col"));
    }
}
