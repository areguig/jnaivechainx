package io.areguig.jnaivechainx.blockchain;

import java.util.LinkedList;

import lombok.ToString;

@ToString
public class Chain {
  private LinkedList<Block> content;

  public Chain(){
    this.content = new LinkedList<>();
  }

  public Chain(Block genesis){
    super();
    this.chainBlock(genesis);
  }

  public Chain chainBlock(Block block){
    this.content.add(block);
    return this;
  }

  public LinkedList<Block> getChainOfBlocks(){
    return content;
  }
}
