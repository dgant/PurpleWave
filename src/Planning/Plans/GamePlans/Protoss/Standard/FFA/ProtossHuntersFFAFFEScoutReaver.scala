package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{Get, Tech, Upgrade}
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
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(1,   Protoss.Forge),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.PhotonCannon),
    Get(18,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon),
    Get(1,   Protoss.Gateway))
  
  override def priorityAttackPlan: Plan = new Attack(Protoss.Scout)
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Parallel(
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(Upgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(Tech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(Upgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(Upgrade(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(5, Protoss.Zealot),       new Build(Upgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon),  new Build(Upgrade(Protoss.ScoutSpeed))),
    new If(new UnitsAtLeast(8, Protoss.Scout),        new Build(Upgrade(Protoss.ScoutVisionRange))),
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
      Get(3, Protoss.PhotonCannon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(2, Protoss.Gateway)),
    new BuildGasPumps(),
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.RoboticsSupportBay),
      Get(2, Protoss.RoboticsFacility),
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(3, Protoss.Stargate),
      Get(3, Protoss.Gateway),
      Get(1, Protoss.Observatory),
      Get(1, Protoss.FleetBeacon),
      Get(2, Protoss.CyberneticsCore)),
    new RequireMiningBasesFFA(3),
    new Build(
      Get(5, Protoss.Stargate),
      Get(3, Protoss.RoboticsFacility),
      Get(12, Protoss.Gateway)),
    new RequireMiningBasesFFA(4),
    new UpgradeContinuously(Protoss.Shields)
  )
}
