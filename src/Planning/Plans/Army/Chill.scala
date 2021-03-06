package Planning.Plans.Army

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class Chill(initialMatcher: UnitMatcher) extends Plan {
  
  val chillers = new LockUnits(this)
  chillers.matcher = initialMatcher
  chillers.counter = CountEverything

  override def onUpdate() {
    chillers.acquire(this)
    chillers.units.foreach(chillOut(_, chillers.units.size))
  }
  
  private def chillOut(chiller: FriendlyUnitInfo, count: Int) {
    val base = ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(chiller.pixelDistanceTravelling)
    val tile = base.map(b => PurpleMath.sample(Circle.points(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    val intent = new Intention
    intent.toTravel = Some(tile.pixelCenter)
    intent.canFlee = true
    chiller.agent.intend(this, intent)
  }
}
