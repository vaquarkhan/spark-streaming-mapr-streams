package com.mapr.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import org.apache.spark.streaming.kafka.v09.KafkaUtils;
import scala.Tuple2;

import java.util.*;
import java.util.regex.Pattern;


public class SparkConsumer {
  private static final Pattern SPACE = Pattern.compile(" ");

  public static void main(String args[]) throws Exception {
/*
    Properties properties = new Properties();
    properties.put("serializer.class", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("key.serializer.class", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("value.serializer.class", "org.apache.kafka.common.serialization.StringDeserializer");
    properties.put("metadata.broker.list", "localhost:9092");
    properties.put("request.required.acks", "1");
*/
    String brokers = "maprdemo:9092";
    String topics = "/tmp/spark-test-stream:topic1";

    SparkConf sparkConf = new SparkConf().setAppName("MapRStreams");
    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(30));

    Set<String> topicsSet = new HashSet<String>(Arrays.asList(topics.split(",")));
    Map<String, String> kafkaParams = new HashMap<String, String>();
    //kafkaParams.put("zookeeper.connect", "localhost:2181");
    //kafkaParams.put("metadata.broker.list", brokers);
    kafkaParams.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    kafkaParams.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    // Create direct kafka stream with brokers and topics
    JavaPairInputDStream<String, String> directKafkaStream = KafkaUtils.createDirectStream(jssc, String.class, String.class, kafkaParams, topicsSet);
    // Get the lines, split them into words, count the words and print
    JavaDStream<String> lines = directKafkaStream.map(new Function<Tuple2<String, String>, String>() {
      @Override
      public String call(Tuple2<String, String> tuple2) {
        return tuple2._2();
      }
    });
    JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
      @Override
      public Iterable<String> call(String x) {
        return Arrays.asList(SPACE.split(x));
      }
    });
    JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
        new PairFunction<String, String, Integer>() {
          @Override
          public Tuple2<String, Integer> call(String s) {
            return new Tuple2<>(s, 1);
          }
        }).reduceByKey(
        new Function2<Integer, Integer, Integer>() {
          @Override
          public Integer call(Integer i1, Integer i2) {
            return i1 + i2;
          }
        });
    wordCounts.print();

    jssc.start();
    jssc.awaitTermination();
  }
}
