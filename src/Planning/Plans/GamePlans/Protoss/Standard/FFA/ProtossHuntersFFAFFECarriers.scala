package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
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

class ProtossHuntersFFAFFECarriers extends GameplanTemplate {
  
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
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(Get(Protoss.ZealotSpeed))),
    new RequireMiningBases(2),
    new Pump(Protoss.Arbiter,        2, 1),
    new Pump(Protoss.Observer,       2, 1),
    new Pump(Protoss.Reaver,         4, 1),
    new Pump(Protoss.Carrier),
    new Build(
      Get(3, Protoss.PhotonCannon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)),
    new BuildGasPumps(),
    new BuildCannonsAtExpansions(4),
    new Build(
      Get(8, Protoss.PhotonCannon),
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Stargate),
      Get(1, Protoss.RoboticsSupportBay),
      Get(1, Protoss.FleetBeacon),
      Get(2, Protoss.CyberneticsCore),
      Get(3, Protoss.Stargate)),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new RequireMiningBasesFFA(3),
    new Build(
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(1, Protoss.ArbiterTribunal),
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory)),
    new RequireMiningBasesFFA(4),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      Get(5, Protoss.Stargate),
      Get(12, Protoss.Gateway)),
    new RequireMiningBasesFFA(5)
  )
}
