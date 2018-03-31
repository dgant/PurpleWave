package Micro.Actions.Combat.Techniques.Common

import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Activators.{Activator, WeightedMean}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class ActionTechnique extends Action {
  
  val activator: Activator = new WeightedMean(this)
  
  val applicabilityBase = 1.0
  def applicabilitySelf(unit: FriendlyUnitInfo): Double = 1.0
  def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = Some(1.0)
  def significanceOther(unit: FriendlyUnitInfo, other: UnitInfo): Double = {
    val framesOfInvolvement = unit.matchups
      .framesOfEntanglementPerThreat
      .getOrElse(other, Double.NegativeInfinity)
    0.5 + PurpleMath.fastSigmoid(framesOfInvolvement / 12.0) / 2.0
  }
}
