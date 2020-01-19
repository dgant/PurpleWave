package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchRecruitableForCombat, UnitMatchWorkers}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class GoalRazeProxies(var pixel: Pixel) extends SquadGoalBasic {

  override def toString: String = "Raze proxies in " + pixel.zone.name

  override def run() {
    var keyTarget: Option[UnitInfo] = None
    if (squad.enemies.forall(_.isAny(Protoss.Probe, Protoss.Pylon, Protoss.Gateway))) {
      keyTarget = ByOption.minBy(squad.enemies.view.filter(_.is(Protoss.Pylon)).toVector)(u => u.totalHealth * (if (u.complete) 1 else 100))
    }
    val bunker = squad.enemies.find(e => e.isBunker() && ! e.complete)
    keyTarget = keyTarget.orElse(bunker.flatMap(_.matchups.allies.find(a => a.unitClass.isWorker && a.orderTarget == bunker)))

    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
      toAttack = keyTarget
      canFlee = false
    }))
  }

  unitMatcher = UnitMatchOr(UnitMatchRecruitableForCombat, UnitMatchWorkers)
}