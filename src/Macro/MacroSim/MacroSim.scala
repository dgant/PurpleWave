package Macro.MacroSim

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrades
import Utilities.Time.Forever

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final class MacroSim {
  var startFrame: Int = 0
  val requests  = new ArrayBuffer[MacroRequest]
  val redundant = new ArrayBuffer[MacroRequest]()
  val denied    = new ArrayBuffer[MacroRequest]()
  val steps     = new mutable.ArrayBuffer[MacroStep]()

  private def trulyUnoccupied(unit: UnitInfo): Boolean = unit.complete && unit.remainingOccupationFrames == 0
  def reset(): Unit = {
    startFrame = With.frame
    requests.clear()
    denied.clear()
    redundant.clear()
    steps.clear()

    // Construct initial state
    val initialStep = new MacroStep
    val initialState = initialStep.state
    initialState.minerals = With.self.minerals
    initialState.gas = With.self.gas
    initialState.supplyAvailable = With.units.ours.filter(u => u.complete || u.isAny(Zerg.Lair, Zerg.Hive)).map(_.unitClass.supplyProvided).sum
    initialState.supplyUsed = With.units.ours.map(_.unitClass.supplyRequired).sum
    initialState.mineralPatches = With.self.bases.view.map(_.minerals.count(_.mineralsLeft >= 8)).sum
    initialState.geysers = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.complete && g.gasLeft > 0)).sum
    Upgrades.all.foreach(u => initialState.upgrades(u) = With.self.getUpgradeLevel(u))
    initialState.techs ++= Techs.all.view.filter(With.self.hasTech)
    insert(initialStep)

    // Construct events for things in progress
    // TODO: Count Eggs/Cocoon/Lurker Egg as what they're making
    With.units.ours.foreach(u => initialState.units(u.unitClass) += 1)
    With.units.ours.filter(trulyUnoccupied).foreach(u => initialState.producers(u.unitClass) += 1)
    With.units.ours.filterNot(trulyUnoccupied).foreach(u => {
      val step = new MacroStep
      val event = step.event
      event.dFrames = u.remainingOccupationFrames
      if ( ! u.complete && ! u.isAny(Zerg.Lair, Zerg.Hive)) {
        event.dSupplyAvailable += u.unitClass.supplyProvided
      }
      if (u.upgrading) {
        event.dUpgrade = u.upgradingType
        event.dUpgradeLevel = 1 + initialState.upgrades(u.upgradingType)
      } else if (u.teching) {
        event.dTech = u.techingType
      } else if (u.unitClass.isGas) {
        event.dGeysers += 1
      } else if ( ! u.complete && u.unitClass.isResourceDepot && ! u.isAny(Zerg.Lair, Zerg.Hive)) {
        val base = u.base.filter(_.townHall.contains(u))
        base.foreach(b => event.dMineralPatches += b.minerals.count(_.mineralsLeft >= 8))
      } else if (u.morphing) {
        // TODO: Add/Subtract
      }
      event.dProducer1 = u.unitClass
      event.dProducer1N = 1
      steps += step
    })

    // Populate states as of each event
    updateStatesFrom(1)
  }

  def simulate(): Unit = {
    // Temporary: Populate requests from the scheduler to test the sim
    // This will need to be replaced once BuildRequests are replaced with MacroRequests
    With.scheduler.audit.view.flatMap(_._2).foreach(buildRequest => {
      if (buildRequest.buildable.upgradeOption.exists(u => steps.last.state.upgrades(u) < buildRequest.total)) {
        requests += new MacroRequest
        requests.last.upgrade = buildRequest.buildable.upgradeOption
        requests.last.min = buildRequest.total
        requests.last.max = buildRequest.total
      } else if (buildRequest.buildable.techOption.exists(t => ! steps.last.state.techs.contains(t))) {
        requests += new MacroRequest
        requests.last.tech = buildRequest.buildable.techOption
      } else if (buildRequest.buildable.unitOption.isDefined) {
        requests += new MacroRequest
        requests.last.unit = buildRequest.buildable.unitOption
        requests.last.min = buildRequest.total
        requests.last.max = buildRequest.total
      }
      val macroRequest = new MacroRequest
    })

    // Construct events for our requests
    // TODO: Skip fulfilled requests
    requests.foreach(request => {
      if (request.unit.exists(u => steps.last.state.units(u) >= request.maxMax)
        || request.upgrade.exists(u => steps.last.state.upgrades(u) >= request.maxMax)
        || request.tech.exists(t => steps.last.state.techs.contains(t))) {
        redundant += request
      } else {
        val insertAfter = steps.indices.find(canInsertAfter(request, _))
        if (insertAfter.isEmpty) {
          denied += request
        }
        insertAfter.foreach(i => {
          val stepBefore = steps(i)
          val framesAfter = Math.ceil(Seq(
            stepBefore.event.dFrames,
            if (request.mineralsRequired == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.mineralsRequired  - stepBefore.state.minerals)  / With.accounting.incomePerFrameMinerals, Forever()),
            if (request.gasRequired      == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.gasRequired       - stepBefore.state.gas)       / With.accounting.incomePerFrameGas,    Forever()))
            .max).toInt
          if (framesAfter < Forever()) {
            val stepStart           = new MacroStep
            val stepFinish          = new MacroStep
            val eventStart          = stepStart.event
            val eventFinish         = stepFinish.event
            stepStart.request       = Some(request)
            stepFinish.request      = Some(request)
            eventStart.dFrames      = framesAfter
            eventStart.dMinerals    = - request.mineralsRequired
            eventStart.dGas         = -request.gasRequired
            eventStart.dSupplyUsed  = request.supplyRequired
            eventStart.dProducer1   = request.producerRequired
            eventStart.dProducer1N  = - request.producersRequired
            eventFinish.dFrames     = eventStart.dFrames + request.framesRequired
            eventFinish.dProducer1  = eventStart.dProducer1
            eventFinish.dProducer1N = - eventStart.dProducer1N
            request.unit.foreach(u => {
              eventStart.dUnit1 = u
              eventStart.dUnit1N = u.copiesProduced
              if (u.isGas) {
                eventFinish.dGeysers = 1
              }
            })
            if (Seq(Protoss.HighTemplar, Protoss.DarkTemplar, Zerg.Larva, Zerg.Drone, Zerg.CreepColony, Zerg.Spire, Zerg.Hydralisk, Zerg.Mutalisk).contains(request.producerRequired)) {
              eventStart.dUnit2 = request.producerRequired
              eventStart.dUnit2N = - request.producersRequired
            }
            request.unit.foreach(unit => {
              eventFinish.dSupplyAvailable = unit.supplyProvided
              eventFinish.dProducer2 = unit
              eventFinish.dProducer2N = unit.copiesProduced
            })
            request.tech.foreach(eventFinish.dTech = _)
            request.upgrade.foreach(upgrade => {
              eventFinish.dUpgrade = upgrade
              eventFinish.dUpgradeLevel = request.min
            })
            val iStart = insert(stepStart)
            insert(stepFinish)
            updateStatesFrom(iStart)
          }
        })
      }
    })
  }

  private def canInsertAfter(request: MacroRequest, i: Int): Boolean = {
    // Find a state where we can build the request
    val step = steps(i)
    if (request.mineralsRequired > Math.max(0, step.state.minerals) && With.accounting.incomePerFrameMinerals == 0) return false
    if (request.gasRequired > Math.max(0, step.state.gas) && With.accounting.incomePerFrameGas == 0) return false
    // TODO: Inject supply?
    if (request.supplyRequired > step.state.supplyAvailable - step.state.supplyUsed) return false
    // TODO: Inject prerequisites?
    if (request.upgradeRequired.exists(u => step.state.upgrades(u._1) < u._2)) return false
    if (request.techRequired.exists(t => ! step.state.techs.contains(t))) return false
    if (step.state.producers(request.producerRequired) < request.producersRequired) return false
    if (request.unitsRequired.exists(step.state.units(_) == 0)) return false
    canInsertBefore(request, i + 1)
  }

  private def canInsertBefore(request: MacroRequest, i: Int): Boolean = {
    var j = i
    while(j < steps.length) {
      val state = steps(j).state
      if (request.producerRequired != UnitClasses.None && state.producers(request.producerRequired) < request.producersRequired) return false
      if (request.mineralsRequired > Math.max(0, state.minerals)) return false
      if (request.gasRequired > Math.max(0, state.gas)) return false
      if (request.supplyRequired > Math.max(0, state.supplyAvailable - state.supplyUsed)) return false
      j += 1
    }
    true
  }

  def insert(step: MacroStep): Int = {
    // Binary search to figure out where to fit the step
    val dFrames = step.event.dFrames
    var min = 1
    var max = steps.length
    while(true) {
      val at = (min + max) / 2
      val dFramesBefore = if (at <= 0) Int.MinValue else steps(at - 1).event.dFrames
      val dFramesAfter = if (at >= steps.length) Int.MaxValue else steps(at).event.dFrames
      if (dFramesBefore > dFrames) {
        max = at - 1
      } else if (dFramesAfter <= dFrames) {
        min = at + 1
      } else {
        steps.insert(at, step)
        return at
      }
    }
    throw new RuntimeException("Failed to insert a MacroStep")
  }

  def mineralsMinedBy(dFrame: Int): Int = (With.accounting.incomePerFrameMinerals * dFrame).toInt
  def gasMinedBy(dFrame: Int): Int = (With.accounting.incomePerFrameGas * dFrame).toInt

  def updateStatesFrom(index: Int): Unit = {
    var i = index
    while (i < steps.length) {
      val stateLast = steps(i - 1).state
      val stateNext = steps(i).state
      val event = steps(i).event
      val dFrames = event.dFrames - steps(i - 1).event.dFrames
      stateNext.minerals = stateLast.minerals + event.dMinerals + (dFrames * With.accounting.incomePerFrameMinerals).toInt
      stateNext.gas = stateLast.gas + event.dGas + (dFrames * With.accounting.incomePerFrameGas).toInt
      stateNext.supplyAvailable = stateLast.supplyAvailable + event.dSupplyAvailable
      stateNext.supplyUsed = stateLast.supplyUsed + event.dSupplyUsed
      stateNext.mineralPatches = stateLast.mineralPatches + event.dMineralPatches
      stateNext.geysers = stateLast.geysers + event.dGeysers
      stateNext.techs = stateLast.techs
      stateNext.upgrades = stateLast.upgrades
      stateNext.units = stateLast.units.clone
      stateNext.producers = stateLast.producers.clone
      if (event.dTech != Techs.None) {
        stateNext.techs += event.dTech
      }
      if (event.dUpgrade != Upgrades.None) {
        stateNext.upgrades(event.dUpgrade) = event.dUpgradeLevel
      }
      if (event.dUnit1 != UnitClasses.None) {
        stateNext.units(event.dUnit1) += event.dUnit1N
      }
      if (event.dUnit2 != UnitClasses.None) {
        stateNext.units(event.dUnit2) += event.dUnit2N
      }
      if (event.dProducer1 != UnitClasses.None) {
        stateNext.producers(event.dProducer1) += event.dProducer1N
      }
      if (event.dProducer2 != UnitClasses.None) {
        stateNext.producers(event.dProducer2) += event.dProducer2N
      }
      i += 1
    }
  }
}

