package org.mongoDB.tpcHQueries.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;

import com.mongodb.client.MongoCursor;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.*;

import com.mongodb.client.model.Sorts.*;

@Path("/TpcH/Denormalized")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/TpcH/Denormalized", description = "testing tpcH queries with denormalized data model")
public class TpcHDenormalizedModelResource {

    private static final String CLASS_NAME = TpcHDenormalizedModelResource.class
            .getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> denormalized_order;
    private MongoCollection<Document> out;

    DateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss",
            java.util.Locale.ENGLISH);

    public TpcHDenormalizedModelResource(MongoClient mongoClient) {

        this.mongoClient = mongoClient;

        this.database = this.mongoClient.getDatabase("mydb");

        denormalized_order = this.database.getCollection("denormalized_order");

    }

    @GET
    @Path("/q1")
    @Timed
    @ApiOperation(value = "get result of TPCH Q1 using this model", notes = "Returns mongoDB document(s)", response = Document.class, responseContainer = "list")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public ArrayList<Document> getQ1Results() {

        AggregateIterable<Document> result;

        try {

            String matchStringQuery = "{\"$match\":{\"Items\":{\"$elemMatch\":{\"SHIPDATE\":{\"$lte\":ISODate(\"2000-01-01T00:00:00.000Z\")}}}}}";

            String unWindStringQuery = "{$unwind: \"$Items\"}";

            String projectStringQuery = "{\"$project\":{\"Items.RETURNFLAG\":1,\"Items.LINESTATUS\":1,\"Items.QUANTITY\":1,\"Items.EXTENDEDPRICE\":1,\"Items.DISCOUNT\":1,\"l_dis_min_1\":{\"$subtract\":[1,\"$Items.DISCOUNT\"]},\"l_tax_plus_1\":{\"$add\":[\"$Items.TAX\",1]}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"RETURNFLAG\":\"$Items.RETURNFLAG\",\"LINESTATUS\":\"$Items.LINESTATUS\"},\"sum_qty\":{\"$sum\":\"$Items.QUANTITY\"},\"sum_base_price\":{\"$sum\":\"$Items.EXTENDEDPRICE\"},\"sum_disc_price\":{\"$sum\":{\"$multiply\":[\"$Items.EXTENDEDPRICE\",\"$l_dis_min_1\"]}},\"sum_charge\":{\"$sum\":{\"$multiply\":[\"$Items.EXTENDEDPRICE\",{\"$multiply\":[\"$l_tax_plus_1\",\"$l_dis_min_1\"]}]}},\"avg_price\":{\"$avg\":\"$Items.EXTENDEDPRICE\"},\"avg_disc\":{\"$avg\":\"$Items.DISCOUNT\"},\"count_order\":{\"$sum\":1}}}";

            String sortStringQuery = "{\"$sort\":{\"Items.RETURNFLAG\":1,\"Items.LINESTATUS\":1}}";

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

            result = this.denormalized_order.aggregate(aggregateQuery);

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
    public ArrayList<Document> getQ2Results() {

        AggregateIterable<Document> result;

        try {

            String matchStringQuery = "{\"$match\":{\"Customer.MKTSEGMENT\":\"AUTOMOBILE\",\"ORDERDATE\":{\"$lte\":ISODate(\"2000-01-01T00:00:00.000Z\") },\"Items.SHIPDATE\":{\"$gte\": ISODate(\"1990-01-01T00:00:00.000Z\")}}}";

            String unWindStringQuery = "{$unwind: \"$Items\"}";

            String projectStringQuery = "{\"$project\":{\"ORDERDATE\":1,\"SHIPPRIORITY\":1,\"Items.EXTENDEDPRICE\":1,\"l_dis_min_1\":{\"$subtract\":[1,\"$Items.DISCOUNT\"]}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"ORDERKEY\":\"$ORDERKEY\",\"ORDERDATE\":\"$ORDERDATE\",\"SHIPPRIORITY\":\"$SHIPPRIORITY\"},\"revenue\":{\"$sum\":{\"$multiply\":[\"$Items.EXTENDEDPRICE\",\"$l_dis_min_1\"]}}}}";

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

            result = this.denormalized_order.aggregate(aggregateQuery);

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
    @Path("/q4")
    @Timed
    @ApiOperation(value = "get result of TPCH Q4 using this model", notes = "Returns mongoDB document(s)", response = Document.class, responseContainer = "list")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public ArrayList<Document> getQ3Results() {

        AggregateIterable<Document> result;

        try {

            String projectStringQuery = "{\"$project\":{\"ORDERDATE\":1,\"ORDERPRIORITY\":1,\"eq\":{\"$cond\":[{\"$lt\":[\"$Items.COMMITDATE\",\"$Items.RECEIPTDATE\"]},0,1]}}}";

            String matchStringQuery = "{\"$match\": {\"ORDERDATE\": {\"$gte\": ISODate(\"1990-01-01T00:00:00.000Z\")},\"ORDERDATE\": {\"$lt\": ISODate(\"2000-01-01T00:00:00.000Z\")},\"eq\":{\"$eq\":1}}}";

            String groupStringQuery = "{\"$group\":{\"_id\":{\"ORDERPRIORITY\":\"$ORDERPRIORITY\"},\"order_count\":{\"$sum\":1}}}";

            String sortStringQuery = "{\"$sort\":{\"ORDERPRIORITY\":1}}";


            BsonDocument projectBsonQuery = BsonDocument
                    .parse(projectStringQuery);


            BsonDocument matchBsonQuery = BsonDocument
                    .parse(matchStringQuery);

            BsonDocument groupBsonQuery = BsonDocument.parse(groupStringQuery);

            BsonDocument sortBsonQuery = BsonDocument.parse(sortStringQuery);


            ArrayList<Bson> aggregateQuery = new ArrayList<Bson>();

            aggregateQuery.add(projectBsonQuery);
            aggregateQuery.add(matchBsonQuery);
            aggregateQuery.add(groupBsonQuery);
            aggregateQuery.add(sortBsonQuery);

            result = this.denormalized_order.aggregate(aggregateQuery);

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

}
