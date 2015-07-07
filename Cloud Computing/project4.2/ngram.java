package edu.cmu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

import java.util.Map;

public class ngram {
    
	public static class Map extends MapReduceBase implements
    Mapper<LongWritable, Text, Text, IntWritable> {
        
		public static String form(String[] words, int start, int end) {
			StringBuilder sb = new StringBuilder();
			for (int i = start; i < end; i++)
				sb.append((i > start ? " " : "") + words[i]);
			return sb.toString();
		}
        
		public static List<String> ngrams(String str, int n) {
			List<String> ngrams = new ArrayList<String>();
			String temp = str.replaceAll("[^a-zA-Z ]", " ");
			String[] words = temp.trim().replaceAll(" +", " ").toLowerCase().split(" ");
			for (int i = 0; i < words.length - n + 1; i++) {
				ngrams.add(form(words, i, i + n));
			}
			return ngrams;
		}
		
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
        
		public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output, Reporter reporter)
        throws IOException {
			String line = value.toString();
			for (int n = 1; n <= 5; n++) {
				for (String ngram : ngrams(line, n)) {
					word.set(ngram);
					output.collect(word, one);
				}
			}
		}
	}
    
	public static class Reduce extends MapReduceBase implements
    Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output, Reporter reporter)
        throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}
    
	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(ngram.class);
		conf.setJobName("ngram");
        
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
        
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