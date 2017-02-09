package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.{AllParallel, AllSerial, CompleteOnce}
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSerial {

  children.set(List(
    new TrainUnit(UnitType.Protoss_Probe),
    new TrainUnit(UnitType.Protoss_Probe),
    new AllParallel { children.set(List(
      new CompleteOnce { child.set( new BuildProxyTwoGateways )},
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe)
    ))},
    new AllParallel { description.set(Some("Post-proxy build order")); children.set(List(
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
     new BuildBuilding(UnitType.Protoss_Pylon),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new BuildBuilding(UnitType.Protoss_Gateway),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new BuildBuilding(UnitType.Protoss_Gateway),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new DestroyEconomy
    ))}
  ))
}
