package Development

import bwapi.UnitType

object TypeDescriber {
  def describeUnitType(unitType:UnitType):String = {
    unitType.toString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replaceAll("_", " ")
  }
}
