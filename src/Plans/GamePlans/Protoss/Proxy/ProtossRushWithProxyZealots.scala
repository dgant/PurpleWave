package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Compound.{Simultaneous, Serial}
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends Simultaneous {
  
  setChildren(List(
    new Serial { setChildren(List(
      new TrainUnit(UnitType.Protoss_Probe),
      new TrainUnit(UnitType.Protoss_Probe),
      new Simultaneous { setChildren(List(
        new Serial { setChildren(List(
            new RequireUnitAtLocation {
              setUnitMatcher(UnitMatchWorker)
              setUnitPreference(new UnitPreferClose{setPositionFinder(PositionProxyPylon)})
              setPositionFinder(PositionProxyPylon)
              setRange(4 * 32)
            },
            new BuildBuilding(UnitType.Protoss_Pylon)   { setPositionFinder(PositionProxyPylon) },
            new BuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway) },
            new BuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway) },
            new RequireEnemyBaseLocation
          ))},
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
