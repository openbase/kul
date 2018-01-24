/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.DefaultInitializable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * The synchronization future is used to guarantee that the change done by the internal
 * future has at one time been synchronized.
 *
 * @param <T> The return type of the internal future.
 * @author pleminoq
 */
public abstract class AbstractSynchronizationFuture<T, DATA_PROVIDER extends DataProvider<?>> implements Future<T>, DefaultInitializable {

    protected final Logger logger;

    private final SyncObject CHECK_LOCK = new SyncObject("WaitForUpdateLock");

    private final Observer notifyChangeObserver = (Observer) (Observable source, Object data) -> {
        synchronized (CHECK_LOCK) {
            CHECK_LOCK.notifyAll();
        }
    };

    private final Future<T> internalFuture;
    private Future synchronisationFuture;

    protected final DATA_PROVIDER dataProvider;

    /**
     * @param internalFuture
     * @param dataProvider
     */
    public AbstractSynchronizationFuture(final Future<T> internalFuture, final DATA_PROVIDER dataProvider) {
        this.internalFuture = internalFuture;
        this.dataProvider = dataProvider;
        this.logger = LoggerFactory.getLogger(dataProvider.getClass());
    }

    @Deprecated
    public void init() {
        // create a synchronisation task which makes sure that the change requested by
        // the internal future has at one time been synchronized to the remote
        synchronisationFuture = GlobalCachedExecutorService.submit(() -> {
            dataProvider.addDataObserver(notifyChangeObserver);
            try {
                dataProvider.waitForData();
                T result = internalFuture.get();
                waitForSynchronization(result);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not sync with internal future!", ex, logger);
            } finally {
                dataProvider.removeDataObserver(notifyChangeObserver);
            }
            return null;
        });
    }

    public void validateInitialization() throws InvalidStateException {
        if (synchronisationFuture == null) {
            throw new InvalidStateException(this + " not initialized!");
        }
    }

    private boolean checkInitialization() {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, ex), logger);
            return false;
        }
        return true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!checkInitialization()) {
            return false;
        }
        return synchronisationFuture.cancel(mayInterruptIfRunning) && internalFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        if (!checkInitialization()) {
            return true;
        }
        return synchronisationFuture.isCancelled() && internalFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        if (!checkInitialization()) {
            return true;
        }
        return synchronisationFuture.isDone() && internalFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown

        if (checkInitialization()) {
            synchronisationFuture.get();
        }

        return internalFuture.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown
        if (checkInitialization()) {
            synchronisationFuture.get(timeout, unit);
        }

        return internalFuture.get(timeout, unit);
    }

    public Future<T> getInternalFuture() {
        return internalFuture;
    }

    private void waitForSynchronization(T message) throws CouldNotPerformException, InterruptedException {
        try {
            try {
                beforeWaitForSynchronization();
            } catch (final Exception ex) {
                if (ex instanceof InterruptedException) {
                    throw (InterruptedException) ex;
                }
                throw new CouldNotPerformException("Pre execution task failed!", ex);
            }

            synchronized (CHECK_LOCK) {
                while (!check(message)) {
                    CHECK_LOCK.wait();
                }
            }
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not wait for synchronization!", ex);
        }
    }

    /**
     * Called to add an observer to the component whose synchronization is waited for
     * by this future. This is done in this way because sometimes the change is notified
     * through a normal observer and sometime by a data observer so the internal call
     * changes.
     *
     * @param observer In this case always the notify change observer that is added.
     */
    @Deprecated
    protected void addObserver(Observer observer) {
        // are never called because of deprecation!
    }

    /**
     * Remove the notify change observer from the component whose synchronization is
     * waited for after the synchronization is complete or failed.
     *
     * @param observer In this case always the notify change observer that is added.
     */
    @Deprecated
    protected void removeObserver(Observer observer) {
        // are never called because of deprecation!
    }

    /**
     * Called before the synchronization task enters its loop. Can for example
     * be used to wait for initial data so that the check that is done afterwards
     * in the loop does not fail immediately.
     * <p>
     * Note: Method can be overwritten for custom pre synchronization actions.
     *
     * @throws CouldNotPerformException if something goes wrong
     */
    protected void beforeWaitForSynchronization() throws CouldNotPerformException {
        // Method can be overwritten for custom pre synchronization actions.
    }

    /**
     * Called inside of the synchronization loop to check if the synchronization is complete.
     *
     * @param message the return value of the internal future
     * @return true if the synchronization is complete and else false
     * @throws CouldNotPerformException if something goes wrong in the check
     */
    protected abstract boolean check(final T message) throws CouldNotPerformException;
}
