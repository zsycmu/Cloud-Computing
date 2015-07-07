

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import java.util.HashSet;

public class InvertedIndex {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		private Text word = new Text();
		private Text loc = new Text();
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			FileSplit fs = (FileSplit) reporter.getInputSplit();
			String location = fs.getPath().getName();
			loc.set(location);
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line.toLowerCase());
			while (tokenizer.hasMoreTokens()) {
				word.set(tokenizer.nextToken() + ":");
				output.collect(word, loc);
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			boolean first = true;
			StringBuilder result = new StringBuilder();
            HashSet<String> set = new HashSet<String>();
			while (values.hasNext()) {
				if (!set.contains(values.toString())){
					if (!first) {
						result.append(" ");
					}
					first = false;
					result.append(values.toString());
					set.add(values.next().toString());
				}
			}
			
			output.collect(key, new Text(result.toString()));
		}
	}
	
	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(InvertedIndex.class);
		
		conf.setJobName("InvertedIndex");
		
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
