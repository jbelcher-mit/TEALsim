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
public class InverterMagneticConfiguration extends SimEM {

    private static final long serialVersionUID = 3256443586278208051L;
    /** The angle slider. */
    PropertyDouble angleSlider = new PropertyDouble();
    /** The angle of rotation*/
    double angle;
    double rangle = 0.;  
    /** The friction in the world. */
    double friction;
    PropertyDouble frictionSlider = new PropertyDouble();
    /** The position of the central magnet*/
    double position_x = 0.;  
    PropertyDouble position_x_Slider = new PropertyDouble();
    
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
    
    CylindricalBarMagnet centralMagnet;
    CylindricalBarMagnet movingMagnet;
    CylindricalBarMagnet magnet01;
    CylindricalBarMagnet magnet02;
    CylindricalBarMagnet magnet03;
    CylindricalBarMagnet magnet04;
    CylindricalBarMagnet magnet05;
    CylindricalBarMagnet magnet06;
    double fixedRadius;
    double delta_angle;
    
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;

    public InverterMagneticConfiguration() {

        super();
        title = "Inverter Magnet";
        
       
        TDebug.setGlobalLevel(0);

        // Building the world.
        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., 0.,0.));
        
        // create the sliders to control the amount of friction in the model
        frictionSlider.setText("Friction");
        frictionSlider.setMinimum(0.);
        frictionSlider.setMaximum(5.);
        frictionSlider.setPaintTicks(true);
        frictionSlider.addPropertyChangeListener("value", this);
        frictionSlider.setValue(0.0);
        frictionSlider.setVisible(true);
 

        position_x_Slider.setText("Position");
        position_x_Slider.setMinimum(0.);
        position_x_Slider.setMaximum(100.);
        position_x_Slider.setPaintTicks(true);
        position_x_Slider.addPropertyChangeListener("value", this);
        position_x_Slider.setValue(0.0);
        position_x_Slider.setVisible(true);

        
        // create the slider to control the rotation of the satellite magnets
        angleSlider.setText("Rotation");
        angleSlider.setMinimum(0.);
        angleSlider.setMaximum(360.);
        angleSlider.setPaintTicks(true);
        angleSlider.addPropertyChangeListener("value", this);
        angleSlider.setValue(0.0);
        angleSlider.setVisible(true);
        // add the slider to a control group and add this to the scene

        ControlGroup controls = new ControlGroup();
        controls.setText("Parameters");
        controls.add(frictionSlider);
        controls.add(angleSlider);
        controls.add(position_x_Slider);
        addElement(controls);

        Rendered nativeObject01 = new Rendered(); 
        ShapeNode ShapeNodeNative01 = new ShapeNode();

        double lengthPendulum= 20.;  // maximum of 23
        double heightSupport = 25.;
        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, .2, lengthPendulum));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 0));
        nativeObject01.setPosition(new Vector3d(0,heightSupport,0.));
        nativeObject01.setModelOffsetPosition(new Vector3d(0,-lengthPendulum/2,0.));
        nativeObject01.setDirection(new Vector3d(1.,0.,0.));
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
        double fixedMuBig = 550;
        double fixedRadius =2.7;
        double MagnetRadius = 2.;
 //       position_x = 0.;
        double MagnetRadiusSmall=MagnetRadius/4;


        centralMagnet = new CylindricalBarMagnet();
        centralMagnet.setRadius(MagnetRadius);
        centralMagnet.setMass(.05);
        centralMagnet.setMu(fixedMuBig);
        centralMagnet.setID("magnet01");
        centralMagnet.setPickable(false);
        centralMagnet.setColliding(false);
        centralMagnet.setGeneratingP(true);
        centralMagnet.setPosition(new Vector3d(position_x, 0.,0.));
        centralMagnet.setMoveable(false);
        centralMagnet.setRotable(false);
        SphereCollisionController sccx = new SphereCollisionController(centralMagnet);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        centralMagnet.setCollisionController(sccx);
        addElement(centralMagnet);
        
        CylindricalBarMagnet magnet01 = new CylindricalBarMagnet();
        double delta_angle = 2.*Math.PI/6.;
        double angle = rangle*Math.PI/180.;
        TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
        magnet01.setRadius(MagnetRadiusSmall);
        magnet01.setMass(.05);
        magnet01.setMu(fixedMu);
        magnet01.setID("magnet01");
        magnet01.setPickable(false);
        magnet01.setColliding(false);
        magnet01.setGeneratingP(true);
        magnet01.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
        magnet01.setMoveable(false);
        magnet01.setRotable(false);
        SphereCollisionController sccx1 = new SphereCollisionController(magnet01);
        sccx1.setRadius(MagnetRadiusSmall);
        sccx1.setTolerance(0.1);
        sccx1.setMode(SphereCollisionController.WALL_SPHERE);
        magnet01.setCollisionController(sccx1);
        addElement(magnet01);
        

        angle = delta_angle+ angle;
        TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
        CylindricalBarMagnet magnet02 = new CylindricalBarMagnet();
        magnet02.setRadius(MagnetRadiusSmall);
        magnet02.setMass(1.0);
        magnet02.setMu(fixedMu);
        magnet02.setID("magnet02");
        magnet02.setPickable(false);
        magnet02.setColliding(false);
        magnet02.setGeneratingP(true);
        magnet02.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
        magnet02.setMoveable(false);
        magnet02.setRotable(false);
        sccx = new SphereCollisionController(magnet02);
        sccx.setRadius(MagnetRadiusSmall);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        magnet02.setCollisionController(sccx);
       addElement(magnet02);
       
       angle = delta_angle+ angle;
       TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
       CylindricalBarMagnet magnet03 = new CylindricalBarMagnet();
       magnet03.setRadius(MagnetRadiusSmall);
       magnet03.setMass(1.0);
       magnet03.setMu(fixedMu);
       magnet03.setID("magnet03");
       magnet03.setPickable(false);
       magnet03.setColliding(false);
       magnet03.setGeneratingP(true);
       magnet03.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
       magnet03.setMoveable(false);
       magnet03.setRotable(false);
       sccx = new SphereCollisionController(magnet03);
       sccx.setRadius(MagnetRadiusSmall);
       sccx.setTolerance(0.1);
       sccx.setMode(SphereCollisionController.WALL_SPHERE);
       magnet03.setCollisionController(sccx);
      addElement(magnet03);
      
      angle = angle+delta_angle;
      TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
      CylindricalBarMagnet magnet04 = new CylindricalBarMagnet();
      magnet04.setRadius(MagnetRadiusSmall);
      magnet04.setMass(1.0);
      magnet04.setMu(fixedMu);
      magnet04.setID("magnet04");
      magnet04.setPickable(false);
      magnet04.setColliding(false);
      magnet04.setGeneratingP(true);
      magnet04.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
      magnet04.setMoveable(false);
      magnet04.setRotable(false);
      sccx = new SphereCollisionController(magnet04);
      sccx.setRadius(MagnetRadiusSmall);
      sccx.setTolerance(0.1);
      sccx.setMode(SphereCollisionController.WALL_SPHERE);
      magnet04.setCollisionController(sccx);
     addElement(magnet04);
     
     angle = angle+delta_angle;
     TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
     CylindricalBarMagnet magnet05 = new CylindricalBarMagnet();
     magnet05.setRadius(MagnetRadiusSmall);
     magnet05.setMass(1.0);
     magnet05.setMu(fixedMu);
     magnet05.setID("magnet05");
     magnet05.setPickable(false);
     magnet05.setColliding(false);
     magnet05.setGeneratingP(true);
     magnet05.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
     magnet05.setMoveable(false);
     magnet05.setRotable(false);
     sccx = new SphereCollisionController(magnet05);
     sccx.setRadius(MagnetRadiusSmall);
     sccx.setTolerance(0.1);
     sccx.setMode(SphereCollisionController.WALL_SPHERE);
     magnet05.setCollisionController(sccx);
    addElement(magnet05);
    
    angle = angle+delta_angle;
    TDebug.println(0," angle and rangle  " +angle +"   "  +rangle );
    CylindricalBarMagnet magnet06 = new CylindricalBarMagnet();
    magnet06.setRadius(MagnetRadiusSmall);
    magnet06.setMass(1.0);
    magnet06.setMu(fixedMu);
    magnet06.setID("magnet06");
    magnet06.setPickable(false);
    magnet06.setColliding(false);
    magnet06.setGeneratingP(true);
    magnet06.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
    magnet06.setMoveable(false);
    magnet06.setRotable(false);
    sccx = new SphereCollisionController(magnet06);
    sccx.setRadius(MagnetRadiusSmall);
    sccx.setTolerance(0.1);
    sccx.setMode(SphereCollisionController.WALL_SPHERE);
    magnet06.setCollisionController(sccx);
   addElement(magnet06);
   

   
        movingMagnet = new CylindricalBarMagnet();
        movingMagnet.setRadius(MagnetRadius);
        //movingMagnet.setPauliDistance(4.*MagnetRadius);
        movingMagnet.setMass(2.);
        movingMagnet.setMu(fixedMuBig);
        movingMagnet.setID("movingMagnet");
        movingMagnet.setPickable(false);
        movingMagnet.setColliding(true);
        movingMagnet.setGeneratingP(true);
        // not really set here, see resetCylindricalBarMagnet
        movingMagnet.setPosition(new Vector3d(0.,0., 0.));
        movingMagnet.setMoveable(true);
        movingMagnet.setRotable(false);
        movingMagnet.setConstrained(true);
        sccx = new SphereCollisionController(movingMagnet);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        //movingMagnet.addPropertyChangeListener("charge",this );
        addElement(movingMagnet);
         
 		PlanarConstraint arc = new PlanarConstraint(new Vector3d(0.,1.,0.));
		movingMagnet.addConstraint(arc);
 		
        int maxStep = 25;

        double startFL=MagnetRadius;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        
        // put field lines on moving magnet
        int numberFLA = 35;
        maxStep = 200;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(movingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.5);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(movingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.5);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
 //           fmanager.addFieldLine(fl);
        }
        
        numberFLA = 25;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(movingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*1.);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
//            fmanager.addFieldLine(fl);
        }
//        }
        
        // put field lines on centralMagnet
        maxStep = 20;
        numberFLA = 15;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(centralMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.4);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
 //           fmanager.addFieldLine(fl);
            

        }
        // put field lines on stationary 01 magnet
        startFL=MagnetRadiusSmall;
        maxStep = 40;
        numberFLA =15;
        double frac = .6;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(magnet01, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }

      // put field lines on stationary 02 magnet
              
//numberFLA = 5;
for (int j = 0; j < numberFLA; j++) {
    RelativeFLine fl = new RelativeFLine(magnet02, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
    fl.setType(Field.B_FIELD);
    fl.setKMax(maxStep);
    fmanager.addFieldLine(fl);
}
    
  //  numberFLA = 5;
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet03, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
        fmanager.addFieldLine(fl);
    }
        
    
 //   numberFLA = 5;
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet04, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
      fmanager.addFieldLine(fl);
    }
        
    
 //   numberFLA = 5;
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet05, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
        fmanager.addFieldLine(fl);
    }
        
//         maxStep=300;   
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet06, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
        fmanager.addFieldLine(fl);
    }
    
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet06, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
        fmanager.addFieldLine(fl);
    }
    for (int j = 0; j < numberFLA; j++) {
        RelativeFLine fl = new RelativeFLine(magnet06, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*frac);
        fl.setType(Field.B_FIELD);
        fl.setKMax(maxStep);
        fmanager.addFieldLine(fl);
    }
        
            

        
        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        PropertyDouble MuSlider = new PropertyDouble();
        MuSlider.setText("Player Mu:");
        MuSlider.setMinimum(-500.);
        MuSlider.setMaximum(500.);
        MuSlider.setBounds(40, 535, 415, 50);
        MuSlider.setPaintTicks(true);
        MuSlider.addRoute(movingMagnet, "Mu");
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
 //       addElement(params);
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

        TealAction ta = new TealAction("Inverter Magnet", this);
        addAction("Help", ta);
        
        TealAction tb = new TealAction("Execution & View", this);
        addAction("Help", tb);




        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Inverter Magnet") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/invertermagnet.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Level complete") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        	
        } else if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
        } 
        else {
            super.actionPerformed(e);
        }
    }



    public void reset(double heightSupport, double lengthPendulum) {
        mSEC.stop();
        mSEC.reset();
        resetCylindricalBarMagnet(heightSupport,lengthPendulum);
        theEngine.requestRefresh();
        watch.setActionEnabled(true);
    }

    private void resetCylindricalBarMagnet(double heightSupport, double lengthPendulum) {

        movingMagnet.setPosition(new Vector3d(10., 0., 3.));
        movingMagnet.setDirection(new Vector3d(0,1, 0));
    }


    public void resetCamera() {
    	mViewer.setLookAt(new Point3d(0.,.8,1.5), new Point3d(0,0,0), new Vector3d(0,1,0));

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
 //               Vector3d cali = movingMagnet.getPosition();
 //               Vector3d temp = new Vector3d(cali);
 //               Vector3d center = new Vector3d(0.,25.,0.);
//               temp.sub(center);
 //               double distance = temp.length();

 
                 score.setText(String.valueOf(time));
                score.setText(String.valueOf(time));
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(movingMagnet.getPosition()))) {
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

    /** Define the action initiated by the slider (i.e., set theEngine damping). 
     * @param pce The property change event when the friction slider is changed. */
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == frictionSlider) {
            friction = ((Double) pce.getNewValue()).doubleValue();
            theEngine.setDamping(friction);
            
        }   else    if (source == position_x_Slider) {
                position_x = ((Double) pce.getNewValue()).doubleValue();
                TDebug.println(0,+position_x );
//               centralMagnet.setPosition(new Vector3d(position_x, 0.,0.));
                
        }   else    if (source == angleSlider) {
        	rangle = ((Double) pce.getNewValue()).doubleValue();
            TDebug.println(0,rangle+  "  "+ fixedRadius + "  " + delta_angle );
            fixedRadius=2.7;
            delta_angle= 2.*Math.PI/6.;
        	rangle = rangle*Math.PI/180.;
            angle = rangle;
 //          magnet01.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
            angle = delta_angle+ angle;
//         magnet02.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
           angle = delta_angle+ angle;
//           magnet03.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
          angle = angle+delta_angle;
//          magnet04.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
         angle = angle+delta_angle;
 //        magnet05.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));
        angle = angle+delta_angle;
//        magnet06.setPosition(new Vector3d(fixedRadius*Math.sin(angle),  0., fixedRadius*Math.cos(angle)));



//           centralMagnet.setPosition(new Vector3d(position_x, 0.,0.));
            
                
        } else {
            super.propertyChange(pce);
        }
    }   

}
