package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Allocation.Prioritized
import Macro.Requests.RequestUnit
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds
import Utilities.UnitFilters.IsTownHall

class Supplier extends Prioritized {
  private val depot : UnitClass = With.self.supplyClass

  def update(): Unit = {

    /////////////////////////////
    // Construct initial state //
    /////////////////////////////

    val halls         = With.units.ours.filter(IsTownHall).toVector
    val producers     = With.units.ours.filter(_.isAny(productionTypes: _*)).toVector.sortBy(_.remainingOccupationFrames)
    val consumers     = producers.map(produceConsumer).sortBy(_.framesAhead)

    var simDepots     = With.units.countOurs(depot)
    var simSupplyUsed = With.self.supplyUsed400
    var simFrames     = 0

    //////////////////////////////////////////////
    // Answer questions about the current state //
    //////////////////////////////////////////////

    // How much supply are town halls providing at some future time?
    def supplyFromHalls: Int = halls
      // Include supply from halls that will finish faster than any depot we could construct
      // Include some slack time, too, to leverage nearly-done halls
      .filter(h => h.remainingCompletionFrames < Seconds(5)() + Math.max(depot.buildFrames, simFrames) || h.isAny(Zerg.Lair, Zerg.Hive))
      .map(_.unitClass.supplyProvided)
      .sum

    // How many depots will we need at some future time?
    def depotsNeeded: Int = Math.ceil((simSupplyUsed - supplyFromHalls).toDouble / depot.supplyProvided).toInt

    // Request depots now or at some future time
    def requestDepots(): Unit = {
      val depotsAfter = depotsNeeded
      if (depotsAfter > simDepots) {
        simDepots = depotsAfter
        With.scheduler.request(this, RequestUnit(depot, simDepots, minFrameArg = With.frame + simFrames - depot.buildFrames))
      }
    }

    ///////////////////////////
    // Simulate future state //
    ///////////////////////////

    requestDepots() // In case we have a deficit right now

    var i = 0
    while(i < consumers.length) {
      val consumer = consumers(i)
      simSupplyUsed += consumer.unitClass.supplyRequired
      requestDepots()
      i += 1
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
    val framesAhead   = producer.remainingOccupationFrames
    val consumers     = producer.unitClass.unitsTrained.map(SupplyConsumer(framesAhead, _))
    val hungriest     = consumers.maxBy(_.supplyPerDepot)
    hungriest
  }

  private case class SupplyConsumer(framesAhead: Int, unitClass: UnitClass) {
    val cyclesPerDepot  : Double = Math.max(1.0, unitClass.buildFrames.toDouble / depot.buildFrames.toDouble)
    val supplyPerDepot  : Double = unitClass.supplyRequired * unitClass.copiesProduced * cyclesPerDepot
  }
}
