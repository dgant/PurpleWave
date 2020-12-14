package Information.Battles

import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object BattleClassificationFilters {
  def isEligibleLocal(unit: UnitInfo): Boolean = {
    isEligible(unit) && unit.likelyStillThere
  }
  
  def isEligibleGlobal(unit: UnitInfo): Boolean = {
    isEligible(unit)
  }
  
  private def isEligible(unit: UnitInfo): Boolean = (
    unit.alive
      && ! unit.unitClass.isSpell
      && ! unit.invincible
      && (unit.complete || unit.unitClass.isBuilding || unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon))
  )
}
