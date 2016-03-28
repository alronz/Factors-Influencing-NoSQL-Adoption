package org.mongoDB.tpcHQueries;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class NormalizedExampleModel {

    private MongoCollection<Document> normalized_customer;
    private MongoCollection<Document> normalized_supplier;
    private MongoCollection<Document> normalized_partsupp;
    private MongoCollection<Document> normalized_order;
    private MongoCollection<Document> normalized_region;
    private MongoCollection<Document> normalized_nation;
    private MongoCollection<Document> normalized_part;
    private MongoCollection<Document> normalized_lineitem;

    private MongoClient mongoClient;
    private MongoDatabase database;


    DateFormat format = new SimpleDateFormat("yyyy-mm-dd",
            java.util.Locale.ENGLISH);


    public NormalizedExampleModel(MongoClient mongoClient) {

        this.mongoClient = mongoClient;

        this.database = this.mongoClient.getDatabase("mydb");
    }


    void initialiseData() {

        createRegion();
        createNation();
        createCustomer();
        createSupplier();
        createPart();
        createPartSupp();
        createOrder();
        createLineItem();
    }

    private void createCustomer() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/customer.txt");

        if (this.database.getCollection("normalized_customer") == null) {
            this.database.createCollection("normalized_customer");
        } else {
            this.database.getCollection("normalized_customer").drop();
            this.database.createCollection("normalized_customer");
        }

        normalized_customer = this.database
                .getCollection("normalized_customer");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document customer = new Document();

                customer.append("CUSTKEY", lineFields[0])
                        .append("NAME", lineFields[1]).append("ADDRESS", lineFields[2])
                        .append("PHONE", lineFields[4]).append("ACCTBAL", Double.valueOf(lineFields[5]))
                        .append("MKTSEGMENT", lineFields[6])
                        .append("COMMENT", lineFields[7]).append("NATIONKEY", lineFields[3]);

                this.normalized_customer.insertOne(customer);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    void createSupplier() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/supplier.txt");

        if (this.database.getCollection("normalized_supplier") == null) {
            this.database.createCollection("normalized_supplier");
        } else {
            this.database.getCollection("normalized_supplier").drop();
            this.database.createCollection("normalized_supplier");
        }

        normalized_supplier = this.database
                .getCollection("normalized_supplier");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document supplier = new Document();

                supplier.append("SUPPKEY", lineFields[0])
                        .append("NAME", lineFields[1]).append("ADDRESS", lineFields[2])
                        .append("PHONE", lineFields[4]).append("ACCTBAL", Double.valueOf(lineFields[5]))
                        .append("COMMENT", lineFields[6])
                        .append("NATIONKEY", lineFields[3]);

                this.normalized_supplier.insertOne(supplier);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void createPart() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/part.txt");

        if (this.database.getCollection("normalized_part") == null) {
            this.database.createCollection("normalized_part");
        } else {
            this.database.getCollection("normalized_part").drop();
            this.database.createCollection("normalized_part");
        }

        normalized_part = this.database
                .getCollection("normalized_part");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document part = new Document();

                part.append("PARTKEY", lineFields[0])
                        .append("NAME", lineFields[1]).append("MFGR", lineFields[2])
                        .append("BRAND", lineFields[3]).append("TYPE", lineFields[4])
                        .append("SIZE", lineFields[5])
                        .append("CONTAINER", lineFields[6]).append("RETAILPRICE", Double.valueOf(lineFields[7])).append("COMMENT", lineFields[8]);

                this.normalized_part.insertOne(part);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    void createPartSupp() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/partsupp.txt");

        if (this.database.getCollection("normalized_partsupp") == null) {
            this.database.createCollection("normalized_partsupp");
        } else {
            this.database.getCollection("normalized_partsupp").drop();
            this.database.createCollection("normalized_partsupp");
        }

        normalized_partsupp = this.database
                .getCollection("normalized_partsupp");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document partsupp = new Document();

                partsupp.append("PARTKEY", lineFields[0])
                        .append("SUPPKEY", lineFields[1]).append("AVAILQTY", Double.valueOf(lineFields[2]))
                        .append("SUPPLYCOST", Double.valueOf(lineFields[3])).append("COMMENT", lineFields[4]);

                this.normalized_partsupp.insertOne(partsupp);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void createOrder() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/order.txt");

        if (this.database.getCollection("normalized_order") == null) {
            this.database.createCollection("normalized_order");
        } else {
            this.database.getCollection("normalized_order").drop();
            this.database.createCollection("normalized_order");
        }

        normalized_order = this.database
                .getCollection("normalized_order");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document order = new Document();

                order.append("ORDERKEY", lineFields[0])
                        .append("CUSTKEY", lineFields[1]).append("ORDERSTATUS", lineFields[2])
                        .append("TOTALPRICE", Double.valueOf(lineFields[3])).append("ORDERDATE", format.parse(lineFields[4]))
                        .append("ORDERPRIORITY", lineFields[5]).append("CLERK", lineFields[6])
                        .append("SHIPPRIORITY", lineFields[7]).append("COMMENT", lineFields[8]);

                this.normalized_order.insertOne(order);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void createRegion() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/region.txt");

        if (this.database.getCollection("normalized_region") == null) {
            this.database.createCollection("normalized_region");
        } else {
            this.database.getCollection("normalized_region").drop();
            this.database.createCollection("normalized_region");
        }

        normalized_region = this.database
                .getCollection("normalized_region");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document region = new Document();

                region.append("REGIONKEY", lineFields[0])
                        .append("NAME", lineFields[1]).append("COMMENT", lineFields[2]);

                this.normalized_region.insertOne(region);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void createNation() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/nation.txt");

        if (this.database.getCollection("normalized_nation") == null) {
            this.database.createCollection("normalized_nation");
        } else {
            this.database.getCollection("normalized_nation").drop();
            this.database.createCollection("normalized_nation");
        }

        normalized_nation = this.database
                .getCollection("normalized_nation");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document nation = new Document();

                nation.append("NATIONKEY", lineFields[0])
                        .append("NAME", lineFields[1]).append("REGIONKEY", lineFields[2]).append("COMMENT", lineFields[3]);

                this.normalized_nation.insertOne(nation);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void createLineItem() {

        File file = new File("src/main/java/org/mongoDB/tpcHQueries/data/lineitem.txt");

        if (this.database.getCollection("normalized_lineitem") == null) {
            this.database.createCollection("normalized_lineitem");
        } else {
            this.database.getCollection("normalized_lineitem").drop();
            this.database.createCollection("normalized_lineitem");
        }

        normalized_lineitem = this.database
                .getCollection("normalized_lineitem");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                // process the line.

                String[] lineFields = line.split(Pattern.quote("|"));

                Document lineitem = new Document();

                lineitem.append("ORDERKEY", lineFields[0])
                        .append("PARTKEY", lineFields[1]).append("SUPPKEY", lineFields[2]).append("LINENUMBER", lineFields[3])
                        .append("QUANTITY", Double.valueOf(lineFields[4])).append("EXTENDEDPRICE", Double.valueOf(lineFields[5]))
                        .append("DISCOUNT", Double.valueOf(lineFields[6])).append("TAX", Double.valueOf(lineFields[7]))
                        .append("RETURNFLAG", lineFields[8]).append("LINESTATUS", lineFields[9])
                        .append("SHIPDATE", format.parse(lineFields[10])).append("COMMITDATE", format.parse(lineFields[11]))
                        .append("RECEIPTDATE", format.parse(lineFields[12])).append("SHIPINSTRUCT", lineFields[13])
                        .append("SHIPMODE", lineFields[14]).append("COMMENT", lineFields[15]);

                this.normalized_lineitem.insertOne(lineitem);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
