package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtBases, BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Protoss, Zerg}

class MassPhotonCannon extends GameplanModeTemplate {
  
  // Maybe?
  // override def defaultWorkerPlan: Plan = new TrainWorkersContinuously(oversaturate = true)
  
  override val buildOrder: Seq[BuildRequest] =
    if (With.enemy.isTerran)
      Vector(
        RequestAtLeast(8, Protoss.Probe),
        RequestAtLeast(1, Protoss.Pylon),
        RequestAtLeast(14, Protoss.Probe),
        RequestAtLeast(1, Protoss.Nexus),
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(1, Protoss.Zealot),
        RequestAtLeast(1, Protoss.PhotonCannon))
    else if (With.enemy.isProtoss)
      Vector(
        RequestAtLeast(8, Protoss.Probe),
        RequestAtLeast(1, Protoss.Pylon),
        RequestAtLeast(10, Protoss.Probe),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(12, Protoss.Probe),
        RequestAtLeast(1, Protoss.PhotonCannon),
        RequestAtLeast(14, Protoss.Probe),
        RequestAtLeast(2, Protoss.PhotonCannon),
        RequestAtLeast(16, Protoss.Probe),
        RequestAtLeast(2, Protoss.Nexus),
        RequestAtLeast(1, Protoss.Gateway))
    else
      Vector(
        RequestAtLeast(8, Protoss.Probe),
        RequestAtLeast(1, Protoss.Pylon),
        RequestAtLeast(9, Protoss.Probe),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(10, Protoss.Probe),
        RequestAtLeast(2, Protoss.PhotonCannon),
        RequestAtLeast(12, Protoss.Probe),
        RequestAtLeast(2, Protoss.Nexus),
        RequestAtLeast(3, Protoss.PhotonCannon))
  
  private def pylonCount = With.units.ours.count(_.is(Protoss.Pylon))
  private def cannonCount = With.units.ours.count(_.is(Protoss.PhotonCannon))
  
  override def scoutExpansionsAt: Int = 400
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(40, Protoss.Interceptor),
      new Check(() => With.self.supplyUsed > 190 * 2)),
    new Attack)
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(RequestTech(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(3, Protoss.Reaver),
      new UpgradeContinuously(Protoss.ScarabDamage)),
    new If(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new UpgradeContinuously(Protoss.DragoonRange)),
    new If(
      new UnitsAtLeast(8, Protoss.Zealot),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new If(
      new Or(
        new EnemyUnitsAtLeast(1, Zerg.Scourge),
        new EnemyUnitsAtLeast(1, Zerg.Mutalisk)),
      new TrainContinuously(Protoss.Corsair, 8)),
    new TrainContinuously(Protoss.HighTemplar, 12),
    new TrainContinuously(Protoss.Carrier),
    new TrainContinuously(Protoss.Reaver),
    new If(
      new Or(
        new UnitsAtMost(0, Protoss.FleetBeacon),
        new Check(() => With.self.minerals > 400)),
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
        With.units.ours.count(_.is(Protoss.Probe)) / 5,
        With.geography.ourBases.size * 6)),
      new Parallel(
        new If(
          new Check(() => pylonCount * 4 < cannonCount),
          new TrainContinuously(Protoss.Pylon, 200, 2)),
        new TrainContinuously(Protoss.PhotonCannon, 400, 6)),
      new Parallel(
        new Build(RequestAtLeast(1, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
        new If(
          new EnemyHasShownCloakedThreat,
          new Build(
            RequestAtLeast(1, Protoss.RoboticsFacility),
            RequestAtLeast(1, Protoss.Observatory),
            RequestAtLeast(2, Protoss.Observer))),
        new If(
          new Check(() => With.self.minerals < 400 && With.self.gas > 500),
          new Build(
            RequestAtLeast(1, Protoss.CitadelOfAdun),
            RequestAtLeast(1, Protoss.TemplarArchives),
            RequestAtLeast(2, Protoss.Gateway))),
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.FleetBeacon),
          RequestAtLeast(1, Protoss.Stargate)),
        new IfOnMiningBases(2,
          new Parallel(
            new Build(
              RequestAtLeast(2, Protoss.Stargate),
              RequestAtLeast(2, Protoss.Gateway),
              RequestAtLeast(3, Protoss.Stargate)),
            new UpgradeContinuously(Protoss.AirDamage))),
        new IfOnMiningBases(3,
          new Parallel(
            new UpgradeContinuously(Protoss.AirArmor),
            new Build(
              RequestAtLeast(2, Protoss.CyberneticsCore),
              RequestAtLeast(2, Protoss.RoboticsFacility),
              RequestAtLeast(4, Protoss.Stargate),
              RequestAtLeast(3, Protoss.Gateway))),
        new IfOnMiningBases(4,
          new Parallel(
            new UpgradeContinuously(Protoss.Shields),
            new Build(RequestAtLeast(8, Protoss.Stargate))),
        new RequireMiningBases(4)
      ))))
  )
}
