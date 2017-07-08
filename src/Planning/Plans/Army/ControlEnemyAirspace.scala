package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.{UnitCountBetween, UnitCountExactly}
import Planning.Composition.UnitMatchers.UnitMatchMobileFlying
import Planning.Composition.UnitPreferences.UnitPreferFast
import Planning.Plan

class ControlEnemyAirspace extends Plan {
  
  description.set("Fly over enemy bases")
  
  val flyers = new Property[LockUnits](new LockUnits {
    unitCounter.set(UnitCountExactly(0))
    unitMatcher.set(UnitMatchMobileFlying)
    unitPreference.set(UnitPreferFast)
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def onUpdate() {
    flyers.get.unitCounter.set(new UnitCountBetween(0, With.geography.enemyBases.size))
    flyers.get.acquire(this)
    val unassignedScouts = flyers.get.units.toBuffer
    
    With.geography.enemyBases
      .toList
      .sortBy(_.lastScoutedFrame)
      .foreach(base => if (unassignedScouts.nonEmpty) {
        val basePixel = base.heart.pixelCenter
        val scout = unassignedScouts.minBy(_.pixelDistanceFast(basePixel))
        unassignedScouts -= scout
        With.executor.intend(new Intention(this, scout) {
          toTravel = Some(basePixel)
          canCower = true
        })
      })
  }
}
