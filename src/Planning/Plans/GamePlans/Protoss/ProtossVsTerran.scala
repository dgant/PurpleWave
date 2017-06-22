package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, ControlEnemyAirspace}
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{HaveUpgrade, UnitsAtLeast}
import Planning.Plans.Macro.Reaction.{EnemyBio, EnemyBioAllIn}
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private val lateGameBuild = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUpgradeLevel(         Protoss.ZealotSpeed,    1),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(2,   Protoss.Forge),
    RequestUnitAtLeast(1,   Protoss.Stargate),
    
    RequestUpgradeLevel(         Protoss.GroundDamage,   1),
    RequestUpgradeLevel(         Protoss.GroundArmor,    1),
    
    RequestUnitAtLeast(1,   Protoss.TemplarArchives)
  )
  
  private object RespondToBioAllInWithReavers extends IfThenElse(
    new EnemyBioAllIn,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private object RespondToBioWithReavers extends IfThenElse(
    new EnemyBio,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private object TakeNatural extends IfThenElse(
    new Or(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new BuildMiningBases(2)
  )
  
  private object TakeThirdBase extends IfThenElse(
    new Or(
      new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new BuildMiningBases(3)
  )
  
  private object BuildDragoonsUntilWeHaveZealotSpeed extends IfThenElse(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  private object UpgradeReavers extends IfThenElse(
    new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
    new Build(RequestUpgradeLevel(Protoss.ScarabDamage))
  )
  
  children.set(Vector(
    new BuildMiningBases(1),
    new Build(ProtossBuilds.OpeningTwoGate1015Dragoons),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    TakeNatural,
    RespondToBioWithReavers,
    TakeThirdBase,
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Scout,  3),
    BuildDragoonsUntilWeHaveZealotSpeed,
    UpgradeReavers,
    new Build(lateGameBuild),
    new ScoutExpansionsAt(60),
    new ScoutAt(10),
    new ControlEnemyAirspace,
    new ConsiderAttacking
  ))
}
