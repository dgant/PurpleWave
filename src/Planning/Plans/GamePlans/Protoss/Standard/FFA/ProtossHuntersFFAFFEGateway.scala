package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.{Plan, Predicate}
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

class ProtossHuntersFFAFFEGateway extends GameplanTemplate {
  
  override val activationCriteria   : Predicate = new Always
  override def placementPlan : Plan = new PlacementForgeFastExpand
  override val scoutPlan     : Plan = NoPlan()
  override def aggressionPlan: Plan = new Aggression(0.8)
  
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
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(Get(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(Get(Protoss.HighTemplarEnergy))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(Get(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(Get(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(Get(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new Pump(Protoss.Arbiter,      2),
    new Pump(Protoss.Observer,     2),
    new Pump(Protoss.HighTemplar,  8,  2),
    new Pump(Protoss.DarkTemplar,  2,  1),
    new Pump(Protoss.Shuttle,      2,  1),
    new Pump(Protoss.Dragoon,      20, 6),
    new Pump(Protoss.Zealot),
    new Build(
      Get(3, Protoss.PhotonCannon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(4, Protoss.Gateway)),
    new BuildGasPumps(),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(6, Protoss.Gateway),
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory)),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      Get(10, Protoss.Gateway),
      Get(1, Protoss.Stargate),
      Get(1, Protoss.ArbiterTribunal),
      Get(20, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(5)
  )
}
