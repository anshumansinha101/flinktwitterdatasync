package com.celtic.flink;

/* java imports */
import java.util.Properties;
/* flink imports */
import org.apache.flink.util.Collector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.FlatMapFunction;
/* parser imports */
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
/* flink streaming twittter imports */
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class TwitterData
{
    public static void main(String[] args) throws Exception
    {
        final StreamExecutionEnvironment env =  StreamExecutionEnvironment.getExecutionEnvironment();

        Properties twitterCredentials = new Properties();
        twitterCredentials.setProperty(TwitterSource.CONSUMER_KEY, "XXXXXXX");
        twitterCredentials.setProperty(TwitterSource.CONSUMER_SECRET, "XXXXXXX");
        twitterCredentials.setProperty(TwitterSource.TOKEN, "XXXXXXXX-XXXXXXXXXX");
        twitterCredentials.setProperty(TwitterSource.TOKEN_SECRET, "XXXX");

        DataStream<String> twitterData = env.addSource(new TwitterSource(twitterCredentials));

        twitterData.flatMap(new TweetParser()).writeAsText("/home/anshuman/tweet");

        env.execute("Twitter Example");
    }

    public static class TweetParser	implements FlatMapFunction<String, Tuple2<String, Integer>>
    {

        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception
        {
            ObjectMapper jsonParser = new ObjectMapper();
            JsonNode node = jsonParser.readValue(value, JsonNode.class);

            boolean isEnglish =
                    node.has("user") &&
                            node.get("user").has("lang") &&
                            node.get("user").get("lang").asText().equals("en");

            boolean hasText = node.has("text");

            //if (isEnglish && hasText)
            if (hasText)
            {
                String tweet = node.get("text").asText();

                out.collect(new Tuple2<String, Integer>(tweet, 1));
            }
        }    }}