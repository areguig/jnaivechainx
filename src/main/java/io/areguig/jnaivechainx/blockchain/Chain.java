package io.areguig.jnaivechainx.blockchain;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.val;

@ToString
@Log
public class Chain {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Block {
    private Integer index;
    private String previousHash;
    private Long timestamp;
    private String data;
    private String hash;
  }

  private static final String ALG = "MD5";

  private LinkedList<Block> chainOfBlocks;

  public Chain() {
    this.chainOfBlocks = new LinkedList<>();
    chainOfBlocks.add(genesis());
  }

  public Block lastBlock() {
    return chainOfBlocks.getLast();
  }

  public LinkedList<Block> getChainOfBlocks() {
    return new LinkedList<>(chainOfBlocks);
  }

  public Chain chainBlock(@NonNull String data) {
    val previousBlock = chainOfBlocks.getLast();
    Integer nextIndex = previousBlock.index + 1;
    Long nextTimestamp = System.currentTimeMillis() / 1000;
    String nextHash = computeHash(nextIndex, previousBlock.hash, nextTimestamp, data);
    chainBlock(new Block(nextIndex, previousBlock.hash, nextTimestamp, data, nextHash));
    return this;
  }

  public boolean chainBlock(@NonNull Block block) {
    if (validateNewBlock(lastBlock(), block)) {
      this.chainOfBlocks.add(block);
    } else {
      log.severe("unable to chain the block, the block is not valid " + block);
      return false;
    }
    return true;
  }

  public void replace(@NonNull Chain newChain) {
    if(newChain.chainOfBlocks.size()>= chainOfBlocks.size()){
      chainOfBlocks=newChain.chainOfBlocks;
    }
  }

  /**
   * Private stuff
   */

  private Block genesis() {
    return new Block(0,
            "0",
            System.currentTimeMillis() / 1000,
            "63N3212",
            UUID.randomUUID().toString());
  }

  /**
   * Static stuff
   */

  public static String computeHash(@NonNull Integer index, @NonNull String previousHash,
                                   @NonNull Long timestamp, @NonNull String data) {
    return new BigInteger(1, DigestUtils.md5(index + previousHash + timestamp + data)).toString(16);
  }

  public static boolean validateNewBlock(Block lastChainBlock, Block newBlock) {
    if (lastChainBlock.index + 1 != newBlock.index) {
      log.severe("index does not match the chain for block " + newBlock);
      return false;
    } else if (!lastChainBlock.hash.equals(newBlock.previousHash)) {
      log.severe("previous hash does not match the chain for block " + newBlock);
      return false;
    } else if (!computeHash(newBlock.index, newBlock.previousHash, newBlock.timestamp, newBlock.data).equals(newBlock.hash)) {
      log.severe("hash not valid for the block " + newBlock);
    }
    return true;
  }

}
