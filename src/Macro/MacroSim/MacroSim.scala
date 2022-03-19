package Macro.MacroSim

import Lifecycle.With
import Macro.Buildables.Buildable
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
  val redundant = new ArrayBuffer[Buildable]()
  val denied    = new ArrayBuffer[Buildable]()
  val steps     = new mutable.ArrayBuffer[MacroStep]()

  def queue: Seq[Buildable] = steps.view.filter(_.request.isDefined).map(_.request.get)

  private def trulyUnoccupied(unit: UnitInfo): Boolean = unit.complete && unit.remainingOccupationFrames == 0
  def simulate(): Unit = {
    val requests = new ArrayBuffer[Buildable]
    redundant.clear()
    denied.clear()
    steps.clear()

    // Construct initial state
    val initialStep = new MacroStep
    val initialState = initialStep.state
    initialState.minerals         = With.self.minerals
    initialState.gas              = With.self.gas
    initialState.supplyAvailable  = With.units.ours.filter(u => u.complete || u.isAny(Zerg.Lair, Zerg.Hive)).map(_.unitClass.supplyProvided).sum
    initialState.supplyUsed       = With.units.ours.map(_.unitClass.supplyRequired).sum
    initialState.mineralPatches   = With.self.bases.view.map(_.minerals.count(_.mineralsLeft >= 8)).sum
    initialState.geysers          = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.complete && g.gasLeft > 0)).sum
    initialState.unitsExtant      = With.macroCounts.oursExtant
    initialState.unitsComplete    = With.macroCounts.oursComplete
    Upgrades.all.foreach(u => initialState.upgrades(u) = With.self.getUpgradeLevel(u))
    initialState.techs ++= Techs.all.view.filter(With.self.hasTech)
    insert(initialStep)

    // Construct events for things in progress
    // TODO: Count Eggs/Cocoon/Lurker Egg as what they're making
    // TODO: Don't occupy Probes
    // TODO: Don't replace morphers
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
      insert(step)
    })

    // Populate states as of each event
    updateStatesFrom(1)

    // Insert requests
    With.scheduler.requests.view.flatMap(_._2).foreach(buildRequest => {
      if (buildRequest.upgrade.exists(u => steps.last.state.upgrades(u) < buildRequest.quantity)) {
        requests += buildRequest
      } else if (buildRequest.tech.exists(t => ! steps.last.state.techs.contains(t))) {
        requests += buildRequest
      } else if (buildRequest.unit.isDefined) {
        requests += buildRequest
      }
    })

    // For each request, associate it with an existing event or attempt to insert a new event
    requests.foreach(request => {
      // TODO: Don't occupy Probes
      // TODO: Don't return producers which morph
      // TODO: Reduce diff for zergling/scourge
      val unitDiff    = request.unit.map(u => request.quantity - steps.head.state.unitsComplete(u)).getOrElse(0)
      val upgradeDiff = request.upgrade.map(u => request.quantity - steps.head.state.upgrades(u)).getOrElse(0)
      val techDiff    = Maff.fromBoolean(request.tech.exists(t => ! steps.head.state.techs.contains(t)))
      val diff        = Seq(unitDiff, upgradeDiff, techDiff).max
      if (diff <= 0) {
        redundant += request
      } else {
        (0 until diff).foreach(iDiff => {
          // Is there an existing event which satisfies this request?
          val matching = steps.find(step => {
            var matches = step.request.isEmpty
            matches &&= request.unit.forall(_ == step.event.dUnitComplete)
            matches &&= request.upgrade.forall(_ == step.event.dUpgrade && request.quantity == step.event.dUpgradeLevel)
            matches &&= request.tech.forall(_ == step.event.dTech)
            matches
          })
          matching.foreach(_.request = Some(request))
          if (matching.isEmpty) {
            // There's no event which satisfies this request.
            // Find the first step where we could fulfill it.
            val insertAfter = steps.indices.find(canInsertAfter(request, _))
            if (insertAfter.isEmpty) { denied += request }
            insertAfter.foreach(i => {
              val stepBefore = steps(i)
              val framesAfter = Math.ceil(Seq(
                stepBefore.event.dFrames,
                if (request.mineralCost == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.mineralCost  - stepBefore.state.minerals)  / With.accounting.ourIncomePerFrameMinerals,  Forever()),
                if (request.gasCost     == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.gasCost      - stepBefore.state.gas)       / With.accounting.ourIncomePerFrameGas,       Forever()))
                .max).toInt
              if (framesAfter < Forever()) {
                val stepStart           = new MacroStep
                val stepFinish          = new MacroStep
                val eventStart          = stepStart.event
                val eventFinish         = stepFinish.event
                stepStart.request       = Some(request)
                stepFinish.request      = Some(request)
                eventStart.dFrames      = framesAfter
                eventStart.dMinerals    = - request.mineralCost
                eventStart.dGas         = - request.gasCost
                eventStart.dSupplyUsed  = request.supplyRequired
                eventStart.dProducer1   = request.producerRequired
                eventStart.dProducer1N  = - request.producersRequired
                eventFinish.dFrames     = eventStart.dFrames + request.buildFrames
                eventFinish.dProducer1  = eventStart.dProducer1
                eventFinish.dProducer1N = - eventStart.dProducer1N
                request.unit.foreach(u => {
                  eventStart.dUnitExtant1       = u
                  eventStart.dUnitExtant1N      = u.copiesProduced
                  eventFinish.dUnitComplete     = u
                  eventFinish.dUnitCompleteN    = u.copiesProduced
                  eventFinish.dProducer2        = u
                  eventFinish.dProducer2N       = u.copiesProduced
                  eventFinish.dSupplyAvailable  = u.supplyProvided
                  eventFinish.dGeysers          = Maff.fromBoolean(u.isGas)
                })
                if (Seq(Protoss.HighTemplar, Protoss.DarkTemplar, Zerg.Larva, Zerg.Drone, Zerg.CreepColony, Zerg.Hydralisk, Zerg.Mutalisk).contains(request.producerRequired)) {
                  eventStart.dUnitExtant2 = request.producerRequired
                  eventStart.dUnitExtant2N = - request.producersRequired
                }
                request.tech.foreach(eventFinish.dTech = _)
                request.upgrade.foreach(upgrade => {
                  eventFinish.dUpgrade = upgrade
                  eventFinish.dUpgradeLevel = request.quantity
                })
                val iStart = insert(stepStart)
                insert(stepFinish)
                updateStatesFrom(iStart)
              }
            })
          }
        })
      }
    })
  }

  private def canInsertAfter(request: Buildable, i: Int): Boolean = {
    // Find a state where we can fulfill the request
    val step = steps(i)
    var cant = false
    cant ||= request.mineralCost > Math.max(0, step.state.minerals) && With.accounting.ourIncomePerFrameMinerals == 0
    cant ||= request.supplyRequired > step.state.supplyAvailable - step.state.supplyUsed
    cant ||= request.upgradeRequired.exists(u => step.state.upgrades(u._1) < u._2)
    cant ||= request.techRequired.exists(t => ! step.state.techs.contains(t))
    cant ||= step.state.producers(request.producerRequired) < request.producersRequired
    cant ||= request.unitsRequired.exists(step.state.unitsExtant(_) == 0)
    cant ||= ! canInsertBefore(request, i + 1)
    // TODO: Treat addons as producers
    ! cant
  }

  private def canInsertBefore(request: Buildable, i: Int): Boolean = {
    // Ensure no future states would be rendered impossible by inserting the request
    var j = i
    var cant = false
    while(j < steps.length && ! cant) {
      val state = steps(j).state
      cant ||= request.producerRequired != UnitClasses.None && state.producers(request.producerRequired) < request.producersRequired
      cant ||= request.mineralCost      > Math.max(0, state.minerals)
      cant ||= request.gasCost          > Math.max(0, state.gas)
      cant ||= request.supplyRequired   > Math.max(0, state.supplyAvailable - state.supplyUsed)
      j += 1
    }
    ! cant
  }

  private def insert(step: MacroStep): Int = {
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

  private def updateStatesFrom(index: Int): Unit = {
    var i = index
    while (i < steps.length) {
      val stateLast = steps(i - 1).state
      val stateNext = steps(i).state
      val event     = steps(i).event
      val dFrames   = event.dFrames - steps(i - 1).event.dFrames
      stateNext.minerals        = stateLast.minerals + event.dMinerals + (dFrames * With.accounting.ourIncomePerFrameMinerals).toInt
      stateNext.gas             = stateLast.gas + event.dGas + (dFrames * With.accounting.ourIncomePerFrameGas).toInt
      stateNext.supplyAvailable = stateLast.supplyAvailable + event.dSupplyAvailable
      stateNext.supplyUsed      = stateLast.supplyUsed + event.dSupplyUsed
      stateNext.mineralPatches  = stateLast.mineralPatches + event.dMineralPatches
      stateNext.geysers         = stateLast.geysers + event.dGeysers
      stateNext.techs           = stateLast.techs
      stateNext.upgrades        = stateLast.upgrades
      stateNext.unitsExtant     = stateLast.unitsExtant.clone
      stateNext.producers       = stateLast.producers.clone
      if (event.dTech         != Techs.None)        stateNext.techs                               += event.dTech
      if (event.dUpgrade      != Upgrades.None)     stateNext.upgrades(event.dUpgrade)            =  event.dUpgradeLevel
      if (event.dUnitExtant1  != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant1)     += event.dUnitExtant1N
      if (event.dUnitExtant2  != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant2)     += event.dUnitExtant2N
      if (event.dUnitComplete != UnitClasses.None)  stateNext.unitsComplete(event.dUnitComplete)  += event.dUnitCompleteN
      if (event.dProducer1    != UnitClasses.None)  stateNext.producers(event.dProducer1)         += event.dProducer1N
      if (event.dProducer2    != UnitClasses.None)  stateNext.producers(event.dProducer2)         += event.dProducer2N
      i += 1
    }
  }
}
