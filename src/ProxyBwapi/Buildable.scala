package ProxyBwapi

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import bwapi.Race

trait Buildable {
  def race: Race
  def productionFrames(quantity: Int): Int
  def mineralCost(quantity: Int): Int
  def gasCost(quantity: Int): Int
  def supplyProvided: Int
  def supplyRequired: Int

  lazy val asUnit     : Option[UnitClass] = Option(asInstanceOf[UnitClass])
  lazy val asTech     : Option[Tech]      = Option(asInstanceOf[Tech])
  lazy val asUpgrade  : Option[Upgrade]   = Option(asInstanceOf[Upgrade])
}
