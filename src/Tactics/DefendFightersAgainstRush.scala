package Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchWarriors, MatchWorker}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Seconds

class DefendFightersAgainstRush extends Prioritized {
  
  val defenders = new LockUnits(this)
  defenders.matcher = MatchWorker

  private def inOurBase(unit: UnitInfo): Boolean = unit.zone.bases.exists(_.owner.isUs)
  def update() {
    lazy val fighters     = With.units.ours .filter(u => u.unitClass.canMove && u.canAttack && ! u.unitClass.isWorker && inOurBase(u) && u.remainingCompletionFrames < Seconds(5)())
    lazy val cannons      = With.units.ours .filter(u => u.aliveAndComplete && u.isAny(Terran.Bunker, Protoss.PhotonCannon))
    lazy val aggressors   = With.units.enemy.filter(u => u.aliveAndComplete && u.is(MatchWarriors) && inOurBase(u))
    lazy val workers      = With.units.ours .filter(u => u.aliveAndComplete && u.unitClass.isWorker)
    lazy val threatening  = aggressors.filter(_.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200))
    if ( ! fingerprintsRequiringFighterProtection.exists(_.matches)) return
    if (fighters.isEmpty)     return
    if (fighters.size > 4)    return
    if (cannons.nonEmpty)     return
    if (aggressors.isEmpty)   return
    if (threatening.isEmpty)  return
    
    val workersNeeded   = 1 + 3 * aggressors.size - 3 * fighters.filter(_.complete).map(_.unitClass.mineralValue).sum / 100.0
    val workerCap       = workers.size - 4
    val workersToFight  = Maff.clamp(workersNeeded, 0, workerCap)
    val target          = fighters.minBy(fighter => aggressors.map(_.pixelDistanceEdge(fighter)).min).pixel
    
    defenders.counter = CountUpTo(workersToFight.toInt)
    defenders.preference = PreferClose(target)
    defenders.acquire(this)
    defenders.units.foreach(_.intend(this, new Intention {
      toTravel = Some(target)
      canFlee = false
    }))
  }

  private lazy val fingerprintsRequiringFighterProtection = Seq(
    With.fingerprints.fourPool,
    With.fingerprints.ninePool,
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113)
}
