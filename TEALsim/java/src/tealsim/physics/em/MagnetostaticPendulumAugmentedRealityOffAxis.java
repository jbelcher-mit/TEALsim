/*
 * Created on Oct September 11, 2024
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package tealsim.physics.em;

import teal.plot.MagnetostaticPendulumTwoBodyEnergyPlot;
import teal.plot.ElectrostaticPendulumTwoBodyEnergyPlot;
import teal.plot.Graph;

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
import teal.sim.constraint.ArcConstraint;
import teal.sim.constraint.SphericalArcConstraint;
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
public class MagnetostaticPendulumAugmentedRealityOffAxis extends SimEM {
	
	//  note I am declaring a serialVersionUID here although I have no idea what this means and it is
	// the same id as in other applications, see the ElectrostaticPendulum  belcher 12/14/2024

    private static final long serialVersionUID = 3256443586278208051L;
    /** The friction slider. */
    PropertyDouble frictionSlider = new PropertyDouble();
    PropertyDouble numberLinesSlider = new PropertyDouble();
    /** The friction in the world. */
    double friction;
    Graph graph;
    MagnetostaticPendulumTwoBodyEnergyPlot eGraph;
    
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
    CylindricalBarMagnet swingingMagnet;
    CylindricalBarMagnet dummyMagnet;
    CylindricalBarMagnet stationaryMagnet;
    Rendered nativeObject01;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;
    PropertyDouble MuSlider;
    double lengthPendulum=15; 
    
    double heightSupport = 25.;

    public MagnetostaticPendulumAugmentedRealityOffAxis() {

        super();
        title = "Magnetostatic Pendulum Augmented Reality Off Axis";
        TDebug.setGlobalLevel(1);
        Graph graph;
        MagnetostaticPendulumTwoBodyEnergyPlot eGraph;
       

        // Building the world.
        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., -9.8,0.));
        
 


        nativeObject01 = new Rendered(); 
        ShapeNode ShapeNodeNative01 = new ShapeNode();

        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, .1, lengthPendulum));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 100));
        nativeObject01.setPosition(new Vector3d(0,heightSupport,0.));
        nativeObject01.setModelOffsetPosition(new Vector3d(0,-lengthPendulum/2,0.));
        nativeObject01.setDirection(new Vector3d(1.,0.,0.));
        addElement(nativeObject01);
        
        
        double scale3DS = 3.*(1.); // this is an overall scale factor for this .3DS object
        // Creating components.
        Loader3DS max = new Loader3DS();
        BranchGroup bg01 = 
         max.getBranchGroup("models/ArmBase.3DS",
         "models/");
        node01.setScale(scale3DS);
        node01.addContents(bg01);    
        importedObject01.setNode3D(node01);
        importedObject01.setPosition(new Vector3d(0., 0., 0.));
        addElement(importedObject01);
        
// change some features of the lighting, background color, etc., from the default values, if desired
        
        mViewer.setBackgroundColor(new Color(240,240,255));
        
        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));

        // Set magnetic dipole characteristics
        double fixedMu = 890.;
        double fixedRadius =0.;
        double MagnetRadius = 1.;
        double MagnetRadius1 = 0.;

        stationaryMagnet = new CylindricalBarMagnet();
        stationaryMagnet.setRadius(MagnetRadius);
        stationaryMagnet.setMass(2.);
        stationaryMagnet.setMu(fixedMu);
        stationaryMagnet.setID("stationaryMagnet");
        stationaryMagnet.setPickable(false);
        stationaryMagnet.setColliding(false);
        stationaryMagnet.setGeneratingP(true);
        stationaryMagnet.setPosition(new Vector3d(0., MagnetRadius1,fixedRadius));
        stationaryMagnet.setMoveable(false);
        stationaryMagnet.setRotable(false);
        SphereCollisionController sccx = new SphereCollisionController(stationaryMagnet);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        stationaryMagnet.setCollisionController(sccx);
        addElement(stationaryMagnet);
        
        dummyMagnet = new CylindricalBarMagnet();
        dummyMagnet.setRadius(MagnetRadius);
        dummyMagnet.setMass(2.);
        dummyMagnet.setMu(1.);
        dummyMagnet.setID("dummyMagnet");
        dummyMagnet.setPickable(false);
        dummyMagnet.setColliding(false);
        dummyMagnet.setGeneratingP(true);
        dummyMagnet.setPosition(new Vector3d(0., MagnetRadius1,fixedRadius));
        dummyMagnet.setMoveable(false);
        dummyMagnet.setRotable(false);
        SphereCollisionController sccx1 = new SphereCollisionController(dummyMagnet);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        dummyMagnet.setCollisionController(sccx);
        
        swingingMagnet = new CylindricalBarMagnet();
        swingingMagnet.setRadius(MagnetRadius);
        //swingingMagnet.setPauliDistance(4.*MagnetRadius);
        swingingMagnet.setMass(5.);
        swingingMagnet.setMu(0);
        swingingMagnet.setID("swingingMagnet");
        swingingMagnet.setPickable(false);
        swingingMagnet.setColliding(true);
        swingingMagnet.setGeneratingP(true);
        swingingMagnet.setPosition(new Vector3d(0.,0., 0.));
        swingingMagnet.setMoveable(true);
        swingingMagnet.setRotable(false);
        swingingMagnet.setConstrained(true);
        sccx = new SphereCollisionController(swingingMagnet);
        sccx.setRadius(MagnetRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        //swingingMagnet.addPropertyChangeListener("charge",this );
        addElement(swingingMagnet);
        
        // ***************************************************************************
        // Graph
        // ***************************************************************************
        graph = new Graph();
        //graph.setBounds(500, 68, 400, 360);
        graph.setXRange(0., 15.);
        graph.setYRange(-0.005, .12);
        graph.setXLabel("Time");
        graph.setYLabel("Energy (Joules)");
 
        JLabel label1 = new JLabel("Magnetic Energy");
        label1.setForeground(Color.RED);
        //label1.setBounds(660, 20, 200, 24);
        label1.setFont(label1.getFont().deriveFont(Font.BOLD));
        JLabel label2 = new JLabel("Kinetic Energy");
        label2.setForeground(Color.BLUE);
        //label2.setBounds(625, 44, 200, 24);
        label2.setFont(label2.getFont().deriveFont(Font.BOLD));
        JLabel label3 = new JLabel("Gravitational Potential Energy");
        label3.setForeground(Color.green);
        label3.setFont(label3.getFont().deriveFont(Font.BOLD));
        JLabel label4 = new JLabel("Total Energy");
        label4.setForeground(Color.BLACK);
        label4.setFont(label3.getFont().deriveFont(Font.BOLD));

        eGraph = new MagnetostaticPendulumTwoBodyEnergyPlot();
        eGraph.setPlotValue(0);  
        eGraph.setBodyOne(swingingMagnet);   
        eGraph.setBodyTwo(stationaryMagnet);
        eGraph.setIndObj(theEngine);
        graph.addPlotItem(eGraph);
        VisualizationControl visControl;
        JTaskPaneGroup params, graphs;
        params = new JTaskPaneGroup();
        params.setText("Parameters2");
//        params.add(slider);
        graphs = new JTaskPaneGroup();
        graphs.setText("Graph of the Three Energies and their Total");
        graphs.add(label1);
        graphs.add(label2);
        graphs.add(label3);
        graphs.add(label4);
        graphs.add(graph);
        // Hack to get around not adding graph as element
        theEngine.addSimElement(graph);
        visControl = new VisualizationControl();
        visControl.setConvolutionModes(DLIC.DLIC_FLAG_E|DLIC.DLIC_FLAG_EP);
        visControl.setActionFlags(VisualizationControl.CHANGE_FL_COLORMODE);
        visControl.setFieldConvolution(mDLIC);
        visControl.setSymmetryCount(2);
        visControl.setFieldLineManager(fmanager);
        visControl.setColorPerVertex(false);
        addElement(graphs);
//        addElement(params);
 //       addElement(visControl);
 		ArcConstraint arc = new ArcConstraint(new Vector3d(.0,heightSupport,0.), new Vector3d(0.,0.,1.), lengthPendulum);
		swingingMagnet.addConstraint(arc);
 		
        int maxStep = 25;

        double startFL=MagnetRadius;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        
        // put field lines on swinging magnet
        int numberFLA = 75;
        maxStep = 500;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(swingingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.2);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(swingingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.6);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
     //       fmanager.addFieldLine(fl);
        }
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(swingingMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.8);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
//        }
        // put field lines on stationary magnet
//        maxStep = 100;
//        numberFLA = 5;
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(stationaryMagnet, ((j ) / (numberFLA*1.)) *2.* Math.PI * 2.,.5 * Math.PI ,startFL*.8);
            fl.setType(Field.B_FIELD);
            fl.setKMax(maxStep);
  //
          fmanager.addFieldLine(fl);
        }

        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        MuSlider = new PropertyDouble();
        MuSlider.setText("Ratio |m/M|");
        MuSlider.setMinimum(0.);
        MuSlider.setMaximum(2.);
        MuSlider.setBounds(40, 535, 415, 50);
        MuSlider.setPaintTicks(true);
  //      MuSlider.addRoute(dummyMagnet, "Mu");
        MuSlider.addRoute(dummyMagnet, "Mu");
        MuSlider.setValue(.1);

        //addElement(MuSlider);
        MuSlider.setVisible(true);
        label = new JLabel("z value of bob:");
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
        ControlGroup params1 = new ControlGroup();
        params1.setText("Control Magnet Moment of Swinging Magnet Compared to Stationary Magnet");
        params1.add(MuSlider);
        params1.add(label);
        params1.add(score);
        addElement(params1);
        
        // create the slider to control the amount of friction in the model
        frictionSlider.setText("Number Field Lines");
        frictionSlider.setMinimum(0.);
        frictionSlider.setMaximum(5.);
        frictionSlider.setPaintTicks(true);
        frictionSlider.addPropertyChangeListener("value", this);
        frictionSlider.setValue(0.0);
        frictionSlider.setVisible(true);
        
        
        // create the slider to control the number of field lines in the model
        numberLinesSlider.setText("Friction");
        numberLinesSlider.setMinimum(5);
        numberLinesSlider.setMaximum(25);
        numberLinesSlider.setPaintTicks(true);
        numberLinesSlider.addPropertyChangeListener("value", this);
        numberLinesSlider.setValue(5);
        numberLinesSlider.setVisible(true);


        // add the slider to a control group and add this to the scene

        ControlGroup controls = new ControlGroup();
        controls.setText("Control Panel");
        controls.add(frictionSlider);
        controls.add(numberLinesSlider);
        addElement(controls);
        
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
        
        theEngine.setDeltaTime(.02);
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

        TealAction ta = new TealAction("Magnetostatic Pendulum Augmented Reality", this);
        addAction("Help", ta);

        ta = new TealAction("Level Complete", "Level Complete", this);
        watch.setAction(ta);


        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Magnetostatic Pendulum Augmented Reality") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/magpendulumaugmented.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Level complete") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/magpendulumaugmented.html");
        	}
        } else {
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

        swingingMagnet.setPosition(new Vector3d(-lengthPendulum, heightSupport, 0));
        swingingMagnet.setDirection(new Vector3d(0,1, 0));
    }


    public void resetCamera() {
    	mViewer.setLookAt(new Point3d(0.,.6,3.), new Point3d(0,0,-20), new Vector3d(0,1,0));

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
                Vector3d cali = swingingMagnet.getPosition();
                double currentMu = dummyMagnet.getMu();
                double currentMuS = stationaryMagnet.getMu();
  //             	TDebug.println(0, " y  " + cali.y + " currentMu " + currentMu + "  cunnenntMs " + currentMuS );  
              double newMu=-(.8195)*currentMu*currentMuS;
 //              	TDebug.println(0, " time  " + time + " newMu " + newMu);
                swingingMagnet.setMu(newMu);
               double resetMu = swingingMagnet.getMu();                
             	TDebug.println(0, " time  " + time + " resetMu " + resetMu +  "newMu  "  + newMu);
  //              stationaryMagnet.setMu(-2.*currentMu);
    //            theEngine.requestRefresh();
  
                Vector3d reference = new Vector3d(0.,heightSupport,0.);
                reference.sub(cali);
                nativeObject01.setDirection(reference);
 //               swingingMagnet.setDirection(reference);
                double angle=-90.;
                angle=(180./Math.PI)*Math.atan2(cali.x,heightSupport-cali.y);
 //           	TDebug.println(0, " time  " + time + " x " + cali.x + " y " + cali.y + " z " +cali.z + " angle " + angle);
                double scale = cali.y/100;
                 score.setText(String.valueOf(scale));
                score.setText(String.valueOf(scale));

                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(swingingMagnet.getPosition()))) {
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
        } else {
            super.propertyChange(pce);
        }
    }   

}
