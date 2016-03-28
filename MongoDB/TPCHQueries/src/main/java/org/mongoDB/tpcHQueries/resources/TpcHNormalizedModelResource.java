package org.mongoDB.tpcHQueries.resources;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.mongodb.client.MongoCursor;
import com.sun.research.ws.wadl.Doc;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;
import com.wordnik.swagger.annotations.*;

@Path("/TpcH/Normalized")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/TpcH/Normalized", description = "testing tpcH queries with normalized data model")
public class TpcHNormalizedModelResource {

    private static final String CLASS_NAME = TpcHNormalizedModelResource.class
            .getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> normalized_customer;
    private MongoCollection<Document> normalized_supplier;
    private MongoCollection<Document> normalized_partsupp;
    private MongoCollection<Document> normalized_order;
    private MongoCollection<Document> normalized_region;
    private MongoCollection<Document> normalized_nation;
    private MongoCollection<Document> normalized_part;
    private MongoCollection<Document> normalized_lineitem;

    public TpcHNormalizedModelResource(MongoClient mongoClient) {

        this.mongoClient = mongoClient;

        this.database = this.mongoClient.getDatabase("mydb");

        normalized_customer = this.database
                .getCollection("normalized_customer");
        normalized_supplier = this.database
                .getCollection("normalized_supplier");
        normalized_partsupp = this.database
                .getCollection("normalized_partsupp");
        normalized_order = this.database.getCollection("normalized_order");
        normalized_region = this.database.getCollection("normalized_region");
        normalized_nation = this.database.getCollection("normalized_nation");
        normalized_part = this.database.getCollection("normalized_part");
        normalized_lineitem = this.database
                .getCollection("normalized_lineitem");

    }

    @GET
    @Path("/q1")
    @Timed
    @ApiOperation(value = "get result of TPCH Q1 using this model", notes = "Returns mongoDB document(s)", response = Document.class, responseContainer = "list")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public ArrayList<Document> getQ1Results() {

        AggregateIterable<Document> result;

        try {

            String matchStringQuery = "{\"$match\":{\"SHIPDATE\":{\"$lte\":ISODate(\"2016-01-01T00:00:00.000Z\")}}}";

            String projectStringQuery = "{\"$project\":{\"RETURNFLAG\":1,\"LINESTATUS\":1,\"QUANTITY\":1,\"EXTENDEDPRICE\":1,\"DISCOUNT\":1,\"l_dis_min_1\":{\"$subtract\":[1,\"$DISCOUNT\"]},\"l_tax_plus_1\":{\"$add\":[\"$TAX\",1]}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"RETURNFLAG\":\"$RETURNFLAG\",\"LINESTATUS\":\"$LINESTATUS\"},\"sum_qty\":{\"$sum\":\"$QUANTITY\"},\"sum_base_price\":{\"$sum\":\"$EXTENDEDPRICE\"},\"sum_disc_price\":{\"$sum\":{\"$multiply\":[\"$EXTENDEDPRICE\",\"$l_dis_min_1\"]}},\"sum_charge\":{\"$sum\":{\"$multiply\":[\"$EXTENDEDPRICE\",{\"$multiply\":[\"$l_tax_plus_1\",\"$l_dis_min_1\"]}]}},\"avg_price\":{\"$avg\":\"$EXTENDEDPRICE\"},\"avg_disc\":{\"$avg\":\"$DISCOUNT\"},\"count_order\":{\"$sum\":1}}}";

            String sortStringQuery = "{\"$sort\":{\"RETURNFLAG\":1,\"LINESTATUS\":1}}";


            BsonDocument matchBsonQuery = BsonDocument.parse(matchStringQuery);

            BsonDocument projectBsonQuery = BsonDocument
                    .parse(projectStringQuery);

            BsonDocument groupBsonQuery = BsonDocument.parse(groupStringQuery);

            BsonDocument sortBsonQuery = BsonDocument.parse(sortStringQuery);


            ArrayList<Bson> aggregateQuery = new ArrayList<Bson>();

            aggregateQuery.add(matchBsonQuery);
            aggregateQuery.add(projectBsonQuery);
            aggregateQuery.add(groupBsonQuery);
            aggregateQuery.add(sortBsonQuery);

            result = this.normalized_lineitem.aggregate(aggregateQuery);

            MongoCursor<Document> iterator = result.iterator();

            ArrayList<Document> results = new ArrayList<Document>();
            while (iterator.hasNext()) {
                Document resultDoc = iterator.next();
                results.add(resultDoc);
            }

            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "internal server error !" + e.getLocalizedMessage());
            final String shortReason = "internal server error !";
            Exception cause = new IllegalArgumentException(shortReason);
            throw new WebApplicationException(cause,
                    javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/q3")
    @Timed
    @ApiOperation(value = "get result of TPCH Q3 using this model", notes = "Returns mongoDB document(s)", response = Document.class, responseContainer = "list")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public ArrayList<Document> getQ3Results() {

        AggregateIterable<Document> result;

        try {

            BsonDocument bsonQuery = BsonDocument
                    .parse("{\"ORDERDATE\":{\"$lte\":ISODate(\"2016-01-01T00:00:00.000Z\") }}");

            this.database.createCollection("normalized_q3_new_joined_orders");

            final MongoCollection<Document> normalized_q3_new_joined_orders = this.database
                    .getCollection("normalized_q3_new_joined_orders");

            this.normalized_order.find(bsonQuery).forEach(
                    new Block<Document>() {
                        @Override
                        public void apply(final Document order) {
                            BsonDocument customerBsonQuery = BsonDocument
                                    .parse("{\"CUSTKEY\":\""
                                            + order.get("CUSTKEY") + "\"}");

                            order.put("customer",
                                    normalized_customer.find(customerBsonQuery)
                                            .first());

                            BsonDocument lineitemsBsonQuery = BsonDocument
                                    .parse("{\"ORDERKEY\":\""
                                            + order
                                            .get("ORDERKEY") + "\"}");

                            order.put("lineitems", normalized_lineitem
                                    .find(lineitemsBsonQuery));

                            normalized_q3_new_joined_orders.insertOne(order);
                        }
                    });

            String matchStringQuery = "{\"$match\":{\"customer.MKTSEGMENT\":\"AUTOMOBILE\",\"lineitems.SHIPDATE\":{\"$gte\": ISODate(\"1990-01-01T00:00:00.000Z\") }}}";

            String unWindStringQuery = "{$unwind: \"$lineitems\"}";

            String projectStringQuery = "{\"$project\":{\"ORDERDATE\":1,\"SHIPPRIORITY\":1,\"lineitems.EXTENDEDPRICE\":1,\"l_dis_min_1\":{\"$subtract\":[1,\"$lineitems.DISCOUNT\"]}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"ORDERKEY\":\"$ORDERKEY\",\"ORDERDATE\":\"$ORDERDATE\",\"SHIPPRIORITY\":\"$SHIPPRIORITY\"},\"revenue\":{\"$sum\":{\"$multiply\":[\"$lineitems.EXTENDEDPRICE\",\"$l_dis_min_1\"]}}}}";

            String sortStringQuery = "{\"$sort\":{\"revenue\":1,\"ORDERDATE\":1}}";

            BsonDocument matchBsonQuery = BsonDocument.parse(matchStringQuery);

            BsonDocument unWindBsonQuery = BsonDocument
                    .parse(unWindStringQuery);

            BsonDocument projectBsonQuery = BsonDocument
                    .parse(projectStringQuery);

            BsonDocument groupBsonQuery = BsonDocument.parse(groupStringQuery);

            BsonDocument sortBsonQuery = BsonDocument.parse(sortStringQuery);


            ArrayList<Bson> aggregateQuery = new ArrayList<Bson>();

            aggregateQuery.add(matchBsonQuery);
            aggregateQuery.add(unWindBsonQuery);
            aggregateQuery.add(projectBsonQuery);
            aggregateQuery.add(groupBsonQuery);
            aggregateQuery.add(sortBsonQuery);

            result = normalized_q3_new_joined_orders.aggregate(aggregateQuery);

            MongoCursor<Document> iterator = result.iterator();

            ArrayList<Document> results = new ArrayList<Document>();
            while (iterator.hasNext()) {
                Document resultDoc = iterator.next();
                results.add(resultDoc);
            }

            normalized_q3_new_joined_orders.drop();

            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "internal server error !" + e.getLocalizedMessage());
            final String shortReason = "internal server error !";
            Exception cause = new IllegalArgumentException(shortReason);
            throw new WebApplicationException(cause,
                    javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/q4")
    @Timed
    @ApiOperation(value = "get result of TPCH Q4 using this model", notes = "Returns mongoDB document(s)", response = Document.class, responseContainer = "list")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public ArrayList<Document> getQ4Results() {

        AggregateIterable<Document> result;

        try {

            BsonDocument bsonQuery = BsonDocument
                    .parse("{\"ORDERDATE\": {\"$gte\": ISODate(\"1990-01-01T00:00:00.000Z\")},\"ORDERDATE\": {\"$lt\": ISODate(\"2000-01-01T00:00:00.000Z\")}}");

            this.database.createCollection("normalized_q4_new_joined_orders");

            final MongoCollection<Document> normalized_q4_new_joined_orders = this.database
                    .getCollection("normalized_q4_new_joined_orders");

            this.normalized_order.find(bsonQuery).forEach(
                    new Block<Document>() {
                        @Override
                        public void apply(final Document order) {

                            BsonDocument lineitemsBsonQuery = BsonDocument
                                    .parse("{\"ORDERKEY\":\""
                                            + order
                                            .get("ORDERKEY") + "\"}");

                            order.put("lineitems", normalized_lineitem
                                    .find(lineitemsBsonQuery));

                            normalized_q4_new_joined_orders.insertOne(order);
                        }
                    });

            String projectStringQuery = "{\"$project\":{\"ORDERDATE\":1,\"ORDERPRIORITY\":1,\"eq\":{\"$cond\":[{\"$lt\":[\"$lineitems.COMMITDATE\",\"$lineitems.RECEIPTDATE\"]},0,1]}}}";

            String matchStringQuery = "{\"$match\":{\"eq\":{\"$eq\":1}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"ORDERPRIORITY\":\"$ORDERPRIORITY\"},\"order_count\":{\"$sum\":1}}}";

            String sortStringQuery = "{\"$sort\":{\"ORDERPRIORITY\":1}}";


            BsonDocument projectBsonQuery = BsonDocument
                    .parse(projectStringQuery);

            BsonDocument matchBsonQuery = BsonDocument.parse(matchStringQuery);

            BsonDocument groupBsonQuery = BsonDocument.parse(groupStringQuery);

            BsonDocument sortBsonQuery = BsonDocument.parse(sortStringQuery);


            ArrayList<Bson> aggregateQuery = new ArrayList<Bson>();

            aggregateQuery.add(projectBsonQuery);
            aggregateQuery.add(matchBsonQuery);
            aggregateQuery.add(groupBsonQuery);
            aggregateQuery.add(sortBsonQuery);

            result = normalized_q4_new_joined_orders.aggregate(aggregateQuery);

            MongoCursor<Document> iterator = result.iterator();

            ArrayList<Document> results = new ArrayList<Document>();
            while (iterator.hasNext()) {
                Document resultDoc = iterator.next();
                results.add(resultDoc);
            }

            normalized_q4_new_joined_orders.drop();

            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "internal server error !" + e.getLocalizedMessage());
            final String shortReason = "internal server error !";
            Exception cause = new IllegalArgumentException(shortReason);
            throw new WebApplicationException(cause,
                    javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
