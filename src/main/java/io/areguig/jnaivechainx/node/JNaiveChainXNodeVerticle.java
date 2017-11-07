package io.areguig.jnaivechainx.node;

import java.util.Arrays;
import java.util.UUID;

import io.areguig.jnaivechainx.blockchain.Chain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.java.Log;
import lombok.val;

import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.EB_BLOCK_ADDR;
import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.EB_CHAIN_ADDR;
import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.EB_CHAIN_MSG;
import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.EB_HEADER_ID;
import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.HTTP_PORT;
import static io.areguig.jnaivechainx.JNaiveChainXNodeVerticleConstant.NODE_NAME;
import static java.util.Objects.requireNonNull;

@Log
public final class JNaiveChainXNodeVerticle extends AbstractVerticle {

  private Chain blockChain;
  private String name;
  private UUID id;
  private Integer port;
  private HttpServer httpServer;
  private EventBus eventBus;
  private DeliveryOptions ebOpt;

  //@PostConstruct
  private void init() {
    name = config().getString(NODE_NAME);
    port = config().getInteger(HTTP_PORT);
    id = UUID.randomUUID();
    blockChain = new Chain();
    requireNonNull(name, "the name is mandatory to create a new node");
    requireNonNull(port, name + " node : an http port must be specified to create a new node");
    httpServer = vertx.createHttpServer();
    eventBus = vertx.eventBus();

    ebOpt = new DeliveryOptions();
    ebOpt.addHeader("id", id.toString());

  }

  @Override
  public void start() {
    init();

    // Start the http server
    Future<HttpServer> httpServerFuture = Future.future();
    startHTTPServer(httpServerFuture);
    ebStartBlockConsumer();
    ebStartChainConsumer();
    // Start node data exchange on the event bus.

    // check if every thing started well
    CompositeFuture.all(Arrays.asList(httpServerFuture)).setHandler(asyncRes -> {
      if (asyncRes.succeeded()) {
        log("Success :" + asyncRes.result());
      } else {
        log("Failed :" + asyncRes.result());
      }
    });
  }

  private void startHTTPServer(Future<HttpServer> future) {

    Router router = Router.router(vertx);

    router.get("/block").handler(this::apiGetBlocks);

    router.route("/block").handler(BodyHandler.create());

    router.post("/block").handler(this::apiPostBlock);

    httpServer.requestHandler(router::accept).listen(port, future.completer());
  }

  private void apiGetBlocks(RoutingContext rc) {
    rc.response().end(Json.encodePrettily(blockChain.getChainOfBlocks()));
  }

  private void apiPostBlock(RoutingContext rc) {
    val data = rc.getBodyAsJson().getString("data");
    val block = blockChain.chainBlock(data).lastBlock();
    log("Added a new block to the chain " + block);
    ebBroadcast(block);
    log("sent the new block through the event bus to other nodes.");
    rc.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(block));
  }


  private void ebStartBlockConsumer() {
    eventBus.consumer(EB_BLOCK_ADDR, message -> {
      if (id.toString().equals(message.headers().get(EB_HEADER_ID))) {
        log("ignoring the received block because it was send by self");
      } else {
        val block = Json.decodeValue(message.body().toString(), Chain.Block.class);
        log("received a new block from another node" + block);
        if (blockChain.chainBlock(block)) {
          log("the block is valid and can be chained to the local chain.");
        } else {
          log("the block is not valid and can't be chained to the local chain. Will ask for the full chain.");
          ebReplaceChain(message.headers().get(EB_HEADER_ID));
        }
      }
    });
  }

  private void ebStartChainConsumer() {
    eventBus.consumer(id.toString(), message -> {
      if (EB_CHAIN_MSG.equals(message.body().toString())) {
        log("node with id " + message.headers().get(EB_HEADER_ID)+" asked for the whole chain.");
        message.reply(Json.encode(blockChain),ebOpt);
      }
    });
  }


  private void ebBroadcast(Chain.Block block) {
    eventBus.publish(EB_BLOCK_ADDR, Json.encode(block), ebOpt);
  }

  private void ebReplaceChain(String nodeAddress) {
    eventBus.send(nodeAddress, EB_CHAIN_MSG,ebOpt,reply -> {
      if (reply.succeeded()) {
        val newChain = Json.decodeValue(reply.result().body().toString(), Chain.class);
        log("new chain from node " + newChain);
        blockChain.replace(newChain);
      } else {
        log("No answer from the node with at address " + nodeAddress);
      }
    });
  }

  private void log(String message) {
    val node = "[" + name + ":" + port + ":" + id + "]";
    log.info(node + message);
  }
}
