package Macro.BuildRequests

import Macro.Buildables.{Buildable, BuildableTech, BuildableUnit, BuildableUpgrade}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

object Get {
  
  def toBuildable(value: Any, level: Int): Buildable = {
    value match {
      case v: UnitClass  => BuildableUnit(v)
      case v: Tech       => BuildableTech(v)
      case v: Upgrade    => BuildableUpgrade(v, level)
    }
  }
  
  def apply(quantity: Int, buildableType: Any): BuildRequest = {
    val shouldAdd = false
    val buildable = toBuildable(buildableType, quantity)
    val output = new BuildRequest(buildable) {
      override val add      : Int = if (shouldAdd) quantity else 0
      override val require  : Int = if (shouldAdd) 0        else quantity
    }
    output
  }
  
  def apply(buildable: Any, quantity: Int = 1): BuildRequest = apply(quantity, buildable)
}
