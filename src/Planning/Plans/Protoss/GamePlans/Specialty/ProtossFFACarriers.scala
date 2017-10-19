package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Always
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsExactly, UpgradeComplete}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.Situational.ForgeFastExpand
import ProxyBwapi.Races.Protoss

class ProtossFFACarriers extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new ForgeFastExpand
  override val defaultScoutPlan     : Plan = NoPlan()
  override val aggression = 0.6
  
  override val buildOrder = Vector(
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.PhotonCannon),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.PhotonCannon),
    RequestAtLeast(1,   Protoss.Gateway))
  
  private class CanBuildZealots extends Or(
    new UnitsExactly(0, Protoss.CyberneticsCore, complete = true),
    new And(
      new UnitsAtLeast(8, Protoss.Dragoon),
      new UpgradeComplete(Protoss.ZealotSpeed, Protoss.Zealot.buildFrames)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon), new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(RequestTech(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new TrainContinuously(Protoss.Arbiter,      3, 1),
    new TrainContinuously(Protoss.Observer,     2),
    new TrainContinuously(Protoss.Reaver,       2),
    new TrainContinuously(Protoss.DarkTemplar,  2),
    new TrainContinuously(Protoss.Carrier),
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new Or(
          new Not(new CanBuildZealots),
          new Check(() => With.self.gas > 300 || With.self.minerals < 1000))),
      new Parallel(
        new If(
          new CanBuildZealots,
          new TrainContinuously(Protoss.Zealot,   24)),
          new TrainContinuously(Protoss.Dragoon,  24)),
      new Parallel(
        new Build(
          RequestAtLeast(3, Protoss.PhotonCannon),
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(3, Protoss.Nexus)),
        new BuildGasPumps(),
        new RequireMiningBases(2),
        new Build(
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(5, Protoss.Gateway),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor),
        new Build(
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(4, Protoss.Nexus),
          RequestAtLeast(1, Protoss.FleetBeacon),
          RequestAtLeast(2, Protoss.CyberneticsCore),
          RequestAtLeast(3, Protoss.Stargate)),
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new BuildCannonsAtExpansions(3), // In addition to earlier
        new RequireMiningBases(3),
        new Build(
          RequestAtLeast(8, Protoss.Gateway),
          RequestAtLeast(4, Protoss.Stargate),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)),
        new RequireMiningBases(5))),
      new Build(
        RequestAtLeast(1, Protoss.RoboticsSupportBay),
        RequestAtLeast(1, Protoss.ArbiterTribunal),
        RequestAtLeast(10, Protoss.Gateway),
        RequestAtLeast(5, Protoss.Stargate)),
      new RequireMiningBases(5),
      new UpgradeContinuously(Protoss.Shields),
      new Build(RequestAtLeast(15, Protoss.Gateway)),
      new RequireMiningBases(6)
  )
}
