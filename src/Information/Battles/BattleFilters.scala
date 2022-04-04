package Information.Battles

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.UnitFilters.IsProxied

object BattleFilters {
  def local(unit: UnitInfo): Boolean = any(unit) && unit.likelyStillThere
  def global(unit: UnitInfo): Boolean = any(unit)
  
  private def any(unit: UnitInfo): Boolean = (
    unit.alive
      && ! unit.unitClass.isSpell
      && ! unit.invincible
      && (unit.complete || unit.unitClass.isBuilding || unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon))
  )

  def home(unit: UnitInfo): Boolean = {
    unit.isFriendly || ! unit.unitClass.isBuilding || IsProxied(unit)
  }

  def away(unit: UnitInfo): Boolean = {
    unit.isEnemy || ! unit.unitClass.isBuilding
  }
}
