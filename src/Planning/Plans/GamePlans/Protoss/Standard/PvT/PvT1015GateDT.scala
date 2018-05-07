package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvTEarly1015GateGoonDT

class PvT1015GateDT extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1015GateGoonDT)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val superSaturate      = true
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening10Gate15GateDragoons
  
  override def scoutAt: Int = super.scoutAt
  
  override val buildPlans = Vector(
    new If(
      new UnitsAtLeast(2, Protoss.DarkTemplar),
      new RequireMiningBases(2)),
    new FlipIf(
      new UnitsAtLeast(5, Protoss.Dragoon),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives)))),
    new RequireMiningBases(2))
}

