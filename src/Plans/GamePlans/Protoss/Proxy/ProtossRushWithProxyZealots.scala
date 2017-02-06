package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Allocation.LockUnitsExactly
import Plans.Generic.Compound.{AllParallel, AllSerial}
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Information.RequireEnemyBaseLocation
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSerial {
  
  val proxyBuilder = new LockUnitsExactly {
    this.description.set(Some("Proxy builder lock"))
    this.unitMatcher.set(UnitMatchWorker)
    this.unitPreference.set(new UnitPreferClose{ positionFinder.set(PositionProxyPylon) })
  }

  children.set(List(
    new TrainUnit(UnitType.Protoss_Probe),
    new AllParallel { description.set(Some("Train next probe and build proxy")); children.set(List(
      new TrainUnit(UnitType.Protoss_Probe),
      new AllSerial { description.set(Some("Build proxy pylon")); children.set(List(
        proxyBuilder,
        new RequireUnitAtLocation                   { this.unitPlan.set(proxyBuilder);    this.positionFinder.set(PositionProxyPylon); this.range.set(32 * 12); this.description.set(Some("Send builder to proxy")) },
        new BuildBuilding(UnitType.Protoss_Pylon)   { this.builderPlan.set(proxyBuilder); this.positionFinder.set(PositionProxyPylon); }
      ))}
    ))},
    new AllParallel { description.set(Some("Post-proxy build order")); children.set(List(
      new AllSerial { description.set(Some("Post-proxy build order")); children.set(List(
        new AllParallel { description.set(Some("Post-proxy build order")); children.set(List(
          new BuildBuilding(UnitType.Protoss_Gateway) { this.builderPlan.set(proxyBuilder); this.positionFinder.set(new PositionProxyGateway); },
          new BuildBuilding(UnitType.Protoss_Gateway) { this.builderPlan.set(proxyBuilder); this.positionFinder.set(new PositionProxyGateway); }
        ))},
        new RequireEnemyBaseLocation { this.scoutPlan.set(proxyBuilder) }
      ))},
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
      new TrainUnit(UnitType.Protoss_Zealot)
    ))}
  ))
}
