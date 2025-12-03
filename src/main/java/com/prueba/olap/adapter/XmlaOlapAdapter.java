package com.prueba.olap.adapter;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;
import com.prueba.olap.service.dto.AggregationRow;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Adaptador XMLA que envía consultas MDX envueltas en SOAP a un endpoint XMLA.
 * Implementa el patrón Adapter de arquitectura hexagonal.
 * - Espera consultas MDX con prefijo "MDX:" 
 * - Usa solo APIs del JDK (HttpClient, DOM)
 */
public class XmlaOlapAdapter implements OlapQueryPort {

    private static final String MDX_PREFIX = "MDX:";
    private static final int TIMEOUT_SECONDS = 20;
    private static final String SOAP_ACTION = "urn:schemas-microsoft-com:xml-analysis:Execute";
    
    private final Function<String, String> fetcher;
    private final String catalog;

    public XmlaOlapAdapter(String endpointUrl, String catalog, String username, String password) {
        validateParameters(endpointUrl, catalog);
        this.catalog = catalog;
        this.fetcher = this.createHttpFetcher(endpointUrl);
    }

    // Constructor para tests
    public XmlaOlapAdapter(Function<String, String> fetcher, String catalog) {
        this.fetcher = fetcher;
        this.catalog = catalog;
    }

    @Override
    public AggregationResponse query(String query, Map<String, Object> params) {
        validateQuery(query);
        String mdx = extractMdxQuery(query);
        String envelope = buildEnvelope(mdx);
        String xml = fetcher.apply(envelope);
        return parseXmlaResponse(xml);
    }
    
    private void validateParameters(String endpointUrl, String catalog) {
        if (endpointUrl == null || endpointUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint URL no puede ser nulo o vacío");
        }
        if (catalog == null || catalog.trim().isEmpty()) {
            throw new IllegalArgumentException("Catalog no puede ser nulo o vacío");
        }
    }
    
    private void validateQuery(String query) {
        if (query == null || !query.startsWith(MDX_PREFIX)) {
            throw new IllegalArgumentException("XmlaOlapAdapter espera consultas MDX con prefijo 'MDX:'");
        }
    }
    
    private String extractMdxQuery(String query) {
        return query.substring(MDX_PREFIX.length()).trim();
    }
    
    private Function<String, String> createHttpFetcher(String endpointUrl) {
        return envelope -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build();
                    
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", SOAP_ACTION)
                    .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                    .build();

                HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                return response.body();
            } catch (Exception e) {
                throw new RuntimeException("Error ejecutando consulta XMLA: " + e.getMessage(), e);
            }
        };
    }

    private String buildEnvelope(String mdx) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        sb.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
        sb.append("<soap:Body>\n");
        sb.append("<Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n");
        sb.append("<Command>\n<Statement><![CDATA[");
        sb.append(mdx);
        sb.append("]]></Statement>\n</Command>\n");
        sb.append("<Properties>\n<PropertyList>\n");
        if (catalog != null && !catalog.isBlank()) {
            sb.append("<Catalog>").append(escapeXml(catalog)).append("</Catalog>\n");
        }
        sb.append("<Format>Tabular</Format>\n");
        sb.append("</PropertyList>\n</Properties>\n");
        sb.append("</Execute>\n</soap:Body>\n</soap:Envelope>");
        return sb.toString();
    }

    private AggregationResponse parseXmlaResponse(String xml) {
        if (xml == null || xml.isBlank()) {
            return new AggregationResponse(List.of());
        }
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            // Configuración de seguridad para prevenir XXE
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList rows = doc.getElementsByTagName("row");
            
            List<AggregationRow> list = new ArrayList<>();
            for (int i = 0; i < rows.getLength(); i++) {
                Node row = rows.item(i);
                Map<String, Object> map = parseRowData(row);
                list.add(new AggregationRow(map));
            }
            return new AggregationResponse(list);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error de configuración del parser XML", e);
        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta XMLA: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> parseRowData(Node row) {
        Map<String, Object> map = new HashMap<>();
        NodeList children = row.getChildNodes();
        
        for (int j = 0; j < children.getLength(); j++) {
            Node node = children.item(j);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String key = node.getNodeName();
                String value = node.getTextContent();
                map.put(key, value);
            }
        }
        return map;
    }

    private static String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
