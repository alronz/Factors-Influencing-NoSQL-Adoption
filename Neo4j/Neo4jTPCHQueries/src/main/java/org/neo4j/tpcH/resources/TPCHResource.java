package org.neo4j.tpcH.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.neo4j.cypher.internal.compiler.v2_2.functions.Str;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wordnik.swagger.annotations.*;

@Path("/TpcH")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/TpcH", description = "TPCH Queries")
public class TPCHResource {

    private static final String CLASS_NAME = TPCHResource.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    GraphDatabaseService graphDb;

    DateFormat format = new SimpleDateFormat("yyyy-mm-dd",
            java.util.Locale.ENGLISH);

    public TPCHResource(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @GET
    @Path("/q1")
    @Timed
    @ApiOperation(value = "get result of TPCH Q1 using this model", notes = "TPCH Queries", response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public String getQ1Results() {

        Result result = null;


        try {

            try (Transaction trans = this.graphDb.beginTx()) {

                String query1 = "MATCH (item:Lineitem)\n" +
                        "WHERE item.SHIPDATE  <= " + format.parse("1998-12-01").getTime() + " \n" +
                        "RETURN item.RETURNFLAG,item.LINESTATUS,sum(item.QUANTITY) AS sum_qty, sum(item.EXTENDEDPRICE) AS sum_base_price, sum(item.EXTENDEDPRICE*(1-item.DISCOUNT)) AS sum_disc_price,\n" +
                        "sum(item.EXTENDEDPRICE*(1-item.DISCOUNT)*(1+item.TAX)) AS sum_charge,avg(item.QUANTITY) AS avg_qty, avg(item.EXTENDEDPRICE) AS avg_price, avg(item.DISCOUNT) AS avg_disc\n" +
                        "ORDER BY item.RETURNFLAG, item.LINESTATUS";

                result = this.graphDb
                        .execute(query1);

                trans.success();
            }

            return result.resultAsString();
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
    @ApiOperation(value = "get result of TPCH Q3 using this model", notes = "TPCH Queries", response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public String getQ3Results() {

        Result result = null;

        try {
            try (Transaction trans = this.graphDb.beginTx()) {

                String query3 = "MATCH  (item:Lineitem) <-[:CONTAINS]- (order:Order ) -[:CREATED_BY]-> (customer:Customer)\n" +
                        "WHERE order.ORDERDATE < 912524220000 AND item.SHIPDATE > 631205820000 AND customer.MKTSEGMENT = 'AUTOMOBILE' \n" +
                        "RETURN order.ORDERKEY, sum(item.EXTENDEDPRICE*(1-item.DISCOUNT)) AS REVENUE, order.ORDERDATE, order.SHIPPRIORITY\n" +
                        "ORDER BY REVENUE DESC, order.ORDERDATE";

                result = this.graphDb
                        .execute(query3);

                trans.success();
            }

            return result.resultAsString();
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
    @ApiOperation(value = "get result of TPCH Q4 using this model", notes = "TPCH Queries", response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "internal server error !")})
    public String getQ4Results() {

        Result result = null;

        try {
            try (Transaction trans = this.graphDb.beginTx()) {

                String query4 = "MATCH  (order:Order) -[:CONTAINS]-> (item:Lineitem) \n" +
                        "WHERE item.COMMITDATE < item.RECEIPTDATE AND order.ORDERDATE >= 631205820000 AND order.ORDERDATE < 912524220000\n" +
                        "RETURN order.ORDERPRIORITY, count(*) AS ORDER_COUNT\n" +
                        "ORDER BY order.ORDERPRIORITY";

                result = this.graphDb
                        .execute(query4);

                trans.success();
            }

            return result.resultAsString();
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
