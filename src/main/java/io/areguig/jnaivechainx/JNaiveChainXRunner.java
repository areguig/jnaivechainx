package io.areguig.jnaivechainx;

import java.util.Arrays;

import io.areguig.jnaivechainx.node.JNaiveChainXNodeVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.val;

public class JNaiveChainXRunner {

  public static void main(String [] args) {
    val vertx = Vertx.vertx();

    val hercule = new JsonObject().put("http.port", 8001).put("node.name", "Hercule");

    val ulysse = new JsonObject().put("http.port", 8002).put("node.name", "Ulysse");

    val eros = new JsonObject().put("http.port", 8003).put("node.name", "Eros");

    Arrays.asList(hercule,ulysse,eros).forEach(v -> vertx.deployVerticle(JNaiveChainXNodeVerticle.class.getName(), new DeploymentOptions().setConfig(v), res -> {
      if (res.succeeded()) {
        System.out.println("Deployment id is: " + res.result());
      } else {
        System.out.println("Deployment failed!"+ res.cause());
        res.cause().printStackTrace();
      }
    }));


  }
}
