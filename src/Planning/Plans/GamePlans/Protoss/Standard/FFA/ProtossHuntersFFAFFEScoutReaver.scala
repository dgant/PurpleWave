package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.ForgeFastExpand
import Planning.Plans.Information.Always
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAFFEScoutReaver extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new ForgeFastExpand
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
  
  override def priorityAttackPlan: Plan = new Attack { attackers.get.unitMatcher.set(Protoss.Scout) }
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Parallel(
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor))),
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(RequestUpgrade(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(5, Protoss.Zealot),       new Build(RequestUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon),  new Build(RequestUpgrade(Protoss.ScoutSpeed))),
    new If(new UnitsAtLeast(8, Protoss.Scout),        new Build(RequestUpgrade(Protoss.ScoutVisionRange))),
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
      RequestAtLeast(3, Protoss.PhotonCannon),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway)),
    new BuildGasPumps(),
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(2, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(3, Protoss.Stargate),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.CyberneticsCore)),
    new RequireMiningBasesFFA(3),
    new Build(
      RequestAtLeast(5, Protoss.Stargate),
      RequestAtLeast(3, Protoss.RoboticsFacility),
      RequestAtLeast(12, Protoss.Gateway)),
    new RequireMiningBasesFFA(4),
    new UpgradeContinuously(Protoss.Shields)
  )
}
