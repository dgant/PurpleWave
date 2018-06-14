package Macro.BuildRequests

import Macro.Buildables.{BuildableTech, BuildableUnit, BuildableUpgrade}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

object Get {
  def apply(shouldAdd: Boolean, v1: Any, v2: Any = None): BuildRequest = {
    val quantity =
      v1 match {
        case integer: Int => integer
        case _ =>
          v2 match {
            case integer: Int => integer
            case _ => 1
          }}
    
    val buildableSource = if(v1.isInstanceOf[Int]) v2 else v1
    
    val buildable = buildableSource match {
      case v: UnitClass  => BuildableUnit(v)
      case v: Tech       => BuildableTech(v)
      case v: Upgrade    => BuildableUpgrade(v)
    }
    
    val output = new BuildRequest(buildable) {
      override val add      : Int = if (shouldAdd) quantity else 0
      override val require  : Int = if (shouldAdd) 0        else quantity
    }
    
    output
  }
}
