package Macro.Scheduling.SmartQueue

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.{Buildable, BuildableUnit}
import Mathematics.PurpleMath
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.CountMap

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SmartQueueState {
  
  /*
  TODO List:
  * Make sure it works
  * Handle addons
  *
   */
  
  var frame           : Int     = With.frame
  var minerals        : Double  = With.self.minerals.toDouble
  var gas             : Double  = With.self.gas.toDouble
  var supplyTotal     : Int     = With.self.supplyTotal
  var supplyUsed      : Int     = With.self.supplyUsed
  val buildersAvailable         = new CountMap[UnitClass]
  val unitsNow                  = new CountMap[UnitClass]
  val unitsQueued               = new CountMap[UnitClass]
  val upgradesQueued            = new CountMap[Upgrade]
  val techsQueued               = new CountMap[Tech]
  
  def supplyLeft: Int = supplyTotal - supplyUsed
  
  private val incomeMineralsPerFrame  : Double = With.economy.ourIncomePerFrameMinerals
  private val incomeGasPerFrame       : Double = With.economy.ourIncomePerFrameGas
  
  var input  = new ArrayBuffer[SmartQueueItem]
  val output = new ArrayBuffer[BuildRequest]
  val events = new mutable.PriorityQueue[SmartQueueEvent]()(Ordering.by(_.frame))
  
  Upgrades.all.foreach(upgrade => upgradesQueued.put(upgrade, With.self.getUpgradeLevel(upgrade)))
  Techs.all.foreach(tech => techsQueued.put(tech, PurpleMath.fromBoolean(With.self.hasTech(tech))))
  
  With.units.ours
    .toSeq
    .foreach(unit => {
      val unitClass = unit.unitClass
      if (unit.framesBeforeBecomingComplete > 0) {
        events += new SmartQueueEventBirthUnit(frame + unit.framesBeforeBecomingComplete, unitClass)
      }
      else {
        unitsNow(unitClass) += 1
        var framesBeforeFreedom: Option[Int] = None
        if (unit.framesBeforeBuildeeComplete > 0) {
          framesBeforeFreedom = Some(unit.framesBeforeBuildeeComplete)
        }
        else if (unit.framesBeforeUpgradeComplete > 0) {
          framesBeforeFreedom = Some(unit.framesBeforeUpgradeComplete)
          upgradesQueued.add(unit.upgradingType, 1)
        }
        else if (unit.framesBeforeTechComplete > 0) {
          framesBeforeFreedom = Some(unit.framesBeforeTechComplete)
          techsQueued.add(unit.techingType, 1)
        }
          
        if (framesBeforeFreedom.isDefined) {
          events += new SmartQueueEventFreeBuilder(frame + framesBeforeFreedom.get, unitClass)
        }
        else {
          buildersAvailable(unitClass) += 1
        }
      }
    })
  
  def stepOneFrame() {
    advanceState()
    updateQueue()
  }
  
  private def advanceState() {
    frame     += 1
    minerals  += incomeMineralsPerFrame
    gas       += incomeGasPerFrame
    while (events.headOption.exists(_.frame <= frame)) {
      val event = events.dequeue()
      event.apply(this)
    }
  }
  
  private def updateQueue() {
    
    var updatedQueue = new ArrayBuffer[SmartQueueItem]()
    
    val inQueue = new CountMap[Buildable]
    val pumpers = unitsNow.clone
    
    def buildersUnavailable(buildable: Buildable): Iterable[BuildableUnit] = {
      buildable.buildersOccupied ++ buildable.buildersConsumed
    }
  
    def canAfford(buildable: Buildable): Boolean = {
      val buildersUsed = new CountMap[UnitClass]
      buildersUnavailable(buildable).foreach(builder => buildersUsed.add(builder.unit, 1))
      val satisfiedBuilder  = buildersUsed.forall(p => p._2 <= buildersAvailable(p._1))
      val satisfiedMinerals = buildable.minerals        <= Math.max(0, buildable.minerals)
      val satisfiedGas      = buildable.gas             <= Math.max(0, buildable.gas)
      val satisfiedSupply   = buildable.supplyRequired  <= Math.max(0, buildable.supplyRequired)
      val satisfied         = satisfiedBuilder && satisfiedMinerals && satisfiedGas && satisfiedSupply
      satisfied
    }
  
    def reserve(buildable: Buildable) {
      buildable.unitOption.foreach(unit               => events += new SmartQueueEventBirthUnit(buildable.frames, unit))
      buildersUnavailable(buildable).foreach(builder  => events += new SmartQueueEventFreeBuilder(buildable.frames, builder.unit))
      buildersUnavailable(buildable).foreach(builder  => buildersAvailable.subtract(builder.unit, 1))
      minerals    -= buildable.minerals
      gas         -= buildable.gas
      supplyUsed  += buildable.supplyRequired
      inQueue.add(buildable, 1)
    }
  
    def pump(buildable: Buildable, queueIfUnbuildable: Boolean) {
      val capacityBuilder   = buildersUnavailable(buildable).map(builder => pumpers(builder.unit)).min
      val capacityMinerals  = PurpleMath.nanToZero(buildable.minerals / (minerals + incomeMineralsPerFrame  * buildable.frames))
      val capacityGas       = PurpleMath.nanToZero(buildable.gas      / (gas      + incomeGasPerFrame       * buildable.frames))
      val capacity          = Math.ceil(Seq(capacityBuilder, capacityMinerals, capacityGas).min).toInt
      val difference        = Math.max(0, capacity - inQueue(buildable))
      
      if (difference > 0) {
        for (i <- 1 to difference) {
          if (queueIfUnbuildable || canAfford(buildable)) {
            reserve(buildable)
            buildersUnavailable(buildable).foreach(builder => pumpers.subtract(builder.unit, 1))
          }
        }
      }
    }
    
    input.foreach(item => {
      item.add.map(_.buildable).foreach(reserve)
      item.pump.map(_.buildable).foreach(pump(_, queueIfUnbuildable = true))
      item.dump.map(_.buildable).foreach(pump(_, queueIfUnbuildable = false))
    })
  }
}