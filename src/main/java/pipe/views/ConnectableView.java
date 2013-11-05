package pipe.views;

import pipe.gui.*;
import pipe.models.Connectable;
import pipe.models.interfaces.IObserver;
import pipe.views.viewComponents.NameLabel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class ConnectableView<T extends Connectable> extends PetriNetViewComponent<T> implements Cloneable, IObserver, Serializable {
    private ConnectableView _lastCopy = null;
    private ConnectableView _original = null;
    private int _copyNumber = 0;

    boolean _attributesVisible = false;

    ConnectableView(T model) {
        this("", model);
    }

    private ConnectableView(String id, T model) {
        this(id, model.getName(), Constants.DEFAULT_OFFSET_X, Constants.DEFAULT_OFFSET_Y, model);
    }

    ConnectableView(String id, String name, double nameOffsetX, double nameOffsetY,
            T model) {
        super(id, name, nameOffsetX, nameOffsetY, model);
        setLocation((int) model.getX(),  (int) model.getY());
        PipeApplicationView view = ApplicationSettings.getApplicationView();
        if (view != null) {
            PetriNetTab tab = view.getCurrentTab();
            ZoomController zoomController = tab.getZoomController();
            addZoomController(zoomController);
        }
    }

    public void setName(String nameInput) {
        super.setNameLabelName(nameInput);
    }

    public void setId(String idInput) {
        _id = idInput;
    }

    public String getId() {
        return (_id != null) ? _id : _nameLabel.getName();
    }

    public String getName() {
        return (_nameLabel != null) ? super.getNameLabelName() : _id;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(getComponentDrawOffset(), getComponentDrawOffset());
        g2.transform(ZoomController.getTransform(_zoomPercentage));
    }

    public Point2D getIntersectOffset(Point2D start) {
        return new Point2D.Double();
    }

    public int centreOffsetTop() {
        return (int) (ZoomController.getZoomedValue(model.getHeight() / 2.0, _zoomPercentage));
    }

    public int centreOffsetLeft() {
        return (int) (ZoomController.getZoomedValue(model.getWidth() / 2.0, _zoomPercentage));
    }

    void updateBounds() {
        double scaleFactor = ZoomController.getScaleFactor(_zoomPercentage);
        double x = model.getX() * scaleFactor;
        double y = model.getY() * scaleFactor;
        _bounds.setBounds((int) x, (int) y, (int) (model.getHeight() * scaleFactor),
                (int) (model.getHeight() * scaleFactor));
        _bounds.grow(getComponentDrawOffset(), getComponentDrawOffset());
        setBounds(_bounds);
    }

    public void addInbound(ArcView newArcView) {
        model.addInbound(newArcView);
    }

    public void addOutbound(ArcView newArcView) {
        model.addOutbound(newArcView);
    }

    public void addInboundOrOutbound(ArcView newArcView) {
        model.addInboundOrOutbound(newArcView);
    }

    public void removeFromArc(ArcView oldArcView) {
        model.removeFromArcs(oldArcView);
    }

    public void removeToArc(ArcView oldArcView) {
        model.removeToArc(oldArcView);
    }

    public void updateConnected() {
        updateArcs(model.outboundArcs());
        updateArcs(model.inboundArcs());
    }

    private void updateArcs(LinkedList<ArcView> arcsFrom) {
        for (ArcView someArcView : arcsFrom) {
            updateEndPoint(someArcView);
            if (someArcView != null) {
                someArcView.updateArcPosition();
            }
        }
    }

    public LinkedList<ArcView> outboundArcs() {
        return model.outboundArcs();
    }

    public LinkedList<ArcView> inboundArcs() {
        return model.inboundArcs();
    }

    public void translate(int x, int y) {
//        setPositionX(_positionX + x);
//        setPositionY(_positionY + y);
        update();
    }

    void setCentre(double x, double y) {
//        setPositionX(x - (getWidth() / 2.0));
//        setPositionY(y - (getHeight() / 2.0));
        update();
    }

    public void update() {

//        setPositionX(_model.getX());
//        setPositionY(_model.getY());
        _nameLabel.setPosition(model.getX() + model.getNameXOffset(), model.getY() + model.getNameYOffset());
        updateBounds();
        updateLabelLocation();
        updateConnected();
    }


    private void updateLabelLocation() {
//        _nameLabel.setPosition(
//                Grid.getModifiedX((int) (_positionX + ZoomController.getZoomedValue(_nameOffsetX, _zoomPercentage))),
//                Grid.getModifiedY((int) (_positionY + ZoomController.getZoomedValue(_nameOffsetY, _zoomPercentage))));
    }

    public void delete() {
        if (getParent() != null) {
            getParent().remove(_nameLabel);
        }
        super.delete();
    }

    public void select() {
        if (_selectable && !_selected) {
            _selected = true;

            Iterator arcsFrom = model.outboundArcs().iterator();
            while (arcsFrom.hasNext()) {
                ((ArcView) arcsFrom.next()).select();
            }

            Iterator arcsTo = model.inboundArcs().iterator();
            while (arcsTo.hasNext()) {
                ((ArcView) arcsTo.next()).select();
            }
            repaint();
        }
    }

    public void addedToGui() {
        _deleted = false;
        _markedAsDeleted = false;
        addLabelToContainer();
        update();
    }

    boolean areNotSameType(ConnectableView o) {
        return (this.getClass() != o.getClass());
    }

    public Iterator getConnectFromIterator() {
        return model.outboundArcs().iterator();
    }

    public Iterator getConnectToIterator() {
        return model.inboundArcs().iterator();
    }

    public abstract void updateEndPoint(ArcView arcView);

    int getCopyNumber() {
        if (_original != null) {
            _original._copyNumber++;
            return _original._copyNumber;
        } else {
            return 0;
        }
    }

    void newCopy(ConnectableView ptObject) {
        if (_original != null) {
            _original._lastCopy = ptObject;
        }
    }

    public ConnectableView getLastCopy() {
        return _lastCopy;
    }

    public void resetLastCopy() {
        _lastCopy = null;
    }

    void setOriginal(ConnectableView ptObject) {
        _original = ptObject;
    }

    public ConnectableView getOriginal() {
        return _original;
    }

    public abstract void showEditor();

    public void setAttributesVisible(boolean flag) {
        _attributesVisible = flag;
    }

    public boolean getAttributesVisible() {
        return _attributesVisible;
    }

    public int getLayerOffset() {
        return Constants.PLACE_TRANSITION_LAYER_OFFSET;
    }

    public abstract void toggleAttributesVisible();

    public void zoomUpdate(int value) {
        _zoomPercentage = value;
        update();
    }

    public PetriNetViewComponent clone() {
        PetriNetViewComponent pnCopy = super.clone();
        pnCopy.setNameLabel((NameLabel) _nameLabel.clone());
        return pnCopy;
    }

    public T getModel() {
        return model;
    }
}
