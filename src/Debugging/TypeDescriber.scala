package Debugging

import bwapi.{TechType, UnitType, UpgradeType}

object TypeDescriber {
  def unit(unitType:UnitType):String =
    unitType.toString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replace("Resource_", "")
      .replace("Special_", "")
      .replaceAll("_", " ")
  
  def tech(techType:TechType):String =
    techType.toString
    .replaceAll("_", " ")
  
  def upgrade(upgradeType:UpgradeType):String =
    upgradeType.toString
      .replaceAll("_", " ")
}
