/*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package tealsim.physics.em;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.field.Field;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.j3d.Node3D;
import teal.render.j3d.WallNode;
import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.EngineObj;
import teal.sim.engine.TEngine;
import teal.physics.em.SimEM;
import teal.physics.em.EMEngine;
import teal.physics.physical.Wall;
import teal.physics.em.PointCharge;
import teal.sim.properties.IsSpatial;
import teal.sim.simulation.SimWorld;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldLineManager;
import teal.sim.spatial.RelativeFLine;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.ui.swing.JTaskPaneGroup;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;

/**
 * @author danziger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EMVideogame extends SimEM {

    private static final long serialVersionUID = 3256443586278208051L;

    JButton but = null;
    JButton but1 = null;
    JTaskPaneGroup vis;
    JLabel label;
    JLabel score;
    double minScore = 100000000.;
    PointCharge playerCharge;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;

    public EMVideogame() {

        super();
        title = "Electrostatic Videogame";
        
       
        TDebug.setGlobalLevel(0);

        // Building the world.
        theEngine.setDamping(0.1);
        theEngine.setGravity(new Vector3d(0., 0., 0.));
        

        // Creating components.

        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));
        /*			
         Vector3d [] level1 = new Vector3d[]
         { 
         new Vector3d(-12.,-12.,-1.),new Vector3d(-12.,12.,-1.),new Vector3d(-12.,12.,1.),
         new Vector3d(-12.,12.,-1.),new Vector3d(12.,12.,-1.),new Vector3d(12.,12.,1.),
         new Vector3d(12.,12.,-1.),new Vector3d(12.,-12.,-1.),new Vector3d(12.,-12.,1.),
         new Vector3d(-12.,-12.,-1.),new Vector3d(-12.,-4.,-1.),new Vector3d(-12.,-4.,1.),
         new Vector3d(-4.,-12.,-1.),new Vector3d(-4.,8.,-1.),new Vector3d(-4.,8.,1.),
         new Vector3d(-4.,8.,-1.),new Vector3d(4.,8.,-1.),new Vector3d(4.,8.,1.),
         new Vector3d(4.,8.,-1.),new Vector3d(4.,-12.,-1.),new Vector3d(4.,-12.,1.),
         new Vector3d(4.,-12.,-1.),new Vector3d(6.,-12.,-1.),new Vector3d(6.,-12.,1.),
         new Vector3d(10.,-12,-1.),new Vector3d(12.,-12.,-1.),new Vector3d(12.,-12.,1.)
         };
         int i = 0;    
         while (i < level1.length)    
         {
         addWall(level1[i++],level1[i++],level1[i++]);
         }  
         */

        // west wall
        addWall(new Vector3d(-12., 0., 0.), new Vector3d(0., 24., 0.), wallheight);

        // north wall
        addWall(new Vector3d(0., 12., 0.), new Vector3d(24., 0., 0.), wallheight);

        // east wall
        addWall(new Vector3d(12., 0., 0.), new Vector3d(0., 24., 0.), wallheight);

        // south walls
        addWall(new Vector3d(-8., -12., 0.), new Vector3d(8., 0., 0.), wallheight);
        addWall(new Vector3d(-4., -4., 0.), new Vector3d(0., 16., 0.), wallheight);
        addWall(new Vector3d(0., 4., 0.), new Vector3d(8., 0., 0.), wallheight);
        addWall(new Vector3d(4., -4., 0.), new Vector3d(0., 16., 0.), wallheight);

        addWall(new Vector3d((4. + (4. / 3.)), -12., 0.), new Vector3d((8. / 3.), 0., 0.), wallheight);
        addWall(new Vector3d((12. - (4. / 3.)), -12., 0.), new Vector3d((8. / 3.), 0., 0.), wallheight);

        // Set charges
        double pointChargeRadius = 0.9;

        PointCharge chargeNW = new PointCharge();
        chargeNW.setRadius(pointChargeRadius);
        //chargeNW.setPauliDistance(4.*pointChargeRadius);
        chargeNW.setMass(1.0);
        chargeNW.setCharge(6.0);
        chargeNW.setID("chargeNW");
        chargeNW.setPickable(false);
        chargeNW.setColliding(false);
        chargeNW.setGeneratingP(true);
        chargeNW.setPosition(new Vector3d(-11.4, 11.4, 0.));
        chargeNW.setMoveable(false);
        SphereCollisionController sccx = new SphereCollisionController(chargeNW);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        chargeNW.setCollisionController(sccx);
        addElement(chargeNW);

        PointCharge chargeNE = new PointCharge();
        chargeNE.setRadius(pointChargeRadius);
        //chargeNE.setPauliDistance(4.*pointChargeRadius);
        chargeNE.setMass(1.0);
        chargeNE.setCharge(6.0);
        chargeNE.setID("chargeNW");
        chargeNE.setPickable(false);
        chargeNE.setColliding(false);
        chargeNE.setGeneratingP(true);
        chargeNE.setPosition(new Vector3d(11.4, 11.4, 0.));
        chargeNE.setMoveable(false);
        sccx = new SphereCollisionController(chargeNE);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        chargeNE.setCollisionController(sccx);
        addElement(chargeNE);

        playerCharge = new PointCharge();
        playerCharge.setRadius(pointChargeRadius);
        //playerCharge.setPauliDistance(4.*pointChargeRadius);
        playerCharge.setMass(1.0);
        playerCharge.setCharge(-1.0);
        playerCharge.setID("playerCharge");
        playerCharge.setPickable(false);
        playerCharge.setColliding(true);
        playerCharge.setGeneratingP(true);
        playerCharge.setPosition(new Vector3d(-11., -10., 0.));
        playerCharge.setMoveable(true);
        sccx = new SphereCollisionController(playerCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        playerCharge.setCollisionController(sccx);
        //playerCharge.addPropertyChangeListener("charge",this );
        addElement(playerCharge);
        int maxStep = 100;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        for (int j = 0; j < 6; j++) {
            RelativeFLine fl = new RelativeFLine(chargeNW, ((j + 1) / 6.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);

            fl = new RelativeFLine(chargeNE, ((j + 1) / 6.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);

            fl = new RelativeFLine(playerCharge, ((j + 1) / 6.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        PropertyDouble chargeSlider = new PropertyDouble();
        chargeSlider.setText("Player Charge:");
        chargeSlider.setMinimum(-10.);
        chargeSlider.setMaximum(10.);
        chargeSlider.setBounds(40, 535, 415, 50);
        chargeSlider.setPaintTicks(true);
        chargeSlider.addRoute(playerCharge, "charge");
        chargeSlider.setValue(-5);
        //addElement(chargeSlider);
        chargeSlider.setVisible(true);
        label = new JLabel("Current Time:");
        score = new JLabel();
        label.setBounds(40, 595, 140, 50);
        score.setBounds(220, 595, 40, 50);
        label.setVisible(true);
        score.setVisible(true);
        //addElement(label);
        //addElement(score);
        watch = new Watcher();
        addElement(watch);

        //JTaskPane tp = new JTaskPane();
        ControlGroup params = new ControlGroup();
        params.setText("Parameters");
        params.add(chargeSlider);
        params.add(label);
        params.add(score);
        addElement(params);
        //tp.add(params);
        VisualizationControl vis = new VisualizationControl();
        vis.setText("Field Visualization");
        mDLIC = new FieldConvolution();
        mDLIC.setComputePlane(new RectangularPlane(theEngine.getBoundingArea()));
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_E | DLIC.DLIC_FLAG_EP);
        vis.setSymmetryCount(1);
        vis.setColorPerVertex(true);
        vis.setFieldLineManager(fmanager);
        vis.setActionFlags(0);
        vis.setColorPerVertex(false);
        
        addElement(vis);
        //tp.add(vis);
        //addElement(tp);

        addActions();
        watch.setActionEnabled(true);
        
        theEngine.setDeltaTime(0.25);
        mSEC.init();

        resetCamera();
        reset();
    }

    private void addWall(Vector3d pos, Vector3d length, Vector3d height) {
        Wall myWall = new Wall(pos, length, height);
        myWall.setElasticity(wallElasticity);
        myWall.setColor(Color.GREEN);
        myWall.setPickable(false);
        WallNode myNode = (WallNode) myWall.getNode3D();
        myNode.setFillAppearance(myAppearance);
        addElement(myWall);
    }

    void addActions() {

        TealAction ta = new TealAction("EM Video Game", this);
        addAction("Help", ta);

        ta = new TealAction("Level Complete", "Level Complete", this);
        watch.setAction(ta);

        TealAction tb = new TealAction("Execution & View", this);
        addAction("Help", tb);

        
    }

    public void actionPerformed(ActionEvent e) {
    	
        if (e.getActionCommand().compareToIgnoreCase("EM Video Game") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        } 
        
        else if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/executionView.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void reset() {
        mSEC.stop();
        mSEC.reset();
        resetPointCharges();
        //theEngine.requestRefresh();
        watch.setActionEnabled(true);
    }

    private void resetPointCharges() {
        playerCharge.setPosition(new Vector3d(-11, -10.0, 0.));
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 2.0), new Point3d(), new Vector3d(0., 1., 0.));

    }

    public class Watcher extends EngineObj implements IsSpatial {

        private static final long serialVersionUID = 3761692286114804280L;
        //Bounds testBounds = new BoundingSphere(new Point3d(11.4,11.4,0.),2.);
        Bounds testBounds = new BoundingBox(new Point3d(8., -16., -1.5), new Point3d(12., -12., 1.5));
        TealAction theAction = null;
        boolean actionEnabled = false;
        boolean mNeedsSpatial = false;

        public void needsSpatial() {
            mNeedsSpatial = true;
        }

        public void setAction(TealAction ac) {
            theAction = ac;
        }

        public void setActionEnabled(boolean state) {
            actionEnabled = state;
        }

        public boolean getActionEnabled() {
            return actionEnabled;
        }

        public void setBounds(Bounds b) {
            testBounds = b;
        }

        public void nextSpatial() {
            if (theEngine != null) {
                double time = theEngine.getTime();
                score.setText(String.valueOf(time));
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(playerCharge.getPosition()))) {
                        System.out.println("congratulations");
                        // Make this a one-shot
                        actionEnabled = false;
                        mSEC.stop();
                        minScore = Math.min(minScore, time);
                        if (theAction != null) {
                            theAction.triggerAction();
                        }
                    }
                }

            }
        }
    }

  

}
