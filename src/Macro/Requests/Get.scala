package Macro.Requests

import ProxyBwapi.Buildable
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

/**
  * Get is just a way for build orders to succinctly reference Buildable[Unit/Tech/Upgrade]
  * with arbitrary argument orders.
  */
object Get {

  def apply(quantity: Int, buildableType: Buildable): RequestBuildable = {
    buildableType match {
      case v: UnitClass  => RequestUnit(v, quantity)
      case v: Tech       => RequestTech(v)
      case v: Upgrade    => RequestUpgrade(v, quantity)
    }
  }

  def apply(buildableType: Buildable, quantity: Int = 1): RequestBuildable = apply(quantity, buildableType)
}
