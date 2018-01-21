package Information.Battles

import ProxyBwapi.UnitInfo.UnitInfo

object BattleClassificationFilters {
  def isEligibleLocal(unit: UnitInfo): Boolean = {
    isEligible(unit) && unit.likelyStillThere
  }
  
  def isEligibleGlobal(unit: UnitInfo): Boolean = {
    isEligible(unit)
  }
  
  private def isEligible(unit: UnitInfo): Boolean = (
    unit.likelyStillAlive
      && (unit.complete || unit.unitClass.isBuilding)
      && ! unit.unitClass.isSpell
  )
}
