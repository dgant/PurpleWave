package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Harass4PoolWorkers extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.isScout
    && unit.totalHealth > 5
    && With.enemies.exists(_.isZerg)
    && With.fingerprints.fourPool()
    && ! unit.matchups.threats.exists(t => Zerg.Zergling(t) && t.pixelsToGetInRange(unit) < 320)
    && unit.matchups.threats.count(t => Zerg.Drone(t) && t.pixelDistanceEdge(unit) < 48 && t.totalHealth >= unit.totalHealth) < 2)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val target = Maff.minBy(unit.matchups.targets.filter(Zerg.Drone))(_.pixelDistanceEdge(unit)).filter(_.orderTarget.exists(_.unitClass.isMinerals))

    if (target.isDefined) {
      unit.agent.toAttack = target
      Commander.attack(unit)
    }
  }
}
