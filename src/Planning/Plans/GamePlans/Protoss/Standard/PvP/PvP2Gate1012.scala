package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Aggression
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2Gate1012

class PvP2Gate1012 extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen2Gate1012)
  override val completionCriteria : Plan      = new Latch(new MiningBasesAtLeast(2))
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely
  override val defaultScoutPlan   : Plan      = new ScoutOn(Protoss.Pylon)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    // If they have goons, we need goons
    new If(
      new Or(
        new EnemyUnitsAtLeast(1, Protoss.Dragoon),
        new EnemyUnitsAtLeast(1, Protoss.CyberneticsCore, complete = true)),
      new Build(
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore))),
    new PvPIdeas.ReactToFFE,
    new PvPIdeas.ReactToExpansion
  )
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012
  override def buildPlans = Vector(
    new If(
      new And(
        new EnemyUnitsAtLeast(1, Protoss.Dragoon),
        new EnemyUnitsAtMost(3, UnitMatchWarriors),
        new EnemyUnitsAtMost(1, Protoss.Zealot),
        new UnitsAtMost(0, Protoss.Dragoon),
        new UnitsAtMost(0, Protoss.Reaver)),
      new Aggression(3.0),
      new Aggression(0.9)),
    new PvPIdeas.TakeBase2,
    new TrainContinuously(Protoss.Reaver, 2),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(4, UnitMatchWarriors),
        new Or(
          new EnemyHasShown(Protoss.Dragoon),
          new EnemyHasShown(Protoss.Assimilator),
          new EnemyHasShown(Protoss.CyberneticsCore))),
      new PvPIdeas.TrainDragoonsOrZealots,
      new Build(
        RequestAtLeast(2, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.RoboticsSupportBay))),
    
    new RequireMiningBases(2),
    new Build(RequestAtLeast(4, Protoss.Gateway))
  )
}
