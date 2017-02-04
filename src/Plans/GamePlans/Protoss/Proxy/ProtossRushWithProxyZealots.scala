package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Compound.{AllSimultaneous, AllSerial}
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSimultaneous {
  
  var holdWorkerAtProxy = new RequireUnitAtLocation {
    setUnitMatcher(UnitMatchWorker)
    setUnitPreference(new UnitPreferClose { setPositionFinder(PositionProxyPylon) })
    setPositionFinder(PositionProxyPylon)
    setRange(4 * 32)
  }
  
  setChildren(List(
    new AllSerial { setChildren(List(
      new TrainUnit(UnitType.Protoss_Probe),
      new AllSimultaneous { setChildren(List(
        new AllSerial { setChildren(List(
          new BuildBuilding(UnitType.Protoss_Pylon)   { setPositionFinder(PositionProxyPylon);  monopolizeWorker = true },
          new BuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway); monopolizeWorker = true },
          new BuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway); monopolizeWorker = true },
          new RequireEnemyBaseLocation
        ))},
        new TrainUnit(UnitType.Protoss_Probe),
        new TrainUnit(UnitType.Protoss_Probe),
        new TrainUnit(UnitType.Protoss_Probe)
      ))},
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Probe),
      new BuildBuilding(UnitType.Protoss_Pylon),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot),
      new TrainUnit(UnitType.Protoss_Zealot)
    ))}
  ))
}
