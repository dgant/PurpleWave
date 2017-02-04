package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Allocation.LockUnitsExactly
import Plans.Generic.Compound.{AllSerial, AllSimultaneous}
import Plans.Generic.Macro.{BuildBuilding, TrainUnit}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

class ProtossRushWithProxyZealots
  extends AllSimultaneous {
  
  var proxyBuilder = new LockUnitsExactly {
    unitMatcher.set(UnitMatchWorker)
    unitPreference.set(new UnitPreferClose{
      positionFinder.set(PositionProxyPylon)
    })
  }

  children.set(List(
    new AllSerial {
      description.set(Some("Build proxy and early probes"))
      children.set(List(
      new TrainUnit(UnitType.Protoss_Probe),
      new AllSimultaneous {
        description.set(Some("Build at proxy"))
        children.set(List(
        new TrainUnit(UnitType.Protoss_Probe),
        new AllSimultaneous { children.set(List(
          proxyBuilder,
          //Dumb hack to send probe in advance.
          new Plan {
            override def isComplete(): Boolean = true
            override def onFrame() {
              if (With.ourUnits.filter(_.getType == UnitType.Protoss_Pylon).isEmpty
                && With.game.self.minerals < UnitType.Protoss_Pylon.mineralPrice()) {
                proxyBuilder.units.foreach(builder =>
                  PositionProxyPylon.find.foreach(position =>
                    builder.move(position.toPosition)
                  ))
              }
            }
          },
          new BuildBuilding(UnitType.Protoss_Pylon)   { builderPlan.set(proxyBuilder); positionFinder.set(PositionProxyPylon); },
          new BuildBuilding(UnitType.Protoss_Gateway) { builderPlan.set(proxyBuilder); positionFinder.set(PositionProxyGateway); },
          new BuildBuilding(UnitType.Protoss_Gateway) { builderPlan.set(proxyBuilder); positionFinder.set(PositionProxyGateway); }
          //new RequireEnemyBaseLocation
        ))},
        new TrainUnit(UnitType.Protoss_Probe),
        new TrainUnit(UnitType.Protoss_Probe)
      ))}
    ))},
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
