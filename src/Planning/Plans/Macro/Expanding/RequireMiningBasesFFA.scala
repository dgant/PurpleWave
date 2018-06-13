package Planning.Plans.Macro.Expanding

import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plans.Compound._
import Planning.Plans.Predicates.OnMap
import Planning.Plans.Predicates.Milestones.{MiningBasesAtMost, UnitsAtLeast}
import Strategery.Hunters

class RequireMiningBasesFFA(bases: Int = 1) extends If(
  // Expand, but be very careful about it if we're on The Hunters/BGH
  new Or(
    new MiningBasesAtMost(2),
    new And(
      new UnitsAtLeast(50, UnitMatchWorkers),
      new UnitsAtLeast(20, UnitMatchWarriors)),
    new Not(new OnMap(Hunters))),
  new RequireMiningBases(bases))