/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.scope;

import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.exception.NotAvailableException;
import rsb.Scope;
import rst.rsb.ScopeType;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class ScopeTransformer {

    public static rsb.Scope transform(final ScopeType.Scope scope) throws CouldNotTransformException {
        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            if (scope.getComponentList().isEmpty()) {
                throw new NotAvailableException("element");
            }
            String stringRep = Scope.COMPONENT_SEPARATOR;
            for(String component : scope.getComponentList()) {
                stringRep += component;
                stringRep += Scope.COMPONENT_SEPARATOR;
            }
            return new Scope(stringRep);
        } catch (Exception ex) {
            throw new CouldNotTransformException(scope, rsb.Scope.class, ex);
        }
    }

    public static ScopeType.Scope transform(final rsb.Scope scope) throws CouldNotTransformException {
        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }
            if (scope.getComponents().isEmpty()) {
                throw new NotAvailableException("components");
            }
            return ScopeType.Scope.newBuilder().addAllComponent(scope.getComponents()).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException(scope, rsb.Scope.class, ex);
        }
    }
}
