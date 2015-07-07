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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class probability {

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
				}
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		private Text printout = new Text();
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			
			HashMap<String, String> map = new HashMap<String, String>();
//			Stack<Entry> stack = new Stack<Entry>();
//			StringBuilder sb = new StringBuilder();
			while (values.hasNext()) {
				int counter = 0;
				if (counter < 5) {
					String getthisvalue = values.next().toString();
					String tempstring = new String(getthisvalue);
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
					
					String w = sb1.toString();
					String count = sb2.toString();
					String keytemp = key.toString();
					int countint = Integer.parseInt(count);
					
					if (countint > 10) {
						output.collect(new Text("hahhha"), new Text(count));
						map.put(keytemp + "@" + w, count);
					}
					counter++;
				} else {
					break;
				}
			}	
			
			Map result = (Map) ngramsort.sortit(map);
		}
	}
	
	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(probability.class);
		conf.setJobName("probability");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}

class ngramsort {
	public static Map sortit(Map mapunsorted) {
		Map mapsort = new TreeMap(new ngramcomparator(mapunsorted));
		mapsort.putAll(mapunsorted);
		return mapsort;
	}
}

class ngramcomparator implements Comparator {
	Map map;
	
	public ngramcomparator(Map map) {
		this.map = map;
	}

	public int compare(Object first, Object second) {
		Comparable valueA = (Comparable) map.get(first);
		Comparable valueB = (Comparable) map.get(second);
		return valueA.compareTo(valueB);
	}
}