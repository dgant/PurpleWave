package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.{FindExpansions, FlyoverEnemyBases, ScoutAt}
import Planning.Plans.Macro.Automatic.Continuous.{BuildPylonsContinuously, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.Automatic.Gas.BuildAssimilators
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{HaveUpgrade, SupplyAtLeast, UnitsAtLeast}
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
  
  children.set(Vector(
    new Build(ProtossBuilds.OpeningTwoGate1015Dragoons),
    new BuildPylonsContinuously,
    new TrainProbesContinuously,
    
    new IfThenElse(
      new EnemyBioAllIn,
      new Build(ProtossBuilds.TechReavers)
    ),
    new IfThenElse(
      new Or(
        new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
      ),
      new Build(ProtossBuilds.TakeNatural)
    ),
    
    new IfThenElse(
      new EnemyBio,
      new Build(ProtossBuilds.TechReavers)
    ),
    new IfThenElse(
      new Or(
        new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
      ),
      new Build(ProtossBuilds.TakeThirdBase)
    ),
    
    new BuildAssimilators,
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Scout,  3),
    new IfThenElse(
      new And(
        new HaveUpgrade(Protoss.ZealotSpeed),
        new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
      new TrainContinuously(Protoss.Zealot),
      new TrainContinuously(Protoss.Dragoon)
    ),
    new Build(ProtossBuilds.TakeNatural),
    new IfThenElse(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))
    ),
    new Build(lateGameBuild),
    new IfThenElse(
      new SupplyAtLeast(100),
      new FindExpansions
    ),
    new ScoutAt(10),
    new FlyoverEnemyBases,
    new Attack
  ))
}
