@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.jpa {
    requires jakarta.persistence;
    requires jakarta.inject;
    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires static com.github.spotbugs.annotations;
    requires static lombok;

    requires ninja.javahacker.annotimpler.convert;
    requires ninja.javahacker.annotimpler.core;
    requires ninja.javahacker.annotimpler.sql;
    requires ninja.javahacker.annotimpler.jpa;
    requires ninja.javahacker.datetime;
    requires ninja.javahacker.magicfactory;
    requires ninja.javahacker.typeser;
}