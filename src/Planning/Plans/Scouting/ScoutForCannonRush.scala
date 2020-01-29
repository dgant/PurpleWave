package Planning.Plans.Scouting

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchNotHoldingResources, UnitMatchWorkers}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}

class ScoutForCannonRush extends Plan {
  val scouts = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchAnd(UnitMatchWorkers, UnitMatchNotHoldingResources))
    unitCounter.set(UnitCountOne)
    interruptable.set(false)
  })

  lazy val previouslyCannonRushed: Boolean = With.strategy.enemyFingerprints(5).contains(With.fingerprints.cannonRush.toString)

  override def onUpdate(): Unit = {
    val gettingCannonRushed = With.fingerprints.cannonRush.matches || (
      With.fingerprints.earlyForge.matches
      && ! With.fingerprints.forgeFe.matches
      && ! With.fingerprints.gatewayFirst.matches)

    var shouldScout = (
      previouslyCannonRushed
        && ! With.fingerprints.gatewayFirst.matches
        && With.frame > GameTime(1, 30)()
        && With.frame < GameTime(6, 0)())
    shouldScout = shouldScout || (gettingCannonRushed && With.frame < GameTime(10, 0)())

    if ( ! shouldScout) return

    val basesToScout = Seq(With.geography.ourMain, With.geography.ourNatural)
    scouts.get.unitPreference.set(UnitPreferClose(
      scouts.get.units.headOption.map(_.pixelCenter).getOrElse(With.geography.home.pixelCenter)))
    scouts.get.acquire(this)
    scouts.get.units.foreach(scout => scout.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canScout = true
      toScoutBases = basesToScout
    }))
  }
}
