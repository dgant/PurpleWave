package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound.Trigger
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT1015DT

class PvT1015GateDT extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT1015DT)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val superSaturate      = true
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.PvT1015GateGoonDT
  override def defaultScoutPlan   = new ScoutOn(Protoss.Gateway, quantity = 2)
  
  override val buildPlans = Vector(
    new EjectScout,
    new Trigger(
      new UnitsAtLeast(2, Protoss.DarkTemplar),
      new RequireMiningBases(2)),
    new PvTIdeas.TrainArmy,
    new RequireMiningBases(2))
}

