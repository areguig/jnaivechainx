package io.areguig.jnaivechainx.node;

import java.util.Arrays;

import io.areguig.jnaivechainx.blockchain.Chain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import lombok.val;

public class JNaiveChainXNodeVerticle extends AbstractVerticle{

  private Chain blockChain;

  @Override
  public void start() {

    blockChain = new Chain();

    // Start the http server
    Future<HttpServer> httpServerFuture = Future.future();
    startHTTPServer(httpServerFuture);

    //

    // check if every thing started well
    CompositeFuture.all(Arrays.asList(httpServerFuture)).setHandler(asyncRes ->{
      if(asyncRes.succeeded()){
        log("Success :"+asyncRes.result());
      }else {
        log("Failed :"+asyncRes.result());
      }
    });
  }

  private void startHTTPServer(Future<HttpServer> future){

    Router router = Router.router(vertx);

    router.get("/block").handler( rc -> rc.response().end(Json.encodePrettily(blockChain.getChainOfBlocks())));
   // router.put("/block").handler();
   // router.get("/products").handler(this::handleListProducts);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(config().getInteger("http.port"),future.completer());
  }

  private Handler<HttpServerRequest> getHttpServerRequestHandler() {
    return req -> req.response().end("Hello Vert.x!");
  }

  private void log (String message){
    val node = "["+config().getString("node.name")+"]:";
    System.out.println(node+message);
  }
}
