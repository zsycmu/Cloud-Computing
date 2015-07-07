import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

public class project_2_3 {
	public static void main(String[] args) throws IOException, InterruptedException{
		
		float throughput = 0;
		int instanceNum = 0;
		//Load the Properties File with AWS Credentials
		Properties properties = new Properties();
	 	properties.load(project_2_3.class.getResourceAsStream("/AwsCredentials.properties"));
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
		
		//Create ELB
		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(bawsc);
		CreateLoadBalancerRequest createELB = new CreateLoadBalancerRequest()
			.withLoadBalancerName("poject2-3")
			.withAvailabilityZones("us-east-1a");
        List<Listener> listeners = new ArrayList<Listener>(1);
        listeners.add(new Listener("HTTP", 80, 80));
        listeners.add(new Listener("HTTP", 8080, 8080));
        createELB.setListeners(listeners);
        CreateLoadBalancerResult lbResult=elb.createLoadBalancer(createELB);
        HealthCheck healthCK = new HealthCheck()
    		.withTarget("HTTP:8080/upload")
    		.withTimeout(5)
    		.withInterval(30)
    		.withUnhealthyThreshold(2)
    		.withHealthyThreshold(10);
        ConfigureHealthCheckRequest healthCheckReq = new ConfigureHealthCheckRequest()
    		.withHealthCheck(healthCK)
    		.withLoadBalancerName(createELB.getLoadBalancerName());
        ConfigureHealthCheckResult confChkResult = elb.configureHealthCheck(healthCheckReq);
        //System.out.println("created load balancer loader");
        //System.out.println(lbResult.getDNSName());
        //Start and add the first instance
        lanchandaddInstance(elb, createELB);
        
        //run benchmark and write the result into the file
        File file1 = new File("outputlarge1.txt");
        try {
        	Runtime r = Runtime.getRuntime();
    		Process p = r.exec("./apache_bench.sh sample.jpg 100000 100" + " " + lbResult.getDNSName().toString() + " " + "logfile");
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
        
        
        //Read the output file to get the requests per second
        try {
        	File file = new File("outputlarge1.txt");
        	FileReader fileReader = new FileReader(file);
        	BufferedReader bufferedReader = new BufferedReader(fileReader);
        	StringBuffer stringBuffer = new StringBuffer();
        	String line;
        	while ((line = bufferedReader.readLine()) != null) {
        		String line2 = line.replaceAll("\\s","");
        		String[] parts = line2.split(":");
        		if(parts[0].equals("Requestspersecond")){
        			String[] parts2 = parts[1].split("\\[");
        			throughput = Float.parseFloat(parts2[0]);
        			//stringBuffer.append(parts2[0]);
        			//stringBuffer.append("\n");
        		}
        	}
        	fileReader.close();
        	//System.out.println("Contents of file:");
        	//System.out.println(stringBuffer.toString());
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        //If the throughput of lower 2000 Requests/sec add one instance
        while(throughput < 2000 ){
        	lanchandaddInstance(elb, createELB);
        	instanceNum++;
        	//run benchmark and write the result into the file
            file1 = new File("outputlarge1.txt");
            try {
            	Runtime r = Runtime.getRuntime();
        		Process p = r.exec("./apache_bench.sh sample.jpg 100000 100" + " " + lbResult.getDNSName().toString() + " " + "logfile");
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
            
            
            //Read the output file to get the requests per second
            try {
            	File file = new File("outputlarge1.txt");
            	FileReader fileReader = new FileReader(file);
            	BufferedReader bufferedReader = new BufferedReader(fileReader);
            	StringBuffer stringBuffer = new StringBuffer();
            	String line;
            	while ((line = bufferedReader.readLine()) != null) {
            		String line2 = line.replaceAll("\\s","");
            		String[] parts = line2.split(":");
            		if(parts[0].equals("Requestspersecond")){
            			String[] parts2 = parts[1].split("\\[");
            			throughput = Float.parseFloat(parts2[0]);
            			//stringBuffer.append(parts2[0]);
            			//stringBuffer.append("\n");
            		}
            	}
            	fileReader.close();
            	//System.out.println("Contents of file:");
            	//System.out.println(stringBuffer.toString());
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        String ThroughPut = Float.toString(throughput);
        String InstanceNum = Integer.toString(instanceNum);
        File result = new File("result.txt");
        try {
            File newTextFile = new File("C:/thetextfile.txt");

            FileWriter fw = new FileWriter(newTextFile);
            fw.write("The final through put is:");
            fw.write("\n");
            fw.write(ThroughPut);
            fw.write("The total number of instance is:");
            fw.write(InstanceNum);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
        }
	}
	
	public static void lanchandaddInstance(AmazonElasticLoadBalancingClient elb, CreateLoadBalancerRequest createELB) throws IOException, InterruptedException{
		//Load the Properties File with AWS Credentials
		Properties properties = new Properties();
		properties.load(project_2_3.class.getResourceAsStream("/AwsCredentials.properties"));
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
		//Create the first instance and add it to ELB
        //Create an Amazon EC2 Client
	 	AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);

	 	//Create Instance Request
	 	RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

	 	//Configure Instance Request
	 	runInstancesRequest.withImageId("ami-69e3d500")
	 		.withInstanceType("m1.small")
	 		.withMinCount(1)
	 		.withMaxCount(1)
	 		.withKeyName("project2.1")
	 		.withSecurityGroups("project2.1")
	 		.withMonitoring(true);

	 	Placement placement = new Placement();
	 	placement.setAvailabilityZone("us-east-1a");
	 	runInstancesRequest.setPlacement(placement);
	 	//Launch Instance
	 	RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);   
	 	//Return the Object Reference of the Instance just Launched
	 	Instance instance = runInstancesResult.getReservation().getInstances().get(0);
	 	
        //Add instance to the ELB
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<Instance> instances = new ArrayList<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }


        //get instance id's
        String id;
        List instanceId=new ArrayList();
        List instanceIdString=new ArrayList();
        Iterator<Instance> iterator=instances.iterator();
        while (iterator.hasNext())
        {
            id=iterator.next().getInstanceId();
            instanceId.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(id));
            instanceIdString.add(id);
        }


        //register the instances to the balancer
        RegisterInstancesWithLoadBalancerRequest register =new RegisterInstancesWithLoadBalancerRequest();
        register.setLoadBalancerName(createELB.getLoadBalancerName());
        register.setInstances((Collection)instanceId);
        RegisterInstancesWithLoadBalancerResult registerWithLoadBalancerResult= elb.registerInstancesWithLoadBalancer(register);
        
        
        Thread.sleep(300000);
	}
}
