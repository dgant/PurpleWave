package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.{AllParallel, AllSerial, CompleteOnce}
import Plans.Generic.Macro.Automatic.{BuildGatewayUnitsContinuously, BuildSupplyContinuously, BuildWorkersContinuously}
import Plans.Generic.Macro.{FollowBuildOrder, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossRushWithProxyZealots extends AllSerial {
  
  val _laterBuildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
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
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 1),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 3)
  )
  
  children.set(List(
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
      new FollowBuildOrder { buildables.set(_laterBuildOrder) }
    ))}
  ))
}
