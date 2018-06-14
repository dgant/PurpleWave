package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get, Tech}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, BuildCannonsAtExpansions}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast, MineralsAtMost}
import ProxyBwapi.Races.Protoss

class MassPhotonCannon extends GameplanModeTemplate {
  
  // Maybe?
  // override def defaultWorkerPlan: Plan = new TrainWorkersContinuously(oversaturate = true)
  
  override val buildOrder: Seq[BuildRequest] =
    if (With.enemy.isTerran)
      Vector(
        Get(8, Protoss.Probe),
        Get(1, Protoss.Pylon),
        Get(14, Protoss.Probe),
        Get(1, Protoss.Nexus),
        Get(1, Protoss.Gateway),
        Get(1, Protoss.Forge),
        Get(1, Protoss.Zealot),
        Get(1, Protoss.PhotonCannon))
    else if (With.enemy.isProtoss)
      Vector(
        Get(8, Protoss.Probe),
        Get(1, Protoss.Pylon),
        Get(10, Protoss.Probe),
        Get(1, Protoss.Forge),
        Get(12, Protoss.Probe),
        Get(1, Protoss.PhotonCannon),
        Get(14, Protoss.Probe),
        Get(2, Protoss.PhotonCannon),
        Get(16, Protoss.Probe),
        Get(2, Protoss.Nexus),
        Get(1, Protoss.Gateway))
    else
      Vector(
        Get(8, Protoss.Probe),
        Get(1, Protoss.Pylon),
        Get(9, Protoss.Probe),
        Get(1, Protoss.Forge),
        Get(10, Protoss.Probe),
        Get(2, Protoss.PhotonCannon),
        Get(12, Protoss.Probe),
        Get(2, Protoss.Nexus),
        Get(3, Protoss.PhotonCannon))
  
  private def pylonCount = With.units.countOurs(Protoss.Pylon)
  private def cannonCount = With.units.countOurs(Protoss.PhotonCannon)
  
  override def scoutExpansionsAt: Int = 400
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(40, Protoss.Interceptor),
      new SupplyOutOf200(190)),
    new Attack)
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Tech(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(3, Protoss.Reaver),
      new UpgradeContinuously(Protoss.ScarabDamage)),
    new If(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new UpgradeContinuously(Protoss.DragoonRange)),
    new If(
      new UnitsAtLeast(8, Protoss.Zealot),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new TrainContinuously(Protoss.HighTemplar, 12),
    new TrainContinuously(Protoss.Carrier),
    new TrainContinuously(Protoss.Reaver),
    new If(
      new Or(
        new UnitsAtMost(0, Protoss.FleetBeacon),
        new MineralsAtLeast(400)),
      new If(
        new Or(
          new UnitsAtMost(0, Protoss.CyberneticsCore, complete = true),
          new Check(() => With.self.minerals > 3 * With.self.gas)),
        new TrainContinuously(Protoss.Zealot),
        new TrainContinuously(Protoss.Dragoon))),
    new If(
      new Or(
        new UnitsAtLeast(12, Protoss.PhotonCannon),
        new UnitsAtLeast(5, Protoss.Reaver)),
      new RequireMiningBases(2)),
    new BuildCannonsAtExpansions(10),
    new If(
      new Or(
        new UnitsAtLeast(30, Protoss.PhotonCannon),
        new UnitsAtLeast(40, Protoss.Interceptor)),
      new RequireMiningBases(3)),
    new If(
      new Or(
        new UnitsAtLeast(50, Protoss.PhotonCannon),
        new UnitsAtLeast(60, Protoss.Interceptor)),
      new RequireMiningBases(4)),
    new BuildCannonsAtBases(4),
    new FlipIf(
      new Check(() => cannonCount >= Math.min(
        With.units.countOurs(Protoss.Probe) / 5,
        With.geography.ourBases.size * 6)),
      new Parallel(
        new If(
          new Check(() =>
            pylonCount * 4 < cannonCount
            && (cannonCount < 4 || With.units.existsOurs(Protoss.CyberneticsCore))),
          new TrainContinuously(Protoss.Pylon, 200, 2)),
        new TrainContinuously(Protoss.PhotonCannon, 400, 6)),
      new Parallel(
        new Build(Get(1, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(Get(1, Protoss.CyberneticsCore)),
        new If(
          new EnemyHasShownCloakedThreat,
          new Build(
            Get(1, Protoss.RoboticsFacility),
            Get(1, Protoss.Observatory),
            Get(2, Protoss.Observer))),
        new If(
          new And(
            new MineralsAtMost(400),
            new GasAtLeast(500)),
          new Build(
            Get(1, Protoss.CitadelOfAdun),
            Get(1, Protoss.TemplarArchives),
            Get(2, Protoss.Gateway))),
        new Build(
          Get(1, Protoss.RoboticsFacility),
          Get(1, Protoss.RoboticsSupportBay),
          Get(1, Protoss.Stargate),
          Get(1, Protoss.FleetBeacon),
          Get(1, Protoss.Stargate)),
        new IfOnMiningBases(2,
          new Parallel(
            new Build(
              Get(2, Protoss.Stargate),
              Get(2, Protoss.Gateway),
              Get(3, Protoss.Stargate)),
            new UpgradeContinuously(Protoss.AirDamage))),
        new IfOnMiningBases(3,
          new Parallel(
            new UpgradeContinuously(Protoss.AirArmor),
            new Build(
              Get(2, Protoss.CyberneticsCore),
              Get(2, Protoss.RoboticsFacility),
              Get(4, Protoss.Stargate),
              Get(3, Protoss.Gateway))),
        new IfOnMiningBases(4,
          new Parallel(
            new UpgradeContinuously(Protoss.Shields),
            new Build(Get(8, Protoss.Stargate))),
        new RequireMiningBases(4)
      ))))
  )
}
