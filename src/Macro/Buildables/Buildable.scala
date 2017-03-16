package Macro.Buildables

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.{Archon, DarkArchon, UnitClass}
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
  
  def requirements:Iterable[Buildable] = List.empty
  def buildersOccupied:Iterable[BuildableUnit] = List.empty
  def buildersBorrowed:Iterable[BuildableUnit] = buildersOccupied.filterNot (b => consumesBuilders.contains(b.unit))
  def buildersConsumed:Iterable[BuildableUnit] = buildersOccupied.filter    (b => consumesBuilders.contains(b.unit))
  
  //Greater Spire is omitted as a hack since the Spire is still useful in the interim
  private val consumesBuilders = Set(
    Archon,
    DarkArchon
  )
}
