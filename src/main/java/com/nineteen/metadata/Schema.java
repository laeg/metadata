package com.nineteen.metadata;

import com.nineteen.metadata.MetadataKeeper;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by laeg on 07/10/2015.
 */
@javax.ws.rs.Path("/schema")
public class Schema {

    private final GraphDatabaseService db;
    private final ObjectMapper objectMapper;
    @Context private final MetadataKeeper metadataKeeper;

    // DateTime
    private final TimeZone tz = TimeZone.getTimeZone("UTC");
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");


    public Schema(@Context GraphDatabaseService db, @Context MetadataKeeper metadataKeeper) {
        this.db = db;
        this.objectMapper = new ObjectMapper();
        this.metadataKeeper = metadataKeeper;

        // Set timezone
        df.setTimeZone(tz);
    }

    @GET
    @Produces("application/json")
    @Path("/")
    public Response get()
    {
        JSONObject jObj = new JSONObject();
        jObj.put("online", 1);
        return Response.ok().entity(jObj).build();
    }


    @GET
    @Produces("application/json")
    @Path("/label/{nodeLabel}")
    public Response returnLabelMetadata(@PathParam("nodeLabel") final String nodeLabel)
    {

        JSONObject jObj = new JSONObject();

        try
        {
//            System.out.println("in get");
//            System.out.println(metaCollection);
//            System.out.println("*" + nodeLabel + "*");
//            System.out.println(metaCollection.get((String) nodeLabel));
//
            if(metadataKeeper.metaCollection.containsKey(nodeLabel))
            {

                jObj =  metadataKeeper.metaCollection.get(nodeLabel);

                System.out.println("got");
                System.out.println(jObj);
            }
        }
        catch (Exception e)
        {
            return Response.ok(e).build();
        }

        return Response.ok().entity(jObj.toString()).build();

    }

    @GET
    @Produces("application/json")
    @Path("/label/{nodeLabel}/{scanType}")
    public Response scanAllNodesByLabel(@PathParam("nodeLabel") final String nodeLabel, @PathParam("scanType") final String scanType) {

        // statistics for label
        /*
            {   // Lazy
                label: {label},
                attributes: [{attributeName}, ...]
                incomingRels: [{relName}, ...],
                outgoingRels: [{relName}, ...]
                lastScan: {time},
                scanType: {type},
                // All
                incomingRelsCount: {int},
                outgoingRelsCount: {int}
            }
         */


        JSONObject jObj = new JSONObject();
        jObj.put("label", nodeLabel);
        jObj.put("scanType", scanType);


        Set<String> attributes = new HashSet<String>();
        Set<String> incomingRels = new HashSet<String>();
        Set<String> outgoingRels = new HashSet<String>();
        int incomingRelsCount = 0;
        int outgoingRelsCount = 0;

        // Return a resouce iterator
        try (Transaction tx = db.beginTx();
             ResourceIterator<Node> ri = db.findNodes(DynamicLabel.label(nodeLabel))) {

            if (scanType.toUpperCase().equals("LAZY")) {


                // Take 1000 Nodes
                int i = 0;

                while (i < 1000) {

                    if (ri.hasNext()) {

                        Node node = ri.next();

                        // Add each property to the map
                        for (String attr : node.getPropertyKeys()) {
                            attributes.add(attr);
                        }

                        // Add each in rel to the map
                        for (Relationship inRel : node.getRelationships(Direction.INCOMING)) {
                            incomingRels.add(inRel.getType().name());
                        }

                        // Add each out rel to the map
                        for (Relationship outRel : node.getRelationships(Direction.OUTGOING)) {
                            outgoingRels.add(outRel.getType().name());
                        }

                    }

                    i++;

                }


            }
            if (scanType.toUpperCase().equals("ALL")) {

                while (ri.hasNext()) {

                    Node node = ri.next();

                    // Add each property to the map
                    for (String attr : node.getPropertyKeys()) {
                        attributes.add(attr);
                    }

                    // Add each in rel to the map
                    for (Relationship inRel : node.getRelationships(Direction.INCOMING)) {
                        incomingRels.add(inRel.getType().name());
                    }

                    // Add each out rel to the map
                    for (Relationship outRel : node.getRelationships(Direction.OUTGOING)) {
                        outgoingRels.add(outRel.getType().name());
                    }

                }

            }

            tx.close();

        } catch (Exception e )
        {
            System.out.println(e);
        }

        jObj.put("attributes", attributes);
        jObj.put("incomingRels", incomingRels);
        jObj.put("outgoingRels", outgoingRels);

        jObj.put("incomingRelsCount", incomingRels.size());
        jObj.put("outgoingRelsCount", outgoingRels.size());


        // Get the current time
        jObj.put("lastScan", df.format(new Date()));

        metadataKeeper.saveValue(nodeLabel, jObj);


        System.out.println("-----");
        System.out.println(jObj);
        System.out.println(metadataKeeper.returnValue(nodeLabel));
        System.out.println("-----");


        return Response.ok().contentLocation(URI.create("/schema/" + nodeLabel)).build();

    }


}
