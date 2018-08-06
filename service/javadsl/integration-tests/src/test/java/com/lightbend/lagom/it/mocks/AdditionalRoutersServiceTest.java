/*
 * Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.lagom.it.mocks;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static org.junit.Assert.assertEquals;
import play.test.*;


import static play.test.Helpers.*;

public class AdditionalRoutersServiceTest {

    private static ServiceTest.TestServer server;

    private static ActorSystem system;
    private static ActorMaterializer materializer;

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
        server = startServer(defaultSetup()
            .withCluster(false).withCassandra(false)
            .configureBuilder(b -> b.bindings(new AdditionalRoutersServiceModule()))
        );
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (materializer != null) {
            materializer.shutdown();
            materializer = null;
        }
        if (system != null) {
            system.terminate();
            system = null;
        }
    }

    @Test
    public void shouldRespondOnAdditionalRouters() throws Exception {

        // call the ping router (instance + bind dsl prefix)
        {
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/ping/");
            Result result = route(server.app(), request);
            assertEquals(OK, result.status());
            assertEquals(result.body().consumeData(materializer).toCompletableFuture().get().utf8String(), "ping");
        }

        // call the pong router (prefixed instance)
        {
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/pong/");
            Result result = route(server.app(), request);
            assertEquals(OK, result.status());
            assertEquals(result.body().consumeData(materializer).toCompletableFuture().get().utf8String(), "pong");
        }

        // call the echo router (router instantiated using Injector + hard-coded prefix)
        {
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/hello-prefixed/");
            Result result = route(server.app(), request);
            assertEquals(OK, result.status());
            assertEquals(result.body().consumeData(materializer).toCompletableFuture().get().utf8String(), "[prefixed] Hello");
        }

        // call the echo router (router instantiated using Injector + bind dsl prefix)
        {
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/hello/");
            Result result = route(server.app(), request);
            assertEquals(OK, result.status());
            assertEquals(result.body().consumeData(materializer).toCompletableFuture().get().utf8String(), "Hello");
        }

    }



}
