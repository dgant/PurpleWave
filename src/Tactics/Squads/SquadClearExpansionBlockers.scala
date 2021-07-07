package Tactics.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchMobileDetector, MatchOr}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.{Minutes, Seconds}

import scala.util.Random

class SquadClearExpansionBlockers extends Squad {
  
  val detectors = new LockUnits(this)
  detectors.matcher = MatchMobileDetector
  detectors.counter = CountOne

  val clearers = new LockUnits(this)
  clearers.matcher = MatchOr(Terran.Marine, Terran.Firebat, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk)
  clearers.counter = CountOne

  def recruit(): Unit = {
    if (With.frame < Minutes(6)()) return
    if ( ! With.enemies.exists(_.isZerg) && ! With.enemies.exists(_.isTerran)) return
    if ( ! With.enemies.exists(_.isZerg) &&   With.enemies.forall(With.unitsShown(_, Terran.SpiderMine) == 0)) return

    val target = With.units.ours
      .find(u => u.intent.toBuild.exists(_.isTownHall) && ! u.unitClass.isBuilding)
      .flatMap(_.intent.toBuildTile.map(_.topLeftPixel))

    if (target.isEmpty) {
      detectors.release()
      clearers.release()
      return
    }

    vicinity = target.get
    detectors.preference = PreferClose(vicinity)
    addUnits(detectors.acquire(this))

    if (With.enemies.exists(_.isZerg) || detectors.units.forall(_.framesToTravelTo(vicinity) > Seconds(5)())) {
      clearers.preference = PreferClose(vicinity)
      addUnits(clearers.acquire(this))
    } else {
      clearers.release()
    }
  }

  def run(): Unit = {
    if (units.isEmpty) return
    targetQueue = Some(SquadAutomation.rankedEnRouteTo(units, vicinity))
    detectors.units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity.add(64, 48)) }))
    clearers.units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity.add(Random.nextInt(192) - 96, Random.nextInt(160) - 80)) }))
  }
}
