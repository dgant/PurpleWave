package Planning.Plans.Macro.Expanding

import Planning.Predicates.Compound.{And, Not, Or}
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Planning.Plans.Compound._
import Planning.Predicates.Milestones.{MiningBasesAtMost, UnitsAtLeast}
import Planning.Predicates.Strategy.OnMap
import Strategery.Hunters

class RequireMiningBasesFFA(bases: Int = 1) extends If(
  // Expand, but be very careful about it if we're on The Hunters/BGH
  new Or(
    new MiningBasesAtMost(2),
    new And(
      new UnitsAtLeast(50, IsWorker),
      new UnitsAtLeast(20, IsWarrior)),
    new Not(new OnMap(Hunters))),
  new RequireMiningBases(bases))