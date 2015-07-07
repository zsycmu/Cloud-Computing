package myaccount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;

public class project2_4 {
	public static void main(String[] args) throws IOException, InterruptedException{
		//Load the Properties File with AWS Credentials
		Properties properties = new Properties();
		properties.load(project2_4.class.getResourceAsStream("/AwsCredentials.properties"));
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
		
		//Create ELB
		createELB(bawsc);
		
        //Create AutoScaling
		AmazonAutoScalingClient autoscaling = createAutoScaling(bawsc);
		
		//Add policy
		PutScalingPolicyRequest addRequest = new PutScalingPolicyRequest()
			.withAutoScalingGroupName("project2_4")
			.withPolicyName("Add Instance")
			.withScalingAdjustment(1)
			.withAdjustmentType("ChangeInCapacity");

		PutScalingPolicyResult addResult = autoscaling.putScalingPolicy(addRequest);
		String addARN = addResult.getPolicyARN();
		
		//Delete policy
		PutScalingPolicyRequest minRequest = new PutScalingPolicyRequest()
			.withAutoScalingGroupName("project2_4")
			.withPolicyName("Add Instance")
			.withScalingAdjustment(-1)
			.withAdjustmentType("ChangeInCapacity");
		PutScalingPolicyResult minResult = autoscaling.putScalingPolicy(minRequest);
		String minARN = minResult.getPolicyARN();
        
        
		String ARN = "arn:aws:sns:us-east-1:035369440749:project2-4";
		
		AmazonCloudWatchClient cloudWatchClient = new AmazonCloudWatchClient(bawsc);
		//Add instance
		List dimensions = new ArrayList();
		Dimension dimension = new Dimension()
			.withName("AutoScalingGroupName")
			.withValue("Project2-4");
		
		List actions = new ArrayList();
		actions.add(ARN); 
		actions.add(addARN);
		
		PutMetricAlarmRequest ScaleUp = new PutMetricAlarmRequest()
			.withAlarmName("Add instance")
			.withMetricName("CPUUtilization")
			.withDimensions(dimensions)
			.withNamespace("AWS/EC2")
			.withComparisonOperator(ComparisonOperator.GreaterThanThreshold)
			.withStatistic(Statistic.Average)
			.withUnit(StandardUnit.Percent)
			.withThreshold(80.0)
			.withPeriod(300)
			.withEvaluationPeriods(1)
			.withAlarmActions(actions);
		
		cloudWatchClient.putMetricAlarm(ScaleUp);
        
		//Delete instance
		List dimensions2 = new ArrayList();
		Dimension dimension2 = new Dimension()
			.withName("AutoScalingGroupName")
			.withValue("Project2-4");
		
		List actions2 = new ArrayList();
		actions2.add(ARN);
		actions2.add(minARN);
		
		PutMetricAlarmRequest downRequest = new PutMetricAlarmRequest()
			.withAlarmName("Delete instance")
			.withMetricName("CPUUtilization")
			.withDimensions(dimensions2)
			.withNamespace("AWS/EC2")
			.withComparisonOperator(ComparisonOperator.LessThanThreshold)
			.withStatistic(Statistic.Average)
			.withUnit(StandardUnit.Percent)
			.withThreshold(20.0)
			.withPeriod(300)
			.withEvaluationPeriods(1)
			.withAlarmActions(actions2);
		
		cloudWatchClient.putMetricAlarm(downRequest);
	}
	
	
	public static void createELB(BasicAWSCredentials bawsc) {
		//Create ELB
		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(bawsc);
		CreateLoadBalancerRequest createELB = new CreateLoadBalancerRequest()
			.withLoadBalancerName("poject2-4")
			.withSecurityGroups("sg-b39b4cd6")
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
	}
	
	//Create auto scaling
	public static AmazonAutoScalingClient createAutoScaling(BasicAWSCredentials bawsc) {
		AmazonAutoScalingClient autoscaling = new AmazonAutoScalingClient(bawsc);
		
		CreateLaunchConfigurationRequest LaunchConfigRequest = new CreateLaunchConfigurationRequest()
			.withLaunchConfigurationName("project2_4")
			.withImageId("ami-99e2d4f0")
			.withInstanceType("m1.small");
		
		List securityGroups = new ArrayList();
		securityGroups.add("sg-b39b4cd6");
		LaunchConfigRequest.setSecurityGroups(securityGroups);
		InstanceMonitoring monitoring = new InstanceMonitoring()
			.withEnabled(true);
		LaunchConfigRequest.setInstanceMonitoring(monitoring);
		autoscaling.createLaunchConfiguration(LaunchConfigRequest);
		List availabilityZone = new ArrayList();
		availabilityZone.add("us-east-1a");
		List elbs = new ArrayList();
		elbs.add("poject2-4");
		
		CreateAutoScalingGroupRequest AutoScalGroupRequest = new CreateAutoScalingGroupRequest()
			.withAutoScalingGroupName("project2_4")
			.withLaunchConfigurationName("project2_4")
			.withAvailabilityZones(availabilityZone)
			.withMinSize(2)
			.withMaxSize(5)
			.withDesiredCapacity(2)
			.withLoadBalancerNames(elbs)
			.withHealthCheckType("ELB")
			.withHealthCheckGracePeriod(300);
		
		autoscaling.createAutoScalingGroup(AutoScalGroupRequest);
		
		return autoscaling;
	}
}