package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Compound.{PlanCompleteAllInParallel, PlanCompleteAllInSerial}
import Plans.Generic.Macro.UnitAtLocation.PlanRequireUnitAtLocation
import Plans.Generic.Macro.UnitCount.PlanCheckUnitCountAtLeast
import Plans.Generic.Macro.{PlanBuildBuilding, PlanTrainUnit}
import Plans.Information.PlanRequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends PlanCompleteAllInParallel {
  
  setChildren(List(
    new PlanCompleteAllInSerial {
      setChildren(List(
        new PlanCheckUnitCountAtLeast {
          setUnitMatcher(UnitMatchWorker);
          setQuantity(5)
        },
        new PlanRequireUnitAtLocation {
          setUnitMatcher(UnitMatchWorker)
          setPositionFinder(PositionProxyPylon)
        },
          new PlanBuildBuilding(UnitType.Protoss_Pylon)   { setPositionFinder(PositionProxyPylon) },
          new PlanBuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway) },
          new PlanBuildBuilding(UnitType.Protoss_Gateway) { setPositionFinder(PositionProxyGateway) },
          new PlanRequireEnemyBaseLocation
        )
      )
    },
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanBuildBuilding(UnitType.Protoss_Pylon),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot)
  ))
}
