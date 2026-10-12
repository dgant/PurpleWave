package Micro.Actions.Scouting

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitFilters.{IsWarrior, IsWorker}

object FightWithZealot extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Protoss.Probe(unit)
    && unit.matchups.threats.exists(_.isAny(Zerg.Zergling, Terran.Marine)))

  override protected def perform(probe: FriendlyUnitInfo): Unit = {
    val zealots = probe.matchups.allies.filter(z => Protoss.Zealot(z) && (probe.zone == z.zone || probe.pixelDistanceEdge(z) < 128))
    if (zealots.isEmpty) return

    val nearestZealot = zealots.minBy(_.pixelDistanceEdge(probe))

    val targets = probe.matchups.targets.filter(t =>
      t.isAny(IsWarrior, IsWorker)
      && (t.matchups.threatsInPixels(16).exists(Protoss.Zealot) || t.totalHealth < 10))

    val target = Maff.minBy(targets)(t => t.totalHealth + 2 * probe.pixelsToGetInRange(t))
    if (target.exists(probe.readyForAttackOrder) && ! probe.matchups.threatsInPixels(64).filterNot(target.contains).exists(_.presumptiveTarget.contains(probe))) {
      probe.agent.toAttack = target
      Commander.attack(probe)
    } else{
      probe.agent.home.set(nearestZealot.projectFrames(-24))
      MicroPathing.tryMovingAlongTilePath(probe, MicroPathing.getThreatAwarePath(probe, preferHome = true))
    }
  }
}
