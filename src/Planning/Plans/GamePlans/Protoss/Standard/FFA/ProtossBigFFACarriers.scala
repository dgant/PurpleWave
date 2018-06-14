package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{Get, Tech, Upgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Always
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtMost}
import Planning.Plans.Predicates.Milestones.{UnitsAtLeast, UnitsExactly, UpgradeComplete}
import ProxyBwapi.Races.Protoss

class ProtossBigFFACarriers extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new PlacementForgeFastExpand
  override val defaultScoutPlan     : Plan = NoPlan()
  override val aggression = 0.6
  
  override val buildOrder = Vector(
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.PhotonCannon),
    Get(18,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon),
    Get(1,   Protoss.Gateway))
  
  private class CanBuildZealots extends Or(
    new UnitsExactly(0, Protoss.CyberneticsCore, complete = true),
    new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(Upgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon), new Build(Upgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(Tech(Protoss.Stasis))),
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
          new Or(new GasAtLeast(300), new MineralsAtMost(1000)))),
      new Parallel(
        new If(
          new CanBuildZealots,
          new TrainContinuously(Protoss.Zealot,   24)),
          new TrainContinuously(Protoss.Dragoon,  24)),
      new Parallel(
        new Build(
          Get(3, Protoss.PhotonCannon),
          Get(1, Protoss.Assimilator),
          Get(1, Protoss.CyberneticsCore),
          Get(4, Protoss.Gateway),
          Get(3, Protoss.Nexus)),
        new BuildGasPumps(),
        new RequireMiningBases(2),
        new Build(
          Get(1, Protoss.CitadelOfAdun),
          Get(5, Protoss.Gateway),
          Get(1, Protoss.TemplarArchives),
          Get(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor),
        new Build(
          Upgrade(Protoss.ZealotSpeed),
          Get(1, Protoss.Stargate),
          Get(4, Protoss.Nexus),
          Get(1, Protoss.FleetBeacon),
          Get(2, Protoss.CyberneticsCore),
          Get(3, Protoss.Stargate)),
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new BuildCannonsAtExpansions(3), // In addition to earlier
        new RequireMiningBases(3),
        new Build(
          Get(8, Protoss.Gateway),
          Get(4, Protoss.Stargate),
          Get(1, Protoss.RoboticsFacility),
          Get(1, Protoss.Observatory)),
        new RequireMiningBases(5))),
      new Build(
        Get(1, Protoss.RoboticsSupportBay),
        Get(1, Protoss.ArbiterTribunal),
        Get(10, Protoss.Gateway),
        Get(5, Protoss.Stargate)),
      new RequireMiningBases(5),
      new UpgradeContinuously(Protoss.Shields),
      new Build(Get(15, Protoss.Gateway)),
      new RequireMiningBases(6)
  )
}
