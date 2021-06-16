package Tactics

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones.EnemiesAtLeast
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.MatchOr
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class ChillOverlords extends Prioritized {
  
  val overlords = new LockUnits(this)
  overlords.matcher = Zerg.Overlord
  overlords.counter = CountEverything

  val cloakedThreat = new Latch(new EnemiesAtLeast(1, MatchOr(
    Terran.Wraith,
    Terran.Ghost,
    Protoss.DarkTemplar,
    Protoss.Arbiter,
    Zerg.Lurker
  )))

  def update() {
    if ( ! With.self.isZerg) {
      return
    }
    if (With.self.hasUpgrade(Zerg.OverlordSpeed)) {
      return
    }
    if (cloakedThreat.apply) {
      return
    }
    overlords.acquire(this)
    overlords.units.foreach(chillOut(_, overlords.units.size))
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int) {
    val base = ByOption.minBy(With.geography.ourBases.map(_.heart.center))(overlord.pixelDistanceSquared)
    val tile = base.map(b => PurpleMath.sample(Circle.points(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    overlord.agent.intend(this, new Intention {
      toTravel = Some(tile.center)
      canFlee = true
    })
  }
}
