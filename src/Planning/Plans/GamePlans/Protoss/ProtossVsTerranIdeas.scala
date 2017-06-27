package Planning.Plans.GamePlans.Protoss

import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Compound.{And, If}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{HaveUpgrade, UnitsAtLeast}
import Planning.Plans.Macro.Reaction.{EnemyBio, EnemyBioAllIn}
import ProxyBwapi.Races.Protoss

object ProtossVsTerranIdeas {
  
  class RespondToBioAllInWithReavers extends If(
    new EnemyBioAllIn,
    new Build(ProtossBuilds.TechReavers: _*)
  )
  
  class RespondToBioWithReavers extends If(
    new EnemyBio,
    new Build(ProtossBuilds.TechReavers: _*)
  )
  
  class BuildDragoonsUntilWeHaveZealotSpeed extends If(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
}
