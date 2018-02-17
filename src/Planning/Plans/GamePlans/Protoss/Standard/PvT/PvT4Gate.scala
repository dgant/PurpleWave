package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.Army.Attack
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.MiningBasesAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly4Gate

class PvT4Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly4Gate)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening10Gate15GateDragoons
  
  override val buildPlans = Vector(
    new RequireMiningBases(2),
    new TrainContinuously(Protoss.Dragoon))
}

