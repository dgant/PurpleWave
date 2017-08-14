package Macro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass
import Utilities.CountMap

import scala.collection.mutable

object Needs {
  
  /*
  Factors that go into desire:
  * Strategy-based: 	Fixed per matchup
  * Usefulness:		Based on enemy composition
  * Synergy:		Based on our current composition
  * Economy now:		How much money we have
  * Economy soon:		How much money we will have
   */
  
  def additional(): Map[UnitClass, Int] = {
  
    val context   = new DesireContext
    val wants     = Desires.wants(context)
    val haves     = mutable.Map[UnitClass, Double](Desires.haves.toSeq: _*)
    val factories = mutable.Map[UnitClass, Double](Desires.haves.toSeq: _*).filter(_._1.unitsTrained.nonEmpty)
    val builds    = new CountMap[UnitClass]
    
    var minerals  = With.self.minerals  + 24 * 60 * With.economy.ourIncomePerFrameMinerals
    var gas       = With.self.gas       + 24 * 60 * With.economy.ourIncomePerFrameGas
    
    while(factories.exists(_._2 > 0 && minerals > 0 && gas > 0)) {
      val mostWanted      = wants.map(want => (want._1, rescale(want._2, haves.getOrElse(want._1, 0.0))))
      val nextMostWanted  = mostWanted.maxBy(_._2)._1
      builds(nextMostWanted) += 1
    }
    
    null
  }
  
  def rescale(desire: Double, have: Double): Double = {
    desire / Math.max(1.0, have)
  }
}
