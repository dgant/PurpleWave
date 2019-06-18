package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Predicates.Always
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Placement.BuildCannonsAtExpansions
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAFFEGatewayCarriers extends GameplanTemplate {
  
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
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new Build(Get(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirDamage)),
    new If(new UnitsAtLeast(3, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirArmor)),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(Get(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(Get(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(Get(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(Get(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new Pump(Protoss.Arbiter,        2, 1),
    new Pump(Protoss.Observer,       2),
    new Pump(Protoss.Reaver,         2),
    new Pump(Protoss.Carrier),
    new Pump(Protoss.DarkTemplar,    1),
    new If(
      new UnitsAtLeast(8, UnitMatchWarriors),
      new Pump(Protoss.HighTemplar,  4, 1)),
    new Pump(Protoss.Dragoon,        12, 6),
    new Pump(Protoss.Zealot),
    new Build(
      Get(3, Protoss.PhotonCannon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(4, Protoss.Gateway)),
    new BuildGasPumps(),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.RoboticsSupportBay),
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(1, Protoss.Observatory),
      Get(1, Protoss.Stargate),
      Get(5, Protoss.PhotonCannon),
      Get(1, Protoss.FleetBeacon),
      Get(2, Protoss.CyberneticsCore),
      Get(3, Protoss.Stargate)),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      Get(1, Protoss.ArbiterTribunal),
      Get(5, Protoss.Stargate)),
    new Build(Get(12, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(4)
  )
}
