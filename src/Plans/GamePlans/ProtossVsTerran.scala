package Plans.GamePlans

import Plans.Army.{Attack, DefendChoke}
import Plans.Compound.{IfThenElse, Parallel}
import Plans.Information.ScoutAt
import Plans.Macro.Automatic.{BuildPylonsContinuously, _}
import Plans.Macro.Build.ScheduleBuildOrder
import Plans.Macro.UnitCount.{SupplyAtLeast, UnitCountAtLeast}
import Strategies.UnitMatchers.UnitMatchWarriors
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  //Via http://wiki.teamliquid.net/starcraft/14_Nexus_(vs._Terran)
  val _13Nexus = List[Buildable] (
    new BuildableUnit(UnitType.Zerg_Queen), //0
    new BuildableUnit(UnitType.Protoss_Nexus), //0
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon), //8
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Nexus), //13
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway), //14
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Assimilator), //15
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core), //17
    new BuildableUnit(UnitType.Protoss_Gateway), //17
    new BuildableUnit(UnitType.Protoss_Zealot), //17
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon), //21
    new BuildableUnit(UnitType.Protoss_Dragoon), //21
    new BuildableUnit(UnitType.Protoss_Dragoon), //23
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUpgrade(UpgradeType.Singularity_Charge), //25
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon), //27
    new BuildableUnit(UnitType.Protoss_Dragoon), //27
    new BuildableUnit(UnitType.Protoss_Dragoon), //29
    new BuildableUnit(UnitType.Protoss_Probe), //31
    new BuildableUnit(UnitType.Protoss_Probe), //32
    new BuildableUnit(UnitType.Protoss_Pylon), //33
    new BuildableUnit(UnitType.Protoss_Probe), //31
    new BuildableUnit(UnitType.Protoss_Probe), //32
    new BuildableUnit(UnitType.Protoss_Dragoon), //35
    new BuildableUnit(UnitType.Protoss_Dragoon) //37
  )
  
  val _lateGame = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun),
    new BuildableUpgrade(UpgradeType.Leg_Enhancements),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Forge),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 1),
    new BuildableUnit(UnitType.Protoss_Templar_Archives),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 2),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 3),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_13Nexus) },
    new IfThenElse {
      predicate.set(new SupplyAtLeast { quantity.set(74) })
      whenTrue.set(new Parallel { children.set(List(
        new BuildPylonsContinuously,
        new BuildWorkersContinuously,
        new TrainContinuously(UnitType.Protoss_Scout),
        new TrainGatewayUnitsContinuously,
        new ScheduleBuildOrder { buildables.set(MassScoutLateGame.build) }
      ))})
    },
    new ScoutAt(28),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(3); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    }
  ))
}
