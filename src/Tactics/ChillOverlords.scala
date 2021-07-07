package Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ChillOverlords extends Prioritized {
  
  val overlords = new LockUnits(this)
  overlords.matcher = Zerg.Overlord
  overlords.counter = CountEverything

  def update() {
    if ( ! With.self.isZerg) return
    if (With.self.hasUpgrade(Zerg.OverlordSpeed)) return
    if (MacroFacts.enemyShownCloakedThreat) return

    overlords.acquire(this)
    overlords.units.foreach(chillOut(_, overlords.units.size))
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int) {
    val base = Maff.minBy(With.geography.ourBases.map(_.heart.center))(overlord.pixelDistanceSquared)
    val tile = base.map(b => Maff.sample(Circle.points(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    overlord.intend(this, new Intention {
      toTravel = Some(tile.center)
      canFlee = true
    })
  }
}
