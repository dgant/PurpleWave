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
  def minerals        : Int = 0
  def gas             : Int = 0
  def frames          : Int = 0
  def supplyRequired  : Int = 0
  def supplyProvided  : Int = 0
  
  def buildersOccupied: Iterable[BuildableUnit] = Vector.empty
  def buildersBorrowed: Iterable[BuildableUnit] = Vector.empty
  def buildersConsumed: Iterable[BuildableUnit] = Vector.empty
}