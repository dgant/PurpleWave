package Tactic.Squads

import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsAny, IsMobileDetector}
import Utilities.UnitPreferences.PreferClose

import scala.util.Random

class SquadClearExpansionBlockers extends Squad {
  
  val detectorLock: LockUnits = new LockUnits(this, IsMobileDetector, CountOne)
  val sweeperLock : LockUnits = new LockUnits(this, IsAny(Terran.Marine, Terran.Firebat, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk), CountOne)

  def launch(): Unit = {
    if (With.frame < Minutes(6)()) return
    if ( ! With.enemies.exists(_.isZerg) && ! With.enemies.exists(_.isTerran)) return
    if ( ! With.enemies.exists(_.isZerg) &&   With.enemies.forall(With.unitsShown(_, Terran.SpiderMine) == 0)) return

    val target = With.units.ours
      .find(u => u.intent.toBuild.exists(_.isTownHall) && ! u.unitClass.isBuilding)
      .flatMap(_.intent.toBuildTile.map(_.topLeftPixel))

    if (target.isEmpty) {
      detectorLock.release()
      sweeperLock.release()
      return
    }

    vicinity = target.get
    detectorLock.setPreference(PreferClose(vicinity)).acquire()

    if ((detectorLock.units.nonEmpty && With.enemies.exists(_.isZerg)) || detectorLock.units.forall(_.framesToTravelTo(vicinity) > Seconds(5)())) {
      sweeperLock.setPreference(PreferClose(vicinity)).acquire()
    } else {
      sweeperLock.release()
    }
  }

  def run(): Unit = {
    if (units.isEmpty) return
    setTargets(SquadAutomation.rankedAround(this))
    detectorLock.units.foreach(_.intend(this).setTerminus(vicinity.add(64, 48)))
    sweeperLock.units.foreach(_.intend(this).setTerminus(vicinity.add(Random.nextInt(192) - 96, Random.nextInt(160) - 80)))
  }
}
