package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.{AllParallel, AllSerial}
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSerial {

  children.set(List(
    new TrainUnit(UnitType.Protoss_Probe),
    new BuildProxyTwoGateways,
    new AllParallel { description.set(Some("Post-proxy build order")); children.set(List(
      new RequireEnemyBaseLocation,
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new DestroyEconomy
    ))}
  ))
}
