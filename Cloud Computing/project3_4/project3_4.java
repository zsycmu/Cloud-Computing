import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.util.Tables;

public class project3_4 {

	static AmazonDynamoDBClient dynamoDB;

    private static void init() throws Exception {
        dynamoDB = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        dynamoDB.setRegion(usWest2);
    }


    public static void main(String[] args) throws Exception {
        init();
        String readFile = "caltech-256.csv";
		BufferedReader br = null;
        try {
            String tableName = "Caltech256";

            if (Tables.doesTableExist(dynamoDB, tableName)) {
            	System.out.println("Table " + tableName + " is already ACTIVE");
            } else {
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("category").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("category").withAttributeType(ScalarAttributeType.S))
                    .withKeySchema(new KeySchemaElement().withAttributeName("picture").withKeyType(KeyType.RANGE))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("picture").withAttributeType(ScalarAttributeType.N))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(500L).withWriteCapacityUnits(500L));
                    
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                System.out.println("Waiting for " + tableName + " to become ACTIVE...");
                Tables.waitForTableToBecomeActive(dynamoDB, tableName);
            }

            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            //Save all the data here.
            
    		String line = "";
    		try {
    			br = new BufferedReader(new FileReader(readFile));
    			br.readLine();
    			while ((line = br.readLine()) != null) {
    				String[] eachLine = line.split(",");
    				String category = eachLine[0];
    				int picture = Integer.parseInt(eachLine[1]);
    				String URL = eachLine[2];
    				Map<String, AttributeValue> item = newItem(category, picture, URL);
    	            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
    	            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
    			}
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		try {
    			br.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private static Map<String, AttributeValue> newItem(String category, int picture, String S3URL) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("category", new AttributeValue(category));
        item.put("picture", new AttributeValue().withN(Integer.toString(picture)));
        item.put("S3URL", new AttributeValue(S3URL));

        return item;
    }
}