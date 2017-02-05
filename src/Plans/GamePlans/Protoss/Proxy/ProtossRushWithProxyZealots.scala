package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Allocation.LockUnitsExactly
import Plans.Generic.Compound.{AllSerial, AllSimultaneous}
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSimultaneous {
  
  val proxyBuilder = new LockUnitsExactly {
    description.set(Some("Proxy builder lock"))
    unitMatcher.set(UnitMatchWorker)
    unitPreference.set(new UnitPreferClose{ positionFinder.set(PositionProxyPylon) })
  }

  children.set(List(
    new TrainUnit(UnitType.Protoss_Probe),
    new AllSerial { description.set(Some("Proxy activities")); children.set(List(
      proxyBuilder,
      new RequireUnitAtLocation                   { unitPlan.set(proxyBuilder);    positionFinder.set(PositionProxyPylon); range.set(32 * 12) },
      new BuildBuilding(UnitType.Protoss_Pylon)   { builderPlan.set(proxyBuilder); positionFinder.set(PositionProxyPylon); },
      new BuildBuilding(UnitType.Protoss_Gateway) { builderPlan.set(proxyBuilder); positionFinder.set(new PositionProxyGateway); },
      new BuildBuilding(UnitType.Protoss_Gateway) { builderPlan.set(proxyBuilder); positionFinder.set(new PositionProxyGateway); },
      new RequireEnemyBaseLocation                { scoutPlan.set(proxyBuilder) }
    ))},
    new TrainUnit(UnitType.Protoss_Probe),
    new TrainUnit(UnitType.Protoss_Probe),
    new TrainUnit(UnitType.Protoss_Probe),
    new TrainUnit(UnitType.Protoss_Zealot),
    new TrainUnit(UnitType.Protoss_Zealot),
    new TrainUnit(UnitType.Protoss_Zealot),
    new TrainUnit(UnitType.Protoss_Probe),
    new BuildBuilding(UnitType.Protoss_Pylon),
    new TrainUnit(UnitType.Protoss_Zealot),
    new TrainUnit(UnitType.Protoss_Zealot),
    new TrainUnit(UnitType.Protoss_Zealot)
  ))
}
