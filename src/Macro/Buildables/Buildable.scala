package Macro.Buildables

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses._
import ProxyBwapi.Upgrades.Upgrade

abstract class Buildable {
  
  def unitOption      : Option[UnitClass]   = None
  def unitsProduced   : Int                 = 0
  def techOption      : Option[Tech]        = None
  def upgradeOption   : Option[Upgrade]     = None
  def upgradeLevel    : Int                 = 0
  def frames          : Int                 = 0

  def is(item: Any): Boolean = unitOption.contains(item) || techOption.contains(item) || upgradeOption.contains(item)
}