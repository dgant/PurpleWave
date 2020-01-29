package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, GasCapsUntouched, Pump}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchInNatural, UnitMatchSiegeTank}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT1015Expand

class PvT1015Expand extends GameplanTemplate {

  class ShouldDoubleExpand extends EnemyStrategy(With.fingerprints.siegeExpand)
  
  override val activationCriteria = new Employing(PvT1015Expand)
  override val completionCriteria = new Or(
    new UnitsAtLeast(1, Protoss.Observatory),
    new UnitsAtLeast(3, Protoss.Nexus),
    new And(new UnitsAtLeast(2, Protoss.Nexus), new Not(new ShouldDoubleExpand)))

  override val removeMineralBlocksAt: Int = 20
  override def scoutPlan = new If(new UpgradeStarted(Protoss.DragoonRange), new Scout)
  override val attackPlan = new Attack
  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.PvT1015GateGoon

  override def aggressionPlan: Plan = new If(
    new And(
      new UpgradeComplete(Protoss.DragoonRange),
      new Not(new EnemyHasShown(Terran.SiegeTankSieged)),
      new Not(new EnemyStrategy(With.fingerprints.twoFac))),
    new Aggression(1.25))

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvTIdeas.ReactToFiveRaxAs2GateCore,
    new PvTIdeas.ReactToWorkerRush)
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtMost(3, Protoss.Dragoon, complete = true),
      new EjectScout),

    new Trigger(
      new And(
        new GasCapsUntouched,
        new UnitsAtLeast(1, Protoss.Dragoon),
        new GasForUpgrade(Protoss.DragoonRange)),
      new CapGasWorkersAt(2)),

    new If(new ShouldDoubleExpand, new RequireMiningBases(3)),

    new If(
      new Or(
        new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE),
        new And(
          new EnemiesAtLeast(1, UnitMatchAnd(Terran.Bunker, UnitMatchInNatural)),
          new EnemiesAtMost(0, UnitMatchSiegeTank))),
      new Pump(Protoss.Dragoon)),

    new Pump(Protoss.Dragoon, 6),
    new RequireMiningBases(2),
    new Pump(Protoss.Dragoon)
  )
}
