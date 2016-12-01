/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;

/**
 * Chain of contexts, hold references to the contexts
 */
public interface ContextChain extends AutoCloseable {

    /**
     * Check if all context are referenced and not null
     * @return false if reference for some context is missing
     */
    boolean isReady();

    /**
     * Add context to the chain, if reference already exist ignore it
     * @param context child of OFPContext
     */
    <T extends OFPContext> void addContext(final T context);

    void addLifecycleService(final LifecycleService lifecycleService);

    /**
     * Stop the working contexts, but not release them
     * @return Future
     */
    Future<Void> stopChain();

    /**
     * Start the contexts, if some context is missing or cant be started returns failed future
     * @return Future
     */
    Future<Void> startChain();

    @Override
    void close();

    /**
     * Change connection if connection were drop and rebuild
     * @param connectionContext
     */
    void changePrimaryConnection(final ConnectionContext connectionContext);

    ListenableFuture<Void> connectionDropped() throws ExecutionException, InterruptedException;

    ContextChainState getContextChainState();

    ConnectionContext getPrimaryConnectionContext();

    void sleepTheChainAndDropConnection();

    void registerServices(@NonNull final ClusterSingletonServiceProvider clusterSingletonServiceProvider);

    void makeDeviceSlave();
}