package com.github.andunker.kafka.tutorial2;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

    String consumerKey = "wRSzyJR92v8d6Xc2PRDVXGIRp";
    String consumerSecret = "TVxMF8tukG74l5L8OqDCN6gwvXG1LThq0RMSIJFkGQMsHQARpP";
    String token = "1265067260881915910-l1pINMWKPrtsyf0oAugNSglaD4W1Vh";
    String secret = "8qbeV3Hqi7byCPVfhSJAatwnkbaoDBFYMr3ea95GIueUj";

    public TwitterProducer() {
    }

    public static void main(String[] args) {

        new TwitterProducer().run();

    }

    public void run() {

        logger.info("Setup");

        /**
         * Set up your blocking queues: Be sure to size these properly based on expected
         * TPS of your stream
         */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);
        // Attempts to establish a connection.
        client.connect();

        // create a kafka producer

        // loop to send tweets to kafka

        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if (msg != null) {
                logger.info(msg);
            }
        }
        logger.info("End of application");
    }



    public Client createTwitterClient(BlockingQueue<String> msgQueue) {

        /**
         * Declare the host you want to connect to, the endpoint, and authentication
         * (basic auth or oauth)
         */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        List<String> terms = Lists.newArrayList("bitcoin");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder().name("Hosebird-Client-01") // optional: mainly for the logs
                .hosts(hosebirdHosts).authentication(hosebirdAuth).endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue)); // optional: use this
                                                                    // if you want to
                                                                    // process client
                                                                    // events

        Client hosebirdClient = builder.build();
        return hosebirdClient;

    }

}