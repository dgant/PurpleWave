package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
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
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.PhotonCannon),
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(2,   Protoss.PhotonCannon),
    GetAtLeast(1,   Protoss.Gateway))
  
  private class CanBuildZealots extends Or(
    new UnitsExactly(0, Protoss.CyberneticsCore, complete = true),
    new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(GetUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon), new Build(GetUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(GetTech(Protoss.Stasis))),
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
          GetAtLeast(3, Protoss.PhotonCannon),
          GetAtLeast(1, Protoss.Assimilator),
          GetAtLeast(1, Protoss.CyberneticsCore),
          GetAtLeast(4, Protoss.Gateway),
          GetAtLeast(3, Protoss.Nexus)),
        new BuildGasPumps(),
        new RequireMiningBases(2),
        new Build(
          GetAtLeast(1, Protoss.CitadelOfAdun),
          GetAtLeast(5, Protoss.Gateway),
          GetAtLeast(1, Protoss.TemplarArchives),
          GetAtLeast(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor),
        new Build(
          GetUpgrade(Protoss.ZealotSpeed),
          GetAtLeast(1, Protoss.Stargate),
          GetAtLeast(4, Protoss.Nexus),
          GetAtLeast(1, Protoss.FleetBeacon),
          GetAtLeast(2, Protoss.CyberneticsCore),
          GetAtLeast(3, Protoss.Stargate)),
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new BuildCannonsAtExpansions(3), // In addition to earlier
        new RequireMiningBases(3),
        new Build(
          GetAtLeast(8, Protoss.Gateway),
          GetAtLeast(4, Protoss.Stargate),
          GetAtLeast(1, Protoss.RoboticsFacility),
          GetAtLeast(1, Protoss.Observatory)),
        new RequireMiningBases(5))),
      new Build(
        GetAtLeast(1, Protoss.RoboticsSupportBay),
        GetAtLeast(1, Protoss.ArbiterTribunal),
        GetAtLeast(10, Protoss.Gateway),
        GetAtLeast(5, Protoss.Stargate)),
      new RequireMiningBases(5),
      new UpgradeContinuously(Protoss.Shields),
      new Build(GetAtLeast(15, Protoss.Gateway)),
      new RequireMiningBases(6)
  )
}
