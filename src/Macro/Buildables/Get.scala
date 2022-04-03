package Macro.Buildables

import ProxyBwapi.BuildableType
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

/**
  * Get is just a way for build orders to succinctly reference Buildable[Unit/Tech/Upgrade]
  * with arbitrary argument orders.
  */
object Get {

  def apply(quantity: Int, buildableType: BuildableType): RequestProduction = {
    buildableType match {
      case v: UnitClass  => RequestUnit(v, quantity)
      case v: Tech       => RequestTech(v)
      case v: Upgrade    => RequestUpgrade(v, quantity)
    }
  }

  def apply(buildableType: BuildableType, quantity: Int = 1): RequestProduction = apply(quantity, buildableType)
}
