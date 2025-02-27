/* $Id: Example_06.java,v 1.2 2008/01/17 14:46:26 jbelcher Exp $ */
/**
 * @author John Belcher 
 * Revision: 1.0 $
 */

package tealsim.physics.examples;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.media.j3d.*;
import javax.vecmath.*;

import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.sim.collision.SphereCollisionController;
import teal.math.RectangularPlane;
import teal.physics.physical.Wall;
import teal.physics.em.PointCharge;
import teal.physics.em.SimEM;
import teal.ui.control.*;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;
import teal.sim.spatial.*;
import teal.sim.control.VisualizationControl;


/** A simulation of a free charge falling under gravity and also interacting electrostatically with
 * a fixed charge located underneath a wall, with a line integral convolution representation of the
 * electric field.  We have a slider that can be used to vary the charge
 * on the fixed charge.  The friction in the world is set to a high value so that the falling charge
 * will quickly settle down to its equilibrium position.  
 *  
 * @author John Belcher
 * @version 1.0 
 * */
public class Example_06 extends SimEM {

    private static final long serialVersionUID = 3257008735204554035L;
    
    /** The fixed-in-space charge slider. */
    PropertyDouble chargeSlider = new PropertyDouble();
    /** The radius of the sphere representing the fixed-in-space charge. */
    double fixedChargeRad = 0.2;
    /** The radius of the sphere representing the floating charge.  */
    double floatingChargeRadius = 0.2;
    /** The friction in the world. */
    double friction = 1.;
    /** The floating charge.  */
    PointCharge floatingCharge;
    /** The fixed charge.  */
    PointCharge fixedCharge;
    /** The initial vector position of the floating charge.  */
    Vector3d floatingChargePos;
    /** The mass of both the floating and the fixed charge. */
    double chargeMass = 0.035;
    /** The charge of the fixed charge. */
    double chargeFixed = 0.;
    /** The charge of the floating charge. */
    double chargeFloat = 1.;
    /** The line integral convolution. */
    private FieldConvolution mDLIC;
 
    public Example_06() {
        super();

        TDebug.setGlobalLevel(0);

        title = "Example_06";
        
		///// Set properties on the SimEngine /////
		// Bounding area represents the characteristic size of the space.
		// setDeltaTime() sets the time step of the simulation.
		// setDamping() sets the damping on the system.
       
        BoundingSphere bs = new BoundingSphere(new Point3d(0, 1.6, 0), 03.5);
        theEngine.setBoundingArea(bs);
        theEngine.setDeltaTime(0.02); 
        theEngine.setDamping(friction);  
        mViewer.setBoundingArea(bs);
              
        fixedCharge = new PointCharge();
        fixedCharge.setCharge(chargeFixed);
        fixedCharge.setPosition(new Vector3d(0., -0.8, 0.));
        fixedCharge.setDirection(new Vector3d(0, 1, 0));
        fixedCharge.setPickable(false);
        fixedCharge.setRotable(false);
        fixedCharge.setMoveable(false);
        fixedCharge.setRadius(fixedChargeRad);
        fixedCharge.setMass(chargeMass);
        addElement(fixedCharge);

        floatingCharge = new PointCharge();
        floatingCharge.setID("Charge");
        floatingCharge.setCharge(chargeFloat);
        floatingCharge.setDirection(new Vector3d(0., 1., 0.));
        floatingChargePos = new Vector3d(0., 1.25, 0.);
        floatingCharge.setPickable(true);
        floatingCharge.setRotable(true);
        floatingCharge.setMoveable(true);
        floatingCharge.setRadius(floatingChargeRadius);
        floatingCharge.setMass(chargeMass);
        
		// Here we add a collisionController to the RingOfCurrent 
        //so that it will be registered as a colliding object, and
		// react appropriately when it touches the Wall.  
        SphereCollisionController sccx = 
        	new SphereCollisionController(floatingCharge);
        sccx.setRadius(floatingChargeRadius);
        sccx.setTolerance(0.01);
        floatingCharge.setColliding(true);
        floatingCharge.setCollisionController(sccx); 
        floatingChargePos = new Vector3d(0., 
        		1.25+ floatingChargeRadius + (floatingChargeRadius * 0.02), 0.);
        addElement(floatingCharge);
      
        // We create a wall that the floating coil sits on.
		// Wall constructor.  		
        Wall wall = new Wall(new Vector3d(0., 0, 0.), 
        		new Vector3d(2., 0., 0.), new Vector3d(0., 0., 2.));
        wall.setElasticity(1.);
        addElement(wall);   
        
        // create the sliders to control the amount of charge
        
        chargeSlider.setText("Qfixed");
        chargeSlider.setMinimum(-10);
        chargeSlider.setMaximum(50);
        chargeSlider.setPaintTicks(true);
        chargeSlider.addPropertyChangeListener("value", this);
        chargeSlider.setValue(0.);
        chargeSlider.setVisible(true);

        // add the slider to a control group and add

        ControlGroup controls = new ControlGroup();
        controls.setText("Parameters");
        controls.add(chargeSlider);
        addElement(controls);
        
        // Add a FieldConvolution generator to the simulation.  
        // A FieldConvolution generates high-resolution 
		// images of a two-dimensional slice of the field.  
        // Below we create the generator and specify the size of the slice.
        RectangularPlane rec = new RectangularPlane(new Vector3d(-2.5, -2.5, 0.),
				new Vector3d(-2.5, 2.5, 0.), new Vector3d(2.5, 2.5, 0.));
		mDLIC = new FieldConvolution();
		mDLIC.setSize(new Dimension(1024, 1024));
		mDLIC.setVisible(false);
		mDLIC.setComputePlane(rec);
        VisualizationControl vis = new VisualizationControl();
        vis.setFieldConvolution(mDLIC);
		vis.setConvolutionModes(DLIC.DLIC_FLAG_E | DLIC.DLIC_FLAG_EP);
        addElement(vis);

        // set paramters for mouseScale 
        
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);

        mSEC.init(); 
        resetCamera();
        // addAction for pulldown menus on TEALsim windows     
        addActions();
        reset();
        
    }

    
    void addActions() {

        TealAction tb = new TealAction("Example_05", this);
        addAction("Help", tb);
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
    }

    public void actionPerformed(ActionEvent e) {
        TDebug.println(1, " Action comamnd: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Example_05") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/example_05.html");
        	}
        }  else {
            super.actionPerformed(e);
        }
        if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
        }  else {
            super.actionPerformed(e);
        }
    }

    public void reset() {
        floatingCharge.setPosition(floatingChargePos);
        floatingCharge.setVelocity(new Vector3d(0.,0.,0.));
        floatingCharge.setDirection(new Vector3d(0., 1., 0.));
        theEngine.setDamping(friction);
        chargeSlider.setValue(0.);
		theEngine.requestRefresh();
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.025, 0.4), 
        		new Point3d(0., 0.025, 0.), new Vector3d(0., 1., 0.));
    }

    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == chargeSlider) {
            chargeFixed = ((Double) pce.getNewValue()).doubleValue();
            fixedCharge.setCharge(chargeFixed);   
            TDebug.println(0,+chargeFixed);
        } else {
            super.propertyChange(pce);
        }
    }
    
}

