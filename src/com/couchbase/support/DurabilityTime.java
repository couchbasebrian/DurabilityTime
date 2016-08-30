package com.couchbase.support;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.ReplicateTo;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment.Builder;

// August 30, 2016
// Brian Williams
// This was created with Couchbase Java Client 2.1.4

public class DurabilityTime {

	private String     documentKeyBase;
	private Bucket     b;
	private JsonObject jsonObject;
	private int        expiry;

	DurabilityTime() {
		// Choose a random document key base for this session.  We do this because we are inserting
		// and this will avoid any document collisions.
		int random = (int) (Math.random() * 10000);
		documentKeyBase = "document-" + random + "-";
		System.out.println("Using document key base: " + documentKeyBase);
		
		// These documents expire after 10 seconds
		expiry          = 10;
	}

	public static void main(String[] args) {
		DurabilityTime dt = new DurabilityTime();
		dt.runTest();
	}

	void runTest() {

		// Please set these for your system
		String host               = "192.168.0.1";
		String bucketName         = "bucketName";

		// We will just use the same document for our tests
		String jsonDocumentString = "{ \"message\" : \"hello\" }";
		jsonObject = com.couchbase.client.java.document.json.JsonObject.fromJson(jsonDocumentString);

		Builder builder = DefaultCouchbaseEnvironment.builder();
		
		// Set all the timeouts to 60 seconds
		long largeTimeout = 60000;
		long connectTimeout    = largeTimeout;
		long kvTimeout         = largeTimeout;
		long disconnectTimeout = largeTimeout;
		long managementTimeout = largeTimeout;
		long queryTimeout      = largeTimeout;
		long viewTimeout       = largeTimeout;

		builder.kvTimeout(kvTimeout);
		builder.connectTimeout(connectTimeout);
		builder.disconnectTimeout(disconnectTimeout);
		builder.managementTimeout(managementTimeout);
		builder.queryTimeout(queryTimeout);
		builder.viewTimeout(viewTimeout);

		// Build the environment, connect to the cluster, and open the bucket
		DefaultCouchbaseEnvironment environment = builder.build();
		Cluster c = CouchbaseCluster.create(environment, "http://" + host + ":8091"); 
		b = c.openBucket(bucketName);

		boolean keepRunning         = true;
		int     iteration           = 0;
		long    delayBetweenInserts = 100; // milliseconds

		// On each pass, we try to insert two documents.  One with durability options and one without.
		// We measure the time for each,a and whether there was an exception
		while (keepRunning) {

			// Each document uses a unique key based on the count and a shared document key base
			InsertResult resultWith    = insertOneDocument(iteration++, PersistTo.MASTER, ReplicateTo.ONE);
			InsertResult resultWithout = insertOneDocument(iteration++, PersistTo.NONE,   ReplicateTo.NONE);

			long elapsedTimeWith    = resultWith.elapsedTime;
			long elapsedTimeWithout = resultWithout.elapsedTime;

			String exceptionWith = "None", exceptionWithout = "None";

			// Translate the long exception message into something smaller that fits on the line
			if (resultWith.exception    != null) { exceptionWith    = getExceptionMessage(resultWith.exception   ); }
			if (resultWithout.exception != null) { exceptionWithout = getExceptionMessage(resultWithout.exception); }

			// Report on the results of these two documents
			System.out.printf("Iteration %6d    With Durability: %10d ms ( %10s ) Without Durability: %10d ms ( %10s )\n", 
					iteration, 
					elapsedTimeWith, 
					exceptionWith,
					elapsedTimeWithout,
					exceptionWithout);

			// Sleep between successive attempts.  
			try {
				Thread.sleep(delayBetweenInserts);
			} catch (Exception e) {
				// If an exception occurs while sleeping then just exit
				e.printStackTrace();
				System.exit(1);
			}

		}

	} // runTest

	// This is for translating the longer exception message into something smaller that fits on the line
	String getExceptionMessage(Exception e) {
		String eClassName = e.getClass().getName();
		String rval = "Exception";

		if (eClassName.equals("com.couchbase.client.java.error.DocumentAlreadyExistsException")) {
			rval = "DocExists";
		}
		if (eClassName.equals("java.lang.RuntimeException")) {
			if (e.getMessage().equals("java.util.concurrent.TimeoutException")) {
				rval = "Timeout";
			}
			else {
				// It wasn't a Runtime Exception that we expected
				rval = "[[ " + e.getMessage() + " ]]";
			}
		}
		if (eClassName.equals("com.couchbase.client.java.error.DurabilityException")) {
			rval = "Durability";
		}
		else {
			// It wasn't an exception that we expected
			e.printStackTrace();
			rval = eClassName;
		}

		return rval;

	}

	// This is for inserting one document, based on the current iteration/count, and also on certain
	// durability options.  Returns an object with the results of the attempt.
	InsertResult insertOneDocument(int i, PersistTo p, ReplicateTo r) {
		InsertResult rval = new InsertResult();
		String documentKey = documentKeyBase + i;
		JsonDocument jsonDocument = JsonDocument.create(documentKey, expiry, jsonObject);
		long t1, t2;
		t1 = System.currentTimeMillis();
		try {
			b.insert(jsonDocument, p, r);
		}
		catch (Exception e) {
			// e.printStackTrace();
			rval.exception = e;
		}
		t2 = System.currentTimeMillis();
		rval.elapsedTime = t2 - t1;
		return rval;
	}

}

// This is just a simple object that contains the results of the test
class InsertResult {
	InsertResult() {
		elapsedTime = 0;
		exception = null;
	}
	long elapsedTime;
	Exception exception;
}

// EOF