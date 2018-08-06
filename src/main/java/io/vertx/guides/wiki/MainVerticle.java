package io.vertx.guides.wiki;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.guides.wiki.database.WikiDatabaseVerticle;
import io.vertx.guides.wiki.http.AuthInitializerVerticle;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Future<String> dbVerticleDeployment = Future.future();
    vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment);
    dbVerticleDeployment.compose(id -> {
      Future<String> authInitDeployment = Future.future();
      vertx.deployVerticle(new AuthInitializerVerticle(), authInitDeployment);
      return authInitDeployment;
    }).compose(id -> {
      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(
        "io.vertx.guides.wiki.http.HttpServerVerticle",
        new DeploymentOptions().setInstances(2),
        httpVerticleDeployment.completer());
      return httpVerticleDeployment;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }
}
