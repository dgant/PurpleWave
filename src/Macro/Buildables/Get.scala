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

  def apply(quantity: Int, buildableType: BuildableType): Buildable = {
    buildableType match {
      case v: UnitClass  => BuildableUnit(v, quantity)
      case v: Tech       => BuildableTech(v)
      case v: Upgrade    => BuildableUpgrade(v, quantity)
    }
  }

  def apply(buildableType: BuildableType, quantity: Int = 1): Buildable = apply(quantity, buildableType)
}
