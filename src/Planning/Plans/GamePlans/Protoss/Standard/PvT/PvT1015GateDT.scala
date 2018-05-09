package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.Army.Attack
import Planning.Plans.Compound.Trigger
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvTEarly1015GateGoonDT

class PvT1015GateDT extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1015GateGoonDT)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val superSaturate      = true
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening10Gate15GateDragoonDT
  
  override def scoutAt: Int = super.scoutAt
  
  override val buildPlans = Vector(
    new Trigger(
      new UnitsAtLeast(2, Protoss.DarkTemplar),
      new RequireMiningBases(2)),
    new PvTIdeas.TrainArmy,
    new RequireMiningBases(2))
}

