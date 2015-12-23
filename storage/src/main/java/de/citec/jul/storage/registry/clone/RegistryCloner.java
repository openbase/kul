package de.citec.jul.storage.registry.clone;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 */
public interface RegistryCloner<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>> {

    public MAP deepCloneRegistryMap(final MAP map) throws CouldNotPerformException;

    public Map<KEY, ENTRY> deepCloneMap(final Map<KEY, ENTRY> map) throws CouldNotPerformException;

    public ENTRY deepCloneEntry(final ENTRY entry) throws CouldNotPerformException;
}
