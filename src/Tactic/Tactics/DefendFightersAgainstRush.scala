package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.UnitPreferences.PreferClose

class DefendFightersAgainstRush extends Tactic {
  
  val defenders = new LockUnits(this)
  defenders.matcher = IsWorker

  private def inOurBase(unit: UnitInfo): Boolean = unit.base.exists(_.isOurs)
  def launch(): Unit = {
    lazy val fighters     = With.units.ours .filter(u => u.complete && IsWarrior(u) && u.base.exists(b => b.isOurs && b.harvestingArea.expand(1, 1).contains(u.pixel)))
    lazy val cannons      = With.units.ours .filter(u => u.complete && u.isAny(Terran.Bunker, Protoss.PhotonCannon))
    lazy val aggressors   = With.units.enemy.filter(u => u.complete && inOurBase(u) && (IsWarrior(u) || With.fingerprints.workerRush()))
    lazy val workers      = With.units.ours .filter(u => u.complete && IsWorker(u))
    lazy val threatening  = aggressors.filter(_.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200))
    if ( ! fingerprintsRequiringFighterProtection.exists(_())) return
    if (fighters.isEmpty)     return
    if (fighters.size > 9)    return
    if (cannons.nonEmpty)     return
    if (threatening.isEmpty)  return
    
    val workersNeeded   = 1 + 3 * (aggressors.map(_.unitClass.mineralValue).sum - fighters.map(_.unitClass.mineralValue).sum) / 100.0
    val workerCap       = workers.size - 4
    val workersToFight  = Maff.clamp(workersNeeded, 0, workerCap)
    val target          = fighters.minBy(fighter => aggressors.map(_.pixelDistanceEdge(fighter)).min).pixel
    
    defenders.counter = CountUpTo(workersToFight.toInt)
    defenders.preference = PreferClose(target)
    defenders.acquire()
    defenders.units.foreach(_.intend(this, new Intention {
      toTravel = Some(target)
      canFlee = false
    }))
  }

  private lazy val fingerprintsRequiringFighterProtection = Seq(
    With.fingerprints.workerRush,
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113,
    With.fingerprints.proxyGateway,
    With.fingerprints.twoGate99,
    With.fingerprints.fourPool,
    With.fingerprints.ninePool,
    With.fingerprints.fiveRax)
}
