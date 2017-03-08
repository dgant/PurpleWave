package Types.Buildable

import bwapi.{TechType, UnitType, UpgradeType}

abstract class Buildable{
  
  def unitOption      : Option[UnitType]    = None
  def unitsProduced   : Int                 = 0
  def techOption      : Option[TechType]    = None
  def upgradeOption   : Option[UpgradeType] = None
  def upgradeLevel    : Int                 = 0
  def minerals        : Int = 0
  def gas             : Int = 0
  def frames          : Int = 0
  def supplyRequired  : Int = 0
  def supplyProvided  : Int = 0
  
  def requirements:Iterable[Buildable] = List.empty
  def buildersOccupied:Iterable[BuildableUnit] = List.empty
  def buildersConsumed:Iterable[BuildableUnit] = {
    //BuildableUnit accounts for lair/hive upgrade
    buildersOccupied
      .filter(unitType => List(
        UnitType.Protoss_High_Templar,
        UnitType.Protoss_Dark_Templar,
        UnitType.Zerg_Larva,
        UnitType.Zerg_Drone,
        UnitType.Zerg_Hydralisk,
        UnitType.Zerg_Mutalisk,
        UnitType.Zerg_Creep_Colony,
        UnitType.Zerg_Lair
      ).contains(unitType))
  }
}
