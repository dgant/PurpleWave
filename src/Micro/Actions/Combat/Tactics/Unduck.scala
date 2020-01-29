package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Duck
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unduck extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.burrowed
    && ! Duck.allowed(unit)
    && Vector(Zerg.Zergling, Zerg.Hydralisk, Zerg.Defiler).contains(unit.unitClass)
    && (unit.agent.shouldEngage || ! unit.effectivelyCloaked || unit.matchups.enemies.forall(e =>
      ! e.unitClass.attacksGround
      || (e.pixelDistanceEdge(unit) - e.pixelRangeAgainst(unit) - Duck.burrowFrames * e.speedApproaching(unit) > 32)))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.unburrow(unit)
  }
}
