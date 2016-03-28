package org.cassandra.tpcH;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.datastax.driver.core.*;

public class TPCHModel {

    private static final String CLASS_NAME = TPCHModel.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);


    DateFormat format = new SimpleDateFormat("yyyy-mm-dd",
            java.util.Locale.ENGLISH);

    Session session;
    private Cluster cluster;


    public TPCHModel(Cluster cluster) {
        this.cluster = cluster;
    }

    void initialiseData() {

        createSchema();

        loadTpchQ1();

        loadTpchQ3();

        loadTpchQ4();

    }


    public ResultSet executeStatement(String statement) {
        ResultSet rs = this.session.execute(statement);

        return rs;
    }

    public void connect() {

        Metadata metadata = cluster.getMetadata();
        LOGGER.info("Connected to cluster: " + metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            LOGGER.info("Datatacenter:" + host.getDatacenter() + " Host:"
                    + host.getAddress() + " Rack:" + host.getRack());

        }
        session = cluster.connect();
    }


    public void close() {
        session.close();
        //	cluster.close();
    }

    private void createSchema() {

        // keyspace create statement
        String createKeySpaceQuery = "CREATE KEYSPACE IF NOT EXISTS CASSANDRA_EXAMPLE_KEYSPACE WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':1};";

        ResultSet rsKeySpace = session.execute(createKeySpaceQuery);

        LOGGER.info(rsKeySpace.toString());

        // tpchQ1 create statement
        String createTpchQ1TableQuery = "CREATE TABLE IF NOT EXISTS CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q1 \n" +
                "(\n" +
                "linestatus text,\n" +
                "orderkey text,\n" +
                "linenumber text,\n" +
                "returnflag text,\n" +
                "quantity double,\n" +
                "extendedprice double,\n" +
                "discount double,\n" +
                "tax double,\n" +
                "shipdate timestamp,\n" +
                "PRIMARY KEY ((returnflag,linestatus),shipdate,orderkey,linenumber)\n" +
                ");";

        ResultSet rsTpchQ1 = session.execute(createTpchQ1TableQuery);
        LOGGER.info(rsTpchQ1.toString());

        // tpchQ3 create statement
        String createTpchQ3Query = "CREATE TABLE IF NOT EXISTS CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q3\n" +
                "(\n" +
                "orderkey text,\n" +
                "linenumber text,\n" +
                "o_orderdate timestamp,\n" +
                "o_shippriority text,\n" +
                "c_mktsegment text,\n" +
                "l_extendedprice double,\n" +
                "l_discount double,\n" +
                "l_shipdate timestamp,\n" +
                "PRIMARY KEY ((orderkey,o_orderdate,o_shippriority),c_mktsegment,l_shipdate,linenumber)\n" +
                ");";

        ResultSet rsTpchQ3 = session.execute(createTpchQ3Query);
        LOGGER.info(rsTpchQ3.toString());


        // tpchQ4 create statement
        String createTpchQ4Query = "CREATE TABLE IF NOT EXISTS CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q4\n" +
                "(\n" +
                "orderkey text,\n" +
                "linenumber text, \n" +
                "o_orderpriority text,\n" +
                "o_orderdate timestamp,\n" +
                "l_receiptdate timestamp,\n" +
                "l_commitdate timestamp,\n" +
                "PRIMARY KEY (o_orderpriority,o_orderdate,orderkey,linenumber)\n" +
                ");";

        ResultSet rsTpchQ4 = session.execute(createTpchQ4Query);
        LOGGER.info(rsTpchQ4.toString());


        // fSumDiscPrice create statement
        String createSumDiscPriceFunctionStatement = "CREATE OR REPLACE FUNCTION  CASSANDRA_EXAMPLE_KEYSPACE.fSumDiscPrice (l_extendedprice double,l_discount double) CALLED ON NULL INPUT RETURNS double LANGUAGE java AS 'return (Double.valueOf( l_extendedprice.doubleValue() *  (1.0 - l_discount.doubleValue() ) ));' ;";

        ResultSet rsf1 = session.execute(createSumDiscPriceFunctionStatement);
        LOGGER.info(rsf1.toString());

        // fSumChargePrice create statement
        String createSumChargePriceFunctionStatement = " CREATE OR REPLACE FUNCTION CASSANDRA_EXAMPLE_KEYSPACE.fSumChargePrice (l_extendedprice double,l_discount double,l_tax double) CALLED ON NULL INPUT RETURNS double LANGUAGE java AS 'return (Double.valueOf( l_extendedprice.doubleValue() *  (1.0 - l_discount.doubleValue() ) * (1.0 + l_tax.doubleValue()) ));';";

        ResultSet rsf2 = session.execute(createSumChargePriceFunctionStatement);
        LOGGER.info(rsf2.toString());

    }

    private void loadTpchQ1() {

        File file = new File("src/main/java/org/cassandra/tpcH/data/lineitem.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineFields = line.split(Pattern.quote("|"));

                LOGGER.info(line.toString());

                String insertStatement = "INSERT INTO CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q1 (orderkey,linenumber,linestatus,returnflag,quantity," +
                        "extendedprice,discount,tax,shipdate) VALUES" +
                        " ('" + lineFields[0] +
                        "','" + lineFields[3] +
                        "','" + lineFields[9] +
                        "','" + lineFields[8] +
                        "'," + Double.valueOf(lineFields[4]) +
                        "," + Double.valueOf(lineFields[5]) +
                        "," + Double.valueOf(lineFields[6]) +
                        "," + Double.valueOf(lineFields[7]) +
                        ",'" + lineFields[10] + "') IF NOT EXISTS;";

                LOGGER.info(insertStatement);

                session.execute(insertStatement);
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    private void loadTpchQ3() {

        File file = new File("src/main/java/org/cassandra/tpcH/data/order.txt");


        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineFields = line.split(Pattern.quote("|"));

                // get c_mktsegment based on the custkey
                String c_mktsegment = null;

                File customerFile = new File("src/main/java/org/cassandra/tpcH/data/customer.txt");
                String customerLine;
                BufferedReader customerBr = new BufferedReader(new FileReader(customerFile));

                while ((customerLine = customerBr.readLine()) != null) {
                    String[] customerLineFields = customerLine.split(Pattern.quote("|"));

                    if (lineFields[1].equals(customerLineFields[0])) {
                        c_mktsegment = customerLineFields[6];
                    }
                }


                File lineItems = new File("src/main/java/org/cassandra/tpcH/data/lineitem.txt");
                String lineItemLine;
                BufferedReader lineItemBr = new BufferedReader(new FileReader(lineItems));

                while ((lineItemLine = lineItemBr.readLine()) != null) {
                    String[] lineItemLineFields = lineItemLine.split(Pattern.quote("|"));

                    if (lineItemLineFields[0].equals(lineFields[0])) {

                        String insertStatement = "INSERT INTO CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q3 (orderkey,linenumber,o_orderdate,o_shippriority," +
                                "c_mktsegment,l_extendedprice,l_discount,l_shipdate) VALUES" +
                                " ('" + lineFields[0] +
                                "','" + lineItemLineFields[3] +
                                "','" + lineFields[4] +
                                "','" + lineFields[5] +
                                "','" + c_mktsegment +
                                "'," + Double.valueOf(lineItemLineFields[5]) +
                                "," + Double.valueOf(lineItemLineFields[6]) +
                                ",'" + lineItemLineFields[10] + "') IF NOT EXISTS";

                        ResultSet rs = session.execute(insertStatement);
                        LOGGER.info(rs.toString());
                    }
                }
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadTpchQ4() {

        File file = new File("src/main/java/org/cassandra/tpcH/data/order.txt");


        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineFields = line.split(Pattern.quote("|"));

                File lineItems = new File("src/main/java/org/cassandra/tpcH/data/lineitem.txt");
                String lineItemLine;
                BufferedReader lineItemBr = new BufferedReader(new FileReader(lineItems));

                while ((lineItemLine = lineItemBr.readLine()) != null) {
                    String[] lineItemLineFields = lineItemLine.split(Pattern.quote("|"));

                    if (lineItemLineFields[0].equals(lineFields[0])) {

                        String insertStatement = "INSERT INTO CASSANDRA_EXAMPLE_KEYSPACE.TPCH_Q4 (linenumber,orderkey,o_orderpriority,o_orderdate,l_receiptdate," +
                                "l_commitdate) VALUES" +
                                " ('" + lineItemLineFields[3] +
                                "','" + lineFields[0] +
                                "','" + lineFields[5] +
                                "','" + lineFields[4] +
                                "','" + lineItemLineFields[12] +
                                "','" + lineItemLineFields[11] + "') IF NOT EXISTS";

                        ResultSet rs = session.execute(insertStatement);
                        LOGGER.info(rs.toString());
                    }
                }
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
