/* $Id: Example_02.java,v 1.2 2008/01/06 21:40:21 jbelcher Exp $ */

package tealsim.physics.examples;
import java.awt.event.ActionEvent;
import javax.media.j3d.*;
import javax.vecmath.*;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.sim.collision.SphereCollisionController;
import teal.sim.engine.EngineObj;
import teal.sim.properties.IsSpatial;
import teal.physics.em.SimEM;
import teal.physics.physical.Wall;
import teal.physics.em.PointCharge;
import teal.util.TDebug;
import tealsim.physics.em.ElectrostaticPendulum.Watcher;

/** A simulation of a charge falling under gravity and bouncing off of a floor.  
 * There are no other electromagnetic objects to interact with, 
 * so there are no electromagnetic interactions 
 * in this simulation.  The collision with the floor is perfectly elastic, and there is no 
 * friction in the model, so the motion repeats indefinitely. 
 *  
 * @author John Belcher
 * @version 1.0 
 * */

public class Example_02 extends SimEM {

    private static final long serialVersionUID = 3257008735204554035L;
    
    /** The floating charge.  */
    PointCharge floatingCharge;
    /** The vector position of the floating charge.  */
    Vector3d floatingChargePos;
    /** The radius of the sphere representing the charge.  */
    double chargeRad = 0.1;
    /** The mass of charge. */
    double chargeMass = .035;
    /** The charge of the charge. */
    double charge = 1.;
    boolean mNeedsSpatial = true;
    public Example_02() {
        super();

        TDebug.setGlobalLevel(0);

        title = "Example_02";
        
		///// Set properties on the SimEngine /////
		// Bounding area represents the size of the simulation
		// setDeltaTime() sets the time step of the simulation.
		// setDamping() sets the damping in the system.
       
        BoundingSphere bs = new BoundingSphere(new Point3d(0, 1.6, 0), 03.5);
        theEngine.setBoundingArea(bs);
        theEngine.setDeltaTime(0.005); 
        theEngine.setDamping(0.);  
        mViewer.setBoundingArea(bs);
        Watcher watch;
         
        floatingCharge = new PointCharge();
        floatingCharge.setID("Charge");
        floatingCharge.setCharge(charge);
        floatingCharge.setDirection(new Vector3d(0., 1., 0.));
        floatingChargePos = new Vector3d(0., 1., 0.);
        floatingCharge.setPickable(true);
        floatingCharge.setRotable(true);
        floatingCharge.setMoveable(true);
        floatingCharge.setRadius(chargeRad);
        floatingCharge.setMass(chargeMass);
        
		// Here we add a collisionController to charge so that it will be
        // registered as a colliding object when it touches the "wall".  
        // We represent the floating charge as a sphere for the purposes of collision
		// detection, since we're only concerned with stopping the charge from 
        // "falling through the floor". The collision controller SphereCollisionController 
        // (in package teal.sim.collision ) is a sphere whose radius is chargeRad, 
        // placed at the center of the floatingCharge,  This has the effect that 
        // when the floatingCharge center position comes within chargeRad of the Wall
        // it collides with the Wall and the floatingCharge bounces.
        
        SphereCollisionController sccx = 
        	new SphereCollisionController(floatingCharge);
        sccx.setRadius(chargeRad);
        sccx.setTolerance(0.01);
        floatingCharge.setColliding(true);
        floatingCharge.setCollisionController(sccx); 
        
        // finally we add the floatingCharge to the world
        
        addElement(floatingCharge);
      
        // We create a "wall" that the floating coil will interact with
        
        Wall wall = new Wall(new Vector3d(0., -.1, 0.), 
        		new Vector3d(2., 0., 0.), new Vector3d(0., 0., 2.));
        wall.setElasticity(1.);
        addElement(wall);

        // set paramters for mouseScale 
        
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);
        watch = new Watcher();
        addElement(watch);
        mSEC.init();
        resetCamera();
        // addAction for pulldown menus on TEALsim windows     
        addActions();
        reset();  
    }
  
    /** Add two menu items to our help menu.  */
    void addActions() {
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
        TealAction tb = new TealAction("Example_02", this);
        addAction("Help", tb);
    }
    
    /** Set responses for when our two help menu items are chosen.  */
    
    public void actionPerformed(ActionEvent e) {
        TDebug.println(0, " Action command: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Example_02") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/example_02.html");
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

    /** Return the simulation to its original configuration.  */
    public void reset() {
        floatingCharge.setPosition(floatingChargePos);
        floatingCharge.setVelocity(new Vector3d(0.,0.,0.));
		theEngine.requestRefresh();
    }
    /** Reset the camera view to its original view */
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.025, 0.4), 
        		new Point3d(0., 0.025, 0.), new Vector3d(0., 1., 0.));
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
    


    public void nextSpatial() {
        if (theEngine != null) {
            double time = theEngine.getTime();
            Vector3d cali = floatingCharge.getPosition();
 //     		System.out.println("    ");
//      		System.out.println("Electrostatic Pendulum   time   " + time + " x pos " + cali.x + " y pos " + cali.y + " z pos "+ cali.z);
            System.out.println(" " + time + ", " + " " + cali.y );
            }
         }
}
}
