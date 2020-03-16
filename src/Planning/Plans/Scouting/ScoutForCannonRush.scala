package Planning.Plans.Scouting

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchNotHoldingResources, UnitMatchWorkers}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.Protoss

class ScoutForCannonRush extends Plan {
  val scouts = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchAnd(UnitMatchWorkers, UnitMatchNotHoldingResources))
    unitCounter.set(UnitCountOne)
    interruptable.set(false)
  })

  lazy val previouslyCannonRushed: Boolean = With.strategy.enemyFingerprints(5).contains(With.fingerprints.cannonRush.toString)

  private val maxScoutDistance: Int = 32 * 25
  lazy val tilesToScout: Array[Tile] = With.geography.allTiles.filter(tile => {
    val i = tile.i
    (
      With.grids.buildableTerrain.getUnchecked(tile.i)
      && With.geography.ourMain.zone.distanceGrid.getUnchecked(i)     < maxScoutDistance
      && With.geography.ourNatural.zone.distanceGrid.getUnchecked(i)  < maxScoutDistance
    )
  })

  override def onUpdate(): Unit = {
    val gettingCannonRushed = With.fingerprints.cannonRush.matches || (
      With.fingerprints.earlyForge.matches
      && ! With.fingerprints.forgeFe.matches
      && ! With.fingerprints.gatewayFirst.matches)

    var shouldScout = (
      previouslyCannonRushed
        && ! With.units.existsEnemy(UnitMatchAnd(Protoss.PhotonCannon, UnitMatchComplete))
        && ! With.fingerprints.gatewayFirst.matches
        && With.frame > GameTime(1, 30)()
        && With.frame < GameTime(6, 0)())
    shouldScout = shouldScout || (gettingCannonRushed && With.frame < GameTime(10, 0)())

    if ( ! shouldScout) return

    scouts.get.unitPreference.set(UnitPreferClose(
      scouts.get.units.headOption.map(_.pixelCenter).getOrElse(With.geography.home.pixelCenter)))
    scouts.get.acquire(this)
    scouts.get.units.foreach(scout => scout.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      toScoutTiles = tilesToScout
    }))
  }
}
