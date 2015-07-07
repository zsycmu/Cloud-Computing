import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;


public class resultlarge {
	public static void main(String[] args) throws IOException, InterruptedException {
		long offsetInMilliseconds = 0;
		//Load the Properties File with AWS Credentials
	 	Properties properties = new Properties();
	 	properties.load(resultlarge.class.getResourceAsStream("/AwsCredentials.properties"));

	 	BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));

	 	//Create an Amazon EC2 Client
	 	AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);

	 	//Create Instance Request
	 	RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

	 	//Configure Instance Request
	 	runInstancesRequest.withImageId("ami-69e3d500")
	 	.withInstanceType("m1.large")
	 	.withMinCount(1)
	 	.withMaxCount(1)
	 	.withKeyName("project2.1")
	 	.withSecurityGroups("project2.1")
	 	.withMonitoring(true);

	 	//Launch Instance
	 	RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);   

	 	//Return the Object Reference of the Instance just Launched
	 	Instance instance=runInstancesResult.getReservation().getInstances().get(0);
	 	
	 	
	 	Thread.sleep(300000);
	 	//Launch an EC2 Client
	 	AmazonEC2Client amazonEC2Client = new AmazonEC2Client(bawsc);
	 	 
	 	//Obtain a list of Reservations
	 	List<Reservation> reservations = amazonEC2Client.describeInstances().getReservations();
	 	File file1 = new File("outputlarge1.txt");
	 	File file2 = new File("outputlarge2.txt");
	 	int reservationCount = reservations.size();
	 	 
	 	for(int i = 0; i < reservationCount; i++) {
	 	    List<Instance> instances = reservations.get(i).getInstances();
	 	 
	 	    int instanceCount = instances.size();
	 	     
	 	    //Print the instance IDs of every instance in the reservation.
	 	    for(int j = 0; j < instanceCount; j++) {
	 	        instance = instances.get(j);
	 	 
	 	        if(instance.getState().getName().equals("running")) {
	 	            //System.out.println(instance.getInstanceId());
	 	            //System.out.println(instance.getPublicDnsName());
	 	            offsetInMilliseconds = new Date().getTime();
		 	        //benchmark
		 	   	 	try {
		 	   	 		Runtime r = Runtime.getRuntime();
		 	   	 		Process p = r.exec("./apache_bench.sh sample.jpg 100000 100" + " " + instance.getPrivateDnsName().toString() + " " + "logfile");
		 	   	 		p.waitFor();
		 	   	 		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 	   	 		String line = "";
		 	   	 		BufferedWriter output1 = new BufferedWriter(new FileWriter(file1));
		 	   	 		while ((line = b.readLine()) != null) {
			 	   	 		try {
			 	   	          output1.write(line + "\n");
			 	   	        } catch ( IOException e ) {
			 	   	           //e.printStackTrace();
			 	   	        }
		 	   	 		}
		 	   	 		output1.close();
	
		 	   	 	}
		 	   	 	catch(IOException e1) {}
		 	   	 	catch(InterruptedException e2) {}
	 	        }
	 	    }
	 	}
	 	//Cloud Watch
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
			.withStartTime(new Date(offsetInMilliseconds))
			.withNamespace("AWS/EC2")
			.withPeriod(60 * 60)
			.withDimensions(new Dimension().withName("InstanceId").withValue(instance.getInstanceId()))
			.withMetricName("CPUUtilization")
			.withStatistics("Average", "Maximum")
			.withEndTime(new Date());
		AmazonCloudWatchClient cw = new AmazonCloudWatchClient(bawsc);
		GetMetricStatisticsResult getMetricStatisticsResult = cw.getMetricStatistics(request);
		double avgCPUUtilization = 0;
		List<Datapoint> dataPoint = getMetricStatisticsResult.getDatapoints();
		for (Object aDataPoint : dataPoint) {
			Datapoint dp = (Datapoint) aDataPoint;
			avgCPUUtilization = dp.getAverage();
			String result = Double.toString(avgCPUUtilization);
			//System.out.println(instance.getInstanceId() + " instance's average CPU utilization : " + dp.getAverage());
			
			try {
				BufferedWriter output2 = new BufferedWriter(new FileWriter(file2));
				output2.write(result + "\n");
				output2.close();
			} 
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
}
