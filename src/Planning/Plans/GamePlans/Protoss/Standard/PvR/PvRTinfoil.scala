package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.GamePlans.Protoss.Situational.{DefendFFEWithProbesAgainst4Pool, DefendFightersAgainstEarlyPool}
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpShuttleAndReavers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Compound.And
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvRTinfoil2018

class PvRTinfoil extends GameplanTemplateVsRandom {
  
  override val activationCriteria = new Employing(PvRTinfoil2018)
  override def scoutPlan   = NoPlan()

  override def attackPlan = new If(new UnitsAtLeast(6, Protoss.Gateway, complete = true), new ConsiderAttacking)

  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(Protoss.Forge),
    Get(10, Protoss.Probe),
    Get(2, Protoss.PhotonCannon),
    Get(12, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(13, Protoss.Probe),
    Get(3, Protoss.PhotonCannon),
    Get(14, Protoss.Probe))
  
  override def buildPlans = Vector(
    new CapGasAt(300),
    new DefendFightersAgainstEarlyPool,
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fourPool),
        new FrameAtLeast(GameTime(2, 5)()),
        new FrameAtMost(GameTime(5, 0)()),
        new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
        new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
      new DefendFFEWithProbesAgainst4Pool),
    new Pump(Protoss.Observer, 1),
    new PumpShuttleAndReavers(4),
    new UpgradeContinuously(Protoss.ShuttleSpeed),
    new If(
      new And(
        new UnitsAtLeast(1, Protoss.Observer, complete = true),
        new UnitsAtLeast(2, Protoss.Reaver, complete = true)),
      new RequireMiningBases(2)),
    new If(
      new And(
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.Dragoon.buildFrames),
        new GasAtLeast(50)),
      new Pump(Protoss.Dragoon),
      new Pump(Protoss.Zealot)),
    new BuildOrder(
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(4, Protoss.PhotonCannon),
      Get(1, Protoss.RoboticsFacility),
      Get(5, Protoss.PhotonCannon),
      Get(1, Protoss.RoboticsSupportBay),
      Get(6, Protoss.PhotonCannon),
      Get(Protoss.DragoonRange),
      Get(1, Protoss.Observatory)),
    new If(
      new UnitsAtLeast(1, Protoss.Observatory),
      new Build(Get(4, Protoss.Gateway))),
    new Build(Get(Protoss.GroundDamage)),
    new RequireMiningBases(2),
    new IfOnMiningBases(2,
      new Parallel(
        new Build(Get(6, Protoss.Gateway)),
        new BuildGasPumps,
        new RequireMiningBases(3),
        new Build(Get(12, Protoss.Gateway))))
  )
}
