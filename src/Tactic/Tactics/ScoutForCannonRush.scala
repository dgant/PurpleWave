package Tactic.Tactics

import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchNotHoldingResources, MatchWorker}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class ScoutForCannonRush extends Tactic {
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

  def launch(): Unit = {
    val gettingCannonRushed = With.fingerprints.cannonRush() || (
      With.fingerprints.earlyForge()
      && ! With.fingerprints.forgeFe()
      && ! With.fingerprints.gatewayFirst()
      && With.geography.enemyBases.size < 2)

    var shouldScout = (
      previouslyCannonRushed
        && With.enemies.exists(_.isUnknownOrProtoss)
        && ! With.units.existsEnemy(MatchAnd(Protoss.PhotonCannon, MatchComplete))
        && ! With.fingerprints.gatewayFirst()
        && With.frame > GameTime(1, 30)()
        && With.frame < GameTime(6, 0)())
    shouldScout = shouldScout || (gettingCannonRushed && With.frame < GameTime(10, 0)())

    if ( ! shouldScout) return

    scouts.preference = PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.center))
    scouts.acquire()
    scouts.units.foreach(scout => scout.intend(this, new Intention {
      toTravel = Some(With.geography.home.center)
      toScoutTiles = tilesToScout
    }))
  }
}
