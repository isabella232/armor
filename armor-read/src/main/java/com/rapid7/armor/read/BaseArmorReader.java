package com.rapid7.armor.read;

import java.io.IOException;

import com.rapid7.armor.meta.ColumnMetadata;
import com.rapid7.armor.read.slow.SlowArmorShardColumn;
import com.rapid7.armor.shard.ShardId;
import com.rapid7.armor.store.ReadStore;

public class BaseArmorReader {
  protected ReadStore store;

  public BaseArmorReader(ReadStore store) {
    this.store = store;
  }

  public ColumnMetadata getColumnMetadata(String tenant, String table, String columnId, int shardNum) throws IOException {
    ShardId shardId = store.findShardId(tenant, table, shardNum);
    if (shardId == null)
      return null;
    SlowArmorShardColumn armorShard = store.getSlowArmorShard(shardId, columnId);
    if (armorShard == null)
      return null;
    return armorShard.getMetadata();
  }
}
