package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.Army.Attack
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.MiningBasesAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1015GateGoonPressure

class PvT1015GatePressure extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1015GateGoonPressure)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val superSaturate      = true
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening10Gate15GateDragoons
  
  override val buildPlans = Vector(
    new TrainContinuously(Protoss.Dragoon),
    new RequireMiningBases(2))
}

