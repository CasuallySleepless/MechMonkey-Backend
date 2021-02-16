package org.casuallysleepless;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;

class Switch{
	String type;
	String name;
	double price;
	String thumbnail;
	String desc;
	String manufacturer;
	int key_travel;
	int actuaction_force;
};



public class MainVerticle extends AbstractVerticle {

	private Gson gson = new GsonBuilder()
			.create();

	final JsonObject loadedConfig = new JsonObject();

    @Override
    public void start() {

    	doConfig()
				.compose(this::storeConfig)
                .compose(this::configRouter)
				.compose(this::startServer);

    	vertx.eventBus().consumer("product.vertx.addr", msg ->{
			Switch res = new Switch();
			res.type = "clicky";
			res.name = "Kailh Box Navy";
			res.price = 4.2;
			res.thumbnail = "url";
			res.desc = "text";
			res.manufacturer = "Kailh";
			res.key_travel = 10;
			res.actuaction_force = 70;
    		msg.reply(res);
		});
    }

    Future<Router> configRouter(Void unused){
		Router router = Router.router(vertx);
		router.route().handler(CorsHandler.create("*"));
		router.route().handler(LoggerHandler.create());

		router.get("/api/v1/product/*").handler(this::productHandler);

		return Future.succeededFuture(router);
	}

	void productHandler(RoutingContext ctx){
    	vertx.eventBus().request("product.vertx.addr", "", reply->{
			ctx.response()
					.putHeader("content-type", "application/json; charset=utf-8")
					.setStatusMessage("OK")
					.end(gson.toJson(reply.result().body()));
		});
	}

    Future<Void> startServer(Router router){
		JsonObject http = loadedConfig.getJsonObject("http");
		int port = http.getInteger("port", 8080);
		vertx.createHttpServer().requestHandler(router).listen(port);

		return Future.succeededFuture();
	}

    Future<JsonObject> doConfig(){
		ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
				.setType("file")
				.setFormat("json")
				.setConfig(new JsonObject().put("path", "config.json"));

		ConfigStoreOptions cliConfig = new ConfigStoreOptions()
				.setType("json")
				.setConfig(config());

		ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
				.addStore(defaultConfig)
				.addStore(cliConfig);

		ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

		return Future.future(promise -> cfgRetriever.getConfig(promise));
	}

	Future<Void> storeConfig(JsonObject config) {
    	loadedConfig.mergeIn(config);
    	return Future.succeededFuture();
	}

}
