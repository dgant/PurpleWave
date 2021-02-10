package Planning.Plans.Army

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones.EnemiesAtLeast
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.MatchOr
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class ChillOverlords extends Prioritized {
  
  val overlords = new LockUnits
  overlords.unitMatcher.set(Zerg.Overlord)
  overlords.unitCounter.set(UnitCountEverything)

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
    if (cloakedThreat.isComplete) {
      return
    }
    overlords.acquire(this)
    overlords.units.foreach(chillOut(_, overlords.units.size))
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int) {
    val base = ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(overlord.pixelDistanceSquared)
    val tile = base.map(b => PurpleMath.sample(Circle.points(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    val intent = new Intention
    intent.toTravel = Some(tile.pixelCenter)
    intent.canFlee = true
    overlord.agent.intend(this, intent)
  }
}
