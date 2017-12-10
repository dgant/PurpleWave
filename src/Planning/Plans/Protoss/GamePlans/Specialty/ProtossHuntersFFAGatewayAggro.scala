package Planning.Plans.Protoss.GamePlans.Specialty

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Always
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsExactly, UpgradeComplete}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.ForgeFastExpand
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAGatewayAggro extends GameplanModeTemplate {
  
  override val activationCriteria   : Plan = new Always
  override def defaultPlacementPlan : Plan = new ForgeFastExpand
  override val defaultScoutPlan     : Plan = NoPlan()
  override val aggression = 0.6
  
  override val buildOrder = ProtossBuilds.Opening_10Gate12Gas14Core
  
  private class CanBuildZealots extends Or(
    new UnitsExactly(0, Protoss.CyberneticsCore, complete = true),
    new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(RequestUpgrade(Protoss.HighTemplarEnergy))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(RequestUpgrade(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(RequestUpgrade(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(RequestTech(Protoss.Stasis))),
    new TrainContinuously(Protoss.Arbiter,      2),
    new TrainContinuously(Protoss.Observer,     2),
    new TrainContinuously(Protoss.Reaver,       2),
    new TrainContinuously(Protoss.HighTemplar,  8,  2),
    new TrainContinuously(Protoss.DarkTemplar,  2,  1),
    new TrainContinuously(Protoss.Dragoon,      20, 6),
    new TrainContinuously(Protoss.Zealot),
    new Build(
      RequestAtLeast(3, Protoss.Gateway)),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)),
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(2, Protoss.Forge)),
    new BuildCannonsAtExpansions(3),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      RequestAtLeast(12, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.ArbiterTribunal),
      RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBasesFFA(4),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(5)
  )
}
