package Information.Battles

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade
import Utilities.?
import Utilities.UnitFilters.{IsRangedGoon, IsSpeedVulture, IsSpeedling, IsSpeedlot}

object BattleFilters {
  def local(unit: UnitInfo): Boolean = any(unit) && unit.likelyStillThere
  def global(unit: UnitInfo): Boolean = any(unit)
  
  private def any(unit: UnitInfo): Boolean = (
    unit.alive
      && ! unit.unitClass.isSpell
      && ! unit.invincible
      && (unit.complete || unit.unitClass.isBuilding || unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon))
  )

  def defending(unit: UnitInfo): Boolean = {
    unit.isFriendly || ! unit.unitClass.isBuilding || unit.proxied
  }

  def attacking(unit: UnitInfo): Boolean = {
    unit.isEnemy || ! unit.unitClass.isBuilding || unit.proxied
  }

  def slugging(unit: UnitInfo): Boolean = {
    ! unit.unitClass.isBuilding
  }

  private def foesLack  (player: PlayerInfo, upgrades : Upgrade*)   : Boolean = ! player.enemies.exists(e => upgrades.exists(_(e)))
  private def foesLackU (player: PlayerInfo, units    : UnitClass*) : Boolean = ?(player.isFriendly, With.unitsShown.allEnemies(units: _*) == 0,  ! With.units.existsOurs(units: _*))
  private def teamHasU  (player: PlayerInfo, units    : UnitClass*) : Boolean = ?(player.isFriendly, With.units.existsOurs(units: _*),              With.unitsShown.allEnemies(units: _*) > 0)
  def skirmish(unit: UnitInfo): Boolean = {
    val p = unit.player
    var output = unit.isAny(IsSpeedVulture, Terran.Wraith, IsRangedGoon, Protoss.DarkTemplar, Protoss.Corsair, Protoss.Arbiter, Protoss.Carrier, Protoss.Scout, Zerg.Mutalisk, Zerg.Scourge)
    output ||= Terran.Vulture(unit)   && foesLack(p, Terran.VultureSpeed, Zerg.ZerglingSpeed)
    output ||= Zerg.Zergling(unit)    && foesLack(p, Zerg.ZerglingSpeed)    && foesLackU(p, Terran.Vulture)
    output ||= IsSpeedling(unit)      && foesLack(p, Terran.VultureSpeed)
    output ||= Protoss.Dragoon(unit)  && foesLack(p, Protoss.DragoonRange)  && foesLackU(p, Terran.Vulture, Zerg.Zergling)
    output ||= IsSpeedlot(unit)       && foesLack(p, Terran.VultureSpeed)
    output ||= unit.isAny(Terran.Marine, Terran.Firebat, Terran.Marine)     && Terran.Stim(p)
    output ||= Protoss.Reaver(unit)   && foesLackU(p, Terran.Wraith, Zerg.Mutalisk, Zerg.Scourge) && teamHasU(p, Protoss.Shuttle)
    output
  }
}
