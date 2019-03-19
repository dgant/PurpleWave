package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeStarted}
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchInNatural}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT1015Expand

class PvT1015Expand extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvT1015Expand)
  override val completionCriteria = new Or(new UnitsAtLeast(3, Protoss.Nexus), new UnitsAtLeast(1, Protoss.Observatory))
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
    new Pump(Protoss.Dragoon),
    new If(
      new EnemyStrategy(With.fingerprints.twoFac, With.fingerprints.twoFacVultures),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(3, Protoss.Gateway))),
    new RequireMiningBases(3)
  )
}
