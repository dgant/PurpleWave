package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
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
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.PhotonCannon),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.PhotonCannon),
    RequestAtLeast(1,   Protoss.Gateway))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirDamage)),
    new If(new UnitsAtLeast(3, Protoss.Carrier),      new UpgradeContinuously(Protoss.AirArmor)),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(RequestUpgrade(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(RequestUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(RequestTech(Protoss.Stasis))),
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
      RequestAtLeast(3, Protoss.PhotonCannon),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(4, Protoss.Gateway)),
    new BuildGasPumps(),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(5, Protoss.PhotonCannon),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.CyberneticsCore),
      RequestAtLeast(3, Protoss.Stargate)),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      RequestAtLeast(1, Protoss.ArbiterTribunal),
      RequestAtLeast(5, Protoss.Stargate)),
    new Build(RequestAtLeast(12, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(4)
  )
}
