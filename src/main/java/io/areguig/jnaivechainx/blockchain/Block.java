package io.areguig.jnaivechainx.blockchain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Block {
  private final Integer index;
  private final String previousHash;
  private final Long timestamp;
  private final String data;
  private final String hash;
}
