package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.{AllParallel, AllSerial, CompleteOnce}
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSerial {

  children.set(List(
    new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
    new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
    new AllParallel { children.set(List(
      new CompleteOnce { child.set(new BuildProxyTwoGateways) },
      new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) },
      new CompleteOnce { child.set(new TrainUnit(UnitType.Protoss_Probe)) }, //Probe #8, enough to support two Gateways
      //It takes about 4 probes to continuously produce a zealot at a time.
      new TrainUnit(UnitType.Protoss_Zealot),
      new DestroyEconomy,
      new TrainUnit(UnitType.Protoss_Probe), //9
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new TrainUnit(UnitType.Protoss_Probe), //10
      new TrainUnit(UnitType.Protoss_Zealot),
      
      // 8 supply available
      
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe), //11
      new TrainUnit(UnitType.Protoss_Probe), //12 -- enough for three gateways
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new BuildBuilding(UnitType.Protoss_Gateway),
      //8 supply available
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe), //13
      new TrainUnit(UnitType.Protoss_Probe), //14
      new TrainUnit(UnitType.Protoss_Probe), //15
      new TrainUnit(UnitType.Protoss_Probe), //16 -- maybe enough for four gateways
      new BuildBuilding(UnitType.Protoss_Pylon),
      //8 supply available
      new TrainUnit(UnitType.Protoss_Probe), //17
      new BuildBuilding(UnitType.Protoss_Gateway),
      new TrainUnit(UnitType.Protoss_Probe), //18 -- about enough for four gateways
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot)
    ))}
  ))
}
