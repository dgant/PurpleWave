package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Aggression
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.BuildHuggingNexus
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZ4Gate99, PvZ4GateDragoonAllIn}

class PvZ4Gate extends GameplanModeTemplate {
  
  override val activationCriteria     = new Employing(PvZ4GateDragoonAllIn)
  override val completionCriteria     = new Latch(new MiningBasesAtLeast(2))
  override val scoutExpansionsAt      = 90
  override def buildOrder             = ProtossBuilds.OpeningTwoGate1012
  override def defaultWorkerPlan      = NoPlan()
  override def defaultScoutPlan       = new ScoutOn(Protoss.Pylon)
  override def defaultPlacementPlan   = new BuildHuggingNexus
  override def defaultAggressionPlan  = new If(
    new UnitsAtMost(8, UnitMatchWarriors, complete = true),
    new Aggression(1.2),
    new If(
      new UnitsAtMost(10, UnitMatchWarriors, complete = true),
      new Aggression(1.4),
      new If(
        new UnitsAtMost(15, UnitMatchWarriors, complete = true),
        new Aggression(2.0),
        new Aggression(3.0))))
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new EnemyUnitsAtMost(0, UnitMatchWarriors),
      new UnitsAtLeast(4, UnitMatchWarriors, complete = true)),
    super.defaultAttackPlan)
  
  override def buildPlans = Vector(
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(2, Protoss.Observer))),
  
    new Trigger(
      new UnitsAtLeast(24, UnitMatchWarriors),
      new RequireMiningBases(2)),
  
    new If(
      new GasAtLeast(150),
      new UpgradeContinuously(Protoss.DragoonRange)),
    
    new If(
      new And(
        new GasAtLeast(50),
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeFrames),
        new Or(
          new UnitsAtLeast(15, Protoss.Zealot),
          new Check(() => With.units.ours.count(_.is(Protoss.Zealot)) > With.units.ours.count(_.is(Protoss.Dragoon))),
          new Check(() => With.units.enemy.count(_.is(Zerg.Mutalisk)) * 1.5 > With.units.ours.count(_.is(Protoss.Dragoon))))),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
    new TrainWorkersContinuously,
    
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore), // Of course, the Assimilator SHOULD go first but then we mine too much gas from it
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(4, Protoss.Gateway)),
    
    new Trigger(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new RequireMiningBases(2))
  )
}

class PvZ4Gate99 extends PvZ4Gate {
  override val activationCriteria = new Employing(PvZ4Gate99)
  override def defaultScoutPlan   = new ScoutOn(Protoss.Gateway, quantity = 2)
  override def buildOrder         = ProtossBuilds.OpeningTwoGate99
}