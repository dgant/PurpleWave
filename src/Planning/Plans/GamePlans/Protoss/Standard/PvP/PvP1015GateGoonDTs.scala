package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtMost, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1015GateDTs

class PvP1015GateGoonDTs extends GameplanModeTemplate {
  
  override val activationCriteria : Plan  = new Employing(PvPOpen1015GateDTs)
  override val completionCriteria : Plan  = new Latch(new MiningBasesAtLeast(2))
  override def priorityAttackPlan : Plan  = new PvPIdeas.AttackWithDarkTemplar
  override def defaultAttackPlan  : Plan  = new PvPIdeas.AttackSafely
  override def defaultScoutPlan   : Plan  = new ScoutOn(Protoss.Gateway, quantity = 2)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToFFE)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  private class EnemyNoMobileDetection extends And(
    new EnemyUnitsAtMost(0, Protoss.Observer),
    new EnemyUnitsAtMost(0, Protoss.Observatory))
    
  override def buildPlans = Vector(
    new FlipIf(
      new UnitsAtLeast(5, UnitMatchWarriors),
      new PvPIdeas.TrainArmy,
      new Parallel(
        new If(
          new EnemyNoMobileDetection,
          new BuildOrder(
            RequestAtLeast(1, Protoss.CitadelOfAdun),
            RequestAtLeast(1, Protoss.TemplarArchives),
            RequestAtLeast(2, Protoss.DarkTemplar))),
        new RequireMiningBases(2))
    ),
    new Build(RequestAtLeast(4, Protoss.Gateway))
  )
}
