package com.nineteen.metadata;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;

import org.neo4j.test.server.HTTP;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Created by laeg on 07/10/2015.
 */

public class SchemaTests {

    // DateTime
    private final TimeZone tz = TimeZone.getTimeZone("UTC");
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture("MERGE (p:Person {name: 'Nicole'})-[:KNOWS]->(p2:Person {name: 'Mark'})")
            .withFixture("MERGE (p:Person {name: 'Nicole'})-[:LIKES]->(f:Food {name: 'Pie'})")
            .withExtension("/metadata", "com.nineteen.metadata");


    @Test
    public void shouldProcessNodeLabelLazy() {

//        df.setTimeZone(tz);

        // Given
        URI serverURI = neo4j.httpURI();
        // When
        HTTP.Response response = HTTP.POST(serverURI.resolve("/metadata/schema/label/Person/lazy").toString());


        // Then
        assertEquals(200, response.status());

        // Need to finish this test?
//        assertEquals("{lastScan=2015-10-08T09:54Z, outgoingRels=[KNOWS, LIKES], incomingRelsCount=1, outgoingRelsCount=2, attributes=[name], incomingRels=[KNOWS], label=Person, scanType=lazy}", response.content());


    }

    @Test
    public void shouldReturnNodeMetadata()
    {
        // Given
        URI serverURI = neo4j.httpURI();
        // When
        HTTP.Response response = HTTP.GET(serverURI.resolve("/metadata/schema/label/Person").toString());

        // Then
        assertEquals(200, response.status());

        System.out.println(response);
    }
}
