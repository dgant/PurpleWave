package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Plan
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

class ProtossHuntersFFAFFEGateway extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new PlacementForgeFastExpand
  override val defaultScoutPlan     : Plan = NoPlan()
  override val aggression = 0.8
  
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
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(GetUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(GetTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(GetUpgrade(Protoss.HighTemplarEnergy))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(GetUpgrade(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(GetUpgrade(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(GetUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(GetTech(Protoss.Stasis))),
    new BuildCannonsAtExpansions(2),
    new RequireMiningBases(2),
    new TrainContinuously(Protoss.Arbiter,      2),
    new TrainContinuously(Protoss.Observer,     2),
    new TrainContinuously(Protoss.HighTemplar,  8,  2),
    new TrainContinuously(Protoss.DarkTemplar,  2,  1),
    new TrainContinuously(Protoss.Shuttle,      2,  1),
    new TrainContinuously(Protoss.Dragoon,      20, 6),
    new TrainContinuously(Protoss.Zealot),
    new Build(
      GetAtLeast(3, Protoss.PhotonCannon),
      GetAtLeast(1, Protoss.Assimilator),
      GetAtLeast(1, Protoss.CyberneticsCore),
      GetAtLeast(4, Protoss.Gateway)),
    new BuildGasPumps(),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      GetAtLeast(1, Protoss.CitadelOfAdun),
      GetAtLeast(1, Protoss.TemplarArchives),
      GetAtLeast(6, Protoss.Gateway),
      GetAtLeast(1, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.Observatory)),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      GetAtLeast(10, Protoss.Gateway),
      GetAtLeast(1, Protoss.Stargate),
      GetAtLeast(1, Protoss.ArbiterTribunal),
      GetAtLeast(20, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(5)
  )
}
