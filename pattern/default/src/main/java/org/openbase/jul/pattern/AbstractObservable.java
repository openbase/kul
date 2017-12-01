package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <T> the data type on whose changes is notified
 */
public abstract class AbstractObservable<T> implements Observable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractObservable.class);

    private static final boolean DEFAULT_UNCHANGED_VALUE_FILTER = true;
    private static final Object DEFAULT_SOURCE = null;

    protected final boolean unchangedValueFilter;
    private boolean notificationInProgess;

    protected final Object NOTIFICATION_LOCK = new Object() {
        @Override
        public String toString() {
            return "ObservableNotificationLock";
        }
    };

    private final Object OBSERVER_LOCK = new Object() {
        @Override
        public String toString() {
            return "ObserverLock";
        }
    };

    private final Object NOTIFICATION_METHOD_LOCK = new Object() {
        @Override
        public String toString() {
            return "notifyObserverMethodLock";
        }
    };

    protected final List<Observer<T>> observers;
    protected int latestValueHash;
    private Object source;
    private ExecutorService executorService;
    private HashGenerator<T> hashGenerator;

    /**
     * Construct new Observable.
     */
    public AbstractObservable() {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, DEFAULT_SOURCE);
    }

    /**
     * Construct new Observable.
     *
     * @param source the responsible source of the value notifications.
     */
    public AbstractObservable(final Object source) {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, source);
    }

    /**
     * Construct new Observable
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is
     * the same than notified before.
     */
    public AbstractObservable(final boolean unchangedValueFilter) {
        this(unchangedValueFilter, DEFAULT_SOURCE);
    }

    /**
     * Construct new Observable.
     *
     * If the source is not defined the observable itself will be used as notification source.
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is
     * the same than notified before.
     * @param source the responsible source of the value notifications.
     */
    public AbstractObservable(final boolean unchangedValueFilter, final Object source) {
        this.observers = new ArrayList<>();
        this.unchangedValueFilter = unchangedValueFilter;
        this.notificationInProgess = false;
        this.source = source == DEFAULT_SOURCE ? this : source; // use observer itself if source was not explicit defined.
        this.hashGenerator = new HashGenerator<T>() {

            @Override
            public int computeHash(T value) throws CouldNotPerformException {
                try {
                    return value.hashCode();
                } catch (ConcurrentModificationException ex) {
                    throw new FatalImplementationErrorException("Observable has changed during hash computation in notification! Set a HashGenerator for the observable to control the hash computation yourself!", this, ex);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @param observer
     */
    @Override
    public void addObserver(Observer<T> observer) {
        synchronized (OBSERVER_LOCK) {
            if (observers.contains(observer)) {
                LOGGER.warn("Skip observer registration. Observer[" + observer + "] is already registered!");
                return;
            }
            observers.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param observer
     */
    @Override
    public void removeObserver(Observer<T> observer) {
        synchronized (OBSERVER_LOCK) {
            observers.remove(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        synchronized (OBSERVER_LOCK) {
            observers.clear();
        }
    }

    /**
     * Notify all changes of the observable to all observers only if the observable has changed. The
     * source of the notification is set as this. Because of data encapsulation reasons this method
     * is not included within the Observer interface.
     * Attention! This method is not thread safe against changes of the observable because the check if the observable has changed is
     * done by computing its hash value. Therefore if the observable is a collection and it is changed
     * while notifying a concurrent modification exception can occur. To avoid this compute the
     * observable hash yourself by setting a hash generator.
     * If this method is interrupted a rollback is done by reseting the latestHashValue. Thus the observable
     * has not changed and false is returned.
     *
     * @param observable the value which is notified
     * @return true if the observable has changed
     * @throws MultiException thrown if the notification to at least one observer fails
     * @throws CouldNotPerformException thrown if the hash computation fails
     */
    public boolean notifyObservers(final T observable) throws MultiException, CouldNotPerformException {
        return notifyObservers(this, observable);
    }

    /**
     * Notify all changes of the observable to all observers only if the observable has changed.
     * Because of data encapsulation reasons this method is not included within the Observer
     * interface.
     * Attention! This method is not thread safe against changes of the observable because the check if the observable has changed is
     * done by computing its hash value. Therefore if the observable is a collection and it is changed
     * while notifying a concurrent modification exception can occur. To avoid this compute the
     * observable hash yourself by setting a hash generator.
     * If this method is interrupted a rollback is done by reseting the latestHashValue. Thus the observable
     * has not changed and false is returned.
     *
     * Note: In case the given observable is null this notification will be ignored.
     *
     * @param source the source of the notification
     * @param observable the value which is notified
     * @return true if the observable has changed
     * @throws MultiException thrown if the notification to at least one observer fails
     * @throws CouldNotPerformException thrown if the hash computation fails
     */
    public boolean notifyObservers(final Observable<T> source, final T observable) throws MultiException, CouldNotPerformException {
        synchronized (NOTIFICATION_METHOD_LOCK) {
            if (observable == null) {
                LOGGER.debug("Skip notification because observable is null!");
                return false;
            }

            ExceptionStack exceptionStack = null;
            final Map<Observer<T>, Future<Void>> notificationFutureList = new HashMap<>();

            final ArrayList<Observer<T>> tempObserverList;

            try {
                notificationInProgess = true;
                final int observableHash = hashGenerator.computeHash(observable);
                if (unchangedValueFilter && isValueAvailable() && observableHash == latestValueHash) {
                    LOGGER.debug("Skip notification because " + this + " has not been changed!");
                    return false;
                }

                applyValueUpdate(observable);
                final int lastHashValue = latestValueHash;
                latestValueHash = observableHash;

                synchronized (OBSERVER_LOCK) {
                    tempObserverList = new ArrayList<>(observers);
                }

                for (final Observer<T> observer : tempObserverList) {

                    if (executorService == null) {

                        if (Thread.currentThread().isInterrupted()) {
                            latestValueHash = lastHashValue;
                            return false;
                        }

                        // synchronous notification
                        try {
                            observer.update(source, observable);
                        } catch (InterruptedException ex) {
                            latestValueHash = lastHashValue;
                            Thread.currentThread().interrupt();
                            return false;
                        } catch (Exception ex) {
                            exceptionStack = MultiException.push(observer, ex, exceptionStack);
                        }
                    } else {
                        // asynchronous notification
                        notificationFutureList.put(observer, executorService.submit(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                observer.update(source, observable);
                                return null;
                            }
                        }));
                    }
                }
            } finally {
                assert observable != null;
                notificationInProgess = false;
                synchronized (NOTIFICATION_LOCK) {
                    NOTIFICATION_LOCK.notifyAll();
                }
            }

            //TODO: this check is wrong -> != but when implemented correctly leads bco not starting
            // handle exeception printing for async variant
            if (executorService == null) {
                for (final Entry<Observer<T>, Future<Void>> notificationFuture : notificationFutureList.entrySet()) {
                    try {
                        notificationFuture.getValue().get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return true;
                    } catch (Exception ex) {
                        exceptionStack = MultiException.push(notificationFuture.getKey(), ex, exceptionStack);
                    }

                }
            }
            MultiException.checkAndThrow("Could not notify Data[" + observable + "] to all observer!", exceptionStack);
            return true;
        }
    }

    /**
     * Method is called if a observer notification delivers a new value.
     *
     * @param value the new value
     *
     * Note: Overwrite this method for getting informed about value changes.
     */
    protected void applyValueUpdate(final T value) {
        // overwrite for current state holding obervable implementations.
    }

    /**
     * Set an executor service for the observable. If it is set the notification
     * will be parallelized using this service.
     *
     * @param executorService the executor service which will be used for parallelization
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setHashGenerator(HashGenerator<T> hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    /**
     * Method checks if a notification is currently in progess.
     *
     * @return notificationInProgess returns true if a notification is currently in progess.
     */
    public boolean isNotificationInProgess() {
        return notificationInProgess;
    }

    @Override
    public String toString() {
        return Observable.class.getSimpleName() + "[" + (source == this ? source.getClass().getSimpleName() : source) + "]";
    }
}
