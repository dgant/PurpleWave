package Debugging

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.Upgrades.Upgrade

object TypeDescriber {
  def unit(unitClass: UnitClass):String =
    unitClass.base.toString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replace("Resource_", "")
      .replace("Special_", "")
      .replaceAll("_", " ")
  
  def tech(techType:Tech):String =
    techType.base.toString
    .replaceAll("_", " ")
  
  def upgrade(upgradeType:Upgrade):String =
    upgradeType.base.toString
      .replaceAll("_", " ")
}
