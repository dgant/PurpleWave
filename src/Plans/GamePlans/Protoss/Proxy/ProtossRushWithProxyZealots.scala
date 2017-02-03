package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Compound.{PlanCompleteAllInParallel, PlanCompleteAllInSerial, PlanFulfillRequirements}
import Plans.Generic.Macro.UnitAtLocation.PlanRequireUnitAtLocation
import Plans.Generic.Macro.UnitCount.PlanCheckUnitCount
import Plans.Generic.Macro.{PlanBuildBuilding, PlanTrainUnit}
import Plans.Information.PlanRequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import bwapi.UnitType

class ProtossRushWithProxyZealots extends PlanCompleteAllInParallel{
  kids = List(
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanCompleteAllInParallel { kids = List(
      new PlanCompleteAllInSerial { kids = List(
        new PlanFulfillRequirements { kids = List(
          new PlanCheckUnitCount { unitMatcher = UnitMatchWorker; minimum = 6 },
          new PlanRequireUnitAtLocation(UnitMatchWorker, PositionProxyPylon, leashRange = 1024)
        )},
        new PlanBuildBuilding(UnitType.Protoss_Pylon) { positionFinder = PositionProxyPylon },
        new PlanBuildBuilding(UnitType.Protoss_Gateway) { positionFinder = PositionProxyGateway },
        new PlanBuildBuilding(UnitType.Protoss_Gateway) { positionFinder = PositionProxyGateway },
        new PlanRequireEnemyBaseLocation
      )},
      new PlanTrainUnit(UnitType.Protoss_Probe)
    )},
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Probe),
    new PlanBuildBuilding(UnitType.Protoss_Pylon),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot),
    new PlanTrainUnit(UnitType.Protoss_Zealot)
  )
}
