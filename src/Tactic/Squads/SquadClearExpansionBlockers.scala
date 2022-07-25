package Tactic.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsMobileDetector, IsAny}
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.{Minutes, Seconds}

import scala.util.Random

class SquadClearExpansionBlockers extends Squad {
  
  val detectorLock = new LockUnits(this)
  detectorLock.matcher = IsMobileDetector
  detectorLock.counter = CountOne

  val sweeperLock = new LockUnits(this)
  sweeperLock.matcher = IsAny(Terran.Marine, Terran.Firebat, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk)
  sweeperLock.counter = CountOne

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
    detectorLock.preference = PreferClose(vicinity)
    detectorLock.acquire()

    if ((detectorLock.units.nonEmpty && With.enemies.exists(_.isZerg)) || detectorLock.units.forall(_.framesToTravelTo(vicinity) > Seconds(5)())) {
      sweeperLock.preference = PreferClose(vicinity)
      sweeperLock.acquire()
    } else {
      sweeperLock.release()
    }
  }

  def run(): Unit = {
    if (units.isEmpty) return
    targets = Some(SquadAutomation.rankedAround(this))
    detectorLock.units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity.add(64, 48)) }))
    sweeperLock.units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity.add(Random.nextInt(192) - 96, Random.nextInt(160) - 80)) }))
  }
}
