package Plans.GamePlans.Protoss.Proxy

import Plans.Army.DestroyEconomy
import Plans.Compound.{AllParallel, AllSerial, CompleteOnce}
import Plans.Information.RequireEnemyBaseLocation
import Plans.Macro.Automatic.{BuildGatewayUnitsContinuously, BuildSupplyContinuously, BuildWorkersContinuously}
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder, TrainUnit}
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossRushWithProxyZealots extends AllSerial {
  
  val _laterBuildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Singularity_Charge),
    new BuildableUnit(UnitType.Protoss_Forge),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 1),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 1),
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun),
    new BuildableUpgrade(UpgradeType.Leg_Enhancements),
    new BuildableUnit(UnitType.Protoss_Templar_Archives),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 2),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 3),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 1),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 3)
  )
  
  children.set(List(
    new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
    new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
    new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
    new CompleteOnce { child.set(
      new AllParallel { children.set(List(
        new CompleteOnce { child.set(new BuildProxyTwoGateways) },
        new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
        new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) }
      ))}
    )},
    new AllParallel { children.set(List(
      new CompleteOnce { child.set(new RequireEnemyBaseLocation) },
      new DestroyEconomy,
      new BuildSupplyContinuously,
      new BuildGatewayUnitsContinuously,
      new BuildWorkersContinuously,
      new ScheduleBuildOrder { buildables.set(_laterBuildOrder) },
      new FollowBuildOrder
    ))}
  ))
}
