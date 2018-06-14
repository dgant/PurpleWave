package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchInNatural}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Not, Or, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT1015Expand

class PvT1015Expand extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT1015Expand)
  override val completionCriteria = new Or(new UnitsAtLeast(3, Protoss.Nexus), new UnitsAtLeast(1, Protoss.Observatory))
  override def defaultScoutPlan   = new If(new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeFrames(1)), new Scout)
  override val defaultAttackPlan  = new Attack
  
  override val buildOrder = ProtossBuilds.Opening10Gate15GateExpand
  
  override def buildPlans: Seq[Plan] = Vector(
    new Trigger(
      new Or(
        new EnemyBasesAtLeast(2),
        new EnemiesAtLeast(1, UnitMatchAnd(Terran.Bunker, UnitMatchInNatural))),
      new RequireMiningBases(3)),
    new TrainContinuously(Protoss.Dragoon),
    new If(
      new Not(new SafeAtHome),
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory))),
    new RequireMiningBases(3)
  )
}
