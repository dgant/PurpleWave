package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchNotHoldingResources, MatchWorker}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class ScoutForCannonRush extends Plan {
  val scouts = new LockUnits(this)
  scouts.matcher = MatchAnd(MatchWorker, MatchNotHoldingResources)
  scouts.counter = CountOne
  scouts.interruptable = false

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
        && ! With.units.existsEnemy(MatchAnd(Protoss.PhotonCannon, MatchComplete))
        && ! With.fingerprints.gatewayFirst.matches
        && With.frame > GameTime(1, 30)()
        && With.frame < GameTime(6, 0)())
    shouldScout = shouldScout || (gettingCannonRushed && With.frame < GameTime(10, 0)())

    if ( ! shouldScout) return

    scouts.preference = PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.pixelCenter))
    scouts.acquire(this)
    scouts.units.foreach(scout => scout.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      toScoutTiles = tilesToScout
    }))
  }
}
