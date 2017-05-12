package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Attack, ControlEnemyAirspace}
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic.Continuous.{BuildPylonsContinuously, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.Automatic.Gas.BuildAssimilators
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expansion.WhenMinedOutExpand
import Planning.Plans.Macro.Milestones.{HaveUpgrade, UnitsAtLeast}
import Planning.Plans.Macro.Reaction.{EnemyBio, EnemyBioAllIn}
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private val lateGameBuild = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUpgrade(         Protoss.ZealotSpeed,    1),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(2,   Protoss.Forge),
    RequestUnitAtLeast(1,   Protoss.Stargate),
    
    RequestUnitAtLeast(5,   Protoss.Nexus),
    RequestUpgrade(         Protoss.GroundDamage,   1),
    RequestUpgrade(         Protoss.GroundArmor,    1),
    
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    RequestUpgrade(         Protoss.GroundDamage,   2),
    RequestUpgrade(         Protoss.GroundArmor,    2),
    RequestUpgrade(         Protoss.GroundDamage,   3),
    RequestUpgrade(         Protoss.GroundArmor,    3),
    
    RequestUnitAtLeast(7,   Protoss.Nexus),
    RequestUnitAtLeast(8,   Protoss.Nexus)
  )
  
  private class RespondToBioAllInWithReavers extends IfThenElse(
    new EnemyBioAllIn,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private class RespondToBioWithReavers extends IfThenElse(
    new EnemyBio,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private class WhenSafeToTakeNatural extends IfThenElse(
    new Or(
      new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new Build(ProtossBuilds.TakeNatural)
  )
  
  private class WhenSafeToTakeThirdBase extends IfThenElse(
    new Or(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new Build(ProtossBuilds.TakeThirdBase)
  )
  
  private class BuildDragoonsUntilWeHaveZealotSpeed extends IfThenElse(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  private class UpgradeReavers extends IfThenElse(
    new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
    new Build(RequestUpgrade(Protoss.ScarabDamage))
  )
  
  children.set(Vector(
    new WhenMinedOutExpand,
    new Build(ProtossBuilds.OpeningTwoGate1015Dragoons),
    new BuildPylonsContinuously,
    new TrainProbesContinuously,
    new RespondToBioAllInWithReavers,
    new WhenSafeToTakeNatural,
    new RespondToBioWithReavers,
    new WhenSafeToTakeThirdBase,
    new BuildAssimilators,
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Scout,  3),
    new BuildDragoonsUntilWeHaveZealotSpeed,
    new Build(ProtossBuilds.TakeNatural),
    new UpgradeReavers,
    new Build(lateGameBuild),
    new ScoutExpansionsAt(60),
    new ScoutAt(10),
    new ControlEnemyAirspace,
    new Attack
  ))
}
