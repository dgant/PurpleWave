package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.Aggression
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.BuildHuggingNexus
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Economy.GasAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ.PvZ4GateDragoonAllIn

class PvZ4GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria     = new Employing(PvZ4GateDragoonAllIn)
  override val completionCriteria     = new MiningBasesAtLeast(2)
  override val scoutAt                = 10
  override def buildOrder             = ProtossBuilds.OpeningTwoGate1012
  override def defaultPlacementPlan   = new BuildHuggingNexus
  override def defaultAggressionPlan  = new If(
    new UnitsAtMost(12, UnitMatchWarriors),
    new Aggression(1.0),
    new If(
      new UnitsAtMost(20, UnitMatchWarriors),
      new Aggression(3.0),
      new Aggression(5.0)))
  
  override def buildPlans = Vector(
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(2, Protoss.Observer))),
    
    new If(
      new And(
        new GasAtLeast(50),
        new UnitsAtLeast(4, Protoss.Zealot),
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeTime(1))),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
    new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    
    new Build(RequestAtLeast(4, Protoss.Gateway)),
    
    new Trigger(
      new UnitsAtLeast(12, UnitMatchWarriors),
      initialAfter = new Parallel(
        new RequireMiningBases(2),
        new Build(RequestAtLeast(8, Protoss.Gateway)),
        new RequireMiningBases(3),
        new Build(RequestAtLeast(12, Protoss.Gateway))))
  )
}

