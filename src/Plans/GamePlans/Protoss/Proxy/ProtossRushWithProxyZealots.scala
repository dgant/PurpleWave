package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Allocation.PlanAcquireUnitsExactly
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
  
  var proxyBuilder = new PlanAcquireUnitsExactly {
    setUnitMatcher(UnitMatchWorker)
    setUnitPreference(new UnitPreferClose{ setPositionFinder(PositionProxyPylon) })
  }

  setChildren(List(
    new AllSerial {
      setDescription("Build proxy and early probes")
      setChildren(List(
      new TrainUnit(UnitType.Protoss_Probe),
      new AllSimultaneous {
        setDescription("Build at proxy")
        setChildren(List(
        new TrainUnit(UnitType.Protoss_Probe),
        new AllSimultaneous { setChildren(List(
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
          new BuildBuilding(UnitType.Protoss_Pylon)   { setUnits(proxyBuilder); setPositionFinder(PositionProxyPylon); },
          new BuildBuilding(UnitType.Protoss_Gateway) { setUnits(proxyBuilder); setPositionFinder(PositionProxyGateway); },
          new BuildBuilding(UnitType.Protoss_Gateway) { setUnits(proxyBuilder); setPositionFinder(PositionProxyGateway); }
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
