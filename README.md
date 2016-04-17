Factors Influencing the Adoption of a NoSQL Solution:  A comparison with a focus on the data modelling and query capabilities. 
--------

---

More information about this project are documented in the following website:

[http://alronz.github.io/Factors-Influencing-NoSQL-Adoption/](http://alronz.github.io/Factors-Influencing-NoSQL-Adoption/)



# Example Projects

A set of examples are created to test the data modelling and query capabilities of each database as explained in the [documentation](http://alronz.github.io/Factors-Influencing-NoSQL-Adoption/site/index.html). These examples are created as [dropwizard](http://www.dropwizard.io/) services so that they can be easily tested. In this repository, you will find the examples for each database grouped into a folder with the same database name. 



## Usage

The data used as input for the projects of each database is a small set from the data generated using TPCH DBGEN tool. The data is stored in CSV files that correspond to each object in the [TPCH benchmark data model](http://alronz.github.io/Factors-Influencing-NoSQL-Adoption/site/index.html#data-modelling-testing). For more details about how to generate the data, please have a look at [this](http://kejser.org/tpc-h-data-and-query-generation/) blog post.  You can change the used input data by changing the content of the input files that are located under the "data" folder. For example, the input files for Cassandra are located in [this path](https://github.com/alronz/Factors-Influencing-NoSQL-Adoption/tree/master/Cassandra/CassandraTPCHQueries/src/main/java/org/cassandra/tpcH/data). 


To test the projects, you can run the individual projects for each database and use swagger UI to call the APIs for each TPC-H query. For example, you can run the (Cassandra/CassandraTPCHQueries/) project by running the following:

````
gradle run
````

Then you can find the swagger ui in the following path:

````
http://localhost:{port}/api/swagger
````