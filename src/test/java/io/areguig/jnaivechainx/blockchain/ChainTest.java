package io.areguig.jnaivechainx.blockchain;

import org.junit.Test;

import lombok.val;

import static org.junit.Assert.*;

public class ChainTest {

    @Test
    public void return_a_list_with_a_genesis_block_after_creation() throws Exception{
        val chain = new Chain();
        assertFalse(chain.getChainOfBlocks().isEmpty());
    }

    @Test
    public void the_chained_block_is_the_last_in_the_chain() throws Exception{
        val chain = new Chain();
        val data = "test data for block" ;
        val last = chain.chainBlock(data).lastBlock();
        assertEquals(data,last.getData());
    }

    @Test
    public void chainBlock_returns_true_if_the_block_was_chained_false_otherwise() throws Exception{
        val chain = new Chain();
        val last = chain.lastBlock();
        val timestamp = System.currentTimeMillis()/1000;
        val data = "test data for block" ;
        val hash = Chain.computeHash(last.getIndex()+1,last.getPreviousHash(),timestamp,data);
        Chain.Block newBlock = new Chain.Block(last.getIndex()+1,
                last.getHash(),timestamp,data,hash);
        val sizeBeforeAnyChain = chain.getChainOfBlocks().size();
        assertTrue(chain.chainBlock(newBlock));
        assertEquals(sizeBeforeAnyChain+1,chain.getChainOfBlocks().size());
        assertEquals(chain.lastBlock(),newBlock);

        val sizeAfterFirstChain = chain.getChainOfBlocks().size();
        assertFalse(chain.chainBlock(newBlock));
        assertEquals(sizeAfterFirstChain,chain.getChainOfBlocks().size());
    }

    @Test
    public void block_validation_returns_false_if_index_is_bad() throws Exception{
        val chain = new Chain();
        val last = chain.lastBlock();
        val timestamp = System.currentTimeMillis()/1000;
        val data = "test data for block" ;
        val hash = Chain.computeHash(last.getIndex()+1,last.getPreviousHash(),timestamp,data);
        Chain.Block newBlock = new Chain.Block(last.getIndex(),
                last.getHash(),timestamp,data,hash);
        val sizeBeforeAnyChain = chain.getChainOfBlocks().size();
        assertFalse(chain.chainBlock(newBlock));
        assertNotEquals(sizeBeforeAnyChain+1,chain.getChainOfBlocks().size());
        assertNotEquals(chain.lastBlock(),newBlock);
    }


    @Test
    public void block_validation_returns_false_if_previous_hash_is_bad() throws Exception{
        val chain = new Chain();
        val last = chain.lastBlock();
        val timestamp = System.currentTimeMillis()/1000;
        val data = "test data for block" ;
        val hash = Chain.computeHash(last.getIndex()+1,last.getPreviousHash(),timestamp,data);
        Chain.Block newBlock = new Chain.Block(last.getIndex()+1,"blahhda34567890",timestamp
                ,data,hash);
        val sizeBeforeAnyChain = chain.getChainOfBlocks().size();
        assertFalse(chain.chainBlock(newBlock));
        assertNotEquals(sizeBeforeAnyChain+1,chain.getChainOfBlocks().size());
        assertNotEquals(chain.lastBlock(),newBlock);
    }

    @Test
    public void block_validation_returns_false_if_new_block_hash_is_bad() throws Exception{
        val chain = new Chain();
        val last = chain.lastBlock();
        val timestamp = System.currentTimeMillis()/1000;
        val data = "test data for block" ;
        val hash = "I AM A BAD HASH";
        Chain.Block newBlock = new Chain.Block(last.getIndex()+1,"blahhda34567890",timestamp
                ,data,hash);
        val sizeBeforeAnyChain = chain.getChainOfBlocks().size();
        assertFalse(chain.chainBlock(newBlock));
        assertNotEquals(sizeBeforeAnyChain+1,chain.getChainOfBlocks().size());
        assertNotEquals(chain.lastBlock(),newBlock);
    }
}