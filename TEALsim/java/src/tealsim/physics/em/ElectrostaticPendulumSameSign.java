/*
  Created on Oct 6, 2003 modified June 14 2025
 */

package tealsim.physics.em;
import teal.plot.ElectrostaticPendulumTwoBodyEnergyPlot;
import teal.ui.swing.JTaskPaneGroup;
import teal.plot.Graph;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.swing.JLabel;
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
import teal.sim.control.VisualizationControl;
import teal.sim.engine.EngineObj;
import teal.physics.em.SimEM;
import teal.physics.em.PointCharge;
import teal.sim.properties.IsSpatial;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldLineManager;
import teal.sim.spatial.RelativeFLine;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;
import java.awt.*;
import teal.render.geometry.Cylinder;
import teal.render.j3d.*;

/**
 * @author danziger belcher
 */
public class ElectrostaticPendulumSameSign extends SimEM {
    private static final long serialVersionUID = 3256443586278208051L;
    // instantiate the electromagnetic objects
    PointCharge swingingCharge;
    PointCharge fixedCharge;
    PointCharge dummyCharge;
 //  set the length of the pendulum and the height of the support   
    double lengthPendulum=20.; 
    double heightSupport = 25.;
    // create the friction slider.
    PropertyDouble frictionSlider = new PropertyDouble();
    double friction;
  // create the energy graph 
    Graph graph;
    ElectrostaticPendulumTwoBodyEnergyPlot eGraph;   
    // create a place to put the arm and base 3DS model
    Rendered importedObject01 = new Rendered();
    Node3D node01 = new Node3D();
    Rendered nativeObject01;
    Watcher watch;
    Appearance myAppearance;
 // create the line integral convolution graph   
    protected FieldConvolution mDLIC = null;
// create the field line manager for the charges
    FieldLineManager fmanager = null;

    public ElectrostaticPendulumSameSign() {
        super();
        title = "Electrostatic Pendulum Same Sign";
        TDebug.setGlobalLevel(1);     
        // import the arm/base for stand
        double scale3DS = 3.; // this is an overall scale factor for these .3DS objects
       Loader3DS max = new Loader3DS();
        BranchGroup bg01 = 
         max.getBranchGroup("models/ArmBase.3DS",
         "models/");
        node01.setScale(scale3DS);
        node01.addContents(bg01);
        importedObject01.setNode3D(node01);
        importedObject01.setPosition(new Vector3d(0., 0., 0.));
        addElement(importedObject01);
// initially set friction to zero but changed in public void propertyChange(PropertyChangeEvent pce) {       
        theEngine.setDamping(0.0);
// set gravity
        theEngine.setGravity(new Vector3d(0., -9.8,0.));
        // create the pendulum itself
        nativeObject01 = new Rendered();
        ShapeNode ShapeNodeNative01 = new ShapeNode();
        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, .1, lengthPendulum));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 100));
        nativeObject01.setPosition(new Vector3d(0,heightSupport,0.));
        nativeObject01.setModelOffsetPosition(new Vector3d(0,-lengthPendulum/2,0.));
// the direction of the pendulum is set as the charge moves in public void nextSpatial() {
        nativeObject01.setDirection(new Vector3d(1.,0.,0.));
        addElement(nativeObject01);
// change some features of the lighting, background color, etc., from the default values, if desired  
        mViewer.setBackgroundColor(new Color(240,240,255));      
        // Set characteristics of charges
        double pointChargeRadius = 0.9;
        fixedCharge = new PointCharge();
        fixedCharge.setRadius(pointChargeRadius);
        fixedCharge.setMass(1.0);
        fixedCharge.setCharge(223.0);
        fixedCharge.setID("fixedCharge");
        fixedCharge.setPickable(false);
        fixedCharge.setColliding(true);
        fixedCharge.setGeneratingP(true);
        fixedCharge.setPosition(new Vector3d(0., 0., 0.));
        fixedCharge.setMoveable(false);
        SphereCollisionController sccx = new SphereCollisionController(fixedCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        fixedCharge.setCollisionController(sccx);
        addElement(fixedCharge);

        dummyCharge = new PointCharge();
        dummyCharge.setMass(2.);
        dummyCharge.setCharge(1.);
        dummyCharge.setID("dummyCharge");
        dummyCharge.setPickable(false);
        dummyCharge.setColliding(false);
        dummyCharge.setGeneratingP(true);
  
        dummyCharge.setMoveable(false);
        dummyCharge.setRotable(false);
        sccx.setRadius(1.);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        
        swingingCharge = new PointCharge();
        swingingCharge.setRadius(pointChargeRadius);
        swingingCharge.setMass(5.0);
        swingingCharge.setCharge(6);
        swingingCharge.setID("swingingCharge");
        swingingCharge.setPickable(false);
        swingingCharge.setColliding(true);
        swingingCharge.setGeneratingP(true);
        //  the actual position of this charge is set in private void resetPointCharges(double heightSupport, double lengthPendulum) {
        // which is called before execution begins
        swingingCharge.setPosition(new Vector3d(0.,0., 0.));
        swingingCharge.setMoveable(true);
        swingingCharge.setConstrained(true);
        sccx = new SphereCollisionController(swingingCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        addElement(swingingCharge);
        
        // ***************************************************************************
        // Graph
        // ***************************************************************************
        graph = new Graph();
        graph.setXRange(0., 15.);
        graph.setYRange(-0.005, .14);
        graph.setXLabel("Time");
        graph.setYLabel("Energy (Joules)");
 
        JLabel label1 = new JLabel("Electric Energy");
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

        eGraph = new ElectrostaticPendulumTwoBodyEnergyPlot();
        eGraph.setPlotValue(0);
        eGraph.setBodyOne(swingingCharge);
        eGraph.setBodyTwo(fixedCharge);
        eGraph.setIndObj(theEngine);
        graph.addPlotItem(eGraph);
        VisualizationControl visControl;
        JTaskPaneGroup params, graphs;
        params = new JTaskPaneGroup();
        params.setText("Parameters");
        graphs = new JTaskPaneGroup();
        graphs.setText("Graph of the Three Energies and their Total");
        graphs.add(label1);
        graphs.add(label2);
        graphs.add(label3);
        graphs.add(label4);
        graphs.add(graph);
        // Hack to get around not adding graph as element
        theEngine.addSimElement(graph);
        // set up dlic controls 
        visControl = new VisualizationControl();
        visControl.setConvolutionModes(DLIC.DLIC_FLAG_E|DLIC.DLIC_FLAG_EP);
        visControl.setActionFlags(VisualizationControl.CHANGE_FL_COLORMODE);
        visControl.setFieldConvolution(mDLIC);
        visControl.setSymmetryCount(2);
// add field line manager
        visControl.setFieldLineManager(fmanager);
        visControl.setColorPerVertex(false);
        addElement(graphs);
// constrain movement to an arc  
 		ArcConstraint arc = new ArcConstraint(new Vector3d(.0,heightSupport,0.), new Vector3d(0.,0.,1.), lengthPendulum);
		swingingCharge.addConstraint(arc);
 	//  set maximum number of steps to take along a field line	
        int maxStep = 200;
// set where to start the field line off
        double startFL=pointChargeRadius/2.;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        
        // put field lines on swinging charge 5 azimuth and 5 polar
        int numberFLA = 5;
        int numberFLP =5;
        for (int k = 0; k < numberFLP+2; k++) {
        for (int j = 0; j < numberFLA; j++) {

            RelativeFLine fl = new RelativeFLine(swingingCharge, ((j + 1) / (numberFLA*1.)) * Math.PI * 2.,((k ) / (numberFLP*1.+1.)) * Math.PI ,startFL);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        }
        // put field lines on stationary charge 5 azimuth and 5 polar
        
        numberFLA =5;
        numberFLP =5;

        for (int k = 0; k < numberFLP+2; k++) {
        for (int j = 0; j < numberFLA; j++) {
            RelativeFLine fl = new RelativeFLine(fixedCharge, ((j + 1) / (numberFLA*1.)) * Math.PI * 2.,((k ) / (numberFLP*1.+1.)) * Math.PI ,startFL);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
           fmanager.addFieldLine(fl);
        }
       }
        
        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        PropertyDouble chargeSlider = new PropertyDouble();
        chargeSlider.setText("Ratio |q/Q|");
        chargeSlider.setMinimum(0.);
        chargeSlider.setMaximum(2.);
        chargeSlider.setBounds(40, 535, 415, 50);
        chargeSlider.setPaintTicks(true);
        chargeSlider.addRoute(dummyCharge, "charge");
        chargeSlider.setValue(.3);
        chargeSlider.setVisible(true);
        watch = new Watcher();
        addElement(watch);
        ControlGroup params1 = new ControlGroup();
        params1.setText("Control Charge of Swinging Charge Compared to Stationary Charge");
        params1.add(chargeSlider);
        addElement(params1);
        // properties of the slider to control the amount of friction in the model
        frictionSlider.setText("Friction");
        frictionSlider.setMinimum(0.);
        frictionSlider.setMaximum(5.);
        frictionSlider.setPaintTicks(true);
        frictionSlider.addPropertyChangeListener("value", this);
        frictionSlider.setValue(0.0);
        frictionSlider.setVisible(true);

        // add the slider to a control group and add this to the scene

        ControlGroup controls = new ControlGroup();
        controls.setText("Control Friction in the World");
        controls.add(frictionSlider);
        addElement(controls);
        
        // put in the DLIC buttons 
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

        addActions();
        watch.setActionEnabled(true);
//  set time step for the integration         
        theEngine.setDeltaTime(.02);
        mSEC.init();

        resetCamera();
        reset(heightSupport,lengthPendulum);
    }

    void addActions() {
    	
    	// sets up the help links

        TealAction ta = new TealAction("Electrostatic Pendulum Same Sign", this);
        addAction("Help", ta);

        TealAction tb = new TealAction("Execution & View", this);
        addAction("Help", tb);
        
    }
    
    // continuing setting up help links
    // This method is called when an ActionEvent is received.  Here we tell the application to launch the Help file when
    // the Help ActionEvent defined above is received.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Electrostatic Pendulum Same Sign") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/Ependulumsame.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/executionView.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

    public void reset(double heightSupport, double lengthPendulum) {
        mSEC.stop();
        mSEC.reset();
        resetPointCharges(heightSupport,lengthPendulum);
        watch.setActionEnabled(true);
    }

    private void resetPointCharges(double heightSupport, double lengthPendulum) {
    	// this is where the initial position of the swinging charge is set
        swingingCharge.setPosition(new Vector3d(-lengthPendulum, heightSupport, 0));
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

                Vector3d cali = swingingCharge.getPosition();
                double currentq = dummyCharge.getCharge();
                double currentQ = fixedCharge.getCharge();
  //             	TDebug.println(0, " y  " + cali.y + " currentMu " + currentMu + "  cunnenntMs " + currentMuS );  
              double newQ=currentq*currentQ;
 //              	TDebug.println(0, " time  " + time + " newMu " + newMu);
                swingingCharge.setCharge(newQ);

                Vector3d reference = new Vector3d(0.,heightSupport,0.);
                reference.sub(cali);
//         		System.out.println("    ");
 //           	TDebug.println(0, "Electrostatic Pendulum   time   " + time + " x pos " + cali.x + " y pos " + cali.y + " z pos "+ cali.z);

//           	TDebug.println(0, "swingingCharge   "  + qtest);
// this sets the position of the pendulum so that it follows the charge as it moves
                nativeObject01.setDirection(reference);

// the code below has not real function in this application, left in from the original code emzoo, from which this was adapted
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(swingingCharge.getPosition()))) {
                        System.out.println("congratulations");
                        // Make this a one-shot
                        actionEnabled = false;
                        mSEC.stop();
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
