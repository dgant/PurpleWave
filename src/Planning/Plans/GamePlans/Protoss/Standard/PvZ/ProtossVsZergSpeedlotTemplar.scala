package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZMidgameCorsairSpeedlot

class ProtossVsZergSpeedlotTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgameCorsairSpeedlot)
  override def aggression: Double = 0.75
  
  class AddHighPriorityTech extends Parallel(
    new If(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new UpgradeContinuously(Protoss.DragoonRange)),
    new If(
      new UnitsAtLeast(2, Protoss.HighTemplar),
      new Build(RequestTech(Protoss.PsionicStorm))))
  
  class AddLowPriorityTech extends Parallel(
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new IfOnMiningBases(2,
      new Build(
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestUpgrade(Protoss.ZealotSpeed),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestAtLeast(1, Protoss.Stargate),
        RequestTech(Protoss.PsionicStorm),
        RequestAtLeast(3, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory))),
    new UpgradeContinuously(Protoss.ObserverSpeed),
    new IfOnMiningBases(3,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(2, Protoss.Forge),
        RequestUpgrade(Protoss.HighTemplarEnergy),
        RequestAtLeast(1, Protoss.RoboticsSupportBay),
        RequestUpgrade(Protoss.ScarabDamage))))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.ReactToLurkers,
    new PvZIdeas.ReactToMutalisks
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new FlipIf(
      new SafeAtHome,
      new PvZIdeas.TrainAndUpgradeArmy,
      new Parallel(
        new AddHighPriorityTech,
        new PvZIdeas.TakeSafeThirdBase,
        new PvZIdeas.TakeSafeFourthBase,
        new IfOnMiningBases(4, new AddLowPriorityTech))),
    new PvZIdeas.AddGateways,
    new AddLowPriorityTech
  )
}
