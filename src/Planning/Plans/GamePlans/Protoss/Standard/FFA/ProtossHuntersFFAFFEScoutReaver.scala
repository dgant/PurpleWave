package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Predicates.Always
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Placement.BuildCannonsAtExpansions
import Planning.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAFFEScoutReaver extends GameplanTemplate {
  
  override val activationCriteria   : Predicate = new Always
  override def placementPlan : Plan = new PlacementForgeFastExpand
  override val scoutPlan     : Plan = NoPlan()
  override def aggressionPlan: Plan = new Aggression(0.6)
  
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
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(Get(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(Get(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(Get(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(5, Protoss.Zealot),       new Build(Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon),  new Build(Get(Protoss.ScoutSpeed))),
    new If(new UnitsAtLeast(8, Protoss.Scout),        new Build(Get(Protoss.ScoutVisionRange))),
    new BuildCannonsAtExpansions(4),
    new RequireMiningBases(2),
    new Pump(Protoss.HighTemplar,  6,  1),
    new FlipIf(
      new UnitsAtLeast(4, Protoss.Reaver),
      new Pump(Protoss.Reaver,     8),
      new Parallel(
        new Pump(Protoss.Shuttle, 1),
        new Pump(Protoss.Scout))),
    new Pump(Protoss.DarkTemplar,  2,  1),
    new Pump(Protoss.Dragoon,      8,  4),
    new Pump(Protoss.Zealot),
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
