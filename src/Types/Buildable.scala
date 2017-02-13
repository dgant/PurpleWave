package Types

import bwapi.{TechType, UnitType, UpgradeType}

class Buildable(
  var unit:Option[UnitType] = None,
  var upgrade:Option[UpgradeType] = None,
  var tech:Option[TechType] = None,
  var level:Int = 1) {
  
  def minerals:Int = {
    unit.map(_.mineralPrice).getOrElse(
      upgrade.map(_.mineralPrice).getOrElse(
        tech.map(_.mineralPrice).getOrElse(0)))
  }
  
  def gas:Int = {
    unit.map(_.gasPrice).getOrElse(
      upgrade.map(_.gasPrice).getOrElse(
        tech.map(_.gasPrice).getOrElse(0)))
  }
  
  def supply:Int = {
    unit.map(_.supplyRequired).getOrElse(0)
  }
  
  def time:Int = {
    unit.map(_.buildTime).getOrElse(
      upgrade.map(_.upgradeTime).getOrElse(
        tech.map(_.gasPrice).getOrElse(0)))
  }
  
  def buildersOccupied:Iterable[UnitType] = {
    unit.map(u => List.fill(u.whatBuilds.second)(u.whatBuilds.first)).getOrElse(
      upgrade.map(u => List(u.whatUpgrades)).getOrElse(
        tech.map(r => List(r.whatResearches)).getOrElse(
          List.empty)))
  }
  
  def buildersConsumed:Iterable[UnitType] = {
    buildersOccupied
      .filter(unitType => List(
        UnitType.Protoss_High_Templar,
        UnitType.Protoss_Dark_Templar,
        UnitType.Zerg_Larva,
        UnitType.Zerg_Hydralisk,
        UnitType.Zerg_Mutalisk,
        UnitType.Zerg_Creep_Colony,
        UnitType.Zerg_Hatchery,
        UnitType.Zerg_Lair
      ).contains(unitType))
  }
}
