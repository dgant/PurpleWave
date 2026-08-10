package Macro.Requests

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}

case class RequestMicroSlush(mineralBudget: Int) extends RequestBuildable(UnitClasses.None, 1) {
  override def buildFrames       : Int                = 0
  override def mineralCost       : Int                = Math.max(0, mineralBudget)
  override def gasCost           : Int                = 0
  override def supplyProvided    : Int                = 0
  override def supplyRequired    : Int                = 0
  override def techRequired      : Option[Tech]       = None
  override def unitsRequired     : Seq[UnitClass]     = Seq.empty
  override def producerRequired  : UnitClass          = UnitClasses.None
  override def producersRequired : Int                = 0
  override def addonRequired     : Option[UnitClass]  = None

  override def toString: String = f"Micro slush: $mineralCost minerals"
}
