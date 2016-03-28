package org.neo4j.tpcH;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintDefinition;

public class TPCHModel {

	DateFormat format = new SimpleDateFormat("yyyy-mm-dd",
			java.util.Locale.ENGLISH);

	GraphDatabaseService graphDb;

	// available relationships
	private static enum RelTypes implements RelationshipType {
		CONTAINS, CREATED_BY, FROM, HAS_DETAILS, SUPPLIED_BY, LOCATED_IN
	}

	public TPCHModel(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	void initialiseData() {

		// create the nodes

		createSupplierNodes();

		createCustomerNodes();

		createLineItemNodes();

		createNationNodes();

		createOrderNodes();

		createPartNodes();

		createRegionNodes();

		// create the relationships

		createContainsRelationship();

		createSuppliedByRelationship();

		createCreatedByRelationship();

		createFromCustomerRelationship();

		createFromSupplierRelationship();

		createLocatedInRelationship();

		createHasDetailsRelationship();

	}

	void createContainsRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/lineitem.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node lineitemNode = this.graphDb.findNode(
							DynamicLabel.label("Lineitem"), "LINENUMBER",
							lineFields[3]);

					Node orderNode = this.graphDb.findNode(
							DynamicLabel.label("Order"), "ORDERKEY",
							lineFields[0]);

					Iterable<Relationship> orderRelationships = orderNode
							.getRelationships();
					Iterator<Relationship> orderRelationshipsIterator = orderRelationships
							.iterator();

					boolean relExists = false;

					while (orderRelationshipsIterator.hasNext()) {
						Relationship rel = orderRelationshipsIterator.next();

						if (rel.getEndNode().equals(lineitemNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						orderNode.createRelationshipTo(lineitemNode,
								RelTypes.CONTAINS);

						System.out
								.println("CONTAINS relationship have been created");
					}

					tx.success();
				} catch (Exception e) {
					System.out.println("CONTAINS relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createCreatedByRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/order.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node customerNode = this.graphDb.findNode(
							DynamicLabel.label("Customer"), "CUSTKEY",
							lineFields[1]);

					Node orderNode = this.graphDb.findNode(
							DynamicLabel.label("Order"), "ORDERKEY",
							lineFields[0]);

					Iterable<Relationship> orderRelationships = orderNode
							.getRelationships();
					Iterator<Relationship> orderRelationshipsIterator = orderRelationships
							.iterator();

					boolean relExists = false;

					while (orderRelationshipsIterator.hasNext()) {
						Relationship rel = orderRelationshipsIterator.next();

						if (rel.getEndNode().equals(customerNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						orderNode.createRelationshipTo(customerNode,
								RelTypes.CREATED_BY);
						System.out
								.println("CREATED_BY relationship have been created");
					}

					tx.success();
				} catch (Exception e) {
					System.out
							.println("CREATED_BY relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createFromCustomerRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/customer.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node customerNode = this.graphDb.findNode(
							DynamicLabel.label("Customer"), "CUSTKEY",
							lineFields[0]);

					Node nationNode = this.graphDb.findNode(
							DynamicLabel.label("Nation"), "NATIONKEY",
							lineFields[3]);

					Iterable<Relationship> relationships = customerNode
							.getRelationships();
					Iterator<Relationship> relationshipsIterator = relationships
							.iterator();

					boolean relExists = false;

					while (relationshipsIterator.hasNext()) {
						Relationship rel = relationshipsIterator.next();

						if (rel.getEndNode().equals(nationNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						customerNode.createRelationshipTo(nationNode,
								RelTypes.FROM);
						System.out
								.println("FROM relationship have been created");
					}
					tx.success();
				} catch (Exception e) {
					System.out.println("FROM relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createFromSupplierRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/supplier.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node supplierNode = this.graphDb.findNode(
							DynamicLabel.label("Supplier"), "SUPPKEY",
							lineFields[0]);

					Node nationNode = this.graphDb.findNode(
							DynamicLabel.label("Nation"), "NATIONKEY",
							lineFields[3]);

					Iterable<Relationship> relationships = supplierNode
							.getRelationships();
					Iterator<Relationship> relationshipsIterator = relationships
							.iterator();

					boolean relExists = false;

					while (relationshipsIterator.hasNext()) {
						Relationship rel = relationshipsIterator.next();

						if (rel.getEndNode().equals(nationNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						supplierNode.createRelationshipTo(nationNode,
								RelTypes.FROM);
						System.out
								.println("FROM relationship have been created");
						tx.success();
					}
				} catch (Exception e) {
					System.out.println("FROM relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createLocatedInRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/nation.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node regionNode = this.graphDb.findNode(
							DynamicLabel.label("Region"), "REGIONKEY",
							lineFields[2]);

					Node nationNode = this.graphDb.findNode(
							DynamicLabel.label("Nation"), "NATIONKEY",
							lineFields[0]);

					Iterable<Relationship> relationships = nationNode
							.getRelationships();
					Iterator<Relationship> relationshipsIterator = relationships
							.iterator();

					boolean relExists = false;

					while (relationshipsIterator.hasNext()) {
						Relationship rel = relationshipsIterator.next();

						if (rel.getEndNode().equals(regionNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						nationNode.createRelationshipTo(regionNode,
								RelTypes.LOCATED_IN);
						System.out
								.println("LOCATED_IN relationship have been created");
					}
					tx.success();
				} catch (Exception e) {
					System.out
							.println("LOCATED_IN relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createSuppliedByRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/partsupp.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node partNode = this.graphDb.findNode(
							DynamicLabel.label("Part"), "PARTKEY",
							lineFields[0]);

					Node suppNode = this.graphDb.findNode(
							DynamicLabel.label("Supplier"), "SUPPKEY",
							lineFields[1]);

					Iterable<Relationship> relationships = partNode
							.getRelationships();
					Iterator<Relationship> relationshipsIterator = relationships
							.iterator();

					boolean relExists = false;

					while (relationshipsIterator.hasNext()) {
						Relationship rel = relationshipsIterator.next();

						if (rel.getEndNode().equals(suppNode)) {
							relExists = true;
						}
					}

					if (!relExists) {
						partNode.createRelationshipTo(suppNode,
								RelTypes.SUPPLIED_BY);
						System.out
								.println("SUPPLIED_BY relationship have been created");
					}
					tx.success();
				} catch (Exception e) {
					System.out
							.println("SUPPLIED_BY relationship creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createHasDetailsRelationship() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/lineitem.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] itemLineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				File partSuppFile = new File(
						"src/main/java/org/neo4j/tpcH/data/partsupp.txt");
				try (BufferedReader brPartSupp = new BufferedReader(
						new FileReader(partSuppFile))) {
					String linPartSuppe;

					try (Transaction tx = this.graphDb.beginTx()) {

						Node lineitemNode = this.graphDb.findNode(
								DynamicLabel.label("Lineitem"), "LINENUMBER",
								itemLineFields[3]);

						Node partNode = this.graphDb.findNode(
								DynamicLabel.label("Part"), "PARTKEY",
								itemLineFields[1]);

						Iterable<Relationship> relationships = lineitemNode
								.getRelationships();
						Iterator<Relationship> relationshipsIterator = relationships
								.iterator();

						boolean relExists = false;

						while (relationshipsIterator.hasNext()) {
							Relationship rel = relationshipsIterator.next();

							if (rel.getEndNode().equals(partNode)) {
								relExists = true;
							}
						}

						if (!relExists) {

							Relationship hasDetailsRel = lineitemNode
									.createRelationshipTo(partNode,
											RelTypes.HAS_DETAILS);

							while ((linPartSuppe = brPartSupp.readLine()) != null) {

								String[] partSuppLineFields = linPartSuppe
										.split(Pattern.quote("|"));

								if (partSuppLineFields[0]
										.equals(itemLineFields[1])
										&& partSuppLineFields[1]
												.equals(itemLineFields[2])) {
									hasDetailsRel.setProperty("AVAILQTY",
											partSuppLineFields[2]);
									hasDetailsRel.setProperty("SUPPLYCOST",
											partSuppLineFields[3]);
									hasDetailsRel.setProperty("COMMENT",
											partSuppLineFields[4]);

								}

							}

							System.out
									.println("HAS_DETAILS relationship have been created");
						}

						tx.success();
					} catch (Exception e) {
						System.out
								.println("HAS_DETAILS relationship creation failed");
						System.out.println("Error is "
								+ e.getLocalizedMessage());
					}

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

	void createSupplierNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/supplier.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Supplier"))
					.assertPropertyIsUnique("SUPPKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out.println("unique constraint on SUPPKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node supplier = this.graphDb.createNode(DynamicLabel
							.label("Supplier"));
					supplier.setProperty("SUPPKEY", lineFields[0]);
					supplier.setProperty("NAME", lineFields[1]);
					supplier.setProperty("ADDRESS", lineFields[2]);
					supplier.setProperty("PHONE", lineFields[4]);
					supplier.setProperty("ACCTBAL", lineFields[5]);
					supplier.setProperty("COMMENT", lineFields[6]);

					System.out.println("Supplier node with SUPPKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Supplier node with SUPPKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createCustomerNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/customer.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Customer"))
					.assertPropertyIsUnique("CUSTKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out.println("unique constraint on CUSTKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node customer = this.graphDb.createNode(DynamicLabel
							.label("Customer"));
					customer.setProperty("CUSTKEY", lineFields[0]);
					customer.setProperty("NAME", lineFields[1]);
					customer.setProperty("ADDRESS", lineFields[2]);
					customer.setProperty("PHONE", lineFields[4]);
					customer.setProperty("ACCTBAL", lineFields[5]);
					customer.setProperty("MKTSEGMENT", lineFields[6]);
					customer.setProperty("COMMENT", lineFields[7]);

					System.out.println("Customer node with CUSTKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Customer node with CUSTKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createLineItemNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/lineitem.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Lineitem"))
					.assertPropertyIsUnique("LINENUMBER").create();
			tx.success();
		} catch (Exception e) {
			System.out
					.println("unique constraint on LINENUMBER creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node Lineitem = this.graphDb.createNode(DynamicLabel
							.label("Lineitem"));
					Lineitem.setProperty("LINENUMBER", lineFields[3]);
					Lineitem.setProperty("QUANTITY", Double.valueOf(lineFields[4]));
					Lineitem.setProperty("EXTENDEDPRICE", Double.valueOf(lineFields[5]));
					Lineitem.setProperty("DISCOUNT", Double.valueOf(lineFields[6]));
					Lineitem.setProperty("TAX", Double.valueOf(lineFields[7]));
					Lineitem.setProperty("RETURNFLAG", lineFields[8]);
					Lineitem.setProperty("LINESTATUS", lineFields[9]);
					Lineitem.setProperty("SHIPDATE", format.parse(lineFields[10]).getTime());
					Lineitem.setProperty("COMMITDATE", format.parse(lineFields[11]).getTime());
					Lineitem.setProperty("RECEIPTDATE", format.parse(lineFields[12]).getTime());
					Lineitem.setProperty("SHIPINSTRUCT", lineFields[13]);
					Lineitem.setProperty("SHIPMODE", lineFields[14]);
					Lineitem.setProperty("COMMENT", lineFields[15]);

					System.out.println("Lineitem node with LINENUMBER: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Lineitem node with LINENUMBER: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createNationNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/nation.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Nation"))
					.assertPropertyIsUnique("NATIONKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out
					.println("unique constraint on NATIONKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node Nation = this.graphDb.createNode(DynamicLabel
							.label("Nation"));
					Nation.setProperty("NATIONKEY", lineFields[0]);
					Nation.setProperty("NAME", lineFields[1]);
					Nation.setProperty("COMMENT", lineFields[3]);

					System.out.println("Nation node with NATIONKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Nation node with NATIONKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createOrderNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/order.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Order"))
					.assertPropertyIsUnique("ORDERKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out.println("unique constraint on ORDERKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node Order = this.graphDb.createNode(DynamicLabel
							.label("Order"));
					Order.setProperty("ORDERKEY", lineFields[0]);
					Order.setProperty("ORDERSTATUS", lineFields[2]);
					Order.setProperty("TOTALPRICE", lineFields[3]);
					Order.setProperty("ORDERDATE", format.parse(lineFields[4]).getTime());
					Order.setProperty("ORDERPRIORITY", lineFields[5]);
					Order.setProperty("CLERK", lineFields[6]);
					Order.setProperty("SHIPPRIORITY", lineFields[7]);
					Order.setProperty("COMMENT", lineFields[8]);

					System.out.println("Order node with ORDERKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Order node with ORDERKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createPartNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/part.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Part"))
					.assertPropertyIsUnique("PARTKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out.println("unique constraint on PARTKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node Part = this.graphDb.createNode(DynamicLabel
							.label("Part"));
					Part.setProperty("PARTKEY", lineFields[0]);
					Part.setProperty("NAME", lineFields[1]);
					Part.setProperty("MFGR", lineFields[2]);
					Part.setProperty("BRAND", lineFields[3]);
					Part.setProperty("TYPE", lineFields[4]);
					Part.setProperty("SIZE", lineFields[5]);
					Part.setProperty("CONTAINER", lineFields[6]);
					Part.setProperty("RETAILPRICE", lineFields[7]);
					Part.setProperty("COMMENT", lineFields[8]);

					System.out.println("Part node with PARTKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Part node with PARTKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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

	void createRegionNodes() {

		File file = new File("src/main/java/org/neo4j/tpcH/data/region.txt");

		try (Transaction tx = this.graphDb.beginTx()) {

			this.graphDb.schema().constraintFor(DynamicLabel.label("Region"))
					.assertPropertyIsUnique("REGIONKEY").create();
			tx.success();
		} catch (Exception e) {
			System.out
					.println("unique constraint on REGIONKEY creation failed");
			System.out.println("Error is " + e.getLocalizedMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			if (this.graphDb == null) {
				System.out.println("graphDb is null");
			}

			while ((line = br.readLine()) != null) {
				// process the line.

				String[] lineFields = line.split(Pattern.quote("|"));

				// System.out.println(Arrays.toString(lineFields));

				try (Transaction tx = this.graphDb.beginTx()) {

					Node Region = this.graphDb.createNode(DynamicLabel
							.label("Region"));
					Region.setProperty("REGIONKEY", lineFields[0]);
					Region.setProperty("NAME", lineFields[1]);
					Region.setProperty("COMMENT", lineFields[2]);

					System.out.println("Region node with REGIONKEY: "
							+ lineFields[0] + " have been created");
					tx.success();
				} catch (Exception e) {
					System.out.println("Region node with REGIONKEY: "
							+ lineFields[0] + "  creation failed");
					System.out.println("Error is " + e.getLocalizedMessage());
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
