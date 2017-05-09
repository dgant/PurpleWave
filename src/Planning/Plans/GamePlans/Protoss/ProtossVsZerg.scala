package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestUnitAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, Defend}
import Planning.Plans.Compound._
import Planning.Plans.Information.{FindExpansions, FlyoverEnemyBases, ScoutAt}
import Planning.Plans.Macro.Automatic.Continuous.{BuildPylonsContinuously, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.Automatic.Gas.BuildAssimilators
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{SupplyAtLeast, UnitsAtLeast, UnitsExactly}
import Planning.Plans.Macro.Reaction.{EnemyHydralisks, EnemyMutalisks}
import ProxyBwapi.Races.Protoss

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  private val lateGameBuild = Vector[BuildRequest] (
    
    RequestUnitAtLeast(4,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.Forge),
    RequestUpgrade(         Protoss.DragoonRange),
    RequestUpgrade(         Protoss.GroundDamage, 1),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(2,   Protoss.RoboticsFacility),
    RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    RequestUnitAtLeast(2,   Protoss.Forge),
    RequestUnitAtLeast(7,   Protoss.PhotonCannon),
    RequestUpgrade(         Protoss.GroundDamage, 2),
    RequestUpgrade(         Protoss.GroundArmor, 2),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    
    RequestUnitAtLeast(5,   Protoss.Nexus),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    RequestUpgrade(         Protoss.GroundDamage, 3),
    RequestUpgrade(         Protoss.GroundArmor, 3),
    
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(12,  Protoss.Gateway),
    
    RequestUnitAtLeast(7,   Protoss.Nexus),
    RequestUnitAtLeast(8,   Protoss.Nexus)
  )
  
  private val earlyZealotCount = 8
  
  children.set(Vector(
    new Build(ProtossBuilds.OpeningTwoGate99),
  
    new IfThenElse(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Build(ProtossBuilds.TakeNatural)
    ),
  
    new IfThenElse(
      new And(
        new UnitsAtLeast(16, UnitMatchWarriors),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Corsair)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
      ),
      new Build(ProtossBuilds.TakeThirdBase)
    ),
    
    new IfThenElse(
      new UnitsExactly(0, UnitMatchType(Protoss.CyberneticsCore)),
      new Build(ProtossBuilds.OpeningTwoGate99Zealots),
      new BuildAssimilators
    ),
    
    new BuildPylonsContinuously,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.DarkTemplar, 1),
    
    new IfThenElse(
      new EnemyHydralisks,
      new Build(ProtossBuilds.TechReavers)
    ),
    
    new IfThenElse(
      new EnemyMutalisks,
      new Parallel(
        new Build(ProtossBuilds.TechCorsairs),
        new Build(ProtossBuilds.TechDragoons),
        new Build(RequestUpgrade(Protoss.DragoonRange)),
        new Build(Vector(RequestUpgrade(Protoss.AirArmor))),
        new TrainContinuously(Protoss.Corsair, 12),
        new TrainContinuously(Protoss.Dragoon),
        new Build(Vector(RequestUpgrade(Protoss.AirDamage)))
      ),
      new TrainContinuously(Protoss.Corsair, 3)
    ),
    
    new TrainContinuously(Protoss.DarkTemplar, 2),
    new TrainContinuously(Protoss.Reaver, 2),
    
    new IfThenElse(
      new And(
        new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator))
      ),
      new IfThenElse (
        new UnitsAtLeast(8, UnitMatchType(Protoss.Zealot)),
        new TrainContinuously(Protoss.Dragoon)
      ),
      new TrainContinuously(Protoss.Zealot)
    ),
    
    new Build(ProtossBuilds.TechCorsairs),
    new Build(ProtossBuilds.TechReavers),
    new Build(lateGameBuild),
    new ScoutAt(10),
    new IfThenElse(
      new SupplyAtLeast(80),
      new FindExpansions
    ),
    new FlyoverEnemyBases,
    new Attack { attackers.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) },
    new IfThenElse(
      new UnitsAtLeast(10, UnitMatchWarriors),
      new Attack,
      new Defend
    )
  ))
}
