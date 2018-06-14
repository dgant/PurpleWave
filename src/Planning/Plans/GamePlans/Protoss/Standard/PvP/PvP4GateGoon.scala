package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.BuildRequest
import Planning.Predicates.Compound.Latch
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Employing
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen4GateGoon

class PvP4GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvPOpen4GateGoon)
  override val completionCriteria : Predicate = new Latch(new MiningBasesAtLeast(2))
  override def defaultAttackPlan  : Plan = new PvPIdeas.AttackSafely
  override def defaultScoutPlan   : Plan = new ScoutOn(Protoss.Pylon)
  override val defaultWorkerPlan  : Plan = NoPlan()
  override def emergencyPlans: Seq[Plan] = Vector(new PvPIdeas.ReactToDarkTemplarEmergencies, new PvPIdeas.ReactToTwoGate)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.Opening_4GateDragoon
  
  override val buildPlans = Vector(
    new If(
      new UnitsAtLeast(20, Protoss.Dragoon),
      new RequireMiningBases(2)),
    new Pump(Protoss.Dragoon)
  )
    
}
