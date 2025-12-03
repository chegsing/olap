package com.prueba.olap.adapter;

import com.prueba.olap.service.dto.AggregationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class H2CubeIntegrationTest {

    @Test
    void h2_inMemoryCube_queryReturnsRows() {
        // Setup H2 in-memory database
        DataSource ds = new DriverManagerDataSource("jdbc:h2:mem:olap_test;DB_CLOSE_DELAY=-1", "sa", "");
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(ds);

        // Create a simple fact table and insert sample data
        jdbc.getJdbcTemplate().execute("CREATE TABLE fact_sales (Region VARCHAR(50), Sales INT)");
        jdbc.getJdbcTemplate().execute("INSERT INTO fact_sales (Region, Sales) VALUES ('EMEA', 100)");
        jdbc.getJdbcTemplate().execute("INSERT INTO fact_sales (Region, Sales) VALUES ('APAC', 200)");

        // Create a view that represents the cube (logical)
        jdbc.getJdbcTemplate().execute("CREATE VIEW vw_olap_cube AS SELECT Region, Sales FROM fact_sales ORDER BY Region");

        // Use the adapter, pointing cubeView to the H2 view
        JdbcOlapAdapter adapter = new JdbcOlapAdapter(jdbc, "vw_olap_cube");

        // Use the logical token cube_view in the query; adapter will replace it
        AggregationResponse resp = adapter.query("SELECT Region, Sales FROM cube_view ORDER BY Region", Map.of());

        assertNotNull(resp);
        assertEquals(2, resp.getRows().size());
        // Verificar que los datos están ordenados correctamente
        assertEquals("APAC", resp.getRows().get(0).getValues().get("REGION")); // H2 convierte a mayúsculas
        assertEquals(200, resp.getRows().get(0).getValues().get("SALES"));
        assertEquals("EMEA", resp.getRows().get(1).getValues().get("REGION"));
        assertEquals(100, resp.getRows().get(1).getValues().get("SALES"));
    }
}