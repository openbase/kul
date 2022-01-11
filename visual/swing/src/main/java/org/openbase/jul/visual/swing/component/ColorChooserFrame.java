package org.openbase.jul.visual.swing.component;

/*-
 * #%L
 * JUL Visual Swing
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import java.awt.Color;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ColorChooserFrame extends javax.swing.JFrame {

    public enum UserFeedback {

        Ok, Cancel
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorChooserFrame.class);
    private final Object WAITER_LOCK = new Object();
    private UserFeedback feedback = UserFeedback.Cancel;

    /**
     * Creates new form ColorChooserFrame
     *
     * @throws org.openbase.jul.exception.InstantiationException
     */
    public ColorChooserFrame() throws org.openbase.jul.exception.InstantiationException {
        try {
            initComponents();
            okButton.setEnabled(false);
            cancelButton.setEnabled(false);
            setVisible(true);
            setAlwaysOnTop(true);
        } catch (Exception ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    public static Color getColor() throws CouldNotPerformException {
        Future<Color> colorFuture = GlobalCachedExecutorService.submit(new Callable<Color>() {
            @Override
            public Color call() throws CouldNotPerformException {
                ColorChooserFrame colorChooserFrame = null;
                try {
                    colorChooserFrame = new ColorChooserFrame();
                    synchronized (colorChooserFrame.WAITER_LOCK) {
                        try {
                            colorChooserFrame.okButton.setEnabled(true);
                            colorChooserFrame.cancelButton.setEnabled(true);
                            colorChooserFrame.WAITER_LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                    }

                    if (colorChooserFrame.feedback == UserFeedback.Cancel) {
                        throw new CouldNotPerformException("User cancel action!");
                    }

                    return colorChooserFrame.colorChooser.getColor();
                } finally {
                    if (colorChooserFrame != null) {
                        try {
                            colorChooserFrame.setVisible(false);
                        } catch (Throwable ex) {
                            ExceptionPrinter.printHistory("Coult not close " + ColorChooserFrame.class.getSimpleName() + "!", ex, LOGGER);
                        }
                    }
                }
            }
        });
        try {
            return colorFuture.get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new CouldNotPerformException("Could not get color!", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        colorChooser = new javax.swing.JColorChooser();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        feedback = UserFeedback.Ok;
        synchronized (WAITER_LOCK) {
            okButton.setEnabled(false);
            cancelButton.setEnabled(false);
            WAITER_LOCK.notifyAll();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        feedback = UserFeedback.Cancel;
        synchronized (WAITER_LOCK) {
            okButton.setEnabled(false);
            cancelButton.setEnabled(false);
            WAITER_LOCK.notifyAll();
        }
    }//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
