package com.example.storefinder.domain;

import java.util.List;

/**
 * Source of the store data.
 *
 * <p>The port is declared here, by the domain that needs it, and implemented in {@code data/}.
 * That keeps the dependency pointing inwards: the domain depends on nothing outside itself.
 *
 * <p>Deliberately a single method, so a test can supply a fake with a lambda rather than a
 * mocking framework.
 */
@FunctionalInterface
public interface StoreRepository {

    List<Store> findAll();
}
