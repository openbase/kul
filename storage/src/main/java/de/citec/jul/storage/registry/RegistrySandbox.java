/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.printer.LogLevel;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.registry.clone.RITSCloner;
import de.citec.jul.storage.registry.clone.RegistryCloner;
import de.citec.jul.storage.registry.plugin.RegistryPlugin;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 * @param <P>
 */
public class RegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>, P extends RegistryPlugin<KEY, ENTRY>> extends AbstractRegistry<KEY, ENTRY, MAP, R, P> implements RegistrySandboxInterface<KEY, ENTRY, MAP, R> {

    private RegistryCloner<KEY, ENTRY, MAP> cloner;

    public RegistrySandbox(final MAP entryMap) throws CouldNotPerformException {
        this(entryMap, new RITSCloner<>());
    }

    public RegistrySandbox(final MAP entryMap, final RegistryCloner<KEY, ENTRY, MAP> cloner) throws CouldNotPerformException {
        super(cloner.deepCloneRegistryMap(entryMap));
        this.cloner = cloner;
    }

    @Override
    public void replaceInternalMap(Map<KEY, ENTRY> map) throws CouldNotPerformException {
        super.replaceInternalMap(cloner.deepCloneMap(map));
    }

    @Override
    public ENTRY superRemove(ENTRY entry) throws CouldNotPerformException {
        return super.superRemove(cloner.deepCloneEntry(entry));
    }

    @Override
    public ENTRY update(ENTRY entry) throws CouldNotPerformException {
        return super.update(cloner.deepCloneEntry(entry));
    }

    @Override
    public ENTRY register(ENTRY entry) throws CouldNotPerformException {
        return super.register(cloner.deepCloneEntry(entry));
    }

    @Override
    public void sync(MAP map) throws CouldNotPerformException {
        try {
            entryMap.clear();
            for (Map.Entry<KEY, ENTRY> entry : map.entrySet()) {
                ENTRY deepClone = cloner.deepCloneEntry(entry.getValue());
                entryMap.put(deepClone.getId(), deepClone);
            }
            consistent = true;
        } catch (Exception ex) {
            throw new CouldNotPerformException("FATAL: Sandbox sync failed!", ex);
        }
    }

    @Override
    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Given transaction is invalid because sandbox consistency check failed!", ex), logger, LogLevel.ERROR);
        }
    }
}
