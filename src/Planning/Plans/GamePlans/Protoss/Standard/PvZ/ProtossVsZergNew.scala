package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyMutalisks
import Planning.Plans.Predicates.SafeAtHome
import ProxyBwapi.Races.{Protoss, Zerg}

class ProtossVsZergNew extends GameplanModeTemplate {
  
  override def aggression: Double = 0.9
  
  private class TrainAndUpgradeArmy extends Parallel(
    new If(
      new EnemyHasShownCloakedThreat,
      new TrainContinuously(Protoss.Observer, 3),
      new Parallel()),
    
    // Emergency Dragoons
    new If(
      new EnemyMutalisks,
      new Parallel(
        new TrainMatchingRatio(Protoss.Corsair, 3, 8,   Seq(MatchingRatio(Zerg.Mutalisk, 0.9))),
        new TrainMatchingRatio(Protoss.Dragoon, 0, 10,  Seq(MatchingRatio(Zerg.Mutalisk, 1.25))),
        new TrainContinuously(Protoss.Stargate, 1))),
  
    // Upgrades
    new UpgradeContinuously(Protoss.GroundDamage),
    new If(new UnitsAtLeast(1, Protoss.Corsair), new UpgradeContinuously(Protoss.AirDamage)),
    new If(
      new Or(
        new UnitsAtLeast(2, Protoss.Forge),
        new UpgradeComplete(Protoss.GroundDamage, 3)),
      new UpgradeContinuously(Protoss.GroundArmor)),
    
    // Basic army
    new TrainContinuously(Protoss.DarkTemplar, 1),
    new IfOnMiningBases(2, new TrainContinuously(Protoss.Reaver, 6)),
    new TrainContinuously(Protoss.Observer, 1),
    new If(
      new Check(() => With.units.ours.count(_.is(Protoss.Dragoon)) < With.units.ours.count(_.is(Protoss.Zealot)) / 3),
      new TrainContinuously(Protoss.Dragoon, maximumConcurrentlyRatio = 0.5)),
    new If(
      new Or(
        new UnitsAtMost(10, Protoss.HighTemplar),
        new UnitsAtMost(8, Protoss.Archon)),
      new TrainContinuously(Protoss.HighTemplar, 20, 3)),
    new BuildCannonsAtExpansions(5),
    new TrainContinuously(Protoss.Zealot),
    new If(
      new And(
        new UnitsAtMost(8, Zerg.Hydralisk),
        new UnitsAtMost(1, Zerg.SporeColony)),
      new TrainContinuously(Protoss.Corsair, 6))
  )
  
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
    new IfOnMiningBases(3,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(2, Protoss.Forge),
        RequestUpgrade(Protoss.HighTemplarEnergy),
        RequestAtLeast(1, Protoss.RoboticsSupportBay),
        RequestUpgrade(Protoss.ScarabDamage))))
  
  override def buildPlans: Seq[Plan] = Vector(
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new FlipIf(
      new SafeAtHome,
      new TrainAndUpgradeArmy,
      new Parallel(
        new AddHighPriorityTech,
        new PvZIdeas.TakeSafeThirdBase,
        new PvZIdeas.TakeSafeFourthBase)),
    new AddLowPriorityTech,
    new PvZIdeas.AddGateways
  )
}
