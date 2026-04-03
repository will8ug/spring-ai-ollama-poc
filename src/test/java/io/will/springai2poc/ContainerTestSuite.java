package io.will.springai2poc;

import org.junit.jupiter.api.AfterAll;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Suite
@SelectPackages({"io.will.springai2poc.controller"})
@IncludeClassNamePatterns({"^.*IT$"})
public class ContainerTestSuite {
    private static final ComposeContainer composeContainer =
            new ComposeContainer(new File("infra/docker-compose.yml"));

    @BeforeSuite
    static void beforeSuite() {
        composeContainer
                .waitingFor("milvus-standalone", Wait.forHealthcheck())
                .waitingFor("minio", Wait.forHealthcheck())
                .waitingFor("etcd", Wait.forHealthcheck())
                .start();
    }

    @AfterAll
    static void afterAll() {
        composeContainer.stop();
    }
}
