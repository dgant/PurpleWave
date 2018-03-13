package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1015GateDTs

class PvP1015GateGoonExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan  = new Employing(PvPOpen1015GateDTs)
  override val completionCriteria : Plan  = new Latch(new MiningBasesAtLeast(2))
  override def defaultAttackPlan  : Plan  = new PvPIdeas.AttackSafely
  override def defaultScoutPlan   : Plan  = new ScoutOn(Protoss.Gateway, quantity = 2)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToExpansion,
    new PvPIdeas.ReactToFFE)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  override def buildPlans = Vector(
    new FlipIf(
      new And(
        new UnitsAtLeast(5, Protoss.Dragoon),
        new Or(
          new UnitsAtLeast(8, Protoss.Dragoon),
          new SafeAtHome)),
      new Parallel(
        new TrainContinuously(Protoss.Dragoon),
        new Build(RequestAtLeast(4, Protoss.Gateway)),
      new RequireMiningBases(2)))
  )
}
