/*
 * Created on 
 * Author is 
 *
 */
package pipe.handlers;


import pipe.controllers.PetriNetController;
import pipe.gui.model.PipeApplicationModel;
import pipe.views.viewComponents.AnnotationView;
import uk.ac.imperial.pipe.models.petrinet.Annotation;

import java.awt.Container;

public class NoteHandler
        extends PetriNetObjectHandler<Annotation>
{
   
   
   NoteHandler(AnnotationView view, Container contentpane, Annotation note, PetriNetController controller,  PipeApplicationModel applicationModel) {
      super(contentpane, note, controller, applicationModel);
      enablePopup = true;
   }

}
