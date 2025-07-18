
 /*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: ArcConstraint.java,v 1.13 2007/07/16 22:05:00 pbailey Exp $ 
 * 
 */

package teal.sim.constraint; 

import javax.vecmath.Vector3d;

import teal.util.TDebug;

/**
 * @author mesrob
 *
 */
public class ArcConstraint implements Constraint {

	/* We will represent an arc by its center, radius and a vector which
	 * is the normal to the plane of the arc.
	 */

	protected Vector3d center = null, normal = null;
	protected double radius = 1.;
	protected Vector3d lastReaction = new Vector3d();

	public ArcConstraint( Vector3d ccenter, Vector3d nnormal, double rradius ) {
		setCoefficients( ccenter, nnormal, rradius );
	}

	public void setCoefficients( Vector3d ccenter, Vector3d nnormal, double rradius ) {
		center = new Vector3d(ccenter);
		normal = new Vector3d(nnormal);
		normal.normalize();
		radius = rradius;
	}	

	public void setPoint( Vector3d ccenter ) {
		center = new Vector3d(ccenter);
	}	
	public Vector3d getCenter() {
		return new Vector3d(center);
	}	

	public void setNormal( Vector3d nnormal ) {
		normal = new Vector3d(nnormal);
		normal.normalize();
	}	
	public Vector3d getNormal() {
		return new Vector3d(normal);
	}	

	public void setRadius( double rradius ) {
		radius = rradius;
	}	
	public double getRadius() {
		return radius;
	}	

	public Vector3d getReaction(Vector3d position, Vector3d velocity, Vector3d action) {
		return getReaction(position, velocity, action, 0.);
	}
	
	public Vector3d getReaction(Vector3d position, Vector3d velocity, Vector3d action, double mass) {
		Vector3d radial = new Vector3d(position);
		radial.sub(center);
		radial.normalize();
		
		Vector3d tangential = new Vector3d();
		tangential.cross(radial, normal);
		
		// this part of the code actually does the physics by just 
		// making the radial component of the force zero.
		// you take the action find its tangential component then subtract that
		//  from the action, leaving only the radial component and then 
		// make the reaction the negative of the radial component, and add that to the action,
		// thus killing the radial component (whew!)
		
		double effective = action.dot(tangential);
		Vector3d reaction = new Vector3d(tangential);
		reaction.scaleAdd(-effective, action);
		reaction.negate();
        TDebug.println(2,"ArcConstraint One:  position :" + position +  "  velocity  " + velocity );
        TDebug.println(2,"ArcConstraint Two:  action :"  + action +  " reaction  " + reaction );
		
//  this part of the code apparently does nothing, I think because the mass is zero?
// doesn't seem to make sense in any case
		
		Vector3d centripetal = new Vector3d(radial);
		centripetal.scale(-mass*velocity.lengthSquared()/radius);
		
		reaction.add(centripetal);
		lastReaction.set(reaction);
		return reaction;
	}

	public void set_default() {
		setCoefficients(	new Vector3d(0., 0., 0.),
							new Vector3d(1., 0., 0.),
							1.0 );
	}

	public void set( Constraint c ) {
		if( c instanceof ArcConstraint ) {
			setCoefficients(	((ArcConstraint) c).getCenter(),
								((ArcConstraint) c).getNormal(),
								((ArcConstraint) c).getRadius() );
		}	
	}
	
	public Vector3d getLastReaction() {
		return this.lastReaction;
	}

}
