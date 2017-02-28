package Plans.GamePlans.Protoss.Proxy

import Plans.Army.PressureEnemyBase
import Plans.Compound.{AllParallel, AllSerial, CompleteOnce}
import Plans.Information.RequireEnemyBaseLocation
import Plans.Macro.Automatic.{BuildGatewayUnitsContinuously, BuildSupplyContinuously, BuildWorkersContinuously}
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder, TrainUnit}
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.UnitType

class ProtossRushWithProxyZealots extends AllSerial {
  
  val _laterBuildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway)
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
      new PressureEnemyBase,
      new BuildSupplyContinuously,
      new BuildGatewayUnitsContinuously,
      new BuildWorkersContinuously,
      new ScheduleBuildOrder { buildables.set(_laterBuildOrder) },
      new FollowBuildOrder
    ))}
  ))
}
