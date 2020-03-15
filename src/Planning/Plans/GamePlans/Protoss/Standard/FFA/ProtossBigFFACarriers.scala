package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.Predicates.Compound.{And, Not}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Predicates.Always
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtMost}
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsExactly, UpgradeComplete}
import ProxyBwapi.Races.Protoss

class ProtossBigFFACarriers extends GameplanTemplate {
  
  override val activationCriteria   : Predicate = new Always
  override def placementPlan : Plan = new PlacementForgeFastExpand
  override val initialScoutPlan     : Plan = NoPlan()
  override def aggressionPlan: Plan = new Aggression(0.6)
  
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
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(Get(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon), new Build(Get(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(Get(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new Pump(Protoss.Arbiter,      3, 1),
    new Pump(Protoss.Observer,     2),
    new Pump(Protoss.Reaver,       2),
    new Pump(Protoss.DarkTemplar,  2),
    new Pump(Protoss.Carrier),
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new Or(
          new Not(new CanBuildZealots),
          new Or(new GasAtLeast(300), new MineralsAtMost(1000)))),
      new Parallel(
        new If(
          new CanBuildZealots,
          new Pump(Protoss.Zealot,   24)),
          new Pump(Protoss.Dragoon,  24)),
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
          Get(Protoss.ZealotSpeed),
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
