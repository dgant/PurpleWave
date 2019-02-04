package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Plan
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.Macro.Automatic.Pump
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpenZCoreZ

class PvRZCoreZ extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpenZCoreZ)
  override val completionCriteria = new UnitsAtLeast(2, UnitMatchWarriors)
  override val buildOrder         = ProtossBuilds.ZCoreZ

  override def buildPlans: Vector[Plan] = Vector(
    new DefendFightersAgainstEarlyPool,
    new Pump(Protoss.Dragoon)
  )
}
