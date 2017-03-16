package Macro.Buildables

import Debugging.TypeDescriber
import ProxyBwapi.UnitClass.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

case class BuildableUpgrade(upgrade:Upgrade, level:Int=1) extends Buildable {
  
  override def upgradeOption    : Option[Upgrade]       = Some(upgrade)
  override def upgradeLevel     : Int                   = level
  override def toString         : String                = TypeDescriber.upgrade(upgrade) + " " + upgradeLevel
  override def minerals         : Int                   = upgrade.mineralPrice(upgradeLevel)
  override def gas              : Int                   = upgrade.gasPrice(upgradeLevel)
  override def frames           : Int                   = upgrade.upgradeTime(upgradeLevel)
      
  override def buildersOccupied: Iterable[BuildableUnit] = {
    List(new BuildableUnit(upgrade.whatUpgrades))
  }
  
  override def requirements: Iterable[BuildableUnit] = {
    val requirement = upgrade.whatsRequired(upgradeLevel)
    if (requirement != UnitClasses.None) {
      List(new BuildableUnit(requirement))
    }
    else {
      List.empty
    }
  }
}
