@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.jpa {
    requires transitive jakarta.persistence;
    requires transitive jakarta.inject;
    requires transitive java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;
    requires transitive ninja.javahacker.annotimpler.convert;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive ninja.javahacker.annotimpler.sql;
    requires transitive ninja.javahacker.annotimpler.jpa;
    requires ninja.javahacker.datetime;
    requires ninja.javahacker.typeser;
}