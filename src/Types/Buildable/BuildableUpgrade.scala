package Types.Buildable

import Development.TypeDescriber
import bwapi.{UpgradeType, UnitType}

class BuildableUpgrade(upgrade:UpgradeType, level:Int=1) extends Buildable {
  
  override def upgradeOption    : Option[UpgradeType]   = Some(upgrade)
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
    if (requirement != UnitType.None) {
      List(new BuildableUnit(requirement))
    }
    else {
      List.empty
    }
  }
}
