package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Allocation.Prioritized
import Macro.Requests.RequestUnit
import Mathematics.Maff
import Performance.CacheForever
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.CountMap
import Utilities.Time.{Forever, Seconds}
import Utilities.UnitFilters.IsTownHall

import scala.collection.mutable.ArrayBuffer

class Supplier extends Prioritized {
  val farm: UnitClass = With.self.supplyClass

  var incomeMins      : Double  = 0.0
  var incomeGas       : Double  = 0.0
  var appetite        : Int     = 0
  var simFarms        : Int     = 0
  var simSupplyUsed   : Int     = 0
  var simSupplyHalls  : Int     = 0
  var simFrames       : Int     = 0
  var simMins         : Double  = 0
  var simGas          : Double  = 0

  var halls       : Vector[FriendlyUnitInfo] = Vector.empty
  var producers   : Vector[FriendlyUnitInfo] = Vector.empty
  val consumers   : ArrayBuffer[SupplyConsumer] = new ArrayBuffer[SupplyConsumer]
  val consumed    : ArrayBuffer[(Int, UnitClass)] = new ArrayBuffer[(Int, UnitClass)]

  def update(): Unit = {

    /////////////////////////////
    // Construct initial state //
    /////////////////////////////

    incomeMins      = With.accounting.ourIncomePerFrameMinerals
    incomeGas       = With.accounting.ourIncomePerFrameGas
    appetite        = measureAppetite
    simFarms        = With.units.countOurs(farm)
    simSupplyUsed   = With.self.supplyUsed400
    simSupplyHalls  = 0
    simFrames       = 0
    simMins         = With.self.minerals

    halls           = With.units.ours.filter(IsTownHall).toVector
    producers       = With.units.ours.filter(_.isAny(productionTypes: _*)).toVector
    consumers.clear()
    consumers.appendAll(producers.map(produceConsumer))
    consumed.clear()

    updateSim()

    while(consumers.nonEmpty && simSupplyUsed < appetite) {
      val consumer = consumers.head
      advanceTo(consumer.framesToReady())
      consumed += ((simFrames, consumer.unitClass))
      simSupplyUsed += consumer.unitClass.supplyRequired
      simMins -= consumer.unitClass.mineralPrice
      simGas -= consumer.unitClass.gasPrice
      consumer.cooldown += consumer.unitClass.buildFrames
      updateSim()
    }
  }

  private def measureAppetite: Int = {
    val units = new CountMap[UnitClass]
    With.units.ours.foreach(u => units(u.unitClass) += 1)
    With.scheduler.requests.foreach(_._2.foreach(r => r.unit.foreach(u => units(u) = Math.max(units(u), r.quantity))))
    units.view.map(p => p._1.supplyRequired * p._2).sum
  }

  private def updateSim(): Unit = {
    simSupplyHalls = halls.filter(h => h.remainingCompletionFrames <= simFrames + farm.buildFrames || h.isAny(Zerg.Lair, Zerg.Hive)).map(_.unitClass.supplyProvided).sum
    consumers.foreach(_.framesToReady.invalidate())
    Maff.sortStablyInPlaceBy(consumers)(_.framesToReady())

    val farmsAfter = Math.ceil((simSupplyUsed - simSupplyHalls).toDouble / farm.supplyProvided).toInt
    if (farmsAfter > simFarms) {
      simFarms = farmsAfter
      simMins -= farm.mineralPrice * (farmsAfter - simFarms)
      With.scheduler.request(this, RequestUnit(farm, simFarms, minFrameArg = With.frame + simFrames - farm.buildFrames))
    }

    simSupplyHalls = halls
      // Include supply from halls that will finish faster than any farm we could construct
      // Include some slack time, too, to leverage nearly-done halls
      .filter(h => h.remainingCompletionFrames < Seconds(5)() + Math.max(farm.buildFrames, simFrames) || h.isAny(Zerg.Lair, Zerg.Hive))
      .map(_.unitClass.supplyProvided)
      .sum
  }

  private def advanceTo(newFrame: Int): Unit = {
    if (newFrame > simFrames) {
      val frameDelta = newFrame - simFrames
      simMins += frameDelta * incomeMins
      simGas  += frameDelta * incomeGas
      simFrames = newFrame
    }
  }

  private lazy val productionTypes = Seq(
    Terran.CommandCenter,
    Terran.Barracks,
    Terran.Factory,
    Terran.Starport,
    Protoss.Nexus,
    Protoss.Gateway,
    Protoss.RoboticsFacility,
    Protoss.Stargate
  )

  private def produceConsumer(producer: FriendlyUnitInfo): SupplyConsumer = {
    val hungriestConsumer = producer.unitClass.unitsTrained
      // Don't consider units we can't make yet; eg a Stargate needs 6/200 supply to make Carriers but only 3/200 for Scouts
      .filter(t => t.buildUnitsEnabling.forall(With.units.existsOurs(_)) && t.buildTechEnabling.forall(MacroFacts.techStarted))
      // Prefer considering the cheapest unit we can make for the max supply cost, eg. Zealot is cheaper than Dragoon
      .sortBy(_.buildFrames)
      // Consider the fastest-training unit we can make for the max supply cost, eg. Vulture builds much faster than Tank
      // In all cases I can think of this is actually the same as the previous check but let's be sure
      .sortBy(t => t.mineralPrice + t.gasPrice)
      // Assume we'll use the most supply possible so we accumulate enough supply to afford our supply-hungriest units
      .maxBy(_.supplyRequired)
    new SupplyConsumer(producer.remainingOccupationFrames, hungriestConsumer)
  }

  class SupplyConsumer(var cooldown: Int, val unitClass: UnitClass) {
    val framesToReady = new CacheForever(() =>
      Math.ceil(Seq(
        cooldown,
        if (unitClass.mineralPrice  <= simMins) 0.0 else Maff.nanToN((unitClass.mineralPrice  - simMins) / incomeMins, Forever()),
        if (unitClass.gasPrice      <= simGas)  0.0 else Maff.nanToN((unitClass.gasPrice      - simGas) / incomeMins, Forever()))
      .max).toInt)

    override def toString: String = f"$unitClass: Cooldown = $cooldown, Ready = ${framesToReady()}"
  }
}
