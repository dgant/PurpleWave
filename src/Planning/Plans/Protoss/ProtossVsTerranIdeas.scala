package Planning.Plans.Protoss

import Planning.Plans.Compound.If
import Planning.Plans.Information.Reactive.{EnemyBio, EnemyBioAllIn}
import Planning.Plans.Macro.BuildOrders.Build

object ProtossVsTerranIdeas {
  
  class RespondToBioAllInWithReavers extends If(
    new EnemyBioAllIn,
    new Build(ProtossBuilds.TechReavers: _*)
  )
  
  class RespondToBioWithReavers extends If(
    new EnemyBio,
    new Build(ProtossBuilds.TechReavers: _*)
  )
}
