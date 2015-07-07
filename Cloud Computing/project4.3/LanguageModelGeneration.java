package edu.cmu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.naming.Context;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;

import sort.probability.Reduce;

public class LanguageModelGeneration {

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		
		private Text context = new Text();
		private Text suffix = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			String[] splitline = line.split("\t");
			if (splitline.length == 2) {
				String firstpart = splitline[0];
				String count = splitline[1];
				String[] firstsplit = firstpart.split(" ");
				int length = firstsplit.length;
				if (length >= 2) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < length - 1; i++) {
						sb.append(firstsplit[i] + (i == length - 2 ? "" : " "));
					}
					String suffixtemp = firstsplit[length - 1];
					String printsecondpart = "@" + suffixtemp + "\t" + count;
					context.set(sb.toString());
					suffix.set(printsecondpart);
					output.collect(context, suffix);
				} else {
					context.set(firstsplit[0]);
					suffix.set(count);
					output.collect(context, suffix);
				}
			}
		}
	}
	
	public static class Reduce extends TableReducer<Text, Text, ImmutableBytesWritable> {
		private Text printout = new Text();
		public void reduce(Text key, Iterator<Text> values, Context context)
				throws IOException {
			
			HashMap<String, String> map = new HashMap<String, String>();
//			Stack<Entry> stack = new Stack<Entry>();
//			StringBuilder sb = new StringBuilder();
			String w = null;
			String count = null;
			String keytemp = null;
			while (values.hasNext()) {
				int counter = 0;
				float upper = 0;
				float lower = 0;
				float probability = 0;
				if (counter < 5) {
					String getthisvalue = values.next().toString();
					String tempstring = new String(getthisvalue);
					if (tempstring.startsWith("@")) {
						StringBuffer sb1 = new StringBuffer();
						StringBuffer sb2 = new StringBuffer();
						
						for (int i = 0; i < tempstring.length(); i++) {
							if (Character.isLowerCase(tempstring.charAt(i)) || tempstring.charAt(i) == ' ') {
								sb1.append(tempstring.charAt(i));
							}
							if (Character.isDigit(tempstring.charAt(i))) {
								sb2.append(tempstring.charAt(i));
							}
						}
						
						w = sb1.toString();
						count = sb2.toString();
						keytemp = key.toString();
						int countint = Integer.parseInt(count);
						upper = countint;
						
					} else {
						String[] splitstring = getthisvalue.split("\t");
						lower = Float.parseFloat(splitstring[2]);
						
					}
					if (upper > 2) {
//						output.collect(new Text("hahhha"), new Text(count));
						map.put(keytemp + "@" + w, count);
					}
					counter++;
					
				} else {
					break;
				}
				Map<String, Float> result = mapsort.sortit(map);
				Iterator it = result.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					w = entry.getKey();
					count =  entry.getValue();
					it.remove();
				}
				final byte[] CF = "w".getBytes();
				final byte[] COUNT = "keytemp".getBytes();
				final byte[] VALUE = "probability".getBytes();
				Put put = new Put(Bytes.toBytes(key.toString()));
	    		put.add(CF, COUNT, VALUE);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = HBaseConfiguration.create();
		Job job = new Job(conf, "LanguageModelGeneration");
		job.setJobName("LanguageModelGeneration");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
//		conf.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormat(ImmutableBytesWritable.class);
//		conf.setOutputFormat(TableOutputFormat.class);
		
		TableMapReduceUtil.initTableReducerJob("predict", Reduce.class, job);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
//		FileOutputFormat.setOutputPath(conf, new Path(args[]));
		JobClient.runJob(job);
	}
}

class newngramsort {
	public static Map sortit(Map mapunsorted) {
		Map mapsort = new TreeMap(new newngramcomparator(mapunsorted));
		mapsort.putAll(mapunsorted);
		return mapsort;
	}
}

class newngramcomparator implements Comparator {
	Map map;
	
	public newngramcomparator(Map map) {
		this.map = map;
	}

	public int compare(Object first, Object second) {
		Comparable valueA = (Comparable) map.get(first);
		Comparable valueB = (Comparable) map.get(second);
		return valueA.compareTo(valueB);
	}
}