/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBInformerInterface;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import rsb.Event;
import rsb.Scope;

/**
 *
 * @author mpohling
 * @param <M>
 */
public class NotInitializedRSBInformer<M extends GeneratedMessage> extends NotInitializedRSBParticipant implements RSBInformerInterface<M> {

    public NotInitializedRSBInformer() {
    }

    public NotInitializedRSBInformer(Scope scope) {
        super(scope);
    }
    
    @Override
    public Event send(Event event) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could send event!", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public Event send(M data) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could send data!", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public Class<?> getTypeInfo() throws NotAvailableException {
        throw new NotAvailableException("type info", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public void setTypeInfo(Class<M> typeInfo) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not set type info!", new InvalidStateException("Informer not initialized!"));
    }
}
