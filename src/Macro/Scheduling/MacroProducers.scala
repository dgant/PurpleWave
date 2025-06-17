package Macro.Scheduling

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?

object MacroProducers {
  val all: Vector[MacroProducer] = Vector(
    MacroProducer(Terran.CommandCenter,     Terran.SCV),
    MacroProducer(Terran.Barracks,          Terran.Marine),
    MacroProducer(Terran.Factory,           Terran.Vulture),
    MacroProducer(Terran.Starport,          Terran.Wraith),
    MacroProducer(Protoss.Nexus,            Protoss.Probe),
    MacroProducer(Protoss.Gateway,          Protoss.Zealot),
    MacroProducer(Protoss.RoboticsFacility, Protoss.Reaver),
    MacroProducer(Protoss.Stargate,         Protoss.Corsair),
    MacroProducer(Zerg.Hatchery,            Zerg.Hydralisk),
    MacroProducer(Zerg.Lair,                Zerg.Mutalisk),
    MacroProducer(Zerg.Hive,                Zerg.Ultralisk))

  val terran  : Vector[MacroProducer] = all.filter(_.producer.isTerran)
  val protoss : Vector[MacroProducer] = all.filter(_.producer.isProtoss)
  val zerg    : Vector[MacroProducer] = all.filter(_.producer.isZerg)
  def ours    : Vector[MacroProducer] = ?(With.self.isTerran, terran, ?(With.self.isProtoss, protoss, zerg))
}
