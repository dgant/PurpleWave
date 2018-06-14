package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Predicates.Always
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAFFEScoutReaver extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new PlacementForgeFastExpand
  override val defaultScoutPlan     : Plan = NoPlan()
  override val aggression = 0.6
  
  override val buildOrder = Vector(
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.PhotonCannon),
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(2,   Protoss.PhotonCannon),
    GetAtLeast(1,   Protoss.Gateway))
  
  override def priorityAttackPlan: Plan = new Attack(Protoss.Scout)
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Parallel(
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(GetUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(GetTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(GetUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(GetUpgrade(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(5, Protoss.Zealot),       new Build(GetUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon),  new Build(GetUpgrade(Protoss.ScoutSpeed))),
    new If(new UnitsAtLeast(8, Protoss.Scout),        new Build(GetUpgrade(Protoss.ScoutVisionRange))),
    new BuildCannonsAtExpansions(4),
    new RequireMiningBases(2),
    new TrainContinuously(Protoss.HighTemplar,  6,  1),
    new FlipIf(
      new UnitsAtLeast(4, Protoss.Reaver),
      new TrainContinuously(Protoss.Reaver,     8),
      new Parallel(
        new TrainContinuously(Protoss.Shuttle, 1),
        new TrainContinuously(Protoss.Scout))),
    new TrainContinuously(Protoss.DarkTemplar,  2,  1),
    new TrainContinuously(Protoss.Dragoon,      8,  4),
    new TrainContinuously(Protoss.Zealot),
    new Build(
      GetAtLeast(3, Protoss.PhotonCannon),
      GetAtLeast(1, Protoss.Assimilator),
      GetAtLeast(1, Protoss.CyberneticsCore),
      GetAtLeast(2, Protoss.Gateway)),
    new BuildGasPumps(),
    new Build(
      GetAtLeast(1, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.RoboticsSupportBay),
      GetAtLeast(2, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.CitadelOfAdun),
      GetAtLeast(1, Protoss.TemplarArchives),
      GetAtLeast(3, Protoss.Stargate),
      GetAtLeast(3, Protoss.Gateway),
      GetAtLeast(1, Protoss.Observatory),
      GetAtLeast(1, Protoss.FleetBeacon),
      GetAtLeast(2, Protoss.CyberneticsCore)),
    new RequireMiningBasesFFA(3),
    new Build(
      GetAtLeast(5, Protoss.Stargate),
      GetAtLeast(3, Protoss.RoboticsFacility),
      GetAtLeast(12, Protoss.Gateway)),
    new RequireMiningBasesFFA(4),
    new UpgradeContinuously(Protoss.Shields)
  )
}
