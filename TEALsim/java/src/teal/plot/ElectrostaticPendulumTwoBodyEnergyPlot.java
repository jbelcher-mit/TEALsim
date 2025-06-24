/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TwoBodyEnergyPlot.java,v 1.6 2007/12/04 20:59:52 pbailey Exp $ 
 * 
 */

package teal.plot; 

import javax.vecmath.Vector3d;

import teal.sim.engine.TSimEngine;
import teal.util.TDebug;
import teal.physics.physical.PhysicalObject;
import teal.physics.em.HasCharge;

/**
 * @author danziger
 */
public class ElectrostaticPendulumTwoBodyEnergyPlot implements PlotItem {
	
	PhysicalObject b1 = null;
    
    PhysicalObject b2 = null;
    
    TSimEngine indObj = null;
    
    int dataChannel = 0;
    boolean connected = true;
    boolean initialized = false;
    int plotValue;
    static final int eEnergyPlot = 0;
    static final int kEnergyPlot = 1;
    static final int gpEnergyPlot = 2;
    static final int TotEnergyPlot = 3;
    
    public ElectrostaticPendulumTwoBodyEnergyPlot() {
    
   
    plotValue = eEnergyPlot;
    
    };
    
    public void setPlotValue(int plotval)
    {
    	plotValue = plotval;
    }
    
    public void setBodyOne(PhysicalObject obj)
    {
    	b1 = obj;
    }
    
    public void setBodyTwo(PhysicalObject obj)
    {
    	b2 = obj;
    }
     
    public void setIndObj(TSimEngine obj)
    {
    	indObj = obj;
    }
    
   
	
	public void setDataChannel(int ch)
    {
        dataChannel = ch;
    }
    public int getDataChannel()
    {
        return dataChannel;
    }
    
    protected void initialize()
    {
        initialized = true;
    }

	
	
	public void doPlot(Graph graph)
    {
        //TDebug.println(0,"In doPlot");
        if(!initialized)
            initialize();
        
		double q1 = ((HasCharge)b1).getCharge();
		double q2 = ((HasCharge)b2).getCharge();
		double m1 = b1.getMass();
		Vector3d pos1 = b1.getPosition();
		Vector3d vel1 = b1.getVelocity();
		Vector3d pos2 = b2.getPosition();
		Vector3d r = new Vector3d();
		r.sub(pos1,pos2);
		double rlength = r.length();
        
    	double eEnergy;
    	eEnergy = q1 * q2 * (1/(rlength)); 
    	double t = indObj.getTime();

    	double kEnergy;
    	double gpEnergy;
    	double totEnergy;
    	kEnergy = 0.5 * m1 * vel1.lengthSquared() * 1.;
    	gpEnergy = m1 * (9.8)* (pos1.y-5.) *1.;

    	kEnergy=kEnergy*1.;
    	// the (4.*Math.PI) factor below comes because we have set epsilon naught to 1 in 1 in teal.config.Teal
    	eEnergy=eEnergy/(4.*Math.PI);
    	
    	double factor = 1./10000;
    	gpEnergy=gpEnergy*factor;
    	kEnergy=kEnergy*factor;
    	eEnergy=eEnergy*factor;

    	totEnergy = kEnergy + gpEnergy+eEnergy;
    	TDebug.println(0, "eEnergy: " + eEnergy + " kEnergy: " + kEnergy + " gpEnergy " +gpEnergy + " totEnergy: " + totEnergy);
    	TDebug.println(0,  eEnergy + ", " + kEnergy + ", " +gpEnergy + ", " + totEnergy);

		double xrange [] = graph.getXRange();
		if( t > xrange[1] ) {
			graph.setXRange(xrange[1], xrange[1]+(xrange[1]-xrange[0]));
			graph.clear(0);
			graph.clear(1);
			graph.clear(2);
			graph.clear(3);
		}
	
    	graph.addPoint(0,t,eEnergy,connected);   
    	graph.addPoint(1,t,kEnergy,connected);   
    	graph.addPoint(2,t,gpEnergy,connected); 
    	graph.addPoint(3,t,totEnergy,connected); 

    }
}
