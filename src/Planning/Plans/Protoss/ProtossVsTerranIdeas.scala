package Planning.Plans.Protoss

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Compound.{And, Check, If, Or}
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{HaveUpgrade, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Information.Reactive.{EnemyBio, EnemyBioAllIn}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly4GateAllIn

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
