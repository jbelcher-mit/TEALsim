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
import teal.physics.physical.PhysicalObject;
import teal.physics.em.HasMu;
import teal.util.TDebug;
//import teal.sim.physical.*;

/**
 * @author Belcher
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MagnetostaticPendulumTwoBodyEnergyPlot implements PlotItem {
	
	PhysicalObject b1 = null;
    
    
    PhysicalObject b2 = null;
    
    
    TSimEngine indObj = null;
    
    int dataChannel = 0;
    boolean connected = true;
    boolean initialized = false;
    int plotValue;
    static final int mEnergyPlot = 0;
    static final int kEnergyPlot = 1;
    static final int gpEnergyPlot = 2;
    static final int TotEnergyPlot = 3;
    
    public MagnetostaticPendulumTwoBodyEnergyPlot() {
    
   
    plotValue = mEnergyPlot;
    
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
        
//        Number q1 = (Number) b1.getProperty(b1Charge);
//        Number q2 = (Number) b2.getProperty(b2Charge);
//        Number m1 = (Number) b1.getProperty(b1Mass);
//        Vector3d pos1 = (Vector3d) b1.getProperty(b1Pos);
//        Vector3d vel1 = (Vector3d) b1.getProperty(b1Vel);
        
		double m1 = ((HasMu)b1).getMu();
		double m2 = ((HasMu)b2).getMu();

		double mass1 = b1.getMass();
		Vector3d pos1 = b1.getPosition();
		Vector3d vel1 = b1.getVelocity();
		Vector3d pos2 = b2.getPosition();
		Vector3d r = new Vector3d();
		r.sub(pos1,pos2);
		double rlength = r.length();
        double z =r.z;
        double x = r.x;
        double y = r.y;
//    	TDebug.println(0,  " x: " + x +"  y: " +y +"  z: "+z );
 //   	TDebug.println(0,  " m1: " + m1 +"  m2: " +m2 +"  mass1: "+mass1 );
    	double mEnergy;
    	mEnergy = -m1*m2*(2.*y*y-x*x)/Math.pow(y*y+x*x, 5./2.);
//    	mEnergy=mEnergy*3000./(400*456.4);
 //   	mEnergy = q1 * q2 * (1/(rlength)); // * a constant 8.897e8 * 
//    	mEnergy = mEnergy /(4*456.4);
    	double t = indObj.getTime();
    	//TDebug.println(0, "mEnergy: " + mEnergy );

    	double kEnergy;
    	double gpEnergy;
    	double totEnergy;
    	kEnergy = 0.5 * mass1 * vel1.lengthSquared() * 1.;
//    	kEnergy = kEnergy/1000.;
    	gpEnergy = mass1 *(9.8)*(pos1.y-5.) *1.;
//    	gpEnergy = gpEnergy/100.;
 //   	totEnergy = kEnergy + gpEnergy+mEnergy;
//    	totEnergy = totEnergy/(10.*34.2775);
//    	totEnergy = totEnergy*300.;

  //  	kEnergy=3.44184*kEnergy/3;
 //   	gpEnergy = 2.58145*gpEnergy/3;
//    	mEnergy=mEnergy/30;
    	//
    	//
 //   	kEnergy=kEnergy*2.837417568;
//        gpEnergy = gpEnergy*2.834087781;
 
 //   	kEnergy = kEnergy*2.199842003;
//    	gpEnergy = gpEnergy*2.198209516;
//        double common = 7.;
     	kEnergy=kEnergy*0.001021375;
     	mEnergy=mEnergy*7.82522E-05;
        gpEnergy = gpEnergy*0.001021094;
;
    	totEnergy = kEnergy + gpEnergy+mEnergy;
//    	TDebug.println(0, " mEnergy: " + mEnergy + " kEnergy: " + kEnergy + " gpEnergy " +gpEnergy + " totEnergy: " + totEnergy);
 //   	TDebug.println(0,  mEnergy + ", " + kEnergy + ", " +gpEnergy + ", " + totEnergy);
       	TDebug.println(0, kEnergy + ", " +gpEnergy + ", " + totEnergy);


		double xrange [] = graph.getXRange();
		if( t > xrange[1] ) {
			graph.setXRange(xrange[1], xrange[1]+(xrange[1]-xrange[0]));
			graph.clear(0);
			graph.clear(1);
			graph.clear(2);
			graph.clear(3);
		}
	
    	graph.addPoint(0,t,mEnergy,connected);   
    	graph.addPoint(1,t,kEnergy,connected);   
    	graph.addPoint(2,t,gpEnergy,connected); 
    	graph.addPoint(3,t,totEnergy,connected); 

    }
}
