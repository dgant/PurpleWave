package Macro.MacroSim

import Lifecycle.With
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrades

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final class MacroSim {
  var startFrame: Int = 0
  val requests  =  new ArrayBuffer[MacroRequest]
  val steps     =  new mutable.ArrayBuffer[MacroStep]()

  def reset(): Unit = {
    startFrame = With.frame
    requests.clear()
    steps.clear()

    // Construct initial state
    val state = new MacroState
    state.minerals = With.self.minerals
    state.gas = With.self.gas
    state.supplyAvailable = With.units.ours.filter(_.complete).map(_.unitClass.supplyProvided).sum
    state.supplyUsed = With.units.ours.map(_.unitClass.supplyRequired).sum
    state.mineralPatches = With.self.bases.view.map(_.minerals.count(_.mineralsLeft >= 8)).sum
    state.geysers = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.complete && g.gasLeft > 0)).sum
    Upgrades.all.foreach(u => state.upgrades(u) = With.self.getUpgradeLevel(u))
    state.techs ++= Techs.all.view.filter(With.self.hasTech)
    insert(MacroStep(state, new MacroEvent))

    // Construct events for things in progress
    With.units.ours.foreach(u => state.units(u.unitClass) += 1)
    With.units.ours.filter(_.remainingOccupationFrames == 0).foreach(u => state.producers(u.unitClass) += 1)
    With.units.ours.filter(_.remainingOccupationFrames > 0).foreach(u => {
      val event = new MacroEvent
      event.dFrames = u.remainingCompletionFrames
      event.dSupplyAvailable += u.unitClass.supplyProvided
      if (u.upgrading) {
        event.dUpgrade = u.upgradingType
        event.dUpgradeLevel = 1 + state.upgrades(u.upgradingType)
      } else if (u.teching) {
        event.dTech = u.techingType
      } else if (u.unitClass.isGas) {
        event.dGeysers += 1
      } else if (u.unitClass.isResourceDepot) {
        val base = u.base.filter(_.townHall.contains(u))
        base.foreach(b => event.dMineralPatches += b.minerals.count(_.mineralsLeft >= 8))
      } else if (u.morphing) {
        // TODO: Subtract types that are going away if required
      }
      event.dProducer = u.unitClass
      event.dProducerN = 1
      insert(MacroStep(new MacroState, event))
      state.producers(u.unitClass) += 1
    })

    // Populate states as of each event
    updateStatesAfter(0)

    // Construct events for our requests
  }

  def insert(step: MacroStep): Unit = {
    // Binary search to figure out where to fit the step
    val dFrames = step.event.dFrames
    var min = 0
    var max = steps.length - 1
    while(true) {
      val at = (min + max) / 2
      val dFramesBefore = if (at == 0) Int.MinValue else steps(at - 1).event.dFrames
      val dFramesAfter = if (at == steps.length) Int.MaxValue else steps(at).event.dFrames
      if (dFramesBefore <= dFrames) {
        min = at + 1
      } else if (dFramesAfter < dFrames) {
        max = at - 1
      } else {
        steps.insert(at, step)
        return
      }
    }
  }

  def mineralsMinedBy(dFrame: Int): Int = (With.accounting.incomePerFrameMinerals * dFrame).toInt
  def gasMinedBy(dFrame: Int): Int = (With.accounting.incomePerFrameGas * dFrame).toInt

  def updateStatesAfter(index: Int): Unit = {
    var state: MacroState = null
    var i = index
    while (i < steps.length) {
      val step = steps(i)
      if (i > index) {
        val newState = step.state
        val event = step.event
        newState.supplyAvailable = state.supplyAvailable + event.dSupplyAvailable
        newState.supplyUsed = state.supplyUsed + event.dSupplyUsed
        newState.mineralPatches = state.mineralPatches + event.dMineralPatches
        newState.geysers = state.geysers + event.dGeysers
        newState.techs = state.techs
        newState.upgrades = state.upgrades
        newState.units = state.units.clone
        newState.producers = state.producers.clone
        if (event.dTech != Techs.None) {
          newState.techs += event.dTech
        }
        if (event.dUpgrade != Upgrades.None) {
          newState.upgrades(event.dUpgrade) = event.dUpgradeLevel
        }
        if (event.dUnit1 != UnitClasses.None) {
          newState.units(event.dUnit1) += event.dUnit1N
        }
        if (event.dUnit2 != UnitClasses.None) {
          newState.units(event.dUnit2) += event.dUnit2N
        }
        if (event.dProducer != UnitClasses.None) {
          newState.producers(event.dProducer) += event.dProducerN
        }
      }
      state = steps(index).state
      i += 1
    }
  }
}
