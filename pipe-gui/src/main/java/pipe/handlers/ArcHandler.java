package pipe.handlers;

import pipe.actions.petrinet.SplitArcAction;
import pipe.controllers.PetriNetController;
import pipe.gui.model.PipeApplicationModel;
import pipe.gui.widgets.ArcWeightEditorPanel;
import pipe.gui.widgets.EscapableDialog;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ArcType;
import uk.ac.imperial.pipe.models.petrinet.Connectable;

import javax.swing.*;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Class used to implement methods corresponding to mouse events on arcs.
 */
public class ArcHandler<S extends Connectable, T extends Connectable>
        extends PetriNetObjectHandler<Arc<S, T>> {


    public ArcHandler(Container contentpane, Arc<S, T> component, PetriNetController controller,  PipeApplicationModel applicationModel) {
        super(contentpane, component, controller, applicationModel);
        enablePopup = true;
    }

    /**
     * Creates the popup menu that the user will see when they right click on a
     * component
     */
    @Override
    public JPopupMenu getPopup(MouseEvent e) {
        int popupIndex = 0;
        JMenuItem menuItem;
        JPopupMenu popup = super.getPopup(e);


        menuItem = new JMenuItem(new SplitArcAction(petriNetController.getArcController(component),
                e.getPoint()));
        menuItem.setText("Split Arc Segment");
        popup.insert(menuItem, popupIndex++);

        popup.insert(new JPopupMenu.Separator(), popupIndex++);

        if (component.getType().equals(ArcType.NORMAL)) {
            menuItem = new JMenuItem("Edit Weight");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showEditor();
                }
            });
            popup.insert(menuItem, popupIndex++);

            popup.insert(new JPopupMenu.Separator(), popupIndex);
        }
        return popup;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        //       if (!ApplicationSettings.getApplicationModel().isEditionAllowed()){
        //         return;
        //      }
        //      if (e.getClickCount() == 2){
        //         ArcView arcView = (ArcView) component;
        //         if (e.isControlDown()){
        //             ApplicationSettings.getApplicationView().getCurrentTab().getHistoryManager().addNewEdit(
        //                    arcView.getArcPath().insertPointAt(
        //                            new Point2D.Float(arcView.getX() + e.getX(),
        //                            arcView.getY() + e.getY()), e.isAltDown()));
        //         } else {
        //            arcView.getSource().select();
        //            arcView.getTarget().select();
        //            justSelected = true;
        //         }
        //      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //       switch (ApplicationSettings.getApplicationModel().getMode()) {
        //         case Constants.SELECT:
        //            if (!isDragging){
        //               break;
        //            }
        //            ArcView currentObject = (ArcView) component;
        //            Point oldLocation = currentObject.getLocation();
        //            // Calculate translation in mouse
        //            int transX = Grid.getModifiedValue(e.getX() - dragInit.x);
        //            int transY = Grid.getModifiedY(e.getY() - dragInit.y);
        //            ((PetriNetTab)contentPane).getSelectionObject().translateSelection(
        //                     transX, transY);
        //            dragInit.translate(
        //                     -(currentObject.getLocation().x - oldLocation.x - transX),
        //                     -(currentObject.getLocation().y - oldLocation.y - transY));
        //      }
    }

    // Alex Charalambous: No longer does anything since you can't simply increment
    // the weight of the arc because multiple weights for multiple colours exist
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    public void showEditor() {
        // Build interface
        EscapableDialog guiDialog = new EscapableDialog(petriNetController.getPetriNetTab().getApplicationView(), "PIPE", true);

        ArcWeightEditorPanel arcWeightEditor = new ArcWeightEditorPanel(guiDialog.getRootPane(), petriNetController,
                petriNetController.getArcController(component));

        guiDialog.add(arcWeightEditor);

        guiDialog.getRootPane().setDefaultButton(null);

        guiDialog.setResizable(false);

        // Make window fit contents' preferred size
        guiDialog.pack();

        // Move window to the middle of the screen
        guiDialog.setLocationRelativeTo(null);

        guiDialog.setVisible(true);

        guiDialog.dispose();
    }
}
