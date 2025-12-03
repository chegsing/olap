package com.prueba.olap.adapter;

import com.prueba.olap.service.dto.AggregationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.util.Map;

class JdbcOlapAdapterTest {

    @Test
    void query_replacesCubeViewAndReturnsRows() {
        NamedParameterJdbcTemplate jdbc = Mockito.mock(NamedParameterJdbcTemplate.class);
        SqlRowSet rowSet = Mockito.mock(SqlRowSet.class);
        SqlRowSetMetaData metaData = Mockito.mock(SqlRowSetMetaData.class);
        
        Mockito.when(jdbc.queryForRowSet(Mockito.anyString(), Mockito.any(MapSqlParameterSource.class)))
               .thenReturn(rowSet);
        Mockito.when(rowSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rowSet.getMetaData()).thenReturn(metaData);
        Mockito.when(metaData.getColumnCount()).thenReturn(1);
        Mockito.when(metaData.getColumnLabel(1)).thenReturn("col");
        Mockito.when(rowSet.getObject(1)).thenReturn("v");

        JdbcOlapAdapter adapter = new JdbcOlapAdapter(jdbc, "my_view");
        AggregationResponse resp = adapter.query("SELECT col FROM cube_view", Map.of());

        Mockito.verify(jdbc).queryForRowSet(Mockito.eq("SELECT col FROM my_view"), 
                                           Mockito.any(MapSqlParameterSource.class));
        Assertions.assertEquals(1, resp.getRows().size());
        Assertions.assertEquals("v", resp.getRows().get(0).getValues().get("col"));
    }
}
