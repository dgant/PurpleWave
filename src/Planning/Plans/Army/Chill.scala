package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Circle
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.FriendlyUnitInfo


class Chill(initialMatcher: UnitMatcher) extends Plan {
  
  val chillers = new LockUnits(this)
  chillers.matcher = initialMatcher
  chillers.counter = CountEverything

  override def onUpdate() {
    chillers.acquire()
    chillers.units.foreach(chillOut(_, chillers.units.size))
  }
  
  private def chillOut(chiller: FriendlyUnitInfo, count: Int) {
    val base = Maff.minBy(With.geography.ourBases.map(_.heart.center))(chiller.pixelDistanceTravelling)
    val tile = base.map(b => Maff.sample(Circle.points(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    val intent = new Intention
    intent.toTravel = Some(tile.center)
    chiller.intend(this, intent)
  }
}
