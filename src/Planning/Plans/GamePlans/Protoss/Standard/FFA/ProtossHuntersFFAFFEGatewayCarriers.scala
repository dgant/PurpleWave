package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Always
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAFFEGatewayCarriers extends GameplanModeTemplate {
  
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
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new Build(GetUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(GetTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirDamage)),
    new If(new UnitsAtLeast(3, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirArmor)),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(GetUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(GetUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(GetUpgrade(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(GetUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(GetTech(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new TrainContinuously(Protoss.Arbiter,        2, 1),
    new TrainContinuously(Protoss.Observer,       2),
    new TrainContinuously(Protoss.Reaver,         2),
    new TrainContinuously(Protoss.Carrier),
    new TrainContinuously(Protoss.DarkTemplar,    1),
    new If(
      new UnitsAtLeast(8, UnitMatchWarriors),
      new TrainContinuously(Protoss.HighTemplar,  4, 1)),
    new TrainContinuously(Protoss.Dragoon,        12, 6),
    new TrainContinuously(Protoss.Zealot),
    new Build(
      GetAtLeast(3, Protoss.PhotonCannon),
      GetAtLeast(1, Protoss.Assimilator),
      GetAtLeast(1, Protoss.CyberneticsCore),
      GetAtLeast(4, Protoss.Gateway)),
    new BuildGasPumps(),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      GetAtLeast(1, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.RoboticsSupportBay),
      GetAtLeast(1, Protoss.CitadelOfAdun),
      GetAtLeast(1, Protoss.TemplarArchives),
      GetAtLeast(1, Protoss.Observatory),
      GetAtLeast(1, Protoss.Stargate),
      GetAtLeast(5, Protoss.PhotonCannon),
      GetAtLeast(1, Protoss.FleetBeacon),
      GetAtLeast(2, Protoss.CyberneticsCore),
      GetAtLeast(3, Protoss.Stargate)),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      GetAtLeast(1, Protoss.ArbiterTribunal),
      GetAtLeast(5, Protoss.Stargate)),
    new Build(GetAtLeast(12, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(4)
  )
}
