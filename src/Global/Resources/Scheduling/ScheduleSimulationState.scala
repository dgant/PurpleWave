package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ScheduleSimulationState(
  var frame               : Int,
  var minerals            : Int,
  var gas                 : Int,
  var supplyAvailable     : Int,
  var unitsOwned          : mutable.HashMap[UnitType, Int],
  var unitsAvailable      : mutable.HashMap[UnitType, Int],
  var techsOwned          : mutable.Set[TechType],
  var upgradeLevels       : mutable.Map[UpgradeType, Int],
  val eventQueue          : mutable.PriorityQueue[SimulationEvent],
  val isDisposableCopy    : Boolean = false) {

  def disposableCopy:ScheduleSimulationState =
    new ScheduleSimulationState(
      frame,
      minerals,
      gas,
      supplyAvailable,
      unitsOwned.clone,
      unitsAvailable.clone,
      techsOwned.clone,
      upgradeLevels.clone,
      eventQueue.clone,
      isDisposableCopy = true)
  
  //////////////////////////////////////
  // Features of the simulation state //
  //////////////////////////////////////
  
  def isBuildableNow(buildable: Buildable):Boolean = {
    framesBeforeMinerals(buildable) == 0 &&
    framesBeforeGas(buildable)      == 0 &&
    unmetPrerequisites(buildable).isEmpty
  }
  
  def framesBeforeMinerals(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current = minerals,
    needed  = buildable.minerals,
    rate    = mineralsPerFrame)
  
  def framesBeforeGas(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current = gas,
    needed  = buildable.gas,
    rate    = gasPerFrame)
  
  def framesBeforeResource(
    buildable: Buildable,
    current: Int,
    needed: Int,
    rate: Double):Int = {
    if (current >= needed)               return 0
    if (current <  needed && rate <= 0)  return Int.MaxValue
    Math.max(0, (needed - current)/rate).toInt
  }
  
  def unmetPrerequisites(buildable: Buildable): Iterable[Buildable] = {
    unmetSupplyPrerequisites(buildable) ++ unmetBuildablePrerequisites(buildable)
  }
  
  def unmetSupplyPrerequisites(buildable: Buildable): Iterable[Buildable] = {
    val supplyType = With.game.self.getRace.getSupplyProvider
    (0 to (buildable.supplyRequired - supplyAvailable) / supplyType.supplyProvided)
      .map(i => new BuildableUnit(supplyType))
  }
  
  def unmetBuildablePrerequisites(buildable: Buildable): Iterable[Buildable] = {
    val output = new ListBuffer[Buildable]
    val units  = new mutable.HashMap[UnitType, Int]
    (buildable.requirements ++ buildable.buildersOccupied).foreach(buildable => {
      buildable.unitOption.foreach(unit => units.put(unit, 1 + units.getOrElse(unit, 0)))
      var unmet = false
      unmet ||= buildable.techOption    .exists(tech    => ! techsOwned.contains(tech))
      unmet ||= buildable.upgradeOption .exists(upgrade => upgradeLevels.getOrElse(upgrade, 0) < buildable.upgradeLevel)
      unmet ||= buildable.unitOption    .exists(unit    => unitsAvailable.getOrElse(unit, 0) < units(unit))
      if (unmet) output.append(buildable)
    })
    output
  }
  
  def mineralsPerFrame  : Double  = 0.25 //TODO
  def gasPerFrame       : Double  = 0.25 //TODO
  
  ////////////////////////////
  // Running the simulation //
  ////////////////////////////
  
  def build(buildable: Buildable): ScheduleSimulationBuildResult = {
    if (isBuildableNow(buildable)) {
      return new ScheduleSimulationBuildResult(
        Some(new SimulationEvent(buildable, frame, frame + buildable.frames)))
    }
    val nextInterestingFrame = List(
      frame + framesBeforeMinerals(buildable),
      frame + framesBeforeGas(buildable),
      eventQueue.headOption.map(_.frameEnd).getOrElse(Int.MaxValue))
      .filter(_ > frame)
      .min
    
    if (nextInterestingFrame == Int.MaxValue)
      return new ScheduleSimulationBuildResult(
        None,
        unmetPrerequisites(buildable))
    val nextState = if (isDisposableCopy) this else disposableCopy
    nextState.fastForward(nextInterestingFrame)
    nextState.build(buildable)
  }
  
  def fastForward(nextFrame:Int) {
    minerals  += ((nextFrame - frame) * mineralsPerFrame).toInt
    gas       += ((nextFrame - frame) * gasPerFrame).toInt
    frame     = nextFrame
    while (eventQueue.headOption.exists(_.frameEnd <= frame)) applyEvent(eventQueue.dequeue())
  }
  
  def applyEvent(event: SimulationEvent) {
    val buildable = event.buildable
    supplyAvailable += buildable.supplyProvided
    if ( ! event.isImplicit) buildable.unitOption.foreach(addOwnedUnit)
    buildable.unitOption.foreach(addAvailableUnit)
    buildable.techOption.foreach(addTech)
    buildable.upgradeOption.foreach(addUpgrade(_, buildable.upgradeLevel))
  }
  
  def addOwnedUnit(unitType: UnitType)                = unitsOwned.put(unitType, 1 + unitsOwned.getOrElse(unitType, 0))
  def addAvailableUnit(unitType: UnitType)            = unitsAvailable.put(unitType, 1 + unitsAvailable.getOrElse(unitType, 0))
  def addTech(techType: TechType)                     = techsOwned.add(techType)
  def addUpgrade(upgradeType: UpgradeType, level:Int) = upgradeLevels.put(upgradeType, level)
  
  ///////////////////////////////////
  // Mutating the simulation state //
  ///////////////////////////////////
  
  def startBuilding(buildable:Buildable):SimulationEvent = {
    spendResources(buildable)
    reserveBuilders(buildable)
    enqueue(buildable)
  }
  
  def spendResources(buildable:Buildable) {
    minerals        -= buildable.minerals
    gas             -= buildable.minerals
    supplyAvailable -= buildable.supplyRequired
  }
  
  def reserveBuilders(buildable:Buildable) {
    //TODO: Don't add builder back if it's consumed
    //TODO: Try to account for travel time
    buildable.buildersOccupied.foreach(builder => {
      unitsAvailable.put(builder.unit, -1 + unitsAvailable.getOrElse(builder.unit, 0))
      eventQueue.enqueue(new SimulationEvent(builder, frame, frame + buildable.frames, isImplicit = true))
    })
  }
  
  def enqueue(buildable: Buildable):SimulationEvent = {
    buildable.buildersConsumed.foreach(builder => unitsOwned(builder.unit) -= 1)
    val output = new SimulationEvent(buildable, frame, frame + buildable.frames)
    eventQueue.enqueue(output)
    output
  }
}