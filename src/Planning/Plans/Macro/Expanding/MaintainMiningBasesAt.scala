package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.SafeToMoveOut
import Planning.UnitMatchers.UnitMatchWorkers

class MaintainMiningBasesAt(bases: Int) extends Parallel(
  new Trigger(
    new And(
      new MiningBasesAtLeast(bases),
      new UnitsAtLeast(bases, With.self.townHallClass, complete = true)),
    new If(
      new UnitsAtLeast(bases * 15, UnitMatchWorkers),
      new If(
        new SafeToMoveOut,
        new RequireMiningBases(bases),
        new RequireMiningBases(bases - 1)))))