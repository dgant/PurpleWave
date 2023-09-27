package Tactic.Tactics

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.GameTime
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsAll, IsComplete}
import Utilities.UnitPreferences.PreferClose

class ScoutForCannonRush extends Tactic {

  val scouts: LockUnits = new LockUnits(this, (u:  UnitInfo) => u.unitClass.isWorker && ! u.carrying, CountOne, interruptable = false)

  lazy val previouslyCannonRushed: Boolean = With.strategy.enemyFingerprints(5).contains(With.fingerprints.cannonRush.toString)

  private val maxScoutDistance: Int = 32 * 25
  lazy val tilesToScout: Vector[Tile] = With.geography.allTiles.filter(tile => tile.buildableUnchecked && (tile.metro.exists(_.isOurs) || With.geography.ourFoyer.zone.distanceGrid.getUnchecked(tile.i)  < maxScoutDistance))

  def launch(): Unit = {
    val gettingCannonRushed = With.fingerprints.cannonRush() || (
      With.fingerprints.earlyForge()
      && ! With.fingerprints.forgeFe()
      && ! With.fingerprints.gatewayFirst()
      && With.geography.enemyBases.size < 2)

    var shouldScout = (
      previouslyCannonRushed
        && With.enemies.exists(_.isUnknownOrProtoss)
        && ! With.units.existsEnemy(IsAll(Protoss.PhotonCannon, IsComplete))
        && ! With.fingerprints.gatewayFirst()
        && With.frame > GameTime(1, 30)()
        && With.frame < GameTime(6, 0)())
    shouldScout ||=  (gettingCannonRushed && With.frame < GameTime(10, 0)())

    if ( ! shouldScout) return

    scouts
      .setPreference(PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.center)))
      .acquire()
      .foreach(_.intend(this).setTerminus(With.geography.home.center).setScout(tilesToScout))
  }
}
