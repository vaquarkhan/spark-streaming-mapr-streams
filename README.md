# spark-streaming-mapr-streams
Example of a Spark Streaming Application consuming from MapR Streams via the Kafka APIs.

# Overview
This is a trivial example of how to consume from MapR Streams in a Spark Streaming application.

# Prerequisites

On a MapR 5.1.0 sandbox I updated Spark to 1.6.1 and ensured the Kafka libraries and client programs are installed:

```
# yum -y install mapr-spark mapr-kafka
```

Then I installed Apache Maven from CentOS software collections. Also install git and fortune:

```
# yum -y install centos-release-scl
# yum -y install maven30
# yum -y install git fortune-mod
```

Now become the `mapr` user:

```
# su - mapr
```

Now clone this repo:

```
$ git clone https://github.com/vicenteg/spark-streaming-mapr-streams.git
```

Build it:

```
$ cd spark-streaming-mapr-streams/
$ source /opt/rh/maven30/enable
$ mvn clean package
```

Create a stream to test with. If you decide to use a different name, be sure to use the same name later, when we launch the Spark Streaming job.

```
$ maprcli stream create -path /tmp/spark-test-stream
```

Let's use `fortune` to generate some text to run through Streams. `fortune` produces output like this:

```
# fortune
Only great masters of style can succeed in being obtuse.
		-- Oscar Wilde

Most UNIX programmers are great masters of style.
		-- The Unnamed Usenetter
```

Were truer words ever spoken? I think not. Anyway...

Start a shell loop, piping `fortune` into the `kafka-console-producer` at a random interval between 0 and 5 seconds. This will create the new topic automatically:

```
$ while :; do
  fortune | /opt/mapr/kafka/kafka-0.9.0/bin/kafka-console-producer.sh --broker-list 1:1 --topic /tmp/spark-test-stream:topic1
  sleep $(($RANDOM % 5))
done
```

You should not see any output.

Now launch the streaming job:

```
$ /opt/mapr/spark/spark-1.6.1/bin/spark-submit \
    --master yarn-client \
    --class com.mapr.example.SparkConsumer \
        target/SparkConsumer-1.0-SNAPSHOT.jar 1:1 /tmp/spark-test-stream:topic1
```

# Reference

About MapR Streams - https://www.mapr.com/products/mapr-streams

Integrating Streams with Spark - http://maprdocs.mapr.com/51/#Spark/Spark_IntegrateMapRStreams_Consume.html

JavaDirectKafkaWordCount example - https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/streaming/JavaDirectKafkaWordCount.java
