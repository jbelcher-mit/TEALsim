/*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package tealsim.physics.em;
import teal.plot.ElectrostaticPendulumTwoBodyEnergyPlot;
import teal.ui.swing.JTaskPaneGroup;
import teal.plot.Graph;
import teal.plot.TwoBodyEnergyPlot;

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
import teal.sim.control.VisualizationControl;
import teal.sim.engine.EngineObj;
import teal.sim.engine.TEngine;
import teal.physics.em.SimEM;
import teal.physics.em.CylindricalBarMagnet;
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
 * @author danziger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ElectrostaticPendulumSameSign extends SimEM {
    /** The friction slider. */
    PropertyDouble frictionSlider = new PropertyDouble();
    /** The friction in the world. */
    double friction;
    Graph graph;
    ElectrostaticPendulumTwoBodyEnergyPlot eGraph;
    
	//  note I am declaring a serialVersionUID here although I have no idea what this means and it is
	// the same id as in other applications, see the MagnetostaticPendulum  belcher 12/14/2024
    private static final long serialVersionUID = 3256443586278208051L;
    
    /** An imported 3DS object (a hemisphere).      */ 
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
    PointCharge swingingCharge;
    PointCharge fixedCharge;
    PointCharge dummyCharge;
    Rendered nativeObject01;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;
    
    double lengthPendulum=20.; 
    
    double heightSupport = 25.;

    public ElectrostaticPendulumSameSign() {

        super();
        title = "Electrostatic Pendulum Same Sign";
        
       
        TDebug.setGlobalLevel(1);

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
        
        
        double scale3DS = 3.; // this is an overall scale factor for these .3DS objects
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

        // Set charges
        double pointChargeRadius = 0.9;

        fixedCharge = new PointCharge();
        fixedCharge.setRadius(pointChargeRadius);
        //fixedCharge.setPauliDistance(4.*pointChargeRadius);
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
        SphereCollisionController sccx1 = new SphereCollisionController(dummyCharge);
        sccx.setRadius(1.);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
//        dummyCharge.setCollisionController(sccx);
        

        swingingCharge = new PointCharge();
        swingingCharge.setRadius(pointChargeRadius);
        //swingingCharge.setPauliDistance(4.*pointChargeRadius);
        swingingCharge.setMass(5.0);
        swingingCharge.setCharge(6);
        swingingCharge.setID("swingingCharge");
        swingingCharge.setPickable(false);
        swingingCharge.setColliding(true);
        swingingCharge.setGeneratingP(true);
        swingingCharge.setPosition(new Vector3d(0.,0., 0.));
        swingingCharge.setMoveable(true);
        swingingCharge.setConstrained(true);
        sccx = new SphereCollisionController(swingingCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
//        swingingCharge.addPropertyChangeListener("charge",this );
//        swingingCharge.addPropertyChangeListener("position", this);
        addElement(swingingCharge);
        
        // ***************************************************************************
        // Graph
        // ***************************************************************************
        graph = new Graph();
        //graph.setBounds(500, 68, 400, 360);
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
//        params.add(slider);
        graphs = new JTaskPaneGroup();
        graphs.setText("Graph of the Three Enegies and their Total");
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
//        addElement(visControl);
      
 		ArcConstraint arc = new ArcConstraint(new Vector3d(.0,heightSupport,0.), new Vector3d(0.,0.,1.), lengthPendulum);
		swingingCharge.addConstraint(arc);
 		
        int maxStep = 200;

        double startFL=pointChargeRadius/2.;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        
        // put field lines on swinging charge
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
        // put field lines on stationary charge
        
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
        //addElement(chargeSlider);
        chargeSlider.setVisible(true);
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
        params1.setText("Control Charge of Swinging Charge Compared to Stationary Charge");
        params1.add(chargeSlider);
        params1.add(label);
        params1.add(score);
        addElement(params1);
        
        
        // create the slider to control the amount of friction in the model
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
        
        theEngine.setDeltaTime(.02);
        mSEC.init();

        resetCamera();
        reset(heightSupport,lengthPendulum);
    }



    void addActions() {

        TealAction ta = new TealAction("Electrostatic Pendulum Same Sign", this);
        addAction("Help", ta);

        ta = new TealAction("Level Complete", "Level Complete", this);
        watch.setAction(ta);


        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Electrostatic Pendulum Same Sign") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/Ependulumsame.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Level complete") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/eEpendulumsame.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

    public void reset(double heightSupport, double lengthPendulum) {
        mSEC.stop();
        mSEC.reset();
        resetPointCharges(heightSupport,lengthPendulum);
        //theEngine.requestRefresh();
        watch.setActionEnabled(true);
    }

    private void resetPointCharges(double heightSupport, double lengthPendulum) {

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
                double time = theEngine.getTime();
                Vector3d cali = swingingCharge.getPosition();
                double currentq = dummyCharge.getCharge();
                double currentQ = fixedCharge.getCharge();
  //             	TDebug.println(0, " y  " + cali.y + " currentMu " + currentMu + "  cunnenntMs " + currentMuS );  
              double newQ=currentq*currentQ;
 //              	TDebug.println(0, " time  " + time + " newMu " + newMu);
                swingingCharge.setCharge(newQ);
                double resetCharge = swingingCharge.getCharge();  
                Vector3d reference = new Vector3d(0.,heightSupport,0.);
                reference.sub(cali);
          		System.out.println("    ");
 //           	TDebug.println(0, "Electrostatic Pendulum   time   " + time + " x pos " + cali.x + " y pos " + cali.y + " z pos "+ cali.z);
         	    Vector3d hetti = fixedCharge.getPosition();
//           	TDebug.println(0, "swingingCharge   "  + qtest);
                nativeObject01.setDirection(reference);
                double scale = cali.y/100;
                score.setText(String.valueOf(scale));
               score.setText(String.valueOf(scale));
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(swingingCharge.getPosition()))) {
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
