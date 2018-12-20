package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class GoalRazeProxies(var pixel: Pixel) extends GoalBasic {

  override def toString: String = "Raze proxies in " + pixel.zone.name

  override def run() {
    var keyPylon: Option[UnitInfo] = None
    if (squad.enemies.forall(_.isAny(Protoss.Probe, Protoss.Pylon, Protoss.Gateway))) {
      keyPylon = ByOption.minBy(squad.enemies.view.filter(_.is(Protoss.Pylon)).toVector)(u => u.totalHealth * (if (u.complete) 1 else 100))
    }

    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
      toAttack = keyPylon
      canFlee = false
    }))
  }
}