package com.rapid7.armor.write;

import com.rapid7.armor.meta.TableMetadata;
import com.rapid7.armor.schema.DataType;
import com.rapid7.armor.shard.ShardId;
import com.rapid7.armor.store.WriteStore;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableWrite implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(TableWrite.class);

  private final String tableName;
  private final String org;
  private final String entityColumnId;
  private final DataType entityColumnType;
  private final WriteStore store;
  private final Map<ShardId, ShardWriter> shards = new HashMap<>();

  public TableWrite(String org, String table, String entityColumnId, DataType dataType, WriteStore store) {
    this.store = store;
    this.org = org;
    this.tableName = table;
    this.entityColumnId = entityColumnId;
    this.entityColumnType = dataType;
  }

  public TableMetadata toTableMetadata() {
    return new TableMetadata(entityColumnId, entityColumnType.getCode());
  }

  public Collection<ShardWriter> getShardWriters() {
    return shards.values();
  }

  public String getTableName() {
    return this.tableName;
  }

  public String getOrg() {
    return this.org;
  }

  public String getEntityColumnId() {
    return entityColumnId;
  }

  public DataType getDataType() {
    return entityColumnType;
  }

  @Override
  public void close() throws IOException {
    for (ShardWriter sw : shards.values()) {
      try {
        sw.close();
      } catch (Exception e) {
        LOGGER.warn("Unable to close shard {}", sw.getShardId(), e);
      }
    }
  }

  public void close(int shard) {
    ShardId shardId = store.buildShardId(org, tableName, shard);
    ShardWriter sw = shards.get(shardId);
    if (sw != null)
      sw.close();
  }

  public ShardWriter getShard(int shard) {
    ShardId shardId = store.buildShardId(org, tableName, shard);
    return shards.get(shardId);
  }

  public void addShard(ShardWriter writer) {
    shards.put(writer.getShardId(), writer);
  }
}
