/*
 * Created on Oct September 11, 2024
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
import javax.media.j3d.BranchGroup;
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
import teal.render.Rendered;
import teal.render.j3d.loaders.Loader3DS;
import teal.sim.collision.SphereCollisionController;
import teal.sim.constraint.PlanarConstraint;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.EngineObj;
import teal.sim.engine.TEngine;
import teal.physics.em.SimEM;
import teal.physics.em.EMEngine;
import teal.physics.physical.Wall;
import teal.physics.em.CylindricalBarMagnet;
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

// from Example_01

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.media.j3d.*;
import javax.vecmath.*;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.render.Rendered;
import teal.render.geometry.Cylinder;
import teal.render.geometry.Sphere;
import teal.render.j3d.*;
import teal.render.j3d.loaders.Loader3DS;
import teal.physics.em.SimEM;
import teal.ui.control.*;
import teal.util.TDebug;

/**
 * @author belcher
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BarMagnetsPlanar extends SimEM {

    private static final long serialVersionUID = 3256443586278208051L;
    
    /** An imported 3DS object (a hemisphere).  */
    Rendered importedObject01 = new Rendered();
    Node3D node01 = new Node3D();
    /** An imported 3DS object (a cone).  */
    Rendered importedObject02 = new Rendered();
    /** A 3D node for the cone. */
    Node3D node02 = new Node3D();

    JButton but = null;
    JButton but1 = null;
    JTaskPaneGroup vis;
    JLabel label;
    JLabel score;
    double minScore = 100000000.;
    CylindricalBarMagnet rotatingCoil;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;

    public BarMagnetsPlanar() {

        super();
        title = "Galvanometer";
        
       
        TDebug.setGlobalLevel(0);

        // Building the world.
        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., 0.,0.));

        Rendered nativeObject01 = new Rendered(); 
        ShapeNode ShapeNodeNative01 = new ShapeNode();

        double lengthPendulum= 20.;  // maximum of 23
        double heightSupport = 25.;
        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, .2, lengthPendulum));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 0));
        nativeObject01.setPosition(new Vector3d(0,heightSupport,0.));
        nativeObject01.setModelOffsetPosition(new Vector3d(0,-lengthPendulum/2,0.));
        nativeObject01.setDirection(new Vector3d(0,1.,0.));
//        addElement(nativeObject01);
        
        
        double scale3DS = 3.; // this is an overall scale factor for this .3DS object
        // Creating components.
       Loader3DS max = new Loader3DS();
        BranchGroup bg01 = 
         max.getBranchGroup("models/ArmBase.3DS",
         "models/");
        node01.setScale(scale3DS);
      node01.addContents(bg01);    
        importedObject01.setNode3D(node01);
        importedObject01.setPosition(new Vector3d(0., 0., 0.));
//        addElement(importedObject01);
        
// change some features of the lighting, background color, etc., from the default values, if desired
        
        mViewer.setBackgroundColor(new Color(240,240,255));
        
        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));

        // Set charges
        double fixedMu = -55.;
        double fixedRadius =2.7;
        double MagnetRadius = 1.;
        double CoilSeperation = 14.;
        double MagnetRadius1 = 0.;
        CylindricalBarMagnet HelmholtzCoilLeft = new CylindricalBarMagnet();
        HelmholtzCoilLeft.setRadius(MagnetRadius);
        HelmholtzCoilLeft.setMass(.05);
        HelmholtzCoilLeft.setMu(fixedMu);
        HelmholtzCoilLeft.setID("HelmholtzCoilLeft");
        HelmholtzCoilLeft.setPickable(false);
        HelmholtzCoilLeft.setColliding(false);
        HelmholtzCoilLeft.setGeneratingP(true);
        HelmholtzCoilLeft.setPosition(new Vector3d(-CoilSeperation/2., 0.,0.));
        HelmholtzCoilLeft.setMoveable(false);
        HelmholtzCoilLeft.setRotable(true);
        HelmholtzCoilLeft.setDirection(new Vector3d(1.,0.,0.));
        HelmholtzCoilLeft.setLength(4.);
        SphereCollisionController sccx = new SphereCollisionController(HelmholtzCoilLeft);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        HelmholtzCoilLeft.setCollisionController(sccx);
        addElement(HelmholtzCoilLeft);
        
        CylindricalBarMagnet HelmholtzCoilRight = new CylindricalBarMagnet();
        HelmholtzCoilRight.setRadius(MagnetRadius);
        HelmholtzCoilRight.setMass(.05);
        HelmholtzCoilRight.setMu(fixedMu);
        HelmholtzCoilRight.setID("HelmholtzCoilRight");
        HelmholtzCoilRight.setPickable(false);
        HelmholtzCoilRight.setColliding(false);
        HelmholtzCoilRight.setGeneratingP(true);
        HelmholtzCoilRight.setPosition(new Vector3d(CoilSeperation/2., 0.,0.));
        HelmholtzCoilRight.setMoveable(false);
        HelmholtzCoilRight.setRotable(true);
        HelmholtzCoilRight.setLength(4.);
        SphereCollisionController sccx1 = new SphereCollisionController(HelmholtzCoilRight);
        sccx1.setRadius(MagnetRadius/2.);
        sccx1.setTolerance(0.1);
        sccx1.setMode(SphereCollisionController.WALL_SPHERE);
        HelmholtzCoilRight.setCollisionController(sccx1);
        HelmholtzCoilRight.setDirection(new Vector3d(-1,0.,1.));
        addElement(HelmholtzCoilRight);

        rotatingCoil = new CylindricalBarMagnet();
        rotatingCoil.setRadius(MagnetRadius);
        //rotatingCoil.setPauliDistance(4.*MagnetRadius);
        rotatingCoil.setMass(2.);
        rotatingCoil.setMu(-.01);
        rotatingCoil.setID("rotatingCoil");
        rotatingCoil.setPickable(false);
        rotatingCoil.setColliding(true);
        rotatingCoil.setGeneratingP(true);
        rotatingCoil.setPosition(new Vector3d(0.,0., 0.));
        rotatingCoil.setMoveable(false);
        rotatingCoil.setRotable(true);
//        rotatingCoil.setConstrained(true);
        sccx = new SphereCollisionController(rotatingCoil);
        rotatingCoil.setDirection(new Vector3d(1.,0.,0.));
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        //rotatingCoil.addPropertyChangeListener("charge",this );
 //       addElement(rotatingCoil);
         
 		PlanarConstraint arc = new PlanarConstraint(new Vector3d(0.,1.,0.));
		rotatingCoil.addConstraint(arc);
 		
        int maxStep = 25;

        double startFL=2.*MagnetRadius;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        
        // put field lines on moving magnet
        int numberFLA = 40;
        maxStep = 200;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(HelmholtzCoilRight, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.4);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(HelmholtzCoilRight, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.6);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
     //       fmanager.addFieldLine(fl);
        }
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(HelmholtzCoilRight, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.8);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
//        }
        
        // put field lines on HelmholtzCoilLeft
        maxStep = 200;
        numberFLA = 5;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(HelmholtzCoilLeft, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.4);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        // put field lines on stationary 01 magnet
        maxStep = 200;
        numberFLA = 5;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(HelmholtzCoilRight, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.4);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
  //          fmanager.addFieldLine(fl);
        }



        
    
    numberFLA = 5;

        
         maxStep=300;   


        
            

        
        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        PropertyDouble MuSlider = new PropertyDouble();
        MuSlider.setText("Player Mu:");
        MuSlider.setMinimum(-500.);
        MuSlider.setMaximum(500.);
        MuSlider.setBounds(40, 535, 415, 50);
        MuSlider.setPaintTicks(true);
        MuSlider.addRoute(rotatingCoil, "Mu");
        MuSlider.setValue(-40);
        //addElement(MuSlider);
        MuSlider.setVisible(true);
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
        params.add(MuSlider);
        params.add(label);
        params.add(score);
        addElement(params);
        //tp.add(params);
        VisualizationControl vis = new VisualizationControl();
        vis.setText("Field Visualization");
        mDLIC = new FieldConvolution();
        mDLIC.setComputePlane(new RectangularPlane(theEngine.getBoundingArea()));
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_B | DLIC.DLIC_FLAG_BP);
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
        
        theEngine.setDeltaTime(1);
        mSEC.init();

        resetCamera();
        reset(heightSupport,lengthPendulum);
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


        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("EM Video Game") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Level complete") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void reset(double heightSupport, double lengthPendulum) {
        mSEC.stop();
        mSEC.reset();
        resetCylindricalBarMagnet(heightSupport,lengthPendulum);
        //theEngine.requestRefresh();
        watch.setActionEnabled(true);
    }

    private void resetCylindricalBarMagnet(double heightSupport, double lengthPendulum) {

        rotatingCoil.setPosition(new Vector3d(0., 0., 0.));
        rotatingCoil.setDirection(new Vector3d(1,0, 0));
    }


    public void resetCamera() {
    	mViewer.setLookAt(new Point3d(0.,.8,4.), new Point3d(0,0,0), new Vector3d(0,1,0));

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
 //               Vector3d cali = rotatingCoil.getPosition();
 //               Vector3d temp = new Vector3d(cali);
 //               Vector3d center = new Vector3d(0.,25.,0.);
//               temp.sub(center);
 //               double distance = temp.length();

 
                 score.setText(String.valueOf(time));
                score.setText(String.valueOf(time));
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(rotatingCoil.getPosition()))) {
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
