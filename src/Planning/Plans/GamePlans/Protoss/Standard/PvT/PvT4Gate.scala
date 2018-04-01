package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{Or, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvTEarly4Gate

class PvT4Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly4Gate)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))
  override val aggression         = 1.4
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening_4GateDragoon
  
  override val buildPlans = Vector(
    new Trigger(
      new Or(
        new MineralsAtLeast(800),
        new UnitsAtLeast(20, UnitMatchWarriors),
        new EnemyUnitsAtLeast(5, UnitMatchSiegeTank)),
      new RequireMiningBases(2)),
    new TrainContinuously(Protoss.Dragoon))
}

