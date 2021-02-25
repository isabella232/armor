package com.rapid7.armor;

import com.rapid7.armor.interval.Interval;
import com.rapid7.armor.store.InstantPredicate;
import com.rapid7.armor.store.Operator;
import com.rapid7.armor.store.S3ReadStore;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.findify.s3mock.S3Mock;

public class S3ReadStoreTest {
  private static final String TEST_BUCKET = "testbucket";
  private static final S3Mock S3_MOCK = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
  private static AmazonS3 client;

  @BeforeAll
  public static void setupBucket() throws ParseException {
    S3_MOCK.start();
    EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2");
    client = AmazonS3ClientBuilder
        .standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(endpoint)
        .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
        .build();
    client.createBucket(TEST_BUCKET);
  }

  @AfterAll
  public static void shutdown() {
    S3_MOCK.shutdown();
  }

  @Test
  public void readStoreTests() throws SdkClientException, JsonProcessingException {
    // First lets setup potential shards and directories in s3, directly write to s3
    String current1 = UUID.randomUUID().toString();
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, String> currentValue1 = new HashMap<>();
    currentValue1.put("current", current1);

    String current2 = UUID.randomUUID().toString();
    HashMap<String, String> currentValue2 = new HashMap<>();
    currentValue2.put("current", current2);

    client.putObject(TEST_BUCKET, "org1/table1/table-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/all/1970-01-01T00:00:00Z/0/" + Constants.CURRENT, mapper.writeValueAsString(currentValue1));


    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org1/table1/single/1970-01-01T00:00:00Z/1/" + Constants.CURRENT, mapper.writeValueAsString(currentValue2));

    client.putObject(TEST_BUCKET, "org2/table1/table-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/0/" + current1 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/0/" + Constants.CURRENT, mapper.writeValueAsString(currentValue1));

    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/1/" + current2 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/single/1970-01-01T00:00:00Z/1/" + Constants.CURRENT, mapper.writeValueAsString(currentValue2));
    
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-01T00:00:00Z/1/" + current2 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-01T00:00:00Z/1/" + current2 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-01T00:00:00Z/1/" + current2 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-01T00:00:00Z/1/" + Constants.CURRENT, mapper.writeValueAsString(currentValue2));
    
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-08T00:00:00Z/1/" + current2 + "/name_S", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-08T00:00:00Z/1/" + current2 + "/level_I", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-08T00:00:00Z/1/" + current2 + "/shard-metadata.armor", " Empty content");
    client.putObject(TEST_BUCKET, "org2/table1/weekly/2021-01-08T00:00:00Z/1/" + Constants.CURRENT, mapper.writeValueAsString(currentValue2));

    S3ReadStore readStore = new S3ReadStore(client, TEST_BUCKET);
    assertEquals(Sets.newHashSet("org1", "org2"), Sets.newHashSet(readStore.getTenants()));
    assertEquals(Arrays.asList("table1"), readStore.getTables("org2"));
    assertEquals(Arrays.asList("table1"), readStore.getTables("org1"));
    
    List<String> intervalStarts = readStore.getIntervalStarts("org2", "table1", Interval.WEEKLY);
    assertEquals(2, intervalStarts.size());
    assertTrue(intervalStarts.contains("2021-01-01T00:00:00Z"));
    assertTrue(intervalStarts.contains("2021-01-08T00:00:00Z"));
    
    Instant instant01012021 = Instant.parse("2021-01-01T00:00:00Z");
    Instant instant01082021 = Instant.parse("2021-01-08T00:00:00Z");
    Instant instant02082021 = Instant.parse("2021-02-08T00:00:00Z");

    InstantPredicate predicate = new InstantPredicate("__startInterval", Operator.EQUALS, Arrays.asList(instant01012021));
    intervalStarts = readStore.getIntervalStarts("org2", "table1", Interval.WEEKLY, predicate);
    assertEquals(1, intervalStarts.size());
    assertTrue(intervalStarts.contains("2021-01-01T00:00:00Z"));
    
    predicate = new InstantPredicate("__startInterval", Operator.EQUALS, Arrays.asList(instant02082021));
    intervalStarts = readStore.getIntervalStarts("org2", "table1", Interval.WEEKLY, predicate);
    assertEquals(0, intervalStarts.size());
    
    predicate = new InstantPredicate("__startInterval", Operator.LESS_THAN, Arrays.asList(instant02082021));
    intervalStarts = readStore.getIntervalStarts("org2", "table1", Interval.WEEKLY, predicate);
    assertEquals(2, intervalStarts.size());
    assertTrue(intervalStarts.contains("2021-01-01T00:00:00Z"));
    assertTrue(intervalStarts.contains("2021-01-08T00:00:00Z"));
    
    predicate = new InstantPredicate("__startInterval", Operator.GREATER_THAN, Arrays.asList(instant01012021));
    intervalStarts = readStore.getIntervalStarts("org2", "table1", Interval.WEEKLY, predicate);
    assertEquals(1, intervalStarts.size());
    assertTrue(intervalStarts.contains("2021-01-08T00:00:00Z"));
  }
}
