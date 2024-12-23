/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasCharge.java,v 1.4 2007/07/17 15:46:54 pbailey Exp $ 
 * 
 */

package teal.physics.em;

/**
 * WorldObjects that have a Mu should implement
 * <code>HasMu</code>
 *
 * Implementing this interface allows read / write access to
 * your Mu property.
 */
public interface HasMu {
  
  /**
   * Sets Mu to new value.
   *
   * @param Mu a new <code>double</code> Mu value.
   */
  public void setMu(double m);
  
  /**
   * Gets current charge value.
   *
   * @return current Mu value in <code>double</code> form.
   */
  public double getMu();
}
