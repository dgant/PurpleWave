package Tactic.Tactics.WorkerPulls

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class PullForFighters extends WorkerPull {
  private lazy val fingerprintsRequiringFighterProtection = Seq(
    With.fingerprints.workerRush,
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113,
    With.fingerprints.proxyGateway,
    With.fingerprints.twoGate99,
    With.fingerprints.fourPool,
    With.fingerprints.ninePool,
    With.fingerprints.fiveRax)

  val minersNeeded        = 4
  lazy val fighters       = With.units.ours .filter(u => u.complete && IsWarrior(u) && u.base.exists(_.isOurs))
  lazy val cannons        = With.units.ours .filter(u => u.complete && u.isAny(Terran.Bunker, Protoss.PhotonCannon))
  lazy val aggressors     = With.units.enemy.filter(u => u.complete && u.base.exists(_.isOurs) && (IsWarrior(u) || With.fingerprints.workerRush()))
  lazy val workers        = With.units.ours .filter(u => u.complete && IsWorker(u))
  lazy val threatening    = aggressors.filter(a => a.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200) && a.base.exists(b => b.isOurs && b.harvestingArea.expand(2, 2).contains(a.pixel)))
  lazy val workersNeeded  = if (skip) 0 else 1 + 3 * (aggressors.map(_.unitClass.mineralValue).sum - fighters.map(_.unitClass.mineralValue).sum) / 100.0
  lazy val workersToFight = Maff.clamp(workersNeeded, 0, workers.size - minersNeeded)
  lazy val target         = fighters.minBy(fighter => aggressors.map(_.pixelDistanceEdge(fighter)).min).pixel

  var skip = false
  skip ||= With.frame > Minutes(6)()
  skip ||= ! fingerprintsRequiringFighterProtection.exists(_())
  skip ||= fighters.isEmpty
  skip ||= fighters.size > 9
  skip ||= cannons.nonEmpty
  skip ||= threatening.isEmpty

  override def apply(): Int = workersToFight.toInt
  override def minRemaining: Int = minersNeeded

  override def employ(defenders: Seq[FriendlyUnitInfo]): Unit = {
    defenders.foreach(_.intend(this, new Intention {
      toTravel = Some(target)
      targets = Some(aggressors.toVector)
      canFlee = false
    }))
  }
}
