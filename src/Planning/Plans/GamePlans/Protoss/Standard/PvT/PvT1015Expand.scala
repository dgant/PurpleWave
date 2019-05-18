package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeStarted}
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchInNatural}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT1015Expand, PvT1015TripleExpand}

class PvT1015Expand extends GameplanTemplate {

  class FactoryAggression extends EnemyStrategy(
    With.fingerprints.twoFac,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFac,
    With.fingerprints.threeFacVultures)
  
  override val activationCriteria = new Employing(PvT1015Expand, PvT1015TripleExpand)
  override val completionCriteria = new Or(
    new UnitsAtLeast(1, Protoss.Observatory),
    new UnitsAtLeast(4, Protoss.Nexus),
    new And(
      new UnitsAtLeast(3, Protoss.Nexus),
      new Not(new Employing(PvT1015TripleExpand))))
  override def scoutPlan = new If(new UpgradeStarted(Protoss.DragoonRange), new Scout)
  override val attackPlan = new Attack
  
  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.PvT1015GateGoonExpand

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvTIdeas.ReactToFiveRaxAs2GateCore,
    new PvTIdeas.ReactToWorkerRush)
  
  override def buildPlans: Seq[Plan] = Vector(
    new Trigger(
      new Or(
        new EnemyBasesAtLeast(2),
        new EnemiesAtLeast(1, UnitMatchAnd(Terran.Bunker, UnitMatchInNatural))),
      new RequireMiningBases(3)),

    new If(
      new FactoryAggression,
      new Parallel(
        new Pump(Protoss.Dragoon),
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(3, Protoss.Gateway)))),
    new Pump(Protoss.Dragoon, 5),
    new If(
      new Employing(PvT1015TripleExpand),
      new RequireMiningBases(4),
      new RequireMiningBases(3)),
    new If(
      new Not(new SafeAtHome),
      new Pump(Protoss.Dragoon))
  )
}
